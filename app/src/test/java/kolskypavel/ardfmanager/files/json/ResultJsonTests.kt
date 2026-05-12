package kolskypavel.ardfmanager.files.json

import ResultJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import junit.framework.TestCase.assertEquals
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.json.adapters.FinalResultJsonAdapter
import kolskypavel.ardfmanager.backend.files.json.adapters.LocalDateTimeAdapter
import kolskypavel.ardfmanager.backend.files.json.adapters.RaceDataJsonAdapter
import kolskypavel.ardfmanager.backend.files.json.temps.FinalResultsJson
import kolskypavel.ardfmanager.backend.files.json.temps.ResultJson
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
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
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalTime
import java.time.Duration
import java.time.LocalDateTime

class ResultJsonTests {
    val dataProcessor = mock(DataProcessor::class.java)

    @Before
    fun setup() {
        `when`(dataProcessor.resultStatusToShortString(org.mockito.kotlin.any()))
            .thenReturn("OK")

        `when`(dataProcessor.punchStatusToShortString(org.mockito.kotlin.any()))
            .thenReturn("OK")
    }

    @Test
    fun testLiveResultsToJson() {
        val race = Race()
        race.startDateTime = LocalDateTime.of(2025, 11, 23, 13, 0, 0)

        val result = Result()
        result.checkTime = SITime(LocalTime.of(12, 49, 3))
        result.startTime = SITime(LocalTime.of(13, 0, 0))
        result.finishTime = SITime(LocalTime.of(14, 15, 0))
        result.resultStatus = ResultStatus.OK
        result.runTime = Duration.ofMinutes(75)
        result.readoutTime = LocalDateTime.of(2025, 11, 23, 14, 18, 24)

        val punches = arrayListOf(
            Punch(13, SITime(LocalTime.of(13, 0, 0)), SIRecordType.START, 1),
            Punch(31, SITime(LocalTime.of(13, 35, 0)), SIRecordType.CONTROL, 1),
            Punch(32, SITime(LocalTime.of(13, 43, 11)), SIRecordType.CONTROL, 1),
            Punch(33, SITime(LocalTime.of(14, 5, 50)), SIRecordType.CONTROL, 1),
            Punch(34, SITime(LocalTime.of(14, 10, 22)), SIRecordType.CONTROL, 1),
        )
        ResultsProcessor.calculateSplits(punches)

        val ap = punches.mapIndexed { index, punch ->
            AliasPunch(
                punch,
                Alias(punch.siCode, index.toString())
            )
        }
        val readoutData = ReadoutData(result, ap)

        val compData = CompetitorData(
            CompetitorCategory(Competitor(), Category("A")),
            readoutData
        )
        val json = ResultJsonAdapter(race, dataProcessor).toJson(compData)

        val moshi: Moshi = Moshi.Builder()
            .add(RaceDataJsonAdapter(dataProcessor))
            .add(LocalDateTimeAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        val out = moshi.adapter(ResultJson::class.java).toJson(json)

        val stream =
            this::class.java.classLoader.getResourceAsStream("json/json_results_filtered_start.ardfjs")
        val valid = stream.bufferedReader().use { it.readText() }.filterNot { it.isWhitespace() }

        assertEquals(valid, out)
    }

    @Test
    fun testFinalResultsToJson() {
        val race = Race()
        race.startDateTime = LocalDateTime.of(2025, 11, 23, 13, 0, 0)

        val result = Result()
        result.checkTime = SITime(LocalTime.of(12, 49, 3))
        result.startTime = SITime(LocalTime.of(13, 0, 0))
        result.finishTime = SITime(LocalTime.of(14, 15, 0))
        result.resultStatus = ResultStatus.OK
        result.runTime = Duration.ofMinutes(75)
        result.readoutTime = LocalDateTime.of(2025, 11, 23, 14, 18, 24)

        val punches = arrayListOf(
            Punch(13, SITime(LocalTime.of(13, 0, 0)), SIRecordType.START, 1),
            Punch(31, SITime(LocalTime.of(13, 35, 0)), SIRecordType.CONTROL, 1),
            Punch(32, SITime(LocalTime.of(13, 43, 11)), SIRecordType.CONTROL, 1),
            Punch(33, SITime(LocalTime.of(14, 5, 50)), SIRecordType.CONTROL, 1),
            Punch(34, SITime(LocalTime.of(14, 10, 22)), SIRecordType.CONTROL, 1),
        )
        ResultsProcessor.calculateSplits(punches)

        val ap = punches.mapIndexed { index, punch ->
            AliasPunch(
                punch,
                Alias(punch.siCode, index.toString())
            )
        }
        val category = Category("A")
        val competitor = Competitor()
        val readoutData = ReadoutData(result, ap)

        val compData = listOf(
            CompetitorData(
                CompetitorCategory(Competitor(), category),
                readoutData
            )
        )
        val controlPoints = listOf(
            ControlPoint(31),
            ControlPoint(32),
            ControlPoint(33),
            ControlPoint(34)
        )

        val aliases = listOf(
            Alias(31, "1"),
            Alias(32, "2"),
            Alias(33, "3"),
            Alias(34, "4")
        )
        val catData = listOf(CategoryData(category, controlPoints, listOf(competitor)))

        val rd = RaceData(race, catData, aliases, compData, emptyList())
        val json = FinalResultJsonAdapter(dataProcessor).toJson(rd)

        val moshi: Moshi = Moshi.Builder()
            .add(RaceDataJsonAdapter(dataProcessor))
            .add(LocalDateTimeAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        val out = moshi.adapter(FinalResultsJson::class.java).toJson(json)

        val stream =
            this::class.java.classLoader.getResourceAsStream("json/json_final_results.ardfjs")
        val valid = stream.bufferedReader().use { it.readText() }.filterNot { it.isWhitespace() }

        assertEquals(valid, out)
    }
}