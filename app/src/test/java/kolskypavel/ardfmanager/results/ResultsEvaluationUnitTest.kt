package kolskypavel.ardfmanager.results

import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.util.Random
import java.util.UUID

/**
 * Tests the correct evaluation of the punches
 * TODO: Add more random data
 */
class ResultsEvaluationUnitTest {

    @Test
    fun testClassicsCorrectData() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }
        controlPoints.last().beacon = true
        ResultsProcessor.evaluateClassics(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        //Check the punches
        for (punch in punches) {
            assertEquals(PunchStatus.VALID, punch.punchStatus)
        }
        assertEquals(6, result.points)
    }

    @Test
    fun testClassicsRandomData() {
        for (t in 0..50) {
            val result = Result(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                true,
                RaceStatus.NOT_PROCESSED,
                0,
                Duration.ZERO
            )
            val punches = ArrayList<Punch>()
            val controlPoints = ArrayList<ControlPoint>()

            val randLength = Random().nextInt(1000) + 1
            var randCode = 0

            for (i in 0..randLength) {

                randCode += Random().nextInt(10) + 1

                punches.add(
                    Punch(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        null,
                        null,
                        SIRecordType.CONTROL,
                        randCode, i, SITime(), null, PunchStatus.UNKNOWN, null
                    )
                )
                controlPoints.add(
                    ControlPoint(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        randCode,
                        i,
                        null,
                        0,
                        0,
                        false,
                        false
                    )
                )
            }

            controlPoints.last().beacon = true
            ResultsProcessor.evaluateClassics(punches, controlPoints, result)
            assertEquals(RaceStatus.VALID, result.raceStatus)
            assertEquals(randLength + 1, result.points)
        }
    }

    @Test
    fun testClassicsDuplicateBeaconEvaluation() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }
        controlPoints.last().beacon = true

        punches.add(
            Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                36, 19, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )

        ResultsProcessor.evaluateClassics(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)

        //Check the punches
        assertEquals(6, result.points)
        assertEquals(PunchStatus.INVALID, punches[punches.size - 2].punchStatus)
    }

    @Test
    fun testClassicsPunchesAfterBeaconEvaluation() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..6) {
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }

        controlPoints.last().beacon = true

        for (i in 3..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
        }

        for (i in 1..2) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
        }

        ResultsProcessor.evaluateClassics(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)

        //Check the punches
        assertEquals(5, result.points)
    }

    @Test
    fun testClassicsRankingOrNoRanking() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()
        ResultsProcessor.evaluateClassics(punches, controlPoints, result)
        assertEquals(RaceStatus.NO_RANKING, result.raceStatus)

        punches.add(
            Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                31, 0, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )
        controlPoints.add(
            ControlPoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                31,
                0,
                null,
                0,
                0,
                false,
                false
            )
        )
        ResultsProcessor.evaluateClassics(punches, controlPoints, result)
        assertEquals(RaceStatus.NO_RANKING, result.raceStatus)
        punches.add(
            Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                32, 0, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )
        controlPoints.add(
            ControlPoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                32,
                0,
                null,
                0,
                0,
                false,
                false
            )
        )
        ResultsProcessor.evaluateClassics(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
    }

    @Test
    fun testOrienteeringCorrectData() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }
        ResultsProcessor.evaluateOrienteering(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        assertEquals(6, result.points)
    }

    @Test
    fun testOrienteeringDataWithMistake() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }
        punches.add(
            2, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                62, 0, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )
        punches.add(
            4, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                64, 0, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )
        ResultsProcessor.evaluateOrienteering(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        assertEquals(6, result.points)
        assertEquals(PunchStatus.INVALID, punches[2].punchStatus)
        assertEquals(PunchStatus.INVALID, punches[4].punchStatus)
    }

    @Test
    fun testOrienteeringIncorrectData() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }
        punches[2].siCode = 44
        ResultsProcessor.evaluateOrienteering(punches, controlPoints, result)
        assertEquals(RaceStatus.DISQUALIFIED, result.raceStatus)
    }

    @Test
    fun testSprintCorrectData() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()
        ResultsProcessor.evaluateSprint(punches, controlPoints, result)
        assertEquals(RaceStatus.NO_RANKING, result.raceStatus)

        for (i in 1..12) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }
        controlPoints[4].separator = true
        controlPoints[7].separator = true
        controlPoints.last().beacon = true

        ResultsProcessor.evaluateSprint(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        assertEquals(12, result.points)
        for (punch in punches) {
            assertEquals(PunchStatus.VALID, punch.punchStatus)
        }

        //Add some random invalid data
        punches.add(
            5, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                99, 15, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )

        punches.add(
            9, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                67, 15, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )
        ResultsProcessor.evaluateSprint(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        assertEquals(12, result.points)
        assertEquals(PunchStatus.UNKNOWN, punches[5].punchStatus)
        assertEquals(PunchStatus.UNKNOWN, punches[9].punchStatus)
    }

    @Test
    fun testSprintDatWithMistakes() {
        // Double punched separator
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()
        ResultsProcessor.evaluateSprint(punches, controlPoints, result)
        assertEquals(RaceStatus.NO_RANKING, result.raceStatus)

        for (i in 1..12) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    false
                )
            )
        }

        controlPoints[4].separator = true
        controlPoints[7].separator = true
        controlPoints.last().beacon = true

        punches.add(
            3, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                34, 15, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )

        punches.add(
            8, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                SIRecordType.CONTROL,
                37, 15, SITime(), null, PunchStatus.UNKNOWN, null
            )
        )
        for (pun in punches.withIndex()) {
            pun.value.order = pun.index + 1
        }

        ResultsProcessor.evaluateSprint(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        assertEquals(12, result.points)
        assertEquals(PunchStatus.DUPLICATE, punches[4].punchStatus)
        assertEquals(PunchStatus.DUPLICATE, punches[8].punchStatus)
    }

    @Test
    fun testSprintAllSeparators() {
        val result = Result(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            true,
            RaceStatus.NOT_PROCESSED,
            0,
            Duration.ZERO
        )
        val punches = ArrayList<Punch>()
        val controlPoints = ArrayList<ControlPoint>()

        for (i in 1..12) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    SIRecordType.CONTROL,
                    30 + i, i, SITime(), null, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    i,
                    null,
                    0,
                    0,
                    false,
                    true
                )
            )
        }

        ResultsProcessor.evaluateSprint(punches, controlPoints, result)
        assertEquals(RaceStatus.VALID, result.raceStatus)
        assertEquals(12, result.points)
        for (punch in punches) {
            assertEquals(PunchStatus.VALID, punch.punchStatus)
        }
    }
}