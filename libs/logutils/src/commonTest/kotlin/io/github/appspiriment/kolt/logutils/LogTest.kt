package io.github.appspiriment.kolt.logutils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogTest {

    @Test
    fun testLogEnableToggle() {
        Log.init(enabled = false)
        assertFalse(Log.enabled)

        Log.init(enabled = true)
        assertTrue(Log.enabled)
    }

    @Test
    fun testLogLevelEnum() {
        assertEquals(LogLevel.VERBOSE, LogLevel.entries[0])
        assertEquals(LogLevel.ERROR, LogLevel.entries[4])
    }
}
