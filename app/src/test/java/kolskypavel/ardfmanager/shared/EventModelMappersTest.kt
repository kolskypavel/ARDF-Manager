package kolskypavel.ardfmanager.shared

import junit.framework.TestCase.assertEquals
import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceBand
import kolskypavel.ardfmanager.backend.room.enums.RaceLevel
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.shared.toEventRaceData
import kolskypavel.ardfmanager.backend.shared.toRoomRaceData
import kolskypavel.ardfmanager.backend.sportident.SITime
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class EventModelMappersTest {
    @Test
    fun mapsRoomRaceDataToSharedEventRaceData() {
        val raceId = uuid("00000000-0000-0000-0000-000000000001")
        val categoryId = uuid("00000000-0000-0000-0000-000000000002")
        val competitorId = uuid("00000000-0000-0000-0000-000000000003")
        val resultId = uuid("00000000-0000-0000-0000-000000000004")
        val punchId = uuid("00000000-0000-0000-0000-000000000005")

        val race = Race(
            id = raceId,
            name = "Test Race",
            apiKey = "api-key",
            startDateTime = LocalDateTime.of(2026, 5, 30, 10, 0),
            raceType = RaceType.CLASSIC,
            raceLevel = RaceLevel.PRACTICE,
            raceBand = RaceBand.M80,
            timeLimit = Duration.ofHours(2)
        )
        val category = Category(
            id = categoryId,
            raceId = raceId,
            name = "M21",
            isMan = true,
            maxAge = null,
            length = 5_000,
            climb = 100,
            order = 1,
            differentProperties = true,
            raceType = RaceType.SPRINT,
            categoryBand = RaceBand.M2,
            timeLimit = Duration.ofMinutes(45),
            controlPointsString = "31 32"
        )
        val controlPoint = ControlPoint(
            id = uuid("00000000-0000-0000-0000-000000000006"),
            categoryId = categoryId,
            siCode = 31,
            type = ControlPointType.CONTROL,
            order = 1
        )
        val competitor = Competitor(
            id = competitorId,
            raceId = raceId,
            categoryId = categoryId,
            firstName = "Pavel",
            lastName = "Kolsky",
            club = "OK",
            index = "OK001",
            isMan = true,
            birthYear = 1980,
            siNumber = 123456,
            siRent = false,
            startNumber = 42,
            drawnRelativeStartTime = Duration.ofMinutes(10)
        )
        val alias = Alias(
            id = uuid("00000000-0000-0000-0000-000000000007"),
            raceId = raceId,
            siCode = 31,
            name = "F1"
        )
        val result = Result(
            id = resultId,
            raceId = raceId,
            competitorId = competitorId,
            siNumber = 123456,
            cardType = 5,
            checkTime = SITime(LocalTime.of(9, 30)),
            startTime = SITime(LocalTime.of(10, 0)),
            finishTime = SITime(LocalTime.of(10, 45)),
            readoutTime = LocalDateTime.of(2026, 5, 30, 10, 46),
            automaticStatus = true,
            resultStatus = ResultStatus.OK,
            points = 2,
            runTime = Duration.ofMinutes(45),
            modified = false,
            sent = true
        )
        result.place = 1
        val punch = Punch(
            id = punchId,
            raceId = raceId,
            resultId = resultId,
            cardNumber = 123456,
            siCode = 31,
            siTime = SITime(LocalTime.of(10, 15)),
            origSiTime = SITime(LocalTime.of(10, 15)),
            punchType = SIRecordType.CONTROL,
            order = 1,
            punchStatus = PunchStatus.VALID,
            split = Duration.ofMinutes(15)
        )

        val raceData = RaceData(
            race = race,
            categories = listOf(CategoryData(category, listOf(controlPoint), listOf(competitor))),
            aliases = listOf(alias),
            competitorData = listOf(
                CompetitorData(
                    CompetitorCategory(competitor, category),
                    ReadoutData(result, listOf(AliasPunch(punch, alias)))
                )
            ),
            unmatchedReadoutData = emptyList()
        )

        val shared = raceData.toEventRaceData()

        assertEquals(raceId.toString(), shared.race.id)
        assertEquals("2026-05-30T10:00", shared.race.startDateTimeIso)
        assertEquals(7_200L, shared.race.timeLimitSeconds)
        assertEquals(2_700L, shared.categories.single().category.timeLimitSeconds)
        assertEquals(600L, shared.competitorData.single().competitorCategory.competitor.drawnStartTimeSeconds)
        assertEquals(900L, shared.competitorData.single().readoutData!!.punches.single().punch.splitSeconds)
        assertEquals("F1", shared.competitorData.single().readoutData!!.punches.single().alias!!.name)
        assertEquals(1, shared.competitorData.single().readoutData!!.result.place)

        val room = shared.toRoomRaceData()
        assertEquals(raceId, room.race.id)
        assertEquals(Duration.ofHours(2), room.race.timeLimit)
        assertEquals(Duration.ofMinutes(45), room.categories.single().category.timeLimit)
        assertEquals(Duration.ofMinutes(10), room.competitorData.single().competitorCategory.competitor.drawnRelativeStartTime)
        assertEquals(Duration.ofMinutes(15), room.competitorData.single().readoutData!!.punches.single().punch.split)
        assertEquals(1, room.competitorData.single().readoutData!!.result.place)
    }

    @Test
    fun roomCompetitorNameHelpersUseSharedFormatting() {
        val competitor = Competitor(
            id = uuid("00000000-0000-0000-0000-000000000011"),
            raceId = uuid("00000000-0000-0000-0000-000000000001"),
            categoryId = null,
            firstName = "Pavel",
            lastName = "Kolsky",
            club = "OK",
            index = "OK001",
            isMan = true,
            birthYear = null,
            siNumber = null,
            siRent = false,
            startNumber = 42,
            drawnRelativeStartTime = null
        )

        assertEquals("KOLSKY Pavel", competitor.getFullName())
        assertEquals("KOLSKY Pavel (42)", competitor.getNameWithStartNumber())
    }

    private fun uuid(value: String): UUID = UUID.fromString(value)
}
