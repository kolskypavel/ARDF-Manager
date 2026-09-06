package kolskypavel.ardfmanager.results

import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceBand
import kolskypavel.ardfmanager.backend.room.enums.RaceLevel
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.room.enums.StartTimeSource
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import kolskypavel.ardfmanager.backend.sportident.SITime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class StartTImeSourceTests {

    private lateinit var dataProcessor: DataProcessor
    private val raceId = UUID.randomUUID()
    private val competitorId = UUID.randomUUID()
    private val raceStartTime = LocalDateTime.of(2023, 10, 1, 10, 0)
    private lateinit var race: Race

    @Before
    fun setup() {
        dataProcessor = mock(DataProcessor::class.java)
        race = Race(
            id = raceId,
            name = "Test Race",
            apiKey = "",
            startDateTime = raceStartTime,
            raceType = RaceType.CLASSIC,
            raceLevel = RaceLevel.PRACTICE,
            raceBand = RaceBand.M80,
            timeLimit = Duration.ofHours(2)
        )
    }

    // Helper methods for data mocking
    private fun createCompetitor(drawnRelativeStartTime: Duration?): Competitor {
        return Competitor(
            id = competitorId,
            raceId = raceId,
            categoryId = null,
            firstName = "John",
            lastName = "Doe",
            club = "Test Club",
            index = "JDOE1",
            isMan = true,
            birthYear = 1990,
            siNumber = 123456,
            siRent = false,
            startNumber = 101,
            drawnRelativeStartTime = drawnRelativeStartTime
        )
    }

    private fun createResult(startTime: SITime?, modified: Boolean, source: StartTimeSource): Result {
        return Result(
            id = UUID.randomUUID(),
            raceId = raceId,
            competitorId = competitorId,
            siNumber = 123456,
            cardType = SIConstants.SI_CARD5,
            checkTime = null,
            startTime = startTime,
            finishTime = SITime(LocalTime.of(11, 0)),
            readoutTime = LocalDateTime.now(),
            automaticStatus = true,
            resultStatus = ResultStatus.OK,
            points = 0,
            runTime = Duration.ofMinutes(60),
            modified = modified,
            sent = false,
            startTimeSource = source
        )
    }

    /**
     * Verifies that if a result is marked as modified (manual edit), 
     * it is not overwritten by a drawn start time from the start list.
     */
    @Test
    fun testUpdateManualPreservation() = runTest {
        val competitor = createCompetitor(drawnRelativeStartTime = Duration.ofMinutes(10))
        val manualStartTime = SITime(LocalTime.of(10, 5))
        val result = createResult(
            startTime = manualStartTime,
            modified = true,
            source = StartTimeSource.PUNCHED
        )

        `when`(dataProcessor.getResultByCompetitor(competitorId)).thenReturn(result)
        `when`(dataProcessor.getPunchesByResult(result.id)).thenReturn(emptyList())

        ResultsProcessor.updateResultsForCompetitor(competitor, race, dataProcessor)

        assertEquals(manualStartTime.getSeconds(), result.startTime?.getSeconds())
        assertEquals(true, result.modified)
    }

    /**
     * Verifies that a punched start time (read from a card) is not 
     * overwritten by a drawn start time during a result update.
     */
    @Test
    fun testUpdatePunchedPreservation() = runTest {
        val competitor = createCompetitor(drawnRelativeStartTime = Duration.ofMinutes(10))
        val punchedStartTime = SITime(LocalTime.of(10, 5))
        val result = createResult(
            startTime = punchedStartTime,
            modified = false,
            source = StartTimeSource.PUNCHED
        )

        `when`(dataProcessor.getResultByCompetitor(competitorId)).thenReturn(result)
        `when`(dataProcessor.getPunchesByResult(result.id)).thenReturn(emptyList())

        ResultsProcessor.updateResultsForCompetitor(competitor, race, dataProcessor)

        assertEquals(punchedStartTime.getSeconds(), result.startTime?.getSeconds())
        assertEquals(StartTimeSource.PUNCHED, result.startTimeSource)
    }

    /**
     * Verifies that if a result currently uses a drawn start time, 
     * it correctly updates when a new drawn start time is provided for the competitor.
     */
    @Test
    fun testUpdateDrawnOverDrawn() = runTest {
        val competitor = createCompetitor(drawnRelativeStartTime = Duration.ofMinutes(20))
        val oldDrawnStartTime = SITime(LocalTime.of(10, 10))
        val result = createResult(
            startTime = oldDrawnStartTime,
            modified = false,
            source = StartTimeSource.DRAWN
        )

        val expectedStartTime = SITime(LocalTime.of(10, 20))

        `when`(dataProcessor.getResultByCompetitor(competitorId)).thenReturn(result)
        `when`(dataProcessor.getPunchesByResult(result.id)).thenReturn(emptyList())

        ResultsProcessor.updateResultsForCompetitor(competitor, race, dataProcessor)

        assertEquals(expectedStartTime.getSeconds(), result.startTime?.getSeconds())
        assertEquals(StartTimeSource.DRAWN, result.startTimeSource)
    }

    /**
     * Verifies that a drawn start time is applied to a result if 
     * no start time currently exists.
     */
    @Test
    fun testUpdateInitialDrawn() = runTest {
        val competitor = createCompetitor(drawnRelativeStartTime = Duration.ofMinutes(15))
        val result = createResult(
            startTime = null,
            modified = false,
            source = StartTimeSource.PUNCHED
        )

        val expectedStartTime = SITime(LocalTime.of(10, 15))

        `when`(dataProcessor.getResultByCompetitor(competitorId)).thenReturn(result)
        `when`(dataProcessor.getPunchesByResult(result.id)).thenReturn(emptyList())

        ResultsProcessor.updateResultsForCompetitor(competitor, race, dataProcessor)

        assertEquals(expectedStartTime.getSeconds(), result.startTime?.getSeconds())
        assertEquals(StartTimeSource.DRAWN, result.startTimeSource)
    }

    /**
     * Verifies that when processing manual punch data, if a START record type 
     * is present, the result's source is correctly updated to PUNCHED.
     */
    @Test
    fun testManualPunchSource() = runTest {
        val result = createResult(startTime = null, modified = false, source = StartTimeSource.DRAWN)
        val startPunchTime = SITime(LocalTime.of(10, 5))
        val punches = arrayListOf(
            Punch(
                UUID.randomUUID(),
                raceId,
                result.id,
                123456,
                0,
                startPunchTime,
                startPunchTime,
                SIRecordType.START,
                0,
                PunchStatus.VALID,
                Duration.ZERO
            )
        )

        `when`(dataProcessor.getCompetitor(competitorId)).thenReturn(createCompetitor(null))

        ResultsProcessor.processManualPunchData(
            result,
            punches,
            null,
            race,
            dataProcessor,
            modified = true
        )

        assertEquals(startPunchTime.getSeconds(), result.startTime?.getSeconds())
        assertEquals(StartTimeSource.PUNCHED, result.startTimeSource)
        assertEquals(true, result.modified)
    }

    /**
     * Verifies that getStartTimeFromStartList correctly sets the 
     * start time source to DRAWN when a time is found.
     */
    @Test
    fun testGetDrawnTimeSource() {
        val competitor = createCompetitor(drawnRelativeStartTime = Duration.ofMinutes(30))
        val result = createResult(startTime = null, modified = false, source = StartTimeSource.PUNCHED)

        val expectedStartTime = SITime(LocalTime.of(10, 30))

        val found = ResultsProcessor.getStartTimeFromStartList(result, competitor, race)

        assertEquals(true, found)
        assertEquals(expectedStartTime.getSeconds(), result.startTime?.getSeconds())
        assertEquals(StartTimeSource.DRAWN, result.startTimeSource)
    }

    /**
     * Verifies that getStartTimeFromStartList returns false and leaves 
     * the source unchanged if the competitor has no drawn start time.
     */
    @Test
    fun testGetDrawnTimeMissing() {
        val competitor = createCompetitor(drawnRelativeStartTime = null)
        val result = createResult(startTime = null, modified = false, source = StartTimeSource.PUNCHED)

        val found = ResultsProcessor.getStartTimeFromStartList(result, competitor, race)

        assertEquals(false, found)
        assertEquals(null, result.startTime)
        assertEquals(StartTimeSource.PUNCHED, result.startTimeSource) // Unchanged
    }
}
