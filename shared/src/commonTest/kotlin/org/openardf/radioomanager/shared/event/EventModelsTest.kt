package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.domain.RaceBand
import org.openardf.radioomanager.shared.domain.RaceLevel
import org.openardf.radioomanager.shared.domain.RaceType
import kotlin.test.Test
import kotlin.test.assertEquals

class EventModelsTest {
    @Test
    fun categoryUsesRacePropertiesWhenNotDifferent() {
        val race = race()
        val category = category(
            differentProperties = false,
            raceType = RaceType.SPRINT,
            raceBand = RaceBand.M2,
            timeLimitSeconds = 60
        )

        assertEquals(RaceType.CLASSIC, category.effectiveRaceType(race))
        assertEquals(RaceBand.M80, category.effectiveRaceBand(race))
        assertEquals(7_200, category.effectiveTimeLimitSeconds(race))
    }

    @Test
    fun categoryUsesOverridesWhenDifferent() {
        val race = race()
        val category = category(
            differentProperties = true,
            raceType = RaceType.SPRINT,
            raceBand = RaceBand.M2,
            timeLimitSeconds = 3_600
        )

        assertEquals(RaceType.SPRINT, category.effectiveRaceType(race))
        assertEquals(RaceBand.M2, category.effectiveRaceBand(race))
        assertEquals(3_600, category.effectiveTimeLimitSeconds(race))
    }

    @Test
    fun competitorFormatsNames() {
        val competitor = EventCompetitor(
            id = "competitor-1",
            raceId = "race-1",
            categoryId = "category-1",
            firstName = "Pavel",
            lastName = "Kolsky",
            club = "OK",
            index = "OK001",
            isMan = true,
            birthYear = 1980,
            siNumber = 123456,
            siRent = false,
            startNumber = 42,
            drawnStartTimeSeconds = null
        )

        assertEquals("KOLSKY Pavel", competitor.fullName())
        assertEquals("KOLSKY Pavel (42)", competitor.nameWithStartNumber())
    }

    private fun race(): EventRace =
        EventRace(
            id = "race-1",
            name = "Test",
            apiKey = "",
            startDateTimeIso = "2026-05-30T10:00",
            raceType = RaceType.CLASSIC,
            raceLevel = RaceLevel.PRACTICE,
            raceBand = RaceBand.M80,
            timeLimitSeconds = 7_200
        )

    private fun category(
        differentProperties: Boolean,
        raceType: RaceType?,
        raceBand: RaceBand?,
        timeLimitSeconds: Long?
    ): EventCategory =
        EventCategory(
            id = "category-1",
            raceId = "race-1",
            name = "M21",
            isMan = true,
            maxAge = null,
            lengthMeters = 5_000,
            climbMeters = 100,
            order = 1,
            differentProperties = differentProperties,
            raceType = raceType,
            raceBand = raceBand,
            timeLimitSeconds = timeLimitSeconds,
            controlPointsString = "31 32"
        )
}
