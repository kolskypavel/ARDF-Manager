package kolskypavel.ardfmanager.results

import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
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
class ResultsEvaluationUnitTests {

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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
                )
            )
        }
        controlPoints.last().type = ControlPointType.BEACON
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
                        randCode,
                        SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                    )
                )
                controlPoints.add(
                    ControlPoint(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        randCode,
                        null,
                        ControlPointType.CONTROL,
                        i,
                        0
                    )
                )
            }

            controlPoints.last().type = ControlPointType.BEACON
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
                )
            )
        }
        controlPoints.last().type = ControlPointType.BEACON

        punches.add(
            Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                36,
                SITime(), SIRecordType.CONTROL, 19, PunchStatus.UNKNOWN, null
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
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
                )
            )
        }

        controlPoints.last().type = ControlPointType.BEACON

        for (i in 3..6) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
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
                31,
                SITime(), SIRecordType.CONTROL, 0, PunchStatus.UNKNOWN, null
            )
        )
        controlPoints.add(
            ControlPoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                31,
                null,
                ControlPointType.CONTROL,
                0,
                0
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
                32,
                SITime(), SIRecordType.CONTROL, 0, PunchStatus.UNKNOWN, null
            )
        )
        controlPoints.add(
            ControlPoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                32,
                null,
                ControlPointType.CONTROL,
                0,
                0
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
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
                62,
                SITime(), SIRecordType.CONTROL, 0, PunchStatus.UNKNOWN, null
            )
        )
        punches.add(
            4, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                64,
                SITime(), SIRecordType.CONTROL, 0, PunchStatus.UNKNOWN, null
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
                )
            )
        }
        controlPoints[4].type = ControlPointType.SEPARATOR
        controlPoints[7].type = ControlPointType.SEPARATOR
        controlPoints.last().type = ControlPointType.BEACON

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
                99,
                SITime(), SIRecordType.CONTROL, 15, PunchStatus.UNKNOWN, null
            )
        )

        punches.add(
            9, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                67,
                SITime(), SIRecordType.CONTROL, 15, PunchStatus.UNKNOWN, null
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
                )
            )
        }

        controlPoints[4].type = ControlPointType.SEPARATOR
        controlPoints[7].type = ControlPointType.SEPARATOR
        controlPoints.last().type = ControlPointType.BEACON

        punches.add(
            3, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                34,
                SITime(), SIRecordType.CONTROL, 15, PunchStatus.UNKNOWN, null
            )
        )

        punches.add(
            8, Punch(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                37,
                SITime(), SIRecordType.CONTROL, 15, PunchStatus.UNKNOWN, null
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
                    30 + i,
                    SITime(), SIRecordType.CONTROL, i, PunchStatus.UNKNOWN, null
                )
            )
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    0
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