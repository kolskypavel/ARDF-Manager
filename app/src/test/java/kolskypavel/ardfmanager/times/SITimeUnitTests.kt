package kolskypavel.ardfmanager.times

import junit.framework.TestCase.assertEquals
import kolskypavel.ardfmanager.backend.sportident.SITime
import org.junit.Test
import java.time.LocalTime

class SITimeUnitTests {

    @Test
    fun checkBasicSeconds() {
        val time = SITime()
        assertEquals("0", time.getSeconds().toString())
        time.setTime(LocalTime.of(1, 0))
        assertEquals("3600", time.getSeconds().toString())
    }

    @Test
    fun checkToString() {
        val time = SITime(LocalTime.of(19, 20), 0, 0)
        assertEquals("19:20:00,0,0", time.toString())
    }

    @Test
    fun checkHalfDayAddition() {
        val time = SITime(LocalTime.of(9, 0))
        assertEquals("32400", time.getSeconds().toString())

        time.addHalfDay()
        assertEquals("75600", time.getSeconds().toString())

        assertEquals("0", time.getDayOfWeek().toString())
        assertEquals("0", time.getWeek().toString())
        time.addHalfDay()
        assertEquals("1", time.getDayOfWeek().toString())

        for (i in 1..13) {
            time.addHalfDay()
        }
        assertEquals("0", time.getDayOfWeek().toString())
        assertEquals("1", time.getWeek().toString())
        time.addHalfDay()
        assertEquals("1", time.getDayOfWeek().toString())
    }

    @Test
    fun checkDayChange() {

    }

    @Test
    fun checkWeekChange() {

    }


    @Test
    fun checkSplits() {

    }
}