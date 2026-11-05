package com.example.model

object BibleData {

    val books: List<BibleBook> = listOf(
        // Stary Testament - Pięcioksiąg i Historyczne
        BibleBook("Rdz", "Księga Rodzaju", "Rdz", Testament.OLD, 50),
        BibleBook("Wj", "Księga Wyjścia", "Wj", Testament.OLD, 40),
        BibleBook("Kpł", "Księga Kapłańska", "Kpł", Testament.OLD, 27),
        BibleBook("Lb", "Księga Liczb", "Lb", Testament.OLD, 36),
        BibleBook("Pwt", "Księga Powtórzonego Prawa", "Pwt", Testament.OLD, 34),
        BibleBook("Joz", "Księga Jozuego", "Joz", Testament.OLD, 24),
        BibleBook("Sdz", "Księga Sędziów", "Sdz", Testament.OLD, 21),
        BibleBook("Rt", "Księga Rut", "Rt", Testament.OLD, 4),
        BibleBook("1Sm", "1 Księga Samuela", "1Sm", Testament.OLD, 31),
        BibleBook("2Sm", "2 Księga Samuela", "2Sm", Testament.OLD, 24),
        BibleBook("1Krl", "1 Księga Królewska", "1Krl", Testament.OLD, 22),
        BibleBook("2Krl", "2 Księga Królewska", "2Krl", Testament.OLD, 25),
        BibleBook("1Krn", "1 Księga Kronik", "1Krn", Testament.OLD, 29),
        BibleBook("2Krn", "2 Księga Kronik", "2Krn", Testament.OLD, 36),
        BibleBook("Ezd", "Księga Ezdrasza", "Ezd", Testament.OLD, 10),
        BibleBook("Ne", "Księga Nehemiasza", "Ne", Testament.OLD, 13),
        BibleBook("Tb", "Księga Tobiasza", "Tb", Testament.OLD, 14),
        BibleBook("Jdt", "Księga Judyty", "Jdt", Testament.OLD, 16),
        BibleBook("Est", "Księga Estery", "Est", Testament.OLD, 10),
        BibleBook("1Mch", "1 Księga Machabejska", "1Mch", Testament.OLD, 16),
        BibleBook("2Mch", "2 Księga Machabejska", "2Mch", Testament.OLD, 15),

        // Mądrościowe
        BibleBook("Hi", "Księga Hioba", "Hi", Testament.OLD, 42),
        BibleBook("Ps", "Księga Psalmów", "Ps", Testament.OLD, 150),
        BibleBook("Prz", "Księga Przysłów", "Prz", Testament.OLD, 31),
        BibleBook("Koh", "Księga Koheleta", "Koh", Testament.OLD, 12),
        BibleBook("Pnp", "Pieśń nad Pieśniami", "Pnp", Testament.OLD, 8),
        BibleBook("Mdr", "Księga Mądrości", "Mdr", Testament.OLD, 19),
        BibleBook("Syr", "Mądrość Syracha", "Syr", Testament.OLD, 51),

        // Prorockie
        BibleBook("Iz", "Księga Izajasza", "Iz", Testament.OLD, 66),
        BibleBook("Jr", "Księga Jeremiasza", "Jr", Testament.OLD, 52),
        BibleBook("Lm", "Lamentacje", "Lm", Testament.OLD, 5),
        BibleBook("Ba", "Księga Barucha", "Ba", Testament.OLD, 6),
        BibleBook("Ez", "Księga Ezechiela", "Ez", Testament.OLD, 48),
        BibleBook("Dn", "Księga Daniela", "Dn", Testament.OLD, 14),
        BibleBook("Oz", "Księga Ozeasza", "Oz", Testament.OLD, 14),
        BibleBook("Jl", "Księga Joela", "Jl", Testament.OLD, 4),
        BibleBook("Am", "Księga Amosa", "Am", Testament.OLD, 9),
        BibleBook("Ab", "Księga Abdiasza", "Ab", Testament.OLD, 1),
        BibleBook("Jon", "Księga Jonasza", "Jon", Testament.OLD, 4),
        BibleBook("Mi", "Księga Micheasza", "Mi", Testament.OLD, 7),
        BibleBook("Na", "Księga Nahuma", "Na", Testament.OLD, 3),
        BibleBook("Ha", "Księga Habakuka", "Ha", Testament.OLD, 3),
        BibleBook("Sof", "Księga Sofoniasza", "Sof", Testament.OLD, 3),
        BibleBook("Ag", "Księga Aggeusza", "Ag", Testament.OLD, 2),
        BibleBook("Za", "Księga Zachariasza", "Za", Testament.OLD, 14),
        BibleBook("Mal", "Księga Malachiasza", "Mal", Testament.OLD, 3),

        // Nowy Testament - Ewangelie i Dzieje
        BibleBook("Mt", "Ewangelia wg św. Mateusza", "Mt", Testament.NEW, 28),
        BibleBook("Mk", "Ewangelia wg św. Marka", "Mk", Testament.NEW, 16),
        BibleBook("Łk", "Ewangelia wg św. Łukasza", "Łk", Testament.NEW, 24),
        BibleBook("J", "Ewangelia wg św. Jana", "J", Testament.NEW, 21),
        BibleBook("Dz", "Dzieje Apostolskie", "Dz", Testament.NEW, 28),

        // Listy św. Pawła
        BibleBook("Rz", "List do Rzymian", "Rz", Testament.NEW, 16),
        BibleBook("1Kor", "1 List do Koryntian", "1Kor", Testament.NEW, 16),
        BibleBook("2Kor", "2 List do Koryntian", "2Kor", Testament.NEW, 13),
        BibleBook("Ga", "List do Galatów", "Ga", Testament.NEW, 6),
        BibleBook("Ef", "List do Efezjan", "Ef", Testament.NEW, 6),
        BibleBook("Flp", "List do Filipian", "Flp", Testament.NEW, 4),
        BibleBook("Kol", "List do Kolosan", "Kol", Testament.NEW, 4),
        BibleBook("1Tes", "1 List do Tesaloniczan", "1Tes", Testament.NEW, 5),
        BibleBook("2Tes", "2 List do Tesaloniczan", "2Tes", Testament.NEW, 3),
        BibleBook("1Tm", "1 List do Tymoteusza", "1Tm", Testament.NEW, 6),
        BibleBook("2Tm", "2 List do Tymoteusza", "2Tm", Testament.NEW, 4),
        BibleBook("Tt", "List do Tytusa", "Tt", Testament.NEW, 3),
        BibleBook("Flm", "List do Filemona", "Flm", Testament.NEW, 1),
        BibleBook("Hbr", "List do Hebrajczyków", "Hbr", Testament.NEW, 13),

        // Listy Powszechne i Apokalipsa
        BibleBook("Jk", "List św. Jakuba", "Jk", Testament.NEW, 5),
        BibleBook("1P", "1 List św. Piotra", "1P", Testament.NEW, 5),
        BibleBook("2P", "2 List św. Piotra", "2P", Testament.NEW, 3),
        BibleBook("1J", "1 List św. Jana", "1J", Testament.NEW, 5),
        BibleBook("2J", "2 List św. Jana", "2J", Testament.NEW, 1),
        BibleBook("3J", "3 List św. Jana", "3J", Testament.NEW, 1),
        BibleBook("Jud", "List św. Judy", "Jud", Testament.NEW, 1),
        BibleBook("Ap", "Apokalipsa św. Jana", "Ap", Testament.NEW, 22)
    )

    private val bookMap: Map<String, BibleBook> = books.associateBy { it.code }

    fun getBook(code: String): BibleBook? = bookMap[code]

    val availablePlans = listOf(
        PlanDefinition(
            id = "plan_bible_365",
            title = "Biblia w 365 dni",
            subtitle = "Całe Pismo Święte w jeden rok",
            description = "Zbalansowany roczny plan obejmujący Stary Testament, Nowy Testament oraz Księgę Psalmów każdego dnia.",
            totalDays = 365,
            chaptersPerDay = "3–4 rozdziały dziennie",
            scopeTag = "Stary i Nowy Testament"
        ),
        PlanDefinition(
            id = "plan_nt_90",
            title = "Nowy Testament w 90 dni",
            subtitle = "Wszystkie 27 ksiąg Nowego Przymierza",
            description = "Intensywny i głęboki plan lektury całego Nowego Testamentu od Ewangelii Mateusza po Apokalipsę.",
            totalDays = 90,
            chaptersPerDay = "ok. 3 rozdziały dziennie",
            scopeTag = "Nowy Testament"
        ),
        PlanDefinition(
            id = "plan_gospels_psalms_60",
            title = "Ewangelie i Psalmy w 60 dni",
            subtitle = "Życie Jezusa i modlitwa Psalmami",
            description = "Idealny plan na rozpoczęcie przygody z Pismem Świętym: cztery Ewangelie uzupełnione o poetyckie Psalmy.",
            totalDays = 60,
            chaptersPerDay = "2–3 rozdziały dziennie",
            scopeTag = "Ewangelie + Psalmy"
        ),
        PlanDefinition(
            id = "plan_wisdom_30",
            title = "Mądrość i Refleksja w 30 dni",
            subtitle = "Przysłowia, Kohelet i Mądrość",
            description = "Miesięczny zbiór praktycznych sentencji życiowych, rozważań nad sensem życia i głębi wiary.",
            totalDays = 30,
            chaptersPerDay = "2 rozdziały dziennie",
            scopeTag = "Księgi Mądrościowe"
        ),
        PlanDefinition(
            id = "plan_mateusz_wobi",
            title = "Plan Jacka Święckiego (Mateusz.pl)",
            subtitle = "Cała Biblia w 2 lata (104 tygodnie, Chemin Neuf)",
            description = "Metoda wspólnoty Chemin Neuf z portalu mateusz.pl (WOBI): ok. 15 minut dziennie przez 6 dni w tygodniu w 4 zbalansowanych cyklach historyczno-teologicznych.",
            totalDays = 624,
            chaptersPerDay = "1–2 fragmenty (ok. 15 min)",
            scopeTag = "Cała Biblia (2 lata)"
        )
    )

    fun generateDayPlan(planId: String, day: Int): DayPlan {
        return when (planId) {
            "plan_mateusz_wobi" -> generateMateuszDay(day)
            "plan_nt_90" -> generateNt90Day(day)
            "plan_gospels_psalms_60" -> generateGospelsPsalms60Day(day)
            "plan_wisdom_30" -> generateWisdom30Day(day)
            else -> generateBible365Day(day)
        }
    }

    private fun generateBible365Day(day: Int): DayPlan {
        val clampedDay = ((day - 1).coerceIn(0, 364))
        // Linear mapping across all chapters
        // Old testament chapters without Psalms: 929 chapters - 150 = 779
        // New testament: 260 chapters
        // Day reading: ~2 chapters OT, 1 chapter NT / Ps
        val otChapters = mutableListOf<Pair<String, Int>>()
        books.filter { it.testament == Testament.OLD && it.code != "Ps" }.forEach { book ->
            for (ch in 1..book.totalChapters) {
                otChapters.add(book.code to ch)
            }
        }

        val ntChapters = mutableListOf<Pair<String, Int>>()
        books.filter { it.testament == Testament.NEW }.forEach { book ->
            for (ch in 1..book.totalChapters) {
                ntChapters.add(book.code to ch)
            }
        }

        val otIndex = (clampedDay * 2) % otChapters.size
        val otPassage1 = otChapters[otIndex]
        val otPassage2 = otChapters[(otIndex + 1) % otChapters.size]

        val ntIndex = clampedDay % ntChapters.size
        val ntPassage = ntChapters[ntIndex]

        val psalmChapter = (clampedDay % 150) + 1

        val passages = listOf(
            ReadingPassage(
                bookCode = otPassage1.first,
                bookName = getBook(otPassage1.first)?.name ?: otPassage1.first,
                chapter = otPassage1.second
            ),
            ReadingPassage(
                bookCode = otPassage2.first,
                bookName = getBook(otPassage2.first)?.name ?: otPassage2.first,
                chapter = otPassage2.second
            ),
            ReadingPassage(
                bookCode = ntPassage.first,
                bookName = getBook(ntPassage.first)?.name ?: ntPassage.first,
                chapter = ntPassage.second
            ),
            ReadingPassage(
                bookCode = "Ps",
                bookName = "Księga Psalmów",
                chapter = psalmChapter
            )
        )

        return DayPlan(
            dayNumber = day,
            title = "Dzień $day: ${passages.first().bookCode} ${passages.first().chapter}, ${ntPassage.first} ${ntPassage.second}",
            passages = passages
        )
    }

    private fun generateNt90Day(day: Int): DayPlan {
        val clampedDay = ((day - 1).coerceIn(0, 89))
        val ntChapters = mutableListOf<Pair<String, Int>>()
        books.filter { it.testament == Testament.NEW }.forEach { book ->
            for (ch in 1..book.totalChapters) {
                ntChapters.add(book.code to ch)
            }
        }

        val startIndex = (clampedDay * 3)
        val passages = mutableListOf<ReadingPassage>()
        for (i in 0..2) {
            val idx = startIndex + i
            if (idx < ntChapters.size) {
                val pair = ntChapters[idx]
                passages.add(
                    ReadingPassage(
                        bookCode = pair.first,
                        bookName = getBook(pair.first)?.name ?: pair.first,
                        chapter = pair.second
                    )
                )
            }
        }

        return DayPlan(
            dayNumber = day,
            title = "Dzień $day: ${passages.map { "${it.bookCode} ${it.chapter}" }.joinToString(", ")}",
            passages = passages
        )
    }

    private fun generateGospelsPsalms60Day(day: Int): DayPlan {
        val clampedDay = ((day - 1).coerceIn(0, 59))
        val gospelChapters = mutableListOf<Pair<String, Int>>()
        listOf("Mt", "Mk", "Łk", "J").forEach { code ->
            val book = getBook(code) ?: return@forEach
            for (ch in 1..book.totalChapters) {
                gospelChapters.add(code to ch)
            }
        }

        val gIndex1 = (clampedDay * 2) % gospelChapters.size
        val gIndex2 = (clampedDay * 2 + 1) % gospelChapters.size
        val gPassage1 = gospelChapters[gIndex1]
        val gPassage2 = gospelChapters[gIndex2]

        val psChapter1 = (clampedDay * 2 % 150) + 1
        val psChapter2 = ((clampedDay * 2 + 1) % 150) + 1

        val passages = listOf(
            ReadingPassage(
                bookCode = gPassage1.first,
                bookName = getBook(gPassage1.first)?.name ?: gPassage1.first,
                chapter = gPassage1.second
            ),
            ReadingPassage(
                bookCode = gPassage2.first,
                bookName = getBook(gPassage2.first)?.name ?: gPassage2.first,
                chapter = gPassage2.second
            ),
            ReadingPassage(
                bookCode = "Ps",
                bookName = "Księga Psalmów",
                chapter = psChapter1
            ),
            ReadingPassage(
                bookCode = "Ps",
                bookName = "Księga Psalmów",
                chapter = psChapter2
            )
        )

        return DayPlan(
            dayNumber = day,
            title = "Dzień $day: ${gPassage1.first} ${gPassage1.second}, Ps $psChapter1",
            passages = passages
        )
    }

    private fun generateWisdom30Day(day: Int): DayPlan {
        val clampedDay = ((day - 1).coerceIn(0, 29))
        val wisdomChapters = mutableListOf<Pair<String, Int>>()
        listOf("Prz", "Koh", "Mdr").forEach { code ->
            val book = getBook(code) ?: return@forEach
            for (ch in 1..book.totalChapters) {
                wisdomChapters.add(code to ch)
            }
        }

        val idx1 = (clampedDay * 2) % wisdomChapters.size
        val idx2 = (clampedDay * 2 + 1) % wisdomChapters.size
        val w1 = wisdomChapters[idx1]
        val w2 = wisdomChapters[idx2]

        val passages = listOf(
            ReadingPassage(
                bookCode = w1.first,
                bookName = getBook(w1.first)?.name ?: w1.first,
                chapter = w1.second
            ),
            ReadingPassage(
                bookCode = w2.first,
                bookName = getBook(w2.first)?.name ?: w2.first,
                chapter = w2.second
            )
        )

        return DayPlan(
            dayNumber = day,
            title = "Dzień $day: ${w1.first} ${w1.second}, ${w2.first} ${w2.second}",
            passages = passages
        )
    }

    private fun generateMateuszDay(day: Int): DayPlan {
        val count = MateuszPlanData.entries.size
        val clampedDay = ((day - 1).coerceIn(0, count - 1))
        val entry = MateuszPlanData.entries[clampedDay]

        val passages = entry.passages.map { p ->
            val book = getBook(p.bookCode)
            ReadingPassage(
                bookCode = p.bookCode,
                bookName = book?.name ?: p.bookCode,
                chapter = p.chapter,
                passageLabel = p.label
            )
        }

        return DayPlan(
            dayNumber = day,
            title = "Dzień $day (${entry.weekLabel}, dz. ${entry.dayOfWeek}): ${entry.rawText}",
            passages = passages
        )
    }
}
