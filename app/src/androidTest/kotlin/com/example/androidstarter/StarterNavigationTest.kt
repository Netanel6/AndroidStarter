package com.example.androidstarter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import com.example.androidstarter.app.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StarterNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun navigationSurvivesRecreationAndDoesNotReplayAfterReturning() {
        assertTrue("Run UI tests in default offline demo mode", BuildConfig.DEMO_MODE)
        compose.onNodeWithTag("catalog-title").assertIsDisplayed()
        compose.onNodeWithTag("product-field-notes").performClick()
        compose.onNodeWithTag("details-name").assertTextEquals("Field notes")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("details-name").assertTextEquals("Field notes")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithTag("details-name").assertIsDisplayed()
        compose.onNodeWithText("Back to catalog").performClick()
        compose.onNodeWithTag("catalog-title").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("catalog-title").assertIsDisplayed()
        compose.onNodeWithTag("product-trail-kit").performClick()
        compose.onNodeWithTag("details-name").assertTextEquals("Trail kit")
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("catalog-title").assertIsDisplayed()
    }
}
