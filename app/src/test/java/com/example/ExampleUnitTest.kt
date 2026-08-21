package com.example

import com.example.network.GeminiConstants
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun verifyDefaultGeminiModel() {
        assertEquals("gemini-3.6-flash", GeminiConstants.DEFAULT_MODEL)
        assertTrue(GeminiConstants.SYSTEM_INSTRUCTION.contains("multi-platform social media content strategist"))
    }
}
