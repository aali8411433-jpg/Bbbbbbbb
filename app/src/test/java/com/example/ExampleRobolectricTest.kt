package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CommandCategory
import com.example.model.Language
import com.example.router.CommandResult
import com.example.router.CommandRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS AI", appName)
    }

    @Test
    fun `test wake phrase routes to configured greeting`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.route("Hey Jarvis", Language.BENGALI, "হ্যাঁ আসগর বাবু, বলো।")
        assertTrue(result is CommandResult.ImmediateResponse)
        val immediate = result as CommandResult.ImmediateResponse
        assertEquals("হ্যাঁ আসগর বাবু, বলো।", immediate.spokenText)
        assertEquals(CommandCategory.GREETING, immediate.category)
    }

    @Test
    fun `test time query returns system info response`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.route("What time is it?", Language.ENGLISH, "Yes sir.")
        assertTrue(result is CommandResult.ImmediateResponse)
        val immediate = result as CommandResult.ImmediateResponse
        assertEquals(CommandCategory.SYSTEM_INFO, immediate.category)
    }

    @Test
    fun `test complex question routes to Gemini AI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.route("What is quantum computing?", Language.ENGLISH, "Yes sir.")
        assertTrue(result is CommandResult.RouteToAi)
        val ai = result as CommandResult.RouteToAi
        assertEquals("What is quantum computing?", ai.query)
    }
}
