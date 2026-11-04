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
        assertEquals(4, plans.size)
        assertTrue(plans.any { it.id == "plan_bible_365" })
        assertTrue(plans.any { it.id == "plan_nt_90" })
        assertTrue(plans.any { it.id == "plan_gospels_psalms_60" })
        assertTrue(plans.any { it.id == "plan_wisdom_30" })
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
}
