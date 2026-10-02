package com.example.util

object UrduTextHelper {

    // Quick insertion characters for Urdu editing
    val URDU_SPECIAL_LETTERS = listOf(
        "ے", "ں", "ۂ", "ئ", "ٹ", "ڈ", "ڑ", "چ", "پ", "ژ", "گ", "ھ", "ہ"
    )

    val URDU_PUNCTUATION = listOf(
        "۔", "،", "؟", "؎", "ؔ", "«", "»", "!"
    )

    val URDU_POETRY_ACCENTS = listOf(
        "مصرع", "شعر", "قافیہ", "ردیف", "مطلع", "مقطع"
    )

    /**
     * Checks if a string contains Urdu / Arabic Unicode characters
     */
    fun isUrdu(text: String): Boolean {
        for (char in text) {
            val codePoint = char.code
            if (codePoint in 0x0600..0x06FF || // Arabic
                codePoint in 0x0750..0x077F || // Arabic Supplement
                codePoint in 0x08A0..0x08FF || // Arabic Extended-A
                codePoint in 0xFB50..0xFDFF || // Arabic Presentation Forms-A
                codePoint in 0xFE70..0xFEFF    // Arabic Presentation Forms-B
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Splits a sentence into timed words evenly if individual word timestamps are not provided
     */
    fun generateWordTimings(text: String, startMs: Long, endMs: Long): List<com.example.model.TimedWord> {
        val tokens = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return emptyList()

        val totalDuration = (endMs - startMs).coerceAtLeast(100L)
        val wordDuration = totalDuration / tokens.size

        return tokens.mapIndexed { index, word ->
            val wStart = startMs + (index * wordDuration)
            val wEnd = if (index == tokens.size - 1) endMs else wStart + wordDuration
            com.example.model.TimedWord(word = word, startMs = wStart, endMs = wEnd)
        }
    }
}
