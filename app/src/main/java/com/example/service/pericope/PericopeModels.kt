package com.example.service.pericope

/**
 * Reprezentuje zakres wersetów w obrębie danego rozdziału księgi.
 */
data class ChapterRange(
    val bookCode: String,
    val chapter: Int,
    val startVerse: Int = 1,
    val endVerse: Int = Int.MAX_VALUE
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
    val verses: List<ExtractedVerse>
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
