package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.PunchStatus
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.domain.SIRecordType

/** Control definition reduced to the fields needed by the course evaluator. */
data class EvaluationControlPoint(
    val siCode: Int,
    val type: ControlPointType
)

/** Punch definition reduced to the fields needed by the course evaluator. */
data class EvaluationPunch(
    val siCode: Int,
    val type: SIRecordType
)

/** Course evaluation output: point total, final result status, and per-punch statuses. */
data class CourseEvaluation(
    val points: Int,
    val resultStatus: ResultStatus,
    val punchStatuses: List<PunchStatus>
)

/** Shared evaluator for ARDF/orienteering course completion rules. */
object CourseEvaluator {
    /** Evaluates punches against a course for the selected race type. */
    fun evaluate(
        raceType: RaceType,
        punches: List<EvaluationPunch>,
        controlPoints: List<EvaluationControlPoint>
    ): CourseEvaluation {
        return when (raceType) {
            RaceType.CLASSIC, RaceType.SHORT, RaceType.FOXORING -> evaluateClassics(punches, controlPoints)
            RaceType.SPRINT -> evaluateSprint(punches, controlPoints)
            RaceType.ORIENTEERING -> evaluateOrienteering(punches, controlPoints)
        }
    }

    private fun evaluateClassics(
        punches: List<EvaluationPunch>,
        controlPoints: List<EvaluationControlPoint>
    ): CourseEvaluation {
        val loop = evaluateLoop(punches, controlPoints)
        return CourseEvaluation(loop.points, statusForPointRace(loop.points), loop.statuses)
    }

    private fun evaluateSprint(
        punches: List<EvaluationPunch>,
        controlPoints: List<EvaluationControlPoint>
    ): CourseEvaluation {
        val statuses = MutableList(punches.size) { PunchStatus.UNKNOWN }
        val separators = controlPoints.withIndex()
            .filter { it.value.type == ControlPointType.SEPARATOR }
            .map { it.value.siCode to it.index }

        val points = if (separators.isNotEmpty()) {
            var total = 0
            var prevPunchSep = 0
            var prevControlSep = 0
            var separatorIndex = 0

            for ((punchIndex, punch) in punches.withIndex()) {
                if (separatorIndex < separators.size && punch.siCode == separators[separatorIndex].first) {
                    total += evaluateLoopInto(
                        punches,
                        controlPoints,
                        statuses,
                        punchRange = prevPunchSep..<punchIndex,
                        controlRange = prevControlSep..<separators[separatorIndex].second
                    )
                    prevPunchSep = punchIndex
                    prevControlSep = separators[separatorIndex].second
                    separatorIndex++
                }
            }

            total + evaluateLoopInto(
                punches,
                controlPoints,
                statuses,
                punchRange = prevPunchSep..<punches.size,
                controlRange = prevControlSep..<controlPoints.size
            )
        } else {
            evaluateLoopInto(
                punches,
                controlPoints,
                statuses,
                punchRange = punches.indices,
                controlRange = controlPoints.indices
            )
        }

        return CourseEvaluation(points, statusForPointRace(points), statuses)
    }

    private fun evaluateOrienteering(
        punches: List<EvaluationPunch>,
        controlPoints: List<EvaluationControlPoint>
    ): CourseEvaluation {
        val statuses = MutableList(punches.size) { PunchStatus.UNKNOWN }
        var controlPointIndex = 0
        var points = 0

        for ((punchIndex, punch) in punches.withIndex()) {
            if (controlPointIndex >= controlPoints.size) {
                break
            }

            if (punch.siCode == controlPoints[controlPointIndex].siCode) {
                controlPointIndex++
                points++
                statuses[punchIndex] = PunchStatus.VALID
            } else {
                statuses[punchIndex] = PunchStatus.INVALID
            }
        }

        return CourseEvaluation(
            points = points,
            resultStatus = if (points == controlPoints.size) ResultStatus.OK else ResultStatus.MISPUNCHED,
            punchStatuses = statuses
        )
    }

    private fun evaluateLoop(
        punches: List<EvaluationPunch>,
        controlPoints: List<EvaluationControlPoint>
    ): LoopEvaluation {
        val statuses = MutableList(punches.size) { PunchStatus.UNKNOWN }
        val points = evaluateLoopInto(punches, controlPoints, statuses, punches.indices, controlPoints.indices)
        return LoopEvaluation(points, statuses)
    }

    private fun evaluateLoopInto(
        punches: List<EvaluationPunch>,
        controlPoints: List<EvaluationControlPoint>,
        statuses: MutableList<PunchStatus>,
        punchRange: IntRange,
        controlRange: IntRange
    ): Int {
        val loopControls = controlRange.map { controlPoints[it] }
        val codes = loopControls.map { it.siCode }.toSet()
        val taken = mutableSetOf<Int>()
        var points = 0
        val beacon = if (loopControls.isNotEmpty() && loopControls.last().type == ControlPointType.BEACON) {
            loopControls.last().siCode
        } else {
            -1
        }

        for (punchIndex in punchRange) {
            val punch = punches[punchIndex]
            if (punch.type == SIRecordType.CONTROL && codes.contains(punch.siCode)) {
                if (!taken.contains(punch.siCode) && punch.siCode != beacon) {
                    statuses[punchIndex] = PunchStatus.VALID
                    points++
                    taken.add(punch.siCode)
                } else if (punch.siCode == beacon) {
                    if (punchIndex == punchRange.last) {
                        statuses[punchIndex] = PunchStatus.VALID
                        points++
                    } else {
                        statuses[punchIndex] = PunchStatus.INVALID
                    }
                } else {
                    statuses[punchIndex] = PunchStatus.DUPLICATE
                }
            } else {
                statuses[punchIndex] = PunchStatus.UNKNOWN
            }
        }
        return points
    }

    private fun statusForPointRace(points: Int): ResultStatus =
        if (points > 1) ResultStatus.OK else ResultStatus.NO_RANKING

    private data class LoopEvaluation(
        val points: Int,
        val statuses: List<PunchStatus>
    )
}
