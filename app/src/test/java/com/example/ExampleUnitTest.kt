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

        // Test multi-chapter span: Rdz 12: - 14:
        val r2 = com.example.service.pericope.PericopeRangeParser.parse("Rdz", 12, "Rdz 12: - 14:")
        assertEquals(3, r2.size)
        assertEquals(12, r2[0].chapter)
        assertEquals(13, r2[1].chapter)
        assertEquals(14, r2[2].chapter)

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
}
