package com.example.service.pericope

import com.example.model.BibleData

object PericopeRangeParser {

    /**
     * Parsuje siglum perykopy na listę zakresów w poszczególnych rozdziałach,
     * zgodnie z notacją planu Jacka Święckiego (Mateusz.pl / Chemin Neuf).
     */
    fun parse(bookCode: String, defaultChapter: Int, label: String): List<ChapterRange> {
        val trimmed = label.trim()
        if (trimmed.isEmpty() || trimmed.contains("rozdz", ignoreCase = true)) {
            return listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
        }

        // Usuń komentarze w nawiasach kwadratowych np. [rekonstrukcja], [tekst] i gwiazdki *
        val cleanLabel = trimmed
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\*+"), "")
            .replace('–', '-')
            .replace('—', '-')
            .trim()

        if (cleanLabel.isEmpty()) {
            return listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
        }

        // Podziel po średnikach (np. "2P 1:; 3:" lub "2Sm 19,41b - 20,22; 22:" lub "Wj 30,11-16; Lb 1: - 4:")
        val subParts = cleanLabel.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        val ranges = mutableListOf<ChapterRange>()

        var currentBookCode = bookCode

        for (part in subParts) {
            // Sprawdź czy segment zawiera własny kod księgi (np. "Wj 30,11-16" lub "Lb 1: - 4:")
            val bookMatch = Regex("^([1-4]?\\s*[A-ZĄĆĘŁŃÓŚŹŻa-ząćęłńóśźż]+)\\s+(.*)$").matchEntire(part)
            val (segmentBookCode, segmentSpec) = if (bookMatch != null) {
                val candidateCode = bookMatch.groupValues[1].replace("\\s+".toRegex(), "")
                val foundBook = BibleData.getBook(candidateCode)
                if (foundBook != null) {
                    currentBookCode = foundBook.code
                    foundBook.code to bookMatch.groupValues[2].trim()
                } else {
                    currentBookCode to part
                }
            } else {
                // Usuń ewentualny skrót księgi na początku pojedynczego siglum
                val stripped = part.replace(Regex("^[1-4]?\\s*[A-ZĄĆĘŁŃÓŚŹŻa-ząćęłńóśźż]+\\s*"), "").trim()
                currentBookCode to (stripped.ifEmpty { part })
            }

            ranges.addAll(parseSegment(segmentBookCode, defaultChapter, segmentSpec))
        }

        return if (ranges.isNotEmpty()) ranges else listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
    }

    private fun parseSegment(bookCode: String, defaultChapter: Int, spec: String): List<ChapterRange> {
        val s = spec.trim()
        if (s.isEmpty()) {
            return listOf(ChapterRange(bookCode, defaultChapter, 1, Int.MAX_VALUE))
        }

        // Reguła 4: pełne rozdziały w przedziale, np. "Joz 13: - 17:" lub "Lb 1: - 4:" lub "Rdz 12: - 14:"
        val mChSpan = Regex("^(\\d+)\\s*:\\s*-\\s*(\\d+)\\s*:$").matchEntire(s)
        if (mChSpan != null) {
            val c1 = mChSpan.groupValues[1].toIntOrNull() ?: defaultChapter
            val c2 = mChSpan.groupValues[2].toIntOrNull() ?: c1
            val start = minOf(c1, c2)
            val end = maxOf(c1, c2)
            return (start..end).map { c -> ChapterRange(bookCode, c, 1, Int.MAX_VALUE) }
        }

        // Reguła 5 & 7: pojedynczy pełny rozdział z dwukropkiem, np. "23:" lub "22:" lub "3:"
        val mChSingle = Regex("^(\\d+)\\s*:$").matchEntire(s)
        if (mChSingle != null) {
            val c = mChSingle.groupValues[1].toIntOrNull() ?: defaultChapter
            return listOf(ChapterRange(bookCode, c, 1, Int.MAX_VALUE))
        }

        // Reguły 1, 2, 3, 7, 8: Notacja z numerem rozdziału i specyfikacją wersetów
        val commaIdx = s.indexOf(',')
        val initialChapter: Int
        val versePortion: String

        if (commaIdx != -1 && s.substring(0, commaIdx).trim().all { it.isDigit() }) {
            initialChapter = s.substring(0, commaIdx).trim().toIntOrNull() ?: defaultChapter
            versePortion = s.substring(commaIdx + 1).trim()
        } else {
            initialChapter = defaultChapter
            versePortion = s
        }

        return parseVerseExpression(bookCode, initialChapter, versePortion)
    }

    /**
     * Parsuje wyrażenie wersetowe po przecinku, uwzględniając podział na kropki ('.'),
     * np.:
     * "14-56" -> wiersze 14 do 56
     * "4,35 - 6,13" -> od 4,35 do 6,13
     * "1 - 26,5.14-16" -> od 25,1 do 26,5, a następnie w rozdziale 26 wiersze 14-16
     * "6-8.15.19 - 2,12" -> wiersze 6-8, wiersz 15, od 19 do 2,12
     */
    private fun parseVerseExpression(bookCode: String, initialChapter: Int, versePortion: String): List<ChapterRange> {
        val ranges = mutableListOf<ChapterRange>()
        var currentChapter = initialChapter

        // Dzielimy na segmenty rozdzielone kropkami przed kolejnym wersetem: \.(?=\d)
        val items = versePortion.split(Regex("\\.(?=\\d)")).map { it.trim() }.filter { it.isNotEmpty() }

        for (item in items) {
            // A) Zakres międzyrozdziałowy: np. "1 - 26,5", "19 - 2,12", "1 - 2,4a", "4b - 3,24", "35 - 6,13"
            val mCross = Regex("^(\\d+)([a-z]?)\\s*-\\s*(\\d+)\\s*,\\s*(\\d+)([a-z]?)$").matchEntire(item)
            if (mCross != null) {
                val v1 = mCross.groupValues[1].toIntOrNull() ?: 1
                val v1Sub = mCross.groupValues[2].firstOrNull()?.lowercaseChar()
                val targetChapter = mCross.groupValues[3].toIntOrNull() ?: currentChapter
                val v2 = mCross.groupValues[4].toIntOrNull() ?: Int.MAX_VALUE
                val v2Sub = mCross.groupValues[5].firstOrNull()?.lowercaseChar()

                for (c in currentChapter..targetChapter) {
                    when {
                        c == currentChapter && c == targetChapter -> {
                            ranges.add(ChapterRange(bookCode, c, v1, v2, v1Sub, v2Sub))
                        }
                        c == currentChapter -> {
                            ranges.add(ChapterRange(bookCode, c, v1, Int.MAX_VALUE, startSubVerse = v1Sub, endSubVerse = null))
                        }
                        c == targetChapter -> {
                            ranges.add(ChapterRange(bookCode, c, 1, v2, startSubVerse = null, endSubVerse = v2Sub))
                        }
                        else -> {
                            ranges.add(ChapterRange(bookCode, c, 1, Int.MAX_VALUE, null, null))
                        }
                    }
                }
                currentChapter = targetChapter
                continue
            }

            // B) Zakres wersetów w bieżącym rozdziale: np. "14-56", "14-16", "6-8", "1-13", "12b-14"
            val mRange = Regex("^(\\d+)([a-z]?)\\s*-\\s*(\\d+)([a-z]?)$").matchEntire(item)
            if (mRange != null) {
                val v1 = mRange.groupValues[1].toIntOrNull() ?: 1
                val v1Sub = mRange.groupValues[2].firstOrNull()?.lowercaseChar()
                val v2 = mRange.groupValues[3].toIntOrNull() ?: v1
                val v2Sub = mRange.groupValues[4].firstOrNull()?.lowercaseChar()
                ranges.add(ChapterRange(bookCode, currentChapter, v1, v2, v1Sub, v2Sub))
                continue
            }

            // C) Pojedynczy werset w bieżącym rozdziale: np. "15", "14", "12a", "10b", "22"
            val mSingle = Regex("^(\\d+)([a-z]?)$").matchEntire(item)
            if (mSingle != null) {
                val v = mSingle.groupValues[1].toIntOrNull() ?: 1
                val vSub = mSingle.groupValues[2].firstOrNull()?.lowercaseChar()
                ranges.add(ChapterRange(bookCode, currentChapter, v, v, vSub, vSub))
                continue
            }

            // D) Awaryjny fallback
            val vFallback = item.filter { it.isDigit() }.toIntOrNull()
            if (vFallback != null) {
                ranges.add(ChapterRange(bookCode, currentChapter, vFallback, vFallback))
            } else {
                ranges.add(ChapterRange(bookCode, currentChapter, 1, Int.MAX_VALUE))
            }
        }

        return if (ranges.isNotEmpty()) ranges else listOf(ChapterRange(bookCode, initialChapter, 1, Int.MAX_VALUE))
    }
}
