package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FeedbackCategory
import com.example.service.feedback.FeedbackService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FeedbackDiagnosticsTest {

    @Test
    fun testCollectDeviceDiagnosticsWithoutPermissions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val diagnostics = FeedbackService.collectDeviceDiagnostics(
            context = context,
            preferredBibleSource = "pismoswiete.pl"
        )

        assertNotNull("osVersion should not be null", diagnostics.osVersion)
        assertTrue("osVersion should contain Android", diagnostics.osVersion.contains("Android"))
        assertTrue("sdkInt should be positive", diagnostics.sdkInt > 0)
        assertNotNull("manufacturer should be present", diagnostics.manufacturer)
        assertNotNull("model should be present", diagnostics.model)
        assertNotNull("appVersion should be present", diagnostics.appVersion)
        assertEquals("pismoswiete.pl", diagnostics.preferredBibleSource)
        assertNotNull("networkType should not be null", diagnostics.networkType)
    }

    @Test
    fun testFeedbackCategories() {
        assertEquals(4, FeedbackCategory.entries.size)
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.BUG })
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.SUGGESTION })
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.QUESTION })
        assertTrue(FeedbackCategory.entries.any { it == FeedbackCategory.OTHER })
    }
}
