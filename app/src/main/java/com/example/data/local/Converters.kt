package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.CaptionAnimationStyle
import com.example.model.CaptionAlignment
import com.example.model.CaptionBgStyle
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.FontFamilyType
import com.example.model.TimedWord
import org.json.JSONArray
import org.json.JSONObject

class RoomConverters {

    @TypeConverter
    fun fromCaptionList(captions: List<CaptionSegment>?): String {
        if (captions.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (seg in captions) {
            val obj = JSONObject().apply {
                put("id", seg.id)
                put("startMs", seg.startMs)
                put("endMs", seg.endMs)
                put("text", seg.text)
                val wordsArray = JSONArray()
                for (w in seg.words) {
                    val wObj = JSONObject().apply {
                        put("word", w.word)
                        put("startMs", w.startMs)
                        put("endMs", w.endMs)
                    }
                    wordsArray.put(wObj)
                }
                put("words", wordsArray)
            }
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toCaptionList(jsonString: String?): List<CaptionSegment> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CaptionSegment>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val wordsList = mutableListOf<TimedWord>()
                if (obj.has("words")) {
                    val wArr = obj.getJSONArray("words")
                    for (j in 0 until wArr.length()) {
                        val wObj = wArr.getJSONObject(j)
                        wordsList.add(
                            TimedWord(
                                word = wObj.getString("word"),
                                startMs = wObj.getLong("startMs"),
                                endMs = wObj.getLong("endMs")
                            )
                        )
                    }
                }
                list.add(
                    CaptionSegment(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        startMs = obj.getLong("startMs"),
                        endMs = obj.getLong("endMs"),
                        text = obj.getString("text"),
                        words = wordsList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @TypeConverter
    fun fromCaptionStyle(style: CaptionStyle?): String {
        val s = style ?: CaptionStyle()
        return JSONObject().apply {
            put("fontFamilyType", s.fontFamilyType.name)
            put("fontSizeSp", s.fontSizeSp.toDouble())
            put("textColorHex", s.textColorHex)
            put("karaokeHighlightColorHex", s.karaokeHighlightColorHex)
            put("strokeEnabled", s.strokeEnabled)
            put("strokeColorHex", s.strokeColorHex)
            put("strokeWidthPx", s.strokeWidthPx.toDouble())
            put("bgStyle", s.bgStyle.name)
            put("bgColorHex", s.bgColorHex)
            put("shadowEnabled", s.shadowEnabled)
            put("shadowColorHex", s.shadowColorHex)
            put("shadowRadiusPx", s.shadowRadiusPx.toDouble())
            put("animationStyle", s.animationStyle.name)
            put("verticalPositionPercent", s.verticalPositionPercent.toDouble())
            put("alignment", s.alignment.name)
            put("letterSpacingSp", s.letterSpacingSp.toDouble())
            put("lineHeightMultiplier", s.lineHeightMultiplier.toDouble())
        }.toString()
    }

    @TypeConverter
    fun toCaptionStyle(jsonString: String?): CaptionStyle {
        if (jsonString.isNullOrBlank()) return CaptionStyle()
        return try {
            val obj = JSONObject(jsonString)
            CaptionStyle(
                fontFamilyType = FontFamilyType.valueOf(obj.optString("fontFamilyType", FontFamilyType.URDU_NASTALIQ.name)),
                fontSizeSp = obj.optDouble("fontSizeSp", 26.0).toFloat(),
                textColorHex = obj.optString("textColorHex", "#FFFFFF"),
                karaokeHighlightColorHex = obj.optString("karaokeHighlightColorHex", "#FBBF24"),
                strokeEnabled = obj.optBoolean("strokeEnabled", true),
                strokeColorHex = obj.optString("strokeColorHex", "#000000"),
                strokeWidthPx = obj.optDouble("strokeWidthPx", 5.0).toFloat(),
                bgStyle = CaptionBgStyle.valueOf(obj.optString("bgStyle", CaptionBgStyle.ROUNDED_PILL.name)),
                bgColorHex = obj.optString("bgColorHex", "#B3000000"),
                shadowEnabled = obj.optBoolean("shadowEnabled", true),
                shadowColorHex = obj.optString("shadowColorHex", "#99000000"),
                shadowRadiusPx = obj.optDouble("shadowRadiusPx", 8.0).toFloat(),
                animationStyle = CaptionAnimationStyle.valueOf(obj.optString("animationStyle", CaptionAnimationStyle.KARAOKE.name)),
                verticalPositionPercent = obj.optDouble("verticalPositionPercent", 0.82).toFloat(),
                alignment = CaptionAlignment.valueOf(obj.optString("alignment", CaptionAlignment.AUTO_RTL.name)),
                letterSpacingSp = obj.optDouble("letterSpacingSp", 0.5).toFloat(),
                lineHeightMultiplier = obj.optDouble("lineHeightMultiplier", 1.3).toFloat()
            )
        } catch (e: Exception) {
            CaptionStyle()
        }
    }
}
