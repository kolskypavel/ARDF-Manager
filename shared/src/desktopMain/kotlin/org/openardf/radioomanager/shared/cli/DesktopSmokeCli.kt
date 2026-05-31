package org.openardf.radioomanager.shared.cli

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.RaceBand
import org.openardf.radioomanager.shared.domain.RaceLevel
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.domain.SIRecordType
import org.openardf.radioomanager.shared.event.EventAlias
import org.openardf.radioomanager.shared.event.EventCategory
import org.openardf.radioomanager.shared.event.EventCategoryData
import org.openardf.radioomanager.shared.event.EventCompetitor
import org.openardf.radioomanager.shared.event.EventCompetitorCategory
import org.openardf.radioomanager.shared.event.EventCompetitorData
import org.openardf.radioomanager.shared.event.EventRace
import org.openardf.radioomanager.shared.event.EventRaceData
import org.openardf.radioomanager.shared.event.EventReadoutData
import org.openardf.radioomanager.shared.event.EventResult
import org.openardf.radioomanager.shared.event.EventValidationRules
import org.openardf.radioomanager.shared.files.EventCsvRows
import org.openardf.radioomanager.shared.files.FileConstants
import org.openardf.radioomanager.shared.files.TemplateRenderer
import org.openardf.radioomanager.shared.files.TimedPunchCsvField
import org.openardf.radioomanager.shared.results.CourseEvaluator
import org.openardf.radioomanager.shared.results.EventResultPlacement
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
    val raceData = sampleRaceData()
    check(EventValidationRules.validateRaceData(raceData).isEmpty())
    check(
        EventResultPlacement.sortByPlace(raceData.competitorData)
            .single()
            .readoutData
            ?.result
            ?.place == 1
    )
    check(
        EventCsvRows.readoutRow(
            siNumber = 123456,
            checkTimeText = null,
            startTimeText = "10:00:00",
            finishTimeText = "10:45:00",
            controlPunches = listOf(TimedPunchCsvField(31, "10:15:00"))
        ) == "123456;;10:00:00;10:45:00;1;31;10:15:00"
    )
    check(
        TemplateRenderer.render(
            "{{race_name}}${FileConstants.KEY_TAB}{{title_results}}",
            mapOf(
                FileConstants.KEY_RACE_NAME to "Desktop smoke",
                FileConstants.KEY_TITLE_RESULTS to "Results"
            )
        ) == "Desktop smoke\tResults"
    )
    println("Radio-O-Manager desktop shared smoke OK")
}

private fun sampleRaceData(): EventRaceData =
    EventRaceData(
        race = EventRace(
            id = "race",
            name = "Desktop smoke",
            apiKey = "",
            startDateTimeIso = "2026-05-30T10:00",
            raceType = RaceType.CLASSIC,
            raceLevel = RaceLevel.PRACTICE,
            raceBand = RaceBand.M80,
            timeLimitSeconds = 7_200
        ),
        categories = listOf(
            EventCategoryData(
                category = EventCategory(
                    id = "M21",
                    raceId = "race",
                    name = "M21",
                    isMan = true,
                    maxAge = null,
                    lengthMeters = 5_000,
                    climbMeters = 100,
                    order = 1,
                    differentProperties = false,
                    raceType = null,
                    raceBand = null,
                    timeLimitSeconds = null,
                    controlPointsString = "31 32"
                ),
                controlPoints = emptyList(),
                competitors = emptyList()
            )
        ),
        aliases = listOf(EventAlias(id = "alias", raceId = "race", siCode = 31, name = "F1")),
        competitorData = listOf(
            EventCompetitorData(
                competitorCategory = EventCompetitorCategory(
                    competitor = EventCompetitor(
                        id = "competitor",
                        raceId = "race",
                        categoryId = "M21",
                        firstName = "Test",
                        lastName = "Runner",
                        club = "",
                        index = "",
                        isMan = true,
                        birthYear = null,
                        siNumber = 123456,
                        siRent = false,
                        startNumber = 1,
                        drawnStartTimeSeconds = null
                    ),
                    category = null
                ),
                readoutData = EventReadoutData(
                    result = EventResult(
                        id = "result",
                        raceId = "race",
                        competitorId = "competitor",
                        siNumber = 123456,
                        cardType = 5,
                        checkTimeSeconds = null,
                        startTimeSeconds = null,
                        finishTimeSeconds = null,
                        readoutDateTimeIso = "2026-05-30T10:45",
                        automaticStatus = true,
                        resultStatus = ResultStatus.OK,
                        points = 2,
                        runTimeSeconds = 2_700,
                        modified = false,
                        sent = false
                    ),
                    punches = emptyList()
                )
            )
        ),
        unmatchedReadoutData = emptyList()
    )
