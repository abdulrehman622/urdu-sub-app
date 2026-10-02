package com.example.model

import java.util.UUID

data class CaptionSegment(
    val id: String = UUID.randomUUID().toString(),
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val words: List<TimedWord> = emptyList()
) {
    val durationMs: Long
        get() = (endMs - startMs).coerceAtLeast(100L)

    fun containsTime(timeMs: Long): Boolean = timeMs in startMs..endMs

    /**
     * Detects if the segment contains Arabic/Urdu/Perso-Arabic script characters
     */
    fun isUrduText(): Boolean {
        for (char in text) {
            val block = Character.UnicodeBlock.of(char)
            if (block == Character.UnicodeBlock.ARABIC ||
                block == Character.UnicodeBlock.ARABIC_SUPPLEMENT ||
                block == Character.UnicodeBlock.ARABIC_EXTENDED_A ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Returns the active timed word or calculates a synthetic proportional active word index
     */
    fun getActiveWordIndex(timeMs: Long): Int {
        if (!containsTime(timeMs)) return -1
        if (words.isNotEmpty()) {
            val idx = words.indexOfFirst { timeMs in it.startMs..it.endMs }
            if (idx != -1) return idx
        }
        // Fallback: divide text by words and interpolate by progress
        val tokenList = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokenList.isEmpty()) return -1
        val progress = (timeMs - startMs).toFloat() / durationMs.toFloat()
        val estimatedIndex = (progress * tokenList.size).toInt().coerceIn(0, tokenList.size - 1)
        return estimatedIndex
    }

    /**
     * Splits this segment into two at given timestamp
     */
    fun splitAt(splitTimeMs: Long): Pair<CaptionSegment, CaptionSegment>? {
        if (splitTimeMs <= startMs + 200L || splitTimeMs >= endMs - 200L) return null
        val tokenList = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val half = (tokenList.size / 2).coerceAtLeast(1)
        val text1 = tokenList.take(half).joinToString(" ")
        val text2 = tokenList.drop(half).joinToString(" ")
        val seg1 = this.copy(id = UUID.randomUUID().toString(), endMs = splitTimeMs, text = text1)
        val seg2 = this.copy(id = UUID.randomUUID().toString(), startMs = splitTimeMs, text = text2)
        return Pair(seg1, seg2)
    }
}
