package com.example

import com.example.model.BibleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testReadingPlansPassagesUrlStructure() {
        val plans = BibleData.availablePlans
        assertTrue("At least 4 plans should be available", plans.size >= 4)

        // Verify day 1 of 365-day plan
        val day1 = BibleData.generateDayPlan("plan_bible_365", 1)
        assertEquals(1, day1.dayNumber)
        assertTrue("Day 1 should have passages", day1.passages.isNotEmpty())

        val firstPassage = day1.passages.first()
        val url = firstPassage.toPismoswieteUrl()
        assertTrue(
            "Passage URL should target pismoswiete.pl demo",
            url.startsWith("https://pismoswiete.pl/demo#/tome/")
        )
    }

    @Test
    fun testBookChaptersIntegrity() {
        BibleData.books.forEach { book ->
            assertTrue("Book ${book.code} should have positive chapter count", book.totalChapters > 0)
            assertNotNull("Book name should not be blank", book.name.isNotBlank())
        }
    }
}

