package kolskypavel.ardfmanager.logging

import kolskypavel.ardfmanager.backend.logging.RollingDebugLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.Instant

class RollingDebugLogTest {

    @Test
    fun writeCreatesPrivateLogLine() {
        val directory = Files.createTempDirectory("rom-debug-log").toFile()
        val logger = RollingDebugLog(
            directory = directory,
            clock = { Instant.parse("2026-05-31T15:00:00Z") }
        )

        logger.write("I", "SI", "Reader connected")

        assertEquals(
            "2026-05-31T15:00:00Z I SI Reader connected\n",
            File(directory, "debug.log").readText()
        )
    }

    @Test
    fun writeSanitizesMultiLineMessages() {
        val directory = Files.createTempDirectory("rom-debug-log").toFile()
        val logger = RollingDebugLog(
            directory = directory,
            clock = { Instant.parse("2026-05-31T15:00:00Z") }
        )

        logger.write("W\n", "SI\tReader", "Line one\nLine two")

        val text = File(directory, "debug.log").readText()
        assertEquals("2026-05-31T15:00:00Z W SI Reader Line one Line two\n", text)
        assertFalse(text.dropLast(1).contains("\n"))
    }

    @Test
    fun writeRotatesAndRetainsConfiguredFileCount() {
        val directory = Files.createTempDirectory("rom-debug-log").toFile()
        val logger = RollingDebugLog(
            directory = directory,
            clock = { Instant.parse("2026-05-31T15:00:00Z") },
            maxFileBytes = 80,
            retainedFileCount = 3
        )

        repeat(5) { index ->
            logger.write("I", "Test", "message-$index-with-padding")
        }

        val files = logger.logFiles().map { it.name }.toSet()
        assertTrue("debug.log should exist", "debug.log" in files)
        assertTrue("first archive should exist", "debug.log.1" in files)
        assertTrue("second archive should exist", "debug.log.2" in files)
        assertFalse("only two archives should be retained", File(directory, "debug.log.3").exists())
    }
}
