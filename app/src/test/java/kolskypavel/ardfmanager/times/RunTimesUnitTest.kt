package kolskypavel.ardfmanager.times

import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class RunTimesUnitTest {

    @Test
    fun durationToMinuteStringTest() {
        assertEquals("00:00", TimeProcessor.durationToMinuteString(Duration.ZERO))
        assertEquals("64:00", TimeProcessor.durationToMinuteString(Duration.ofMinutes(64)))
        assertEquals(
            "59:22",
            TimeProcessor.durationToMinuteString(Duration.ofMinutes(59) + Duration.ofSeconds(22))
        )
        assertEquals("120:00", TimeProcessor.durationToMinuteString(Duration.ofHours(2)))
        assertEquals(
            "120:25",
            TimeProcessor.durationToMinuteString(Duration.ofHours(2) + Duration.ofSeconds(25))
        )
        assertEquals("1000:00", TimeProcessor.durationToMinuteString(Duration.ofMinutes(1000)))
        assertEquals("-10:00", TimeProcessor.durationToMinuteString(Duration.ofMinutes(-10)))
        assertEquals("-100:00", TimeProcessor.durationToMinuteString(Duration.ofMinutes(-100)))
        assertEquals("-10000:00", TimeProcessor.durationToMinuteString(Duration.ofMinutes(-10000)))
    }

    @Test
    fun durationFromStartTest() {
//        assertEquals(
//            null,
//            TimeProcessor.runDurationFromStart()
//        )
    }
}