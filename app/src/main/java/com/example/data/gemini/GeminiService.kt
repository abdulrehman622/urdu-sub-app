package com.example.data.gemini

import android.content.Context
import com.example.BuildConfig
import com.example.model.CaptionSegment
import com.example.model.TimedWord
import com.example.util.UrduTextHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed class TranscriptionResult {
    data class Success(val segments: List<CaptionSegment>, val detectedLanguage: String = "Urdu") : TranscriptionResult()
    data class Error(val message: String, val canRetry: Boolean = true) : TranscriptionResult()
}

class GeminiTranscriptionService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun transcribeAudio(
        audioFile: File?,
        customApiKey: String? = null,
        languagePreference: String = "Urdu"
    ): TranscriptionResult = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) {
            customApiKey.trim()
        } else {
            BuildConfig.GEMINI_API_KEY
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext TranscriptionResult.Error(
                "Gemini API key is not configured. Please enter your API key in Settings, or use sample presets."
            )
        }

        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0L) {
            return@withContext TranscriptionResult.Error("No valid audio track extracted from the video.")
        }

        try {
            // Read audio file bytes into Base64
            val audioBytes = audioFile.readBytes()
            val audioBase64 = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)

            val prompt = """
                You are a world-class speech and song transcription AI specializing in Urdu (اردو), Pakistani music (Ghazal, Coke Studio, Qawwali, Pop), and spoken dialogue.
                Task: Transcribe the vocals or dialogue in this audio file.
                Language Preference: $languagePreference (Use Perso-Arabic Urdu script for Urdu words).
                
                Requirements:
                1. Detect song lyrics, poetry couplets (sher/misra), or spoken sentences.
                2. Output accurate start and end timestamps in milliseconds (startMs and endMs) for each line/cue.
                3. Transcribe Urdu accurately using authentic Urdu spelling (e.g., میں، ہیں، تم، محبت، دل، گلشن).
                4. For each segment, provide the "words" array with startMs and endMs for each word to enable karaoke-style word highlighting.
                5. Return ONLY a valid JSON array in this exact format:
                [
                  {
                    "startMs": 0,
                    "endMs": 3500,
                    "text": "گُلوں میں رنگ بھرے بادِ نو بہار چلے",
                    "words": [
                      {"word": "گُلوں", "startMs": 0, "endMs": 600},
                      {"word": "میں", "startMs": 650, "endMs": 950},
                      {"word": "رنگ", "startMs": 1000, "endMs": 1500},
                      {"word": "بھرے", "startMs": 1550, "endMs": 2000},
                      {"word": "بادِ", "startMs": 2050, "endMs": 2500},
                      {"word": "نو", "startMs": 2550, "endMs": 2850},
                      {"word": "بہار", "startMs": 2900, "endMs": 3200},
                      {"word": "چلے", "startMs": 3250, "endMs": 3500}
                    ]
                  }
                ]
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()

                // Text prompt part
                partsArray.put(JSONObject().apply {
                    put("text", prompt)
                })

                // Audio inlineData part
                val inlineDataObj = JSONObject().apply {
                    put("mimeType", "audio/mp4")
                    put("data", audioBase64)
                }
                partsArray.put(JSONObject().apply {
                    put("inlineData", inlineDataObj)
                })

                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                // Generation config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Empty error response"
                return@withContext TranscriptionResult.Error(
                    "Gemini API returned error (${response.code}): $errorBody"
                )
            }

            val responseBody = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext TranscriptionResult.Error("No transcription candidates returned by Gemini.")
            }

            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val segments = parseGeminiJsonResponse(rawText)
            if (segments.isEmpty()) {
                return@withContext TranscriptionResult.Error("Could not extract timed segments from AI response.")
            }

            TranscriptionResult.Success(segments, languagePreference)
        } catch (e: Exception) {
            e.printStackTrace()
            TranscriptionResult.Error("Network/AI Exception: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun parseGeminiJsonResponse(rawText: String): List<CaptionSegment> {
        val list = mutableListOf<CaptionSegment>()
        try {
            // Find JSON array bounds
            var cleanText = rawText.trim()
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.removePrefix("```json")
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.removePrefix("```")
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.removeSuffix("```")
            }
            cleanText = cleanText.trim()

            val startIndex = cleanText.indexOf('[')
            val endIndex = cleanText.lastIndexOf(']')
            if (startIndex != -1 && endIndex > startIndex) {
                cleanText = cleanText.substring(startIndex, endIndex + 1)
            }

            val jsonArray = JSONArray(cleanText)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val startMs = obj.optLong("startMs", 0L)
                val endMs = obj.optLong("endMs", startMs + 3000L)
                val text = obj.optString("text", "").trim()

                if (text.isNotBlank()) {
                    val wordsList = mutableListOf<TimedWord>()
                    if (obj.has("words")) {
                        val wArray = obj.getJSONArray("words")
                        for (wIdx in 0 until wArray.length()) {
                            val wObj = wArray.getJSONObject(wIdx)
                            wordsList.add(
                                TimedWord(
                                    word = wObj.optString("word", ""),
                                    startMs = wObj.optLong("startMs", startMs),
                                    endMs = wObj.optLong("endMs", endMs)
                                )
                            )
                        }
                    }

                    // If word timings were not provided, calculate proportional timings
                    val finalWords = if (wordsList.isNotEmpty()) {
                        wordsList
                    } else {
                        UrduTextHelper.generateWordTimings(text, startMs, endMs)
                    }

                    list.add(
                        CaptionSegment(
                            id = UUID.randomUUID().toString(),
                            startMs = startMs,
                            endMs = endMs,
                            text = text,
                            words = finalWords
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
