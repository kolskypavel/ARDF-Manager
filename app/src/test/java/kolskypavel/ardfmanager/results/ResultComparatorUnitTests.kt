package kolskypavel.ardfmanager.results

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import org.junit.Test
import java.time.Duration
import java.util.UUID

class ResultComparatorUnitTests {
    @Test
    fun sortByPlaceDelegatesPlacementWithMissingReadoutsLast() {
        val missing = competitorData("missing", result = null)
        val slower = competitorData("slow", result = result("slow", points = 2, runTime = Duration.ofMinutes(20)))
        val faster = competitorData("fast", result = result("fast", points = 2, runTime = Duration.ofMinutes(10)))

        val sorted = ResultsProcessor.run {
            listOf(missing, slower, faster).sortByPlace()
        }

        assertEquals(listOf("fast", "slow", "missing"), sorted.map { it.competitorCategory.competitor.firstName })
        assertEquals(1, sorted[0].readoutData!!.result.place)
        assertEquals(2, sorted[1].readoutData!!.result.place)
        assertNull(sorted[2].readoutData)
    }

    private fun competitorData(name: String, result: Result?): CompetitorData {
        val competitor = Competitor(
            id = uuid(name),
            raceId = uuid("race"),
            categoryId = null,
            firstName = name,
            lastName = "Runner",
            club = "",
            index = "",
            isMan = true,
            birthYear = null,
            siNumber = null,
            siRent = false,
            startNumber = 1,
            drawnRelativeStartTime = null
        )
        return CompetitorData(
            competitorCategory = CompetitorCategory(competitor, category = null),
            readoutData = result?.let { ReadoutData(it, emptyList()) }
        )
    }

    private fun result(seed: String, points: Int, runTime: Duration): Result =
        Result(
            id = uuid("result-$seed"),
            raceId = uuid("race"),
            competitorId = uuid(seed),
            siNumber = null,
            cardType = SIConstants.SI_CARD5,
            checkTime = null,
            startTime = null,
            finishTime = null,
            automaticStatus = true,
            resultStatus = ResultStatus.OK,
            points = points,
            runTime = runTime,
            modified = false,
            sent = false
        )

    private fun uuid(seed: String): UUID =
        UUID.nameUUIDFromBytes(seed.toByteArray())
}
