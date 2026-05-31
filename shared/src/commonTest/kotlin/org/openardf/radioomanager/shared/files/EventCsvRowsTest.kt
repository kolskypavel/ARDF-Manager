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

    @Test
    fun formatsCompetitorStartRows() {
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
            drawnStartTimeSeconds = 600
        )

        assertEquals(
            "42;Kolsky;Pavel;M21;;10:10;OK001;;OK;123456",
            EventCsvRows.competitorStartRow(competitor, "M21", "10:10")
        )
        assertEquals(
            "42;Kolsky;Pavel;M21;;;OK001;;OK;123456",
            EventCsvRows.competitorStartRow(competitor, "M21", null)
        )
    }

    @Test
    fun formatsPunchRows() {
        assertEquals("123456;31;10:15:00", EventCsvRows.punchRow(123456, 31, "10:15:00"))
        assertEquals(";31;10:15:00", EventCsvRows.punchRow(null, 31, "10:15:00"))
    }

    @Test
    fun formatsReadoutRows() {
        val controlPunches = listOf(
            TimedPunchCsvField(siCode = 31, timeText = "10:15:00"),
            TimedPunchCsvField(siCode = 32, timeText = "10:20:00")
        )

        assertEquals(
            "123456;09:30:00;10:00:00;10:45:00;2;31;10:15:00;32;10:20:00",
            EventCsvRows.readoutRow(
                siNumber = 123456,
                checkTimeText = "09:30:00",
                startTimeText = "10:00:00",
                finishTimeText = "10:45:00",
                controlPunches = controlPunches
            )
        )
    }

    @Test
    fun formatsReadoutRowsWithoutPunches() {
        assertEquals(
            ";09:30:00;;;0",
            EventCsvRows.readoutRow(
                siNumber = null,
                checkTimeText = "09:30:00",
                startTimeText = null,
                finishTimeText = null,
                controlPunches = emptyList()
            )
        )
    }
}
