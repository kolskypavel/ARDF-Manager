package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.domain.PunchStatus
import org.openardf.radioomanager.shared.domain.RaceBand
import org.openardf.radioomanager.shared.domain.RaceLevel
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.domain.SIRecordType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventValidationRulesTest {
    @Test
    fun acceptsValidRaceData() {
        assertTrue(EventValidationRules.validateRaceData(raceData()).isEmpty())
    }

    @Test
    fun reportsDuplicateEventData() {
        val raceData = raceData(
            race = race(name = ""),
            categories = listOf(categoryData("M21"), categoryData("M21")),
            aliases = listOf(alias("F1", 31), alias("F1", 32), alias("F2", 31)),
            competitors = listOf(
                competitorData("one", startNumber = 1, siNumber = 123),
                competitorData("two", startNumber = 1, siNumber = 123)
            )
        )

        val issues = EventValidationRules.validateRaceData(raceData)

        assertTrue(issues.contains(EventValidationIssue.BlankRaceName))
        assertTrue(issues.contains(EventValidationIssue.DuplicateCategoryNames(setOf("M21"))))
        assertTrue(issues.contains(EventValidationIssue.DuplicateAliasNames(setOf("F1"))))
        assertTrue(issues.contains(EventValidationIssue.DuplicateAliasCodes(setOf(31))))
        assertTrue(issues.contains(EventValidationIssue.DuplicateStartNumbers(setOf(1))))
        assertTrue(issues.contains(EventValidationIssue.DuplicateSINumbers(setOf(123))))
    }

    @Test
    fun reportsDuplicateStartAndFinishPunches() {
        val issues = EventValidationRules.validateRaceData(
            raceData(
                competitors = listOf(
                    competitorData(
                        id = "one",
                        readoutData = readout(
                            punches = listOf(
                                punch(SIRecordType.START),
                                punch(SIRecordType.START),
                                punch(SIRecordType.FINISH),
                                punch(SIRecordType.FINISH)
                            )
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                EventValidationIssue.MultipleStartPunches(123),
                EventValidationIssue.MultipleFinishPunches(123)
            ),
            issues
        )
    }

    private fun raceData(
        race: EventRace = race(),
        categories: List<EventCategoryData> = listOf(categoryData("M21")),
        aliases: List<EventAlias> = listOf(alias("F1", 31)),
        competitors: List<EventCompetitorData> = listOf(competitorData("one")),
        unmatchedReadoutData: List<EventReadoutData> = emptyList()
    ): EventRaceData =
        EventRaceData(
            race = race,
            categories = categories,
            aliases = aliases,
            competitorData = competitors,
            unmatchedReadoutData = unmatchedReadoutData
        )

    private fun race(name: String = "Race"): EventRace =
        EventRace(
            id = "race",
            name = name,
            apiKey = "",
            startDateTimeIso = "2026-05-30T10:00",
            raceType = RaceType.CLASSIC,
            raceLevel = RaceLevel.PRACTICE,
            raceBand = RaceBand.M80,
            timeLimitSeconds = 7_200
        )

    private fun categoryData(name: String): EventCategoryData =
        EventCategoryData(
            category = EventCategory(
                id = name,
                raceId = "race",
                name = name,
                isMan = true,
                maxAge = null,
                lengthMeters = 0,
                climbMeters = 0,
                order = 0,
                differentProperties = false,
                raceType = null,
                raceBand = null,
                timeLimitSeconds = null,
                controlPointsString = ""
            ),
            controlPoints = emptyList(),
            competitors = emptyList()
        )

    private fun alias(name: String, siCode: Int): EventAlias =
        EventAlias(id = "$name-$siCode", raceId = "race", siCode = siCode, name = name)

    private fun competitorData(
        id: String,
        startNumber: Int = 1,
        siNumber: Int? = 123,
        readoutData: EventReadoutData? = null
    ): EventCompetitorData =
        EventCompetitorData(
            competitorCategory = EventCompetitorCategory(
                competitor = EventCompetitor(
                    id = id,
                    raceId = "race",
                    categoryId = "M21",
                    firstName = id,
                    lastName = "Runner",
                    club = "",
                    index = "",
                    isMan = true,
                    birthYear = null,
                    siNumber = siNumber,
                    siRent = false,
                    startNumber = startNumber,
                    drawnStartTimeSeconds = null
                ),
                category = null
            ),
            readoutData = readoutData
        )

    private fun readout(punches: List<EventPunch>): EventReadoutData =
        EventReadoutData(
            result = EventResult(
                id = "result",
                raceId = "race",
                competitorId = "one",
                siNumber = 123,
                cardType = 5,
                checkTimeSeconds = null,
                startTimeSeconds = null,
                finishTimeSeconds = null,
                readoutDateTimeIso = "2026-05-30T10:00",
                automaticStatus = true,
                resultStatus = ResultStatus.OK,
                points = 0,
                runTimeSeconds = 0,
                modified = false,
                sent = false
            ),
            punches = punches.map { EventAliasPunch(it, alias = null) }
        )

    private fun punch(type: SIRecordType): EventPunch =
        EventPunch(
            id = type.name,
            raceId = "race",
            resultId = "result",
            cardNumber = null,
            siCode = 0,
            siTimeSeconds = 0,
            originalSiTimeSeconds = 0,
            punchType = type,
            order = 0,
            punchStatus = PunchStatus.UNKNOWN,
            splitSeconds = 0
        )
}
