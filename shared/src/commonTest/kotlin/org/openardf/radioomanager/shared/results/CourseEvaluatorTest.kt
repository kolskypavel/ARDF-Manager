package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.PunchStatus
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.domain.SIRecordType
import kotlin.test.Test
import kotlin.test.assertEquals

class CourseEvaluatorTest {
    @Test
    fun evaluatesClassicControlsDuplicatesAndBeacon() {
        val evaluation = CourseEvaluator.evaluate(
            RaceType.CLASSIC,
            punches = punches(31, 31, 36, 32, 36),
            controlPoints = controls(
                31 to ControlPointType.CONTROL,
                32 to ControlPointType.CONTROL,
                36 to ControlPointType.BEACON
            )
        )

        assertEquals(3, evaluation.points)
        assertEquals(ResultStatus.OK, evaluation.resultStatus)
        assertEquals(
            listOf(
                PunchStatus.VALID,
                PunchStatus.DUPLICATE,
                PunchStatus.INVALID,
                PunchStatus.VALID,
                PunchStatus.VALID
            ),
            evaluation.punchStatuses
        )
    }

    @Test
    fun evaluatesSprintBySeparatingLoops() {
        val evaluation = CourseEvaluator.evaluate(
            RaceType.SPRINT,
            punches = punches(31, 32, 40, 31, 32, 36),
            controlPoints = controls(
                31 to ControlPointType.CONTROL,
                32 to ControlPointType.CONTROL,
                40 to ControlPointType.SEPARATOR,
                31 to ControlPointType.CONTROL,
                32 to ControlPointType.CONTROL,
                36 to ControlPointType.BEACON
            )
        )

        assertEquals(6, evaluation.points)
        assertEquals(ResultStatus.OK, evaluation.resultStatus)
        assertEquals(
            listOf(
                PunchStatus.VALID,
                PunchStatus.VALID,
                PunchStatus.VALID,
                PunchStatus.VALID,
                PunchStatus.VALID,
                PunchStatus.VALID
            ),
            evaluation.punchStatuses
        )
    }

    @Test
    fun evaluatesOrienteeringInOrder() {
        val evaluation = CourseEvaluator.evaluate(
            RaceType.ORIENTEERING,
            punches = punches(31, 33),
            controlPoints = controls(
                31 to ControlPointType.CONTROL,
                32 to ControlPointType.CONTROL
            )
        )

        assertEquals(1, evaluation.points)
        assertEquals(ResultStatus.MISPUNCHED, evaluation.resultStatus)
        assertEquals(
            listOf(PunchStatus.VALID, PunchStatus.INVALID),
            evaluation.punchStatuses
        )
    }

    private fun punches(vararg codes: Int): List<EvaluationPunch> =
        codes.map { EvaluationPunch(it, SIRecordType.CONTROL) }

    private fun controls(vararg controls: Pair<Int, ControlPointType>): List<EvaluationControlPoint> =
        controls.map { EvaluationControlPoint(it.first, it.second) }
}
