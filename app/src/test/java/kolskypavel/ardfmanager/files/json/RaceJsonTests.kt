package kolskypavel.ardfmanager.files.json

import com.squareup.moshi.JsonDataException
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.processors.JsonProcessor
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceBand
import kolskypavel.ardfmanager.backend.room.enums.RaceLevel
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.Duration

class RaceJsonTests {
    val dataProcessor: DataProcessor = mock()
    val race = Race()

    @Before
    fun setup() {
        `when`(dataProcessor.resultStatusShortStringToEnum("MP")).thenReturn(ResultStatus.MISPUNCHED)
        `when`(
            dataProcessor
                .shortStringToPunchStatus(org.mockito.kotlin.any())
        )
            .thenReturn(PunchStatus.VALID)

        `when`(dataProcessor.resultStatusShortStringToEnum("MP")).thenReturn(ResultStatus.MISPUNCHED)

        // Prepare startlist
        val compData = ArrayList<CompetitorData>()

        for (i in 1..5) {
            val competitor = Competitor()
            competitor.drawnRelativeStartTime = Duration.ofMinutes(5L * i)

            compData.add(
                CompetitorData(CompetitorCategory(competitor, Category("A")), null)
            )
        }
        `when`(dataProcessor.getCompetitorDataFlowByRace(race.id)).thenReturn(flowOf(compData))
    }

    @Test
    fun testValidFromJson() {

        val stream =
            this::class.java.classLoader.getResourceAsStream("json/json_valid_race_import.ardfjs")
        val raceData = JsonProcessor.importRaceData(stream, dataProcessor)

        assertEquals("EXAMPLE", raceData.race.name)
        assertEquals(LocalDateTime.of(2025, 11, 28, 13, 0, 0), raceData.race.startDateTime)
        assertEquals(RaceType.CLASSIC, raceData.race.raceType)
        assertEquals(RaceBand.M80, raceData.race.raceBand)
        assertEquals(RaceLevel.DISTRICT, raceData.race.raceLevel)

        val categories = raceData.categories.map { it.category.name }.sorted()
        assertEquals(listOf("D19", "D20", "M19", "M20", "Ostatní", "RT"), categories)

        val competitors = raceData.competitorData.map { it.competitorCategory.competitor }
        val startNumbers = competitors.map { it.startNumber }.sorted()
        assertEquals(listOf(40, 41, 42, 43, 44, 45, 46), startNumbers)

        val comp1 =
            raceData.competitorData.find { it.competitorCategory.competitor.siNumber == 10000 }
        assertEquals("KOLSKÝ Pavel", comp1?.competitorCategory?.competitor?.getFullName())
        assertEquals(ResultStatus.MISPUNCHED, comp1?.readoutData?.result?.resultStatus)

    }

    // Should throw exception, since the required start time is missing
    @Test
    fun testInvalidFromJson() {
        val stream =
            this::class.java.classLoader.getResourceAsStream("json/json_invalid_race_import.ardfjs")
        assertThrows(JsonDataException::class.java) {
            JsonProcessor.importRaceData(
                stream,
                dataProcessor
            )
        }
    }

    @Test
    fun testStartlistToJson() = runTest {

        val stream = ByteArrayOutputStream()
        JsonProcessor.exportStartlist(stream, race, dataProcessor)

        assertEquals("", stream.toString())
    }
}