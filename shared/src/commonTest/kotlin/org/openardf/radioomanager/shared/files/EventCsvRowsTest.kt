package org.openardf.radioomanager.shared.files

import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.event.EventCategory
import org.openardf.radioomanager.shared.event.EventCompetitor
import kotlin.test.Test
import kotlin.test.assertEquals

class EventCsvRowsTest {
    @Test
    fun formatsCategoryRows() {
        val category = EventCategory(
            id = "category",
            raceId = "race",
            name = "M21",
            isMan = true,
            maxAge = null,
            lengthMeters = 5_000,
            climbMeters = 100,
            order = 1,
            differentProperties = true,
            raceType = RaceType.SPRINT,
            raceBand = null,
            timeLimitSeconds = 2_700,
            controlPointsString = "31 32"
        )

        assertEquals("M21;1;0;5000;100;1;2;45}", EventCsvRows.categoryRow(category))
    }

    @Test
    fun formatsCompetitorRows() {
        val competitor = EventCompetitor(
            id = "competitor",
            raceId = "race",
            categoryId = "category",
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

        assertEquals("123456;Pavel;Kolsky;M21;1;1980;;OK;;42;OK001", EventCsvRows.competitorRow(competitor, "M21"))
    }
}
