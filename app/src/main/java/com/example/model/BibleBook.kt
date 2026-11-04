package com.example.model

enum class Testament {
    OLD, NEW
}

enum class BibleSource(
    val id: String,
    val title: String,
    val shortLabel: String,
    val translationInfo: String,
    val websiteUrl: String
) {
    PISMOSWIETE(
        id = "pismoswiete",
        title = "pismoswiete.pl",
        shortLabel = "pismoswiete.pl",
        translationInfo = "Edycja Świętego Pawła",
        websiteUrl = "https://pismoswiete.pl"
    ),
    DEON(
        id = "deon",
        title = "biblia.deon.pl",
        shortLabel = "biblia.deon.pl",
        translationInfo = "Biblia Tysiąclecia (Pallottinum)",
        websiteUrl = "https://biblia.deon.pl"
    );

    companion object {
        fun fromId(id: String?): BibleSource {
            return entries.firstOrNull { it.id == id } ?: PISMOSWIETE
        }
    }
}

data class BibleBook(
    val code: String,          // Short code used by pismoswiete.pl and deon (e.g. "Rdz", "Mt")
    val name: String,          // Pełna nazwa (np. "Księga Rodzaju", "Ewangelia wg św. Mateusza")
    val shortName: String,     // Skrót zwyczajowy (np. "Rdz", "Mt")
    val testament: Testament,
    val totalChapters: Int
)

data class ReadingPassage(
    val bookCode: String,
    val bookName: String,
    val chapter: Int,
    val passageLabel: String = "$bookName, rozdz. $chapter"
) {
    fun toPismoswieteUrl(): String = "https://pismoswiete.pl/demo#/tome/$bookCode/chapter/$chapter/"

    fun toDeonUrl(): String {
        val chapterId = DeonBibleMapping.getChapterId(bookCode, chapter)
        return if (chapterId != null) {
            "https://biblia.deon.pl/rozdzial.php?id=$chapterId"
        } else {
            "https://biblia.deon.pl/otworz.php?skrot=${bookCode}+$chapter"
        }
    }

    fun toUrl(source: BibleSource): String {
        return when (source) {
            BibleSource.PISMOSWIETE -> toPismoswieteUrl()
            BibleSource.DEON -> toDeonUrl()
        }
    }
}

data class DayPlan(
    val dayNumber: Int,
    val title: String,
    val passages: List<ReadingPassage>
)

data class PlanDefinition(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val totalDays: Int,
    val chaptersPerDay: String,
    val scopeTag: String
)
