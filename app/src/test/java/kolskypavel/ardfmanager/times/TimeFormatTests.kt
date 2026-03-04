package kolskypavel.ardfmanager.times

import junit.framework.TestCase.assertEquals
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import org.junit.Test
import java.time.LocalDateTime

class TimeFormatTests {

    @Test
    fun testIsoLocalDateTimeFormat(){
        val dateTime = LocalDateTime.of(2026,1,1,21,10,20)
        assertEquals("2026-01-01T21:10:20", TimeProcessor.formatIsoLocalDateTime(dateTime))
    }
}