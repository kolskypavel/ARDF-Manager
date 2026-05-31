package org.openardf.radioomanager.shared.cli

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.SIRecordType
import org.openardf.radioomanager.shared.results.CourseEvaluator
import org.openardf.radioomanager.shared.results.EvaluationControlPoint
import org.openardf.radioomanager.shared.results.EvaluationPunch
import org.openardf.radioomanager.shared.sportident.SportIdentCodes

fun main() {
    check(SportIdentCodes.isSICodeValid(31))

    val evaluation = CourseEvaluator.evaluate(
        RaceType.CLASSIC,
        punches = listOf(
            EvaluationPunch(31, SIRecordType.CONTROL),
            EvaluationPunch(32, SIRecordType.CONTROL)
        ),
        controlPoints = listOf(
            EvaluationControlPoint(31, ControlPointType.CONTROL),
            EvaluationControlPoint(32, ControlPointType.CONTROL)
        )
    )

    check(evaluation.points == 2)
    println("Radio-O-Manager desktop shared smoke OK")
}
