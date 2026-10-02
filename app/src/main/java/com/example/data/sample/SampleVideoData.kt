package com.example.data.sample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.model.CaptionSegment
import com.example.model.TimedWord
import com.example.model.VideoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID

object SampleVideoData {

    data class SampleTrack(
        val id: String,
        val title: String,
        val subtitle: String,
        val durationMs: Long,
        val bgPrimaryHex: String,
        val bgSecondaryHex: String,
        val captions: List<CaptionSegment>
    )

    val SAMPLE_TRACKS = listOf(
        SampleTrack(
            id = "faiz_ghazal",
            title = "گُلوں میں رنگ بھرے",
            subtitle = "فیض احمد فیض • کلاسک غزل",
            durationMs = 24_000L,
            bgPrimaryHex = "#1E1B4B",
            bgSecondaryHex = "#4C1D95",
            captions = listOf(
                CaptionSegment(
                    id = "c1",
                    startMs = 500L,
                    endMs = 4800L,
                    text = "گُلوں میں رنگ بھرے بادِ نو بہار چلے",
                    words = listOf(
                        TimedWord("گُلوں", 500L, 1100L),
                        TimedWord("میں", 1150L, 1600L),
                        TimedWord("رنگ", 1650L, 2200L),
                        TimedWord("بھرے", 2250L, 2900L),
                        TimedWord("بادِ", 2950L, 3500L),
                        TimedWord("نو", 3550L, 3900L),
                        TimedWord("بہار", 3950L, 4400L),
                        TimedWord("چلے", 4450L, 4800L)
                    )
                ),
                CaptionSegment(
                    id = "c2",
                    startMs = 5200L,
                    endMs = 9500L,
                    text = "چلے بھی آؤ کہ گلشن کا کاروبار چلے",
                    words = listOf(
                        TimedWord("چلے", 5200L, 5700L),
                        TimedWord("بھی", 5750L, 6200L),
                        TimedWord("آؤ", 6250L, 6800L),
                        TimedWord("کہ", 6850L, 7200L),
                        TimedWord("گلشن", 7250L, 8000L),
                        TimedWord("کا", 8050L, 8400L),
                        TimedWord("کاروبار", 8450L, 9100L),
                        TimedWord("چلے", 9150L, 9500L)
                    )
                ),
                CaptionSegment(
                    id = "c3",
                    startMs = 10000L,
                    endMs = 14500L,
                    text = "قفس اُداس ہے یارو صبا سے کچھ تو کہو",
                    words = listOf(
                        TimedWord("قفس", 10000L, 10600L),
                        TimedWord("اُداس", 10650L, 11200L),
                        TimedWord("ہے", 11250L, 11700L),
                        TimedWord("یارو", 11750L, 12400L),
                        TimedWord("صبا", 12450L, 13100L),
                        TimedWord("سے", 13150L, 13500L),
                        TimedWord("کچھ", 13550L, 13900L),
                        TimedWord("تو", 13950L, 14200L),
                        TimedWord("کہو", 14250L, 14500L)
                    )
                ),
                CaptionSegment(
                    id = "c4",
                    startMs = 15000L,
                    endMs = 19500L,
                    text = "کہیں تو بہرِ خدا آج ذکرِ یار چلے",
                    words = listOf(
                        TimedWord("کہیں", 15000L, 15600L),
                        TimedWord("تو", 15650L, 16000L),
                        TimedWord("بہرِ", 16050L, 16600L),
                        TimedWord("خدا", 16650L, 17300L),
                        TimedWord("آج", 17350L, 17800L),
                        TimedWord("ذکرِ", 17850L, 18500L),
                        TimedWord("یار", 18550L, 19000L),
                        TimedWord("چلے", 19050L, 19500L)
                    )
                ),
                CaptionSegment(
                    id = "c5",
                    startMs = 20000L,
                    endMs = 23800L,
                    text = "کبھی تو صبح ترے کُنجِ لب سے ہو آغاز",
                    words = listOf(
                        TimedWord("کبھی", 20000L, 20500L),
                        TimedWord("تو", 20550L, 20900L),
                        TimedWord("صبح", 20950L, 21500L),
                        TimedWord("ترے", 21550L, 22000L),
                        TimedWord("کُنجِ", 22050L, 22600L),
                        TimedWord("لب", 22650L, 23000L),
                        TimedWord("سے", 23050L, 23300L),
                        TimedWord("ہو", 23350L, 23550L),
                        TimedWord("آغاز", 23550L, 23800L)
                    )
                )
            )
        ),
        SampleTrack(
            id = "tu_jhoom",
            title = "توں جھوم جھوم (فوک کلام)",
            subtitle = "صوفیانہ سنگیت • کوک اسٹوڈیو اسٹائل",
            durationMs = 20_000L,
            bgPrimaryHex = "#0F172A",
            bgSecondaryHex = "#047857",
            captions = listOf(
                CaptionSegment(
                    id = "tj1",
                    startMs = 600L,
                    endMs = 5200L,
                    text = "میں راضی اپنی ذات نال، میں راضی اپنے حال نال",
                    words = listOf(
                        TimedWord("میں", 600L, 1000L),
                        TimedWord("راضی", 1050L, 1600L),
                        TimedWord("اپنی", 1650L, 2100L),
                        TimedWord("ذات", 2150L, 2700L),
                        TimedWord("نال،", 2750L, 3100L),
                        TimedWord("میں", 3150L, 3500L),
                        TimedWord("راضی", 3550L, 4100L),
                        TimedWord("اپنے", 4150L, 4600L),
                        TimedWord("حال", 4650L, 5000L),
                        TimedWord("نال", 5050L, 5200L)
                    )
                ),
                CaptionSegment(
                    id = "tj2",
                    startMs = 5600L,
                    endMs = 10000L,
                    text = "توں جھوم جھوم، توں جھوم جھوم",
                    words = listOf(
                        TimedWord("توں", 5600L, 6400L),
                        TimedWord("جھوم", 6450L, 7600L),
                        TimedWord("جھوم،", 7650L, 8400L),
                        TimedWord("توں", 8450L, 9000L),
                        TimedWord("جھوم", 9050L, 9600L),
                        TimedWord("جھوم", 9650L, 10000L)
                    )
                ),
                CaptionSegment(
                    id = "tj3",
                    startMs = 10500L,
                    endMs = 15200L,
                    text = "جندڑی دی وگدی ندی نوں ہن نہ موڑ",
                    words = listOf(
                        TimedWord("جندڑی", 10500L, 11400L),
                        TimedWord("دی", 11450L, 11900L),
                        TimedWord("وگدی", 11950L, 12700L),
                        TimedWord("ندی", 12750L, 13400L),
                        TimedWord("نوں", 13450L, 13900L),
                        TimedWord("ہن", 13950L, 14400L),
                        TimedWord("نہ", 14450L, 14750L),
                        TimedWord("موڑ", 14800L, 15200L)
                    )
                ),
                CaptionSegment(
                    id = "tj4",
                    startMs = 15600L,
                    endMs = 19800L,
                    text = "توں جھوم جھوم، اک واری فیر جھوم",
                    words = listOf(
                        TimedWord("توں", 15600L, 16400L),
                        TimedWord("جھوم", 16450L, 17300L),
                        TimedWord("جھوم،", 17350L, 18000L),
                        TimedWord("اک", 18050L, 18450L),
                        TimedWord("واری", 18500L, 19000L),
                        TimedWord("فیر", 19050L, 19400L),
                        TimedWord("جھوم", 19450L, 19800L)
                    )
                )
            )
        ),
        SampleTrack(
            id = "urdu_vlog",
            title = "اردو ٹیک ڈائیلاگ",
            subtitle = "بول چال اور تقریر • یوٹیوب شارٹس",
            durationMs = 18_000L,
            bgPrimaryHex = "#020617",
            bgSecondaryHex = "#0284C7",
            captions = listOf(
                CaptionSegment(
                    id = "v1",
                    startMs = 500L,
                    endMs = 4500L,
                    text = "السلام علیکم دوستو! خوش آمدید اردو سب ٹائٹلز میں",
                    words = listOf(
                        TimedWord("السلام", 500L, 1000L),
                        TimedWord("علیکم", 1050L, 1500L),
                        TimedWord("دوستو!", 1550L, 2200L),
                        TimedWord("خوش", 2250L, 2700L),
                        TimedWord("آمدید", 2750L, 3300L),
                        TimedWord("اردو", 3350L, 3800L),
                        TimedWord("سب", 3850L, 4150L),
                        TimedWord("ٹائٹلز", 4150L, 4350L),
                        TimedWord("میں", 4350L, 4500L)
                    )
                ),
                CaptionSegment(
                    id = "v2",
                    startMs = 5000L,
                    endMs = 9500L,
                    text = "اب آپ اپنی کسی بھی ویڈیو میں اردو لکھ سکتے ہیں",
                    words = listOf(
                        TimedWord("اب", 5000L, 5400L),
                        TimedWord("آپ", 5450L, 5800L),
                        TimedWord("اپنی", 5850L, 6300L),
                        TimedWord("کسی", 6350L, 6800L),
                        TimedWord("بھی", 6850L, 7200L),
                        TimedWord("ویڈیو", 7250L, 7800L),
                        TimedWord("میں", 7850L, 8150L),
                        TimedWord("اردو", 8150L, 8650L),
                        TimedWord("لکھ", 8650L, 9050L),
                        TimedWord("سکتے", 9050L, 9350L),
                        TimedWord("ہیں", 9350L, 9500L)
                    )
                ),
                CaptionSegment(
                    id = "v3",
                    startMs = 10000L,
                    endMs = 14200L,
                    text = "کیپشنز خودکار طریقہ سے آواز کے ساتھ ہم آہنگ ہوں گے",
                    words = listOf(
                        TimedWord("کیپشنز", 10000L, 10600L),
                        TimedWord("خودکار", 10650L, 11300L),
                        TimedWord("طریقہ", 11350L, 11800L),
                        TimedWord("سے", 11850L, 12200L),
                        TimedWord("آواز", 12250L, 12800L),
                        TimedWord("کے", 12850L, 13150L),
                        TimedWord("ساتھ", 13150L, 13550L),
                        TimedWord("ہم آہنگ", 13550L, 13950L),
                        TimedWord("ہوں", 13950L, 14050L),
                        TimedWord("گے", 14050L, 14200L)
                    )
                ),
                CaptionSegment(
                    id = "v4",
                    startMs = 14600L,
                    endMs = 17800L,
                    text = "اور گیلری میں ایکسپورٹ بھی کر سکتے ہیں!",
                    words = listOf(
                        TimedWord("اور", 14600L, 15000L),
                        TimedWord("گیلری", 15050L, 15600L),
                        TimedWord("میں", 15650L, 16000L),
                        TimedWord("ایکسپورٹ", 16050L, 16700L),
                        TimedWord("بھی", 16750L, 17100L),
                        TimedWord("کر", 17150L, 17350L),
                        TimedWord("سکتے", 17350L, 17600L),
                        TimedWord("ہیں!", 17600L, 17800L)
                    )
                )
            )
        )
    )

    /**
     * Synthesizes a real playable vertical MP4 video in cacheDir with an animated aesthetic background.
     * This ensures the app can play, scrub, preview, and burn real video frames without requiring downloads.
     */
    suspend fun createSyntheticSampleVideo(
        context: Context,
        track: SampleTrack
    ): File = withContext(Dispatchers.IO) {
        val targetFile = File(context.cacheDir, "sample_${track.id}.mp4")
        if (targetFile.exists() && targetFile.length() > 50_000L) {
            return@withContext targetFile
        }

        val width = 720
        val height = 1280
        val frameRate = 24
        val durationSeconds = (track.durationMs / 1000).toInt().coerceIn(10, 24)
        val totalFrames = durationSeconds * frameRate
        val bitRate = 2_000_000 // 2 Mbps

        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(targetFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 42f
                textAlign = Paint.Align.CENTER
            }
            val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            val audioWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F59E0B")
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
            }

            val bufferInfo = MediaCodec.BufferInfo()
            val startColor = Color.parseColor(track.bgPrimaryHex)
            val endColor = Color.parseColor(track.bgSecondaryHex)

            for (frame in 0 until totalFrames) {
                val canvas: Canvas = inputSurface.lockCanvas(null)
                try {
                    // Animated gradient
                    val t = frame.toFloat() / totalFrames
                    val sweepOffset = (Math.sin(frame * 0.08) * 120).toFloat()
                    val shader = LinearGradient(
                        0f, 0f,
                        width.toFloat(), height.toFloat() + sweepOffset,
                        startColor, endColor,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = shader
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                    paint.shader = null

                    // Draw decorative central vinyl / sound wave card
                    paint.color = Color.parseColor("#26FFFFFF")
                    val cardRect = RectF(60f, 260f, width - 60f, 740f)
                    canvas.drawRoundRect(cardRect, 32f, 32f, paint)

                    // Draw music play icon & glow
                    paint.color = Color.parseColor("#44F59E0B")
                    canvas.drawCircle(width / 2f, 440f, 80f, paint)
                    paint.color = Color.parseColor("#F59E0B")
                    canvas.drawCircle(width / 2f, 440f, 50f, paint)

                    // Animated sound bars
                    val numBars = 18
                    val barWidth = 14f
                    val spacing = 22f
                    val startX = (width - (numBars * spacing)) / 2f
                    for (b in 0 until numBars) {
                        val phase = frame * 0.15f + (b * 0.4f)
                        val barHeight = 25f + (Math.sin(phase.toDouble()).toFloat() * 45f + 45f)
                        val bx = startX + (b * spacing)
                        val by = 640f
                        canvas.drawLine(bx, by + barHeight / 2f, bx, by - barHeight / 2f, audioWavePaint)
                    }

                    // Titles
                    canvas.drawText(track.title, width / 2f, 850f, textPaint)
                    canvas.drawText(track.subtitle, width / 2f, 905f, subTextPaint)

                    // Reel / Shorts watermark badge
                    paint.color = Color.parseColor("#44000000")
                    canvas.drawRoundRect(width / 2f - 140f, 100f, width / 2f + 140f, 150f, 25f, 25f, paint)
                    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#F8FAFC")
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("URDUSUB • DEMO CLIP", width / 2f, 134f, badgePaint)

                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain encoder
                while (true) {
                    val status = encoder.dequeueOutputBuffer(bufferInfo, 0)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            videoTrackIndex = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    } else if (status >= 0) {
                        val encodedData = encoder.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            // Presentation time
                            bufferInfo.presentationTimeUs = (frame * 1_000_000L) / frameRate
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(status, false)
                    }
                }
            }

            encoder.signalEndOfInputStream()

            // Drain remaining
            var isEos = false
            while (!isEos) {
                val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (status >= 0) {
                    val encodedData = encoder.getOutputBuffer(status)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEos = true
                    }
                    encoder.releaseOutputBuffer(status, false)
                } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            if (muxerStarted) {
                muxer.stop()
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            targetFile
        } finally {
            try {
                encoder?.stop()
                encoder?.release()
            } catch (_: Exception) {}
            try {
                muxer?.release()
            } catch (_: Exception) {}
        }
    }
}
