package com.example.util

data class LyricLine(
    val timeMs: Long,
    val text: String
)

object LrcParser {

    private val LRC_REGEX = Regex("\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})\\](.*)")

    /**
     * Parses standard LRC formatted strings into timestamped LyricLine objects.
     * If no timestamps are present, treats lines as plain text with evenly spaced lines or 0 time.
     */
    fun parse(lrcContent: String?): List<LyricLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = lrcContent.lines()
        val parsed = mutableListOf<LyricLine>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val match = LRC_REGEX.find(trimmed)
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msPart = match.groupValues[3]
                val millis = if (msPart.length == 2) {
                    msPart.toLongOrNull()?.times(10) ?: 0L
                } else {
                    msPart.toLongOrNull() ?: 0L
                }

                val timeMs = (min * 60 * 1000) + (sec * 1000) + millis
                val text = match.groupValues[4].trim()
                parsed.add(LyricLine(timeMs, text))
            } else if (!trimmed.startsWith("[")) {
                // Plain text line without timestamp
                parsed.add(LyricLine(-1L, trimmed))
            }
        }

        return parsed.sortedBy { it.timeMs }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.isBlank()
    }
}
