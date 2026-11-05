package com.example.service.pericope

import java.util.regex.Pattern

object PericopeRangeParser {

    /**
     * Parsuje siglum perykopy na listę zakresów w poszczególnych rozdziałach.
     * Np. "Rdz 1,1 - 2,4a" -> [ChapterRange("Rdz", 1, 1, MAX), ChapterRange("Rdz", 2, 1, 4)]
     * Np. "Rdz 12: - 14:" -> [ChapterRange("Rdz", 12), ChapterRange("Rdz", 13), ChapterRange("Rdz", 14)]
     */
    fun parse(bookCode: String, defaultChapter: Int, label: String): List<ChapterRange> {
        val trimmed = label.trim()
        if (trimmed.isEmpty() || trimmed.contains("rozdz", ignoreCase = true)) {
            return listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
        }

        // Usuń wiodący kod księgi jeśli występuje na początku (np. "Rdz 1,1 - 2,4a" -> "1,1 - 2,4a")
        val clean = trimmed.replace(Regex("^[1-4]?\\s*[A-ZĄĆĘŁŃÓŚŹŻa-ząćęłńóśźż]+\\s*"), "").trim()
        if (clean.isEmpty()) {
            return listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
        }

        val ranges = mutableListOf<ChapterRange>()

        // Przypadek 1: pełne rozdziały w przedziale, np. "12: - 14:" lub "15: - 17:" lub "40: - 41:"
        val mChSpan = Regex("^(\\d+)\\s*:\\s*[-–—]\\s*(\\d+)\\s*:$").matchEntire(clean)
        if (mChSpan != null) {
            val c1 = mChSpan.groupValues[1].toIntOrNull() ?: defaultChapter
            val c2 = mChSpan.groupValues[2].toIntOrNull() ?: c1
            val start = minOf(c1, c2)
            val end = maxOf(c1, c2)
            for (c in start..end) {
                ranges.add(ChapterRange(bookCode, c, 1, Int.MAX_VALUE))
            }
            return ranges
        }

        // Przypadek 2: pojedynczy pełny rozdział z dwukropkiem, np. "23:" lub "24:"
        val mChSingle = Regex("^(\\d+)\\s*:$").matchEntire(clean)
        if (mChSingle != null) {
            val c = mChSingle.groupValues[1].toIntOrNull() ?: defaultChapter
            return listOf(ChapterRange(bookCode, c, 1, Int.MAX_VALUE))
        }

        // Przypadek 3: zakres przekraczający granice rozdziałów, np. "1,1 - 2,4a" lub "2,4b - 3,24" lub "26,34 - 28,9"
        val mCross = Regex("^(\\d+)\\s*,\\s*(\\d+)[a-z]?\\s*[-–—]\\s*(\\d+)\\s*,\\s*(\\d+)[a-z]?.*$").matchEntire(clean)
        if (mCross != null) {
            val c1 = mCross.groupValues[1].toIntOrNull() ?: defaultChapter
            val v1 = mCross.groupValues[2].toIntOrNull() ?: 1
            val c2 = mCross.groupValues[3].toIntOrNull() ?: c1
            val v2 = mCross.groupValues[4].toIntOrNull() ?: Int.MAX_VALUE

            for (c in c1..c2) {
                when (c) {
                    c1 if c == c2 -> ranges.add(ChapterRange(bookCode, c, v1, v2))
                    c1 -> ranges.add(ChapterRange(bookCode, c, v1, Int.MAX_VALUE))
                    c2 -> ranges.add(ChapterRange(bookCode, c, 1, v2))
                    else -> ranges.add(ChapterRange(bookCode, c, 1, Int.MAX_VALUE))
                }
            }
            return ranges
        }

        // Przypadek 4: zakres wersetów w jednym rozdziale, np. "22,20-24" lub "25,1-28" lub "26,1-33"
        val mSingleV = Regex("^(\\d+)\\s*,\\s*(\\d+)[a-z]?\\s*[-–—]\\s*(\\d+)[a-z]?.*$").matchEntire(clean)
        if (mSingleV != null) {
            val c = mSingleV.groupValues[1].toIntOrNull() ?: defaultChapter
            val v1 = mSingleV.groupValues[2].toIntOrNull() ?: 1
            val v2 = mSingleV.groupValues[3].toIntOrNull() ?: Int.MAX_VALUE
            return listOf(ChapterRange(bookCode, c, v1, v2))
        }

        // Przypadek 5: pojedynczy werset, np. "22,20"
        val mOneV = Regex("^(\\d+)\\s*,\\s*(\\d+)[a-z]?$").matchEntire(clean)
        if (mOneV != null) {
            val c = mOneV.groupValues[1].toIntOrNull() ?: defaultChapter
            val v = mOneV.groupValues[2].toIntOrNull() ?: 1
            return listOf(ChapterRange(bookCode, c, v, v))
        }

        // Domyślny fallback: cały rozdział
        return listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
    }
}
