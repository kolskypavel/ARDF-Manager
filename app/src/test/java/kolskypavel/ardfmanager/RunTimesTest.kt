package kolskypavel.ardfmanager

import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class RunTimesTest {

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
    }

    @Test
    fun durationFromStartTest() {
        assertEquals(
            null,
            TimeProcessor.runDurationFromStart(LocalDate.now().plusDays(1), LocalTime.now())
        )
    }
}