package com.example

import com.example.model.BibleData
import org.junit.Assert.assertEquals
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
}
