package com.example.service.pericope

/**
 * Reprezentuje zakres wersetów w obrębie danego rozdziału księgi,
 * włącznie z opcjonalnymi podziałami na podwersety 'a' / 'b' (np. Rdz 2,4a vs Rdz 2,4b).
 */
data class ChapterRange(
    val bookCode: String,
    val chapter: Int,
    val startVerse: Int = 1,
    val endVerse: Int = Int.MAX_VALUE,
    val startSubVerse: Char? = null,
    val endSubVerse: Char? = null
)

/**
 * Pojedynczy wyekstrahowany werset.
 */
data class ExtractedVerse(
    val verseNumber: Int,
    val text: String
)

/**
 * Sekcja rozdziału wewnątrz perykopy (np. Księga Rodzaju, Rozdział 1 ze śródtytułem i wersetami).
 */
data class PericopeSection(
    val bookCode: String,
    val bookName: String,
    val chapter: Int,
    val sectionTitle: String? = null,
    val verses: List<ExtractedVerse>,
    val isDeonFallback: Boolean = false,
    val requiresLogin: Boolean = false
)

/**
 * Kompletna lektura perykopy gotowa do zaprezentowania w czytniku.
 */
data class PericopeReading(
    val bookCode: String,
    val bookName: String,
    val passageLabel: String,
    val sourceName: String,
    val sections: List<PericopeSection>
)
