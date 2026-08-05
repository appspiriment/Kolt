package io.github.appspiriment.kolt.utils.time

import kotlinx.datetime.TimeZone
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimingExtensionsTest {

    @Test
    fun testDms() {
        val degree = 12.5125
        val (deg, min, sec) = degree.dms
        assertEquals(12, deg)
        assertEquals(30, min)
        assertEquals(45, sec)
    }

    @Test
    fun testFromHoursToMillis() {
        val hours = 1.5
        assertEquals(5400000L, hours.fromHoursToMillis())
    }

    @Test
    fun testFromHoursToSeconds() {
        val hours = 1.0
        assertEquals(3600L, hours.fromHoursToSeconds())
    }

    @Test
    fun testToDMSString() {
        val degree = 12.5
        assertEquals("12° 30' 0\"", degree.toDMSString())
    }

    @Test
    fun testFromHoursToHMS() {
        val hours = 1.75
        val (h, m, s) = hours.fromHoursToHMS()
        assertEquals(1, h)
        assertEquals(45, m)
        assertEquals(0, s)
    }

    @Test
    fun testFromHoursToNazhika() {
        // 1 Hour = 2.5 Nazhika
        val hours = 1.0
        val (nazhika, vinazhika) = hours.fromHoursToNazhika()
        assertEquals(2, nazhika)
        assertEquals(30, vinazhika)
    }

    @Test
    fun testHourstoNazhikaVinazhikaString() {
        val hours = 1.0
        assertEquals("2 നാ 30 വി", hours.hourstoNazhikaVinazhikaString())
    }

    @Test
    fun testNazhikaToNazhikaVinazhika() {
        val decimalNazhika = 2.5
        val (nazhika, vinazhika) = decimalNazhika.nazhikaToNazhikaVinazhika()
        assertEquals(2, nazhika)
        assertEquals(30, vinazhika)
    }

    @Test
    fun testMillisToDecimalHour() {
        val millis = 5400000L
        assertTrue(abs(1.5 - millis.millisToDecimalHour()) < 0.0001)
    }

    @Test
    fun testMillisToDays() {
        val millis = 172800000L // 2 days
        assertTrue(abs(2.0 - millis.millisToDays()) < 0.0001)
    }

    @Test
    fun testMillisToHmaTime() {
        val millis = 1768059540000L // 2025-01-01 00:00:00 UTC
        assertEquals("03:39 PM", millis.millisToHmaTime(TimeZone.UTC))

        val nullMillis: Long? = null
        assertNull(nullMillis.millisToHmaTime())
    }

    @Test
    fun testMillisToMMddHmaTime() {
        val millis = 1735689600000L // 2025-01-01 00:00:00 UTC
        assertEquals("Jan 01 12:00 AM", millis.millisToMMddHmaTime(TimeZone.UTC))
    }

    @Test
    fun testMillisToDateTime() {
        val millis = 1735689600000L
        assertEquals("2025-01-01", millis.millisToDateTime("yyyy-MM-dd", TimeZone.UTC))
    }
}
