package org.openardf.radioomanager.shared.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DurationFormatterTest {
    @Test
    fun formatsDurationAsMinuteString() {
        assertEquals("00:00", DurationFormatter.secondsToFormattedString(0, true))
        assertEquals("64:00", DurationFormatter.secondsToFormattedString(64 * 60, true))
        assertEquals("59:22", DurationFormatter.secondsToFormattedString(59 * 60 + 22, true))
        assertEquals("120:00", DurationFormatter.secondsToFormattedString(2 * 60 * 60, true))
        assertEquals("120:25", DurationFormatter.secondsToFormattedString(2 * 60 * 60 + 25, true))
        assertEquals("1000:00", DurationFormatter.secondsToFormattedString(1000 * 60, true))
        assertEquals("-10:00", DurationFormatter.secondsToFormattedString(-10 * 60, true))
        assertEquals("-100:00", DurationFormatter.secondsToFormattedString(-100 * 60, true))
        assertEquals("-10000:00", DurationFormatter.secondsToFormattedString(-10000 * 60, true))
    }

    @Test
    fun formatsDurationAsHourString() {
        assertEquals("00:00:00", DurationFormatter.secondsToFormattedString(0, false))
        assertEquals("00:15:19", DurationFormatter.secondsToFormattedString(15 * 60 + 19, false))
        assertEquals("01:04:00", DurationFormatter.secondsToFormattedString(64 * 60, false))
        assertEquals("00:59:22", DurationFormatter.secondsToFormattedString(59 * 60 + 22, false))
        assertEquals("02:14:22", DurationFormatter.secondsToFormattedString(2 * 60 * 60 + 14 * 60 + 22, false))
    }

    @Test
    fun parsesMinuteStringToSeconds() {
        assertEquals(12 * 60 + 34, DurationFormatter.minuteStringToSeconds("12:34"))
    }

    @Test
    fun rejectsInvalidMinuteStrings() {
        assertFailsWith<IllegalArgumentException> {
            DurationFormatter.minuteStringToSeconds("12")
        }
        assertFailsWith<IllegalArgumentException> {
            DurationFormatter.minuteStringToSeconds("12:xx")
        }
        assertFailsWith<IllegalArgumentException> {
            DurationFormatter.minuteStringToSeconds("12:99")
        }
    }
}
