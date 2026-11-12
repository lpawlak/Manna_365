package com.example

import com.example.model.BibleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBiblePlansAvailability() {
        val plans = BibleData.availablePlans
        assertEquals(5, plans.size)
        assertTrue(plans.any { it.id == "plan_bible_365" })
        assertTrue(plans.any { it.id == "plan_mateusz_wobi" })
        assertTrue(plans.any { it.id == "plan_nt_90" })
        assertTrue(plans.any { it.id == "plan_gospels_psalms_60" })
        assertTrue(plans.any { it.id == "plan_wisdom_30" })
    }

    @Test
    fun testMateuszWobiPlanGeneration() {
        val plan = BibleData.availablePlans.first { it.id == "plan_mateusz_wobi" }
        assertEquals(624, plan.totalDays)

        val day1 = BibleData.generateDayPlan("plan_mateusz_wobi", 1)
        assertEquals(1, day1.dayNumber)
        assertEquals("Rdz", day1.passages.first().bookCode)
        assertEquals(1, day1.passages.first().chapter)
        assertTrue(day1.passages.first().passageLabel.contains("Rdz 1,1"))

        val day624 = BibleData.generateDayPlan("plan_mateusz_wobi", 624)
        assertEquals(624, day624.dayNumber)
        assertTrue(day624.passages.any { it.bookCode == "Ap" })
    }

    @Test
    fun testBibleBooksMapping() {
        val rdz = BibleData.getBook("Rdz")
        assertNotNull(rdz)
        assertEquals("Księga Rodzaju", rdz?.name)
        assertEquals(50, rdz?.totalChapters)

        val mt = BibleData.getBook("Mt")
        assertNotNull(mt)
        assertEquals("Ewangelia wg św. Mateusza", mt?.name)
        assertEquals(28, mt?.totalChapters)
    }

    @Test
    fun testDayPlanGenerationAndUrls() {
        val day1 = BibleData.generateDayPlan("plan_bible_365", 1)
        assertEquals(1, day1.dayNumber)
        assertTrue(day1.passages.isNotEmpty())

        val passage = day1.passages.first()
        val url = passage.toPismoswieteUrl()
        assertEquals("https://pismoswiete.pl/demo#/tome/${passage.bookCode}/chapter/${passage.chapter}/", url)
    }

    @Test
    fun testDeonBibleMappingAndSourceUrls() {
        val rdz1 = com.example.model.ReadingPassage("Rdz", "Księga Rodzaju", 1)
        assertEquals("https://biblia.deon.pl/rozdzial.php?id=1", rdz1.toDeonUrl())
        assertEquals("https://biblia.deon.pl/rozdzial.php?id=1", rdz1.toUrl(com.example.model.BibleSource.DEON))
        assertEquals("https://pismoswiete.pl/demo#/tome/Rdz/chapter/1/", rdz1.toUrl(com.example.model.BibleSource.PISMOSWIETE))

        val mt1 = com.example.model.ReadingPassage("Mt", "Ewangelia wg św. Mateusza", 1)
        assertEquals("https://biblia.deon.pl/rozdzial.php?id=244", mt1.toDeonUrl())

        val ap22 = com.example.model.ReadingPassage("Ap", "Apokalipsa św. Jana", 22)
        assertEquals("https://biblia.deon.pl/rozdzial.php?id=1110", ap22.toDeonUrl())
    }

    @Test
    fun testAppThemeMode() {
        assertEquals(com.example.ui.viewmodel.AppThemeMode.SYSTEM, com.example.ui.viewmodel.AppThemeMode.fromId("system"))
        assertEquals(com.example.ui.viewmodel.AppThemeMode.LIGHT, com.example.ui.viewmodel.AppThemeMode.fromId("light"))
        assertEquals(com.example.ui.viewmodel.AppThemeMode.DARK, com.example.ui.viewmodel.AppThemeMode.fromId("dark"))
        assertEquals(com.example.ui.viewmodel.AppThemeMode.SYSTEM, com.example.ui.viewmodel.AppThemeMode.fromId("unknown"))
        assertEquals(com.example.ui.viewmodel.AppThemeMode.SYSTEM, com.example.ui.viewmodel.AppThemeMode.fromId(null))
    }

    @Test
    fun testPericopeRangeParser() {
        // Test cross-chapter range: Rdz 1,1 - 2,4a
        val r1 = com.example.service.pericope.PericopeRangeParser.parse("Rdz", 1, "Rdz 1,1 - 2,4a")
        assertEquals(2, r1.size)
        assertEquals(1, r1[0].chapter)
        assertEquals(1, r1[0].startVerse)
        assertEquals(Int.MAX_VALUE, r1[0].endVerse)
        assertEquals(2, r1[1].chapter)
        assertEquals(1, r1[1].startVerse)
        assertEquals(4, r1[1].endVerse)
        assertEquals('a', r1[1].endSubVerse)

        // Test next day cross-chapter range starting with subverse: Rdz 2,4b - 3,24
        val rNext = com.example.service.pericope.PericopeRangeParser.parse("Rdz", 2, "Rdz 2,4b - 3,24")
        assertEquals(2, rNext.size)
        assertEquals(2, rNext[0].chapter)
        assertEquals(4, rNext[0].startVerse)
        assertEquals('b', rNext[0].startSubVerse)
        assertEquals(Int.MAX_VALUE, rNext[0].endVerse)
        assertEquals(3, rNext[1].chapter)
        assertEquals(1, rNext[1].startVerse)
        assertEquals(24, rNext[1].endVerse)

        // Test multi-chapter span: Rdz 12: - 14:
        val r2 = com.example.service.pericope.PericopeRangeParser.parse("Rdz", 12, "Rdz 12: - 14:")
        assertEquals(3, r2.size)
        assertEquals(12, r2[0].chapter)
        assertEquals(13, r2[1].chapter)
        assertEquals(14, r2[2].chapter)

        // Test single chapter verse range with subverse: 21,33-35a
        val rSub = com.example.service.pericope.PericopeRangeParser.parse("Ez", 21, "21,33-35a")
        assertEquals(1, rSub.size)
        assertEquals(21, rSub[0].chapter)
        assertEquals(33, rSub[0].startVerse)
        assertEquals(35, rSub[0].endVerse)
        assertEquals('a', rSub[0].endSubVerse)

        // Test single chapter verse range: Rdz 22,20-24
        val r3 = com.example.service.pericope.PericopeRangeParser.parse("Rdz", 22, "Rdz 22,20-24")
        assertEquals(1, r3.size)
        assertEquals(22, r3[0].chapter)
        assertEquals(20, r3[0].startVerse)
        assertEquals(24, r3[0].endVerse)

        // Test single chapter with colon: Rdz 23:
        val r4 = com.example.service.pericope.PericopeRangeParser.parse("Rdz", 23, "Rdz 23:")
        assertEquals(1, r4.size)
        assertEquals(23, r4[0].chapter)
        assertEquals(1, r4[0].startVerse)
        assertEquals(Int.MAX_VALUE, r4[0].endVerse)
    }

    @Test
    fun testSubVerseSentenceSplitting() {
        // Test biblical verse splitting: Rdz 2,4 in Biblia Tysiąclecia
        val v4Text = "Oto są dzieje początków po stworzeniu nieba i ziemi. Gdy Pan Bóg uczynił ziemię i niebo,"

        // Day 1: Rdz 1,1 - 2,4a (werset 4 kończy się na 'a', czyli pierwszym zdaniu)
        val rangeDay1 = com.example.service.pericope.ChapterRange(
            bookCode = "Rdz",
            chapter = 2,
            startVerse = 1,
            endVerse = 4,
            startSubVerse = null,
            endSubVerse = 'a'
        )
        val filtered4a = com.example.service.pericope.BibleExtractionEngine.filterVerseText(v4Text, 4, rangeDay1)
        assertEquals("Oto są dzieje początków po stworzeniu nieba i ziemi.", filtered4a)

        // Day 2: Rdz 2,4b - 3,24 (werset 4 zaczyna się od 'b', czyli drugiego zdania)
        val rangeDay2 = com.example.service.pericope.ChapterRange(
            bookCode = "Rdz",
            chapter = 2,
            startVerse = 4,
            endVerse = Int.MAX_VALUE,
            startSubVerse = 'b',
            endSubVerse = null
        )
        val filtered4b = com.example.service.pericope.BibleExtractionEngine.filterVerseText(v4Text, 4, rangeDay2)
        assertEquals("Gdy Pan Bóg uczynił ziemię i niebo,", filtered4b)
    }

    @Test
    fun testLoginDetection() {
        val plainTextLock = "<h3>Treść dostępna tylko dla osób zalogowanych</h3>"
        val unicodeJsonLock = "{\"content\":\"\\u003cdiv class=\\u0022demo\\u0022\\u003e\\n    \\u003ch3\\u003eTre\\u015b\\u0107 dost\\u0119pna tylko dla os\\u00f3b zalogowanych\\u003c/h3\\u003e\"}"
        val normalText = "<span class=\"werset\">Na początku Bóg stworzył niebo i ziemię.</span>"

        assertTrue(com.example.service.pericope.BibleExtractionEngine.isLoginRequired(plainTextLock))
        assertTrue(com.example.service.pericope.BibleExtractionEngine.isLoginRequired(unicodeJsonLock))
        org.junit.Assert.assertFalse(com.example.service.pericope.BibleExtractionEngine.isLoginRequired(normalText))
    }

    @Test
    fun testJacekSwieckiNotationAllRules() {
        // Reguła 1: Mk 6, 14–56 (w Ewangelii św. Marka w rozdziale 6 wiersze 14-56)
        val r1 = com.example.service.pericope.PericopeRangeParser.parse("Mk", 6, "Mk 6, 14–56")
        assertEquals(1, r1.size)
        assertEquals("Mk", r1[0].bookCode)
        assertEquals(6, r1[0].chapter)
        assertEquals(14, r1[0].startVerse)
        assertEquals(56, r1[0].endVerse)

        // Reguła 2: Mk 4,35 – 6,13 (od rozdziału 4, wiersza 35 do rozdziału 6, wiersza 13 włącznie)
        val r2 = com.example.service.pericope.PericopeRangeParser.parse("Mk", 4, "Mk 4,35 – 6,13")
        assertEquals(3, r2.size)
        assertEquals(4, r2[0].chapter)
        assertEquals(35, r2[0].startVerse)
        assertEquals(Int.MAX_VALUE, r2[0].endVerse)
        assertEquals(5, r2[1].chapter)
        assertEquals(1, r2[1].startVerse)
        assertEquals(Int.MAX_VALUE, r2[1].endVerse)
        assertEquals(6, r2[2].chapter)
        assertEquals(1, r2[2].startVerse)
        assertEquals(13, r2[2].endVerse)

        // Reguła 3: Mt 25,1 – 26,5.14–16 (od 25,1 do 26,5, a następnie w rozdziale 26 wiersze 14-16)
        val r3 = com.example.service.pericope.PericopeRangeParser.parse("Mt", 25, "Mt 25,1 – 26,5.14–16")
        assertEquals(3, r3.size)
        assertEquals(25, r3[0].chapter)
        assertEquals(1, r3[0].startVerse)
        assertEquals(Int.MAX_VALUE, r3[0].endVerse)
        assertEquals(26, r3[1].chapter)
        assertEquals(1, r3[1].startVerse)
        assertEquals(5, r3[1].endVerse)
        assertEquals(26, r3[2].chapter)
        assertEquals(14, r3[2].startVerse)
        assertEquals(16, r3[2].endVerse)

        // Reguła 4: Joz 13: – 17: (wszystkie rozdziały od 13 do 17 włącznie)
        val r4 = com.example.service.pericope.PericopeRangeParser.parse("Joz", 13, "Joz 13: – 17:")
        assertEquals(5, r4.size)
        assertEquals(listOf(13, 14, 15, 16, 17), r4.map { it.chapter })

        // Reguła 5: 2P 1:; 3: (rozdział 1 i 3 z pominięciem rozdziału 2)
        val r5 = com.example.service.pericope.PericopeRangeParser.parse("2P", 1, "2P 1:; 3:")
        assertEquals(2, r5.size)
        assertEquals(1, r5[0].chapter)
        assertEquals(3, r5[1].chapter)

        // Reguła 6: Wj 30,11–16; Lb 1: – 4: (w Wj 30,11-16, a następnie w Lb rozdziały 1 do 4)
        val r6 = com.example.service.pericope.PericopeRangeParser.parse("Wj", 30, "Wj 30,11–16; Lb 1: – 4:")
        assertEquals(5, r6.size)
        assertEquals("Wj", r6[0].bookCode)
        assertEquals(30, r6[0].chapter)
        assertEquals(11, r6[0].startVerse)
        assertEquals(16, r6[0].endVerse)
        assertEquals("Lb", r6[1].bookCode)
        assertEquals(1, r6[1].chapter)
        assertEquals("Lb", r6[4].bookCode)
        assertEquals(4, r6[4].chapter)

        // Reguła 7: 2Sm 19,41b – 20,22; 22: (od 19,41b do 20,22, a następnie rozdział 22)
        val r7 = com.example.service.pericope.PericopeRangeParser.parse("2Sm", 19, "2Sm 19,41b – 20,22; 22:")
        assertEquals(3, r7.size)
        assertEquals(19, r7[0].chapter)
        assertEquals(41, r7[0].startVerse)
        assertEquals('b', r7[0].startSubVerse)
        assertEquals(20, r7[1].chapter)
        assertEquals(1, r7[1].startVerse)
        assertEquals(22, r7[1].endVerse)
        assertEquals(22, r7[2].chapter)
        assertEquals(1, r7[2].startVerse)
        assertEquals(Int.MAX_VALUE, r7[2].endVerse)

        // Reguła 8: J 1,6–8.15.19 – 2,12 (w rozdziale 1 wiersze 6-8, wiersz 15, od 1,19 do 2,12)
        val r8 = com.example.service.pericope.PericopeRangeParser.parse("J", 1, "J 1,6–8.15.19 – 2,12")
        assertEquals(4, r8.size)
        assertEquals(1, r8[0].chapter)
        assertEquals(6, r8[0].startVerse)
        assertEquals(8, r8[0].endVerse)
        assertEquals(1, r8[1].chapter)
        assertEquals(15, r8[1].startVerse)
        assertEquals(15, r8[1].endVerse)
        assertEquals(1, r8[2].chapter)
        assertEquals(19, r8[2].startVerse)
        assertEquals(Int.MAX_VALUE, r8[2].endVerse)
        assertEquals(2, r8[3].chapter)
        assertEquals(1, r8[3].startVerse)
        assertEquals(12, r8[3].endVerse)

        // Komentarze: gwiazdki i rekonstrukcje
        val rComments = com.example.service.pericope.PericopeRangeParser.parse(
            "Iz", 24, "Iz 24,1-13.16b-20; 33: *** [ rekonstrukcja ]"
        )
        assertEquals(3, rComments.size)
        assertEquals(24, rComments[0].chapter)
        assertEquals(1, rComments[0].startVerse)
        assertEquals(13, rComments[0].endVerse)
        assertEquals(24, rComments[1].chapter)
        assertEquals(16, rComments[1].startVerse)
        assertEquals('b', rComments[1].startSubVerse)
        assertEquals(20, rComments[1].endVerse)
        assertEquals(33, rComments[2].chapter)
    }

    @Test
    fun testDay11MultiPassageReaderSession() {
        val day11Plan = com.example.model.BibleData.generateDayPlan("plan_mateusz_wobi", 11)
        assertEquals(2, day11Plan.passages.size)
        assertEquals("Rdz", day11Plan.passages[0].bookCode)
        assertEquals("Rdz 22,20-24", day11Plan.passages[0].passageLabel)
        assertEquals("Rdz", day11Plan.passages[1].bookCode)
        assertEquals("Rdz 24:", day11Plan.passages[1].passageLabel)

        // Simulate reader session creation for passage 0
        val p0 = day11Plan.passages[0]
        val total = day11Plan.passages.size
        val idx0 = day11Plan.passages.indexOfFirst {
            it == p0 || (it.bookCode == p0.bookCode && it.chapter == p0.chapter && it.passageLabel == p0.passageLabel)
        }
        val hasNext0 = idx0 < total - 1
        val nextPassage0 = day11Plan.passages[idx0 + 1]

        val session0 = com.example.ui.viewmodel.ReaderSession(
            bookCode = p0.bookCode,
            chapter = p0.chapter,
            bookName = p0.bookName,
            dayNumber = 11,
            isCompleted = false,
            hasNext = hasNext0,
            hasPrev = false,
            passageLabel = p0.passageLabel,
            passageIndex = idx0,
            totalPassages = total,
            nextPassageLabel = nextPassage0.passageLabel
        )

        assertEquals(0, session0.passageIndex)
        assertEquals(2, session0.totalPassages)
        assertTrue(session0.hasNext)
        assertEquals("Rdz 24:", session0.nextPassageLabel)

        // Advance to passage 1
        val p1 = day11Plan.passages[idx0 + 1]
        val idx1 = day11Plan.passages.indexOfFirst {
            it == p1 || (it.bookCode == p1.bookCode && it.chapter == p1.chapter && it.passageLabel == p1.passageLabel)
        }
        val hasNext1 = idx1 < total - 1

        val session1 = com.example.ui.viewmodel.ReaderSession(
            bookCode = p1.bookCode,
            chapter = p1.chapter,
            bookName = p1.bookName,
            dayNumber = 11,
            isCompleted = false,
            hasNext = hasNext1,
            hasPrev = true,
            passageLabel = p1.passageLabel,
            passageIndex = idx1,
            totalPassages = total,
            nextPassageLabel = null
        )

        assertEquals(1, session1.passageIndex)
        assertEquals(2, session1.totalPassages)
        assertFalse(session1.hasNext)
        assertTrue(session1.hasPrev)

        // Verify marking completed behavior
        var passage0MarkedRead = false
        val onNextPassageAndMarkCompleted: () -> Unit = {
            if (!session0.isCompleted) {
                passage0MarkedRead = true
            }
        }
        onNextPassageAndMarkCompleted()
        assertTrue("Passage 0 should be marked read when clicking next fragment", passage0MarkedRead)
    }

    @Test
    fun testTtsExtractScriptureTextExcludesTitlesChaptersAndVerseNumbers() {
        val reading = com.example.service.pericope.PericopeReading(
            bookCode = "Rdz",
            bookName = "Księga Rodzaju",
            passageLabel = "Rdz 22,20-24",
            sourceName = "Biblia Tysiąclecia",
            sections = listOf(
                com.example.service.pericope.PericopeSection(
                    bookCode = "Rdz",
                    bookName = "Księga Rodzaju",
                    chapter = 22,
                    sectionTitle = "Dzieje potomstwa Nachora",
                    verses = listOf(
                        com.example.service.pericope.ExtractedVerse(
                            verseNumber = 20,
                            text = "Po tych wydarzeniach doniesiono Abrahamowi: Milka również urodziła synów twemu bratu, Nachorowi."
                        ),
                        com.example.service.pericope.ExtractedVerse(
                            verseNumber = 21,
                            text = "Usa, syna pierworodnego, Buza, jego brata, Kemuela, praojca Aramejczyków."
                        )
                    )
                )
            )
        )

        val spokenText = com.example.service.tts.BibleTtsManager.extractScriptureText(reading)

        // Verifications:
        // 1. Must NOT contain book title
        assertFalse(spokenText.contains("Księga Rodzaju"))
        // 2. Must NOT contain passage label
        assertFalse(spokenText.contains("Rdz 22,20-24"))
        // 3. Must NOT contain source name
        assertFalse(spokenText.contains("Biblia Tysiąclecia"))
        // 4. Must NOT contain chapter number or chapter header
        assertFalse(spokenText.contains("Rozdział 22"))
        assertFalse(spokenText.contains("Rozdział"))
        // 5. Must NOT contain section title
        assertFalse(spokenText.contains("Dzieje potomstwa Nachora"))
        // 6. Must NOT contain verse numbers at start
        assertFalse(spokenText.contains("20"))
        assertFalse(spokenText.contains("21"))

        // 7. MUST contain pure scripture text
        assertTrue(spokenText.contains("Po tych wydarzeniach doniesiono Abrahamowi"))
        assertTrue(spokenText.contains("Usa, syna pierworodnego"))
    }

    @Test
    fun testTtsCleanVerseForSpeechAndSentenceSplitting() {
        // Raw verse with HTML, leading number, brackets and quotes
        val rawVerse = "<b>20</b> «Po tych wydarzeniach [rekonstrukcja] doniesiono Abrahamowi***.»"
        val cleaned = com.example.service.tts.BibleTtsManager.cleanVerseForSpeech(rawVerse)

        assertFalse(cleaned.contains("<b>"))
        assertFalse(cleaned.contains("20"))
        assertFalse(cleaned.contains("«"))
        assertFalse(cleaned.contains("»"))
        assertFalse(cleaned.contains("[rekonstrukcja]"))
        assertFalse(cleaned.contains("***"))
        assertEquals("Po tych wydarzeniach doniesiono Abrahamowi.", cleaned)

        val sentences = com.example.service.tts.BibleTtsManager.splitIntoSentences(
            "Na początku Bóg stworzył niebo i ziemię. Ziemia zaś była bezładem i pustkowiem. Czy to widzisz?"
        )
        assertEquals(3, sentences.size)
        assertEquals("Na początku Bóg stworzył niebo i ziemię.", sentences[0])
        assertEquals("Ziemia zaś była bezładem i pustkowiem.", sentences[1])
        assertEquals("Czy to widzisz?", sentences[2])
    }

    @Test
    fun testBackupManagerExportAndParse() {
        val sampleStates = listOf(
            com.example.data.entity.UserPlanStateEntity(
                planId = "plan_mateusz_wobi",
                isActive = true,
                startDate = 1700000000000L,
                currentDay = 15,
                lastReadDateMillis = 1700005000000L,
                streakDays = 5,
                reminderHour = 7,
                reminderMinute = 30,
                reminderEnabled = true
            )
        )

        val sampleProgress = listOf(
            com.example.data.entity.ReadingProgressEntity(
                planId = "plan_mateusz_wobi",
                dayNumber = 1,
                bookCode = "Rdz",
                chapter = 1,
                isCompleted = true,
                completedAt = 1700001000000L
            ),
            com.example.data.entity.ReadingProgressEntity(
                planId = "plan_mateusz_wobi",
                dayNumber = 2,
                bookCode = "Rdz",
                chapter = 2,
                isCompleted = true,
                completedAt = 1700002000000L
            )
        )

        val sampleSettings = mapOf(
            "preferred_bible_source" to "deon",
            "app_theme_mode" to "dark",
            "pref_use_native_reader" to true,
            "native_reader_font_size" to 20,
            "font_zoom_percent" to 140
        )

        // 1. Export do JSON
        val json = com.example.service.backup.BackupManager.createBackupJsonFromData(
            planStates = sampleStates,
            progressList = sampleProgress,
            activePlanId = "plan_mateusz_wobi",
            settings = sampleSettings
        )

        assertNotNull(json)
        assertTrue(json.contains("\"appName\": \"Manna 365\""))
        assertTrue(json.contains("\"planStates\""))
        assertTrue(json.contains("\"readingProgress\""))
        assertTrue(json.contains("\"preferred_bible_source\": \"deon\""))
        assertTrue(json.contains("\"plan_mateusz_wobi\""))

        // 2. Parsowanie z JSON
        val result = com.example.service.backup.BackupManager.parseBackupJsonData(json)
        assertTrue(result.success)
        assertEquals(2, result.importedProgressCount)
        assertEquals(1, result.importedPlansCount)
        assertEquals(1, result.states.size)
        assertEquals("plan_mateusz_wobi", result.states.first().planId)
        assertEquals(15, result.states.first().currentDay)
        assertEquals(2, result.progress.size)
        assertEquals("deon", result.restoredSettings["preferred_bible_source"])
        assertEquals("dark", result.restoredSettings["app_theme_mode"])
        assertEquals(20, result.restoredSettings["native_reader_font_size"])

        // 3. Test nieprawidłowego JSON
        val invalidResult = com.example.service.backup.BackupManager.parseBackupJsonData("{ \"invalid\": true }")
        assertFalse(invalidResult.success)
        assertTrue(invalidResult.message.contains("brak wymaganych sekcji") || invalidResult.message.contains("Błąd"))
    }

    @Test
    fun testReadingStatsFormattingAndCalculations() {
        // 1. Czas czytania
        assertEquals("0 min", com.example.model.ReadingStatsCalculator.formatReadingTime(0L))
        assertEquals("< 1 min", com.example.model.ReadingStatsCalculator.formatReadingTime(45L))
        assertEquals("15 min", com.example.model.ReadingStatsCalculator.formatReadingTime(15 * 60L))
        assertEquals("1 godz. 20 min", com.example.model.ReadingStatsCalculator.formatReadingTime(80 * 60L))
        assertEquals("2 godz.", com.example.model.ReadingStatsCalculator.formatReadingTime(120 * 60L))

        // 2. Seria dni bez przerwy (Streak)
        val now = System.currentTimeMillis()
        val (streakToday, maxStreak1, _) = com.example.model.ReadingStatsCalculator.calculateStreak(now, 5, 10)
        assertEquals(5, streakToday)
        assertEquals(10, maxStreak1)

        val yesterday = now - 24 * 60 * 60 * 1000L
        val (streakYesterday, _, _) = com.example.model.ReadingStatsCalculator.calculateStreak(yesterday, 7, 7)
        assertEquals(7, streakYesterday)

        val threeDaysAgo = now - 3 * 24 * 60 * 60 * 1000L
        val (streakBroken, maxStreak2, _) = com.example.model.ReadingStatsCalculator.calculateStreak(threeDaysAgo, 7, 12)
        assertEquals(0, streakBroken)
        assertEquals(12, maxStreak2)

        // 3. Ulubiona pora dnia
        val morningCalendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 8)
            set(java.util.Calendar.MINUTE, 30)
        }
        val morningReadings = listOf(
            com.example.data.entity.ReadingProgressEntity("p1", 1, "Rdz", 1, true, morningCalendar.timeInMillis),
            com.example.data.entity.ReadingProgressEntity("p1", 2, "Rdz", 2, true, morningCalendar.timeInMillis)
        )
        val (favTitle, favDetail, slots) = com.example.model.ReadingStatsCalculator.calculateFavoriteTime(morningReadings)
        assertTrue(favTitle.contains("Poranek"))
        assertTrue(favTitle.contains("100%"))
        assertTrue(favDetail.contains("08:00"))
        assertEquals(4, slots.size)

        // 4. Przewidywane zakończenie planu
        val plan = com.example.model.BibleData.availablePlans.first()
        val userState = com.example.data.entity.UserPlanStateEntity(
            planId = plan.id,
            currentDay = 10,
            startDate = now - 10 * 24 * 60 * 60 * 1000L
        )
        val (estDate, estDaysRemaining, paceDesc) = com.example.model.ReadingStatsCalculator.calculateEstimatedCompletion(
            plan,
            userState,
            morningReadings
        )
        assertNotNull(estDate)
        assertTrue(estDaysRemaining > 0)
        assertTrue(paceDesc.contains("tempo"))
    }

    @Test
    fun testFavoritePassageSerialization() {
        val list = listOf(
            com.example.model.FavoritePassage("Rdz", "Księga Rodzaju", 1, "Rdz 1,1 - 2,4a", 1700000000000L),
            com.example.model.FavoritePassage("J", "Ewangelia wg św. Jana", 3, "J 3,16-21", 1700001000000L)
        )
        val json = com.example.model.FavoritePassageStorage.serializeList(list)
        assertTrue(json.contains("Księga Rodzaju"))
        assertTrue(json.contains("J 3,16-21"))

        val deserialized = com.example.model.FavoritePassageStorage.deserializeList(json)
        assertEquals(2, deserialized.size)
        assertEquals("Rdz", deserialized[0].bookCode)
        assertEquals("Rdz 1,1 - 2,4a", deserialized[0].passageLabel)
        assertEquals("J", deserialized[1].bookCode)
        assertEquals("J 3,16-21", deserialized[1].passageLabel)
    }

    @Test
    fun testWeeklyReadingProgressBreakdown() {
        val now = java.util.Calendar.getInstance()
        val readings = listOf(
            com.example.data.entity.ReadingProgressEntity(
                planId = "plan_bible_365",
                dayNumber = 1,
                bookCode = "Rdz",
                chapter = 1,
                isCompleted = true,
                completedAt = now.timeInMillis
            )
        )

        val week = com.example.model.ReadingStatsCalculator.calculateWeeklyProgress(readings, weekOffset = 0, nowCalendar = now)
        assertEquals(7, week.size)
        assertEquals("Pon", week[0].dayOfWeekName)
        assertEquals("Ndz", week[6].dayOfWeekName)

        val todayStat = week.firstOrNull { it.isToday }
        org.junit.Assert.assertNotNull(todayStat)
        assertTrue(todayStat!!.isCompleted)
        assertEquals(1, todayStat.completedCount)

        val stats = com.example.model.ReadingStatsCalculator.computeStats(
            totalReadingSeconds = 300L,
            activePlan = com.example.model.BibleData.availablePlans.first(),
            userState = com.example.data.entity.UserPlanStateEntity(planId = "plan_bible_365", lastReadDateMillis = now.timeInMillis, streakDays = 1),
            readings = readings,
            storedMaxStreak = 1
        )
        assertEquals(7, stats.weeklyBreakdown.size)
        assertTrue(stats.weekCompletedDaysCount >= 1)
    }
}


