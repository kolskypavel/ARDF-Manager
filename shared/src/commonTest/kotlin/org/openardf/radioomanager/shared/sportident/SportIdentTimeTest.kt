package org.openardf.radioomanager.shared.sportident

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SportIdentTimeTest {
    @Test
    fun calculatesSecondsFromTimeAndDateFields() {
        val time = SportIdentTime()
        assertEquals(0, time.getSeconds())

        time.setTime(1, 0)
        assertEquals(3600, time.getSeconds())
    }

    @Test
    fun serializesAndParsesStableStorageFormat() {
        val time = SportIdentTime(19, 20, 0, 0, 0)
        assertEquals("19:20:00,0,0", time.toString())

        val parsed = SportIdentTime.from("15:45:30,2,1")
        assertEquals("15:45:30", parsed.getTimeString())
        assertEquals(2, parsed.getDayOfWeek())
        assertEquals(1, parsed.getWeek())
    }

    @Test
    fun rejectsInvalidStorageFormat() {
        assertFailsWith<IllegalArgumentException> {
            SportIdentTime.from("invalid,string")
        }
    }

    @Test
    fun handlesHalfDayAndWeekRollovers() {
        val time = SportIdentTime(9, 0)
        assertEquals(32400, time.getSeconds())

        time.addHalfDay()
        assertEquals(75600, time.getSeconds())
        assertEquals(0, time.getDayOfWeek())
        assertEquals(0, time.getWeek())

        time.addHalfDay()
        assertEquals(1, time.getDayOfWeek())

        repeat(13) {
            time.addHalfDay()
        }

        assertEquals(0, time.getDayOfWeek())
        assertEquals(1, time.getWeek())

        time.addHalfDay()
        assertEquals(1, time.getDayOfWeek())
    }

    @Test
    fun handlesDayAndWeekChanges() {
        val dayChange = SportIdentTime(23, 59, 0, 6, 0)
        dayChange.addDay()
        assertEquals(0, dayChange.getDayOfWeek())
        assertEquals(1, dayChange.getWeek())

        val weekChange = SportIdentTime(12, 0, 0, 0, 0)
        repeat(7) {
            weekChange.addDay()
        }

        assertEquals(0, weekChange.getDayOfWeek())
        assertEquals(1, weekChange.getWeek())
    }

    @Test
    fun calculatesSplitsAndDifferences() {
        val start = SportIdentTime(10, 0, 0, 0, 0)
        val end = SportIdentTime(11, 30, 0, 0, 0)
        assertEquals(90 * 60, SportIdentTime.split(start, end))
        assertEquals(90 * 60, SportIdentTime.difference(end, start))

        val nextDay = SportIdentTime(9, 0, 0, 1, 0)
        assertEquals(23 * 60 * 60, SportIdentTime.split(start, nextDay))
    }

    @Test
    fun comparesByAbsoluteSportIdentSeconds() {
        val t1 = SportIdentTime(8, 0, 0, 0, 0)
        val t2 = SportIdentTime(9, 0, 0, 0, 0)

        assertTrue(t2.isAfter(t1))
        assertTrue(t2.isAtOrAfter(t1))
        assertFalse(t1.isAfter(t2))
        assertEquals(-1, t1.compareTo(t2))
        assertEquals(1, t2.compareTo(t1))
        assertEquals(0, t1.compareTo(SportIdentTime(8, 0, 0, 0, 0)))
    }
}
