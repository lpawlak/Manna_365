package com.example

import com.example.model.DeviceDiagnosticInfo
import com.example.model.FeedbackCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackDiagnosticsTest {

    @Test
    fun testFeedbackCategories() {
        assertEquals(4, FeedbackCategory.entries.size)
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.BUG })
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.SUGGESTION })
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.QUESTION })
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.OTHER })
    }

    @Test
    fun testDeviceDiagnosticInfoModel() {
        val info = DeviceDiagnosticInfo(
            osVersion = "Android 15 (API 35)",
            sdkInt = 35,
            manufacturer = "Samsung",
            model = "Galaxy S26",
            device = "s26",
            appVersion = "7.0",
            appVersionCode = 7,
            networkType = "WIFI",
            screenDensity = "480 dpi",
            preferredBibleSource = "pismoswiete.pl"
        )
        assertEquals("Samsung", info.manufacturer)
        assertEquals("Galaxy S26", info.model)
        assertEquals("7.0", info.appVersion)
        assertEquals(7, info.appVersionCode)
        assertEquals("pismoswiete.pl", info.preferredBibleSource)
    }
}
