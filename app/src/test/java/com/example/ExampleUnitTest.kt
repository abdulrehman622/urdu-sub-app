package com.example

import com.example.data.export.SubtitleExporter
import com.example.model.CaptionSegment
import com.example.model.TimedWord
import com.example.util.TimeUtils
import com.example.util.UrduTextHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testUrduTextDetection() {
        val urduText = "گُلوں میں رنگ بھرے بادِ نو بہار چلے"
        val englishText = "Hello World lyrics"

        assertTrue(UrduTextHelper.isUrdu(urduText))
        assertTrue(!UrduTextHelper.isUrdu(englishText))
    }

    @Test
    fun testTimeFormatting() {
        assertEquals("01:23.4", TimeUtils.formatTimeMs(83450L))
        assertEquals("00:01:23,450", TimeUtils.formatSrtTime(83450L))
    }

    @Test
    fun testSrtGeneration() {
        val captions = listOf(
            CaptionSegment(
                id = "1",
                startMs = 1000L,
                endMs = 4000L,
                text = "گُلوں میں رنگ بھرے",
                words = listOf(
                    TimedWord("گُلوں", 1000L, 2000L),
                    TimedWord("میں", 2100L, 2800L),
                    TimedWord("رنگ", 2900L, 3500L),
                    TimedWord("بھرے", 3600L, 4000L)
                )
            )
        )

        val srt = SubtitleExporter.generateSrtContent(captions)
        assertTrue(srt.contains("00:00:01,000 --> 00:00:04,000"))
        assertTrue(srt.contains("گُلوں میں رنگ بھرے"))
    }
}
