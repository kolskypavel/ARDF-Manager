package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.domain.RaceBand
import org.openardf.radioomanager.shared.domain.RaceLevel
import org.openardf.radioomanager.shared.domain.RaceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventProjectFileTest {
    @Test
    fun defaultsToCurrentSchemaAndAppName() {
        val projectFile = EventProjectFile(raceData = raceData())

        assertEquals(1, projectFile.schemaVersion)
        assertEquals("Radio-O-Manager", projectFile.appName)
        assertTrue(projectFile.isSupportedSchema())
    }

    @Test
    fun rejectsUnsupportedSchemaVersions() {
        assertFalse(EventProjectFileFormat.isSupportedSchema(0))
        assertFalse(EventProjectFileFormat.isSupportedSchema(EventProjectFileFormat.CURRENT_SCHEMA_VERSION + 1))
    }

    private fun raceData(): EventRaceData =
        EventRaceData(
            race = EventRace(
                id = "race",
                name = "Race",
                apiKey = "",
                startDateTimeIso = "2026-05-30T10:00",
                raceType = RaceType.CLASSIC,
                raceLevel = RaceLevel.PRACTICE,
                raceBand = RaceBand.M80,
                timeLimitSeconds = 7_200
            ),
            categories = emptyList(),
            aliases = emptyList(),
            competitorData = emptyList(),
            unmatchedReadoutData = emptyList()
        )
}
