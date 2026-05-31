package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.event.EventCompetitor
import org.openardf.radioomanager.shared.event.EventCompetitorCategory
import org.openardf.radioomanager.shared.event.EventCompetitorData
import org.openardf.radioomanager.shared.event.EventReadoutData
import org.openardf.radioomanager.shared.event.EventResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EventResultPlacementTest {
    @Test
    fun sortsReadoutsBeforeMissingReadoutsAndAssignsPlaces() {
        val missing = competitor("missing", readout = null)
        val slower = competitor("slow", result(points = 2, runTimeSeconds = 200))
        val faster = competitor("fast", result(points = 2, runTimeSeconds = 100))

        val sorted = EventResultPlacement.sortByPlace(listOf(missing, slower, faster))

        assertEquals(listOf("fast", "slow", "missing"), sorted.map { it.competitorCategory.competitor.id })
        assertEquals(1, sorted[0].readoutData!!.result.place)
        assertEquals(2, sorted[1].readoutData!!.result.place)
        assertNull(sorted[2].readoutData)
    }

    @Test
    fun assignsSamePlaceForEqualPointsAndRunTime() {
        val first = competitor("first", result(points = 2, runTimeSeconds = 100))
        val second = competitor("second", result(points = 2, runTimeSeconds = 100))
        val third = competitor("third", result(points = 1, runTimeSeconds = 90))

        val sorted = EventResultPlacement.sortByPlace(listOf(third, second, first))

        assertEquals(listOf(1, 1, 2), sorted.map { it.readoutData!!.result.place })
    }

    @Test
    fun groupsByCategoryBeforeAssigningPlaces() {
        val categoryAFirst = competitor("a1", result(points = 2, runTimeSeconds = 100), categoryId = "a")
        val categoryASecond = competitor("a2", result(points = 1, runTimeSeconds = 100), categoryId = "a")
        val categoryBFirst = competitor("b1", result(points = 1, runTimeSeconds = 100), categoryId = "b")

        val grouped = EventResultPlacement.groupByCategoryAndSortByPlace(
            listOf(categoryASecond, categoryBFirst, categoryAFirst)
        )

        assertEquals(listOf(1, 2), grouped["a"]!!.map { it.readoutData!!.result.place })
        assertEquals(listOf(1), grouped["b"]!!.map { it.readoutData!!.result.place })
    }

    private fun competitor(
        id: String,
        readout: EventResult?,
        categoryId: String? = "category"
    ): EventCompetitorData =
        EventCompetitorData(
            competitorCategory = EventCompetitorCategory(
                competitor = EventCompetitor(
                    id = id,
                    raceId = "race",
                    categoryId = categoryId,
                    firstName = id,
                    lastName = "Runner",
                    club = "",
                    index = "",
                    isMan = true,
                    birthYear = null,
                    siNumber = null,
                    siRent = false,
                    startNumber = 1,
                    drawnStartTimeSeconds = null
                ),
                category = categoryId?.let {
                    eventCategory(it)
                }
            ),
            readoutData = readout?.let {
                EventReadoutData(result = it, punches = emptyList())
            }
        )

    private fun result(points: Int, runTimeSeconds: Long): EventResult =
        EventResult(
            id = "result-$points-$runTimeSeconds",
            raceId = "race",
            competitorId = "competitor",
            siNumber = null,
            cardType = 5,
            checkTimeSeconds = null,
            startTimeSeconds = null,
            finishTimeSeconds = null,
            readoutDateTimeIso = "2026-05-30T10:00",
            automaticStatus = true,
            resultStatus = ResultStatus.OK,
            points = points,
            runTimeSeconds = runTimeSeconds,
            modified = false,
            sent = false
        )

    private fun eventCategory(id: String) =
        org.openardf.radioomanager.shared.event.EventCategory(
            id = id,
            raceId = "race",
            name = id,
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
        )
}
