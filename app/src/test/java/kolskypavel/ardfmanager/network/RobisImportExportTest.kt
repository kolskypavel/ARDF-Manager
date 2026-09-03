package kolskypavel.ardfmanager.network

import junit.framework.TestCase.assertTrue
import kolskypavel.ardfmanager.BuildConfig
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.network.ProviderClient
import kolskypavel.ardfmanager.backend.network.workers.RobisWorker
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.ProviderType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceBand
import kolskypavel.ardfmanager.backend.room.enums.RaceLevel
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import kolskypavel.ardfmanager.backend.sportident.SITime
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

// The test checks if results are exporting to ROBis normally
@RunWith(RobolectricTestRunner::class)
class RobisImportExportTest {
    val dataProcessor: DataProcessor = mock()

    @Before
    fun setup() {
        `when`(dataProcessor.resultStatusToShortString(ResultStatus.OK))
            .thenReturn("OK")
        `when`(dataProcessor.resultStatusToShortString(ResultStatus.DID_NOT_FINISH))
            .thenReturn("DNF")

        `when`(dataProcessor.punchStatusToShortString(org.mockito.kotlin.any()))
            .thenReturn("OK")

        // Import test properties
        `when`(dataProcessor.resultStatusShortStringToEnum("OK")).thenReturn(ResultStatus.OK)
        `when`(dataProcessor.punchStatusShortStringToEnum("OK")).thenReturn(PunchStatus.VALID)
        `when`(dataProcessor.punchStatusShortStringToEnum("DP")).thenReturn(PunchStatus.DUPLICATE)
    }

    /**
     * Generates mock race data for testing purposes.
     * Contains one category (M20) with three competitors and their punches.
     */
    fun getGeneratedMockRaceData(): RaceData {
        val raceId = UUID.randomUUID()
        val race = Race(
            raceId,
            "Mock Race",
            "",
            LocalDateTime.of(2023, 10, 1, 10, 0),
            RaceType.CLASSIC,
            RaceLevel.NATIONAL,
            RaceBand.M80,
            Duration.ofMinutes(120)
        )

        val categoryId = UUID.randomUUID()
        val category = Category(
            categoryId,
            raceId,
            "M20",
            true,
            null,
            5000,
            100,
            1,
            false,
            RaceType.CLASSIC,
            RaceBand.M80,
            Duration.ofMinutes(120),
            "1,2,3,4,5,B"
        )

        // Add ControlPoints to CategoryData
        val controlPoints = (1..5).map { i ->
            ControlPoint(UUID.randomUUID(), categoryId, i, ControlPointType.CONTROL, i)
        } + ControlPoint(UUID.randomUUID(), categoryId, 99, ControlPointType.BEACON, 6)

        val competitors = (1..3).map { i ->
            Competitor(
                UUID.randomUUID(),
                raceId,
                categoryId,
                "First$i",
                "Last$i",
                "Club$i",
                "AAA$i",
                true,
                2000,
                1000 + i,
                false,
                i,
                Duration.ofMinutes(i * 5L)
            )
        }

        val categoryData = CategoryData(category, controlPoints, competitors)

        val competitorDataList = competitors.mapIndexed { index, competitor ->
            val resultId = UUID.randomUUID()
            val drawnStart = competitor.drawnRelativeStartTime ?: Duration.ZERO
            val baseTime = LocalTime.of(10, 0)
            val startTime = SITime(baseTime.plus(drawnStart))

            // Last competitor is DNF with no punches and place 0
            val status = if (index == 2) ResultStatus.DID_NOT_FINISH else ResultStatus.OK
            val points = if (status == ResultStatus.OK) 6 else 0
            val runTime = when (index) {
                0 -> Duration.ofMinutes(50)
                1 -> Duration.ofMinutes(75)
                else -> Duration.ZERO
            }
            val place = when (index) {
                0 -> 1
                1 -> 2
                else -> 0
            }

            val finishTime = if (status == ResultStatus.OK) {
                SITime(startTime.getSeconds() + runTime.seconds)
            } else {
                startTime
            }

            val result = Result(
                resultId,
                raceId,
                competitor.id,
                competitor.siNumber,
                SIConstants.SI_CARD5,
                SITime(baseTime.minusMinutes(5)),
                startTime,
                finishTime,
                LocalDateTime.now(),
                true,
                status,
                points,
                runTime,
                false,
                false
            )
            result.place = place

            val punches = if (status == ResultStatus.OK) {
                val cpCodes = listOf(1, 2, 3, 4, 5, 99) // 99 for Beacon 'B'
                val numSegments = cpCodes.size + 1 // CPs + finish segment
                val splitSeconds = runTime.seconds / numSegments

                val cpPunches = cpCodes.mapIndexed { cpIdx, siCode ->
                    val order = cpIdx + 1
                    val punchTime = SITime(startTime.getSeconds() + (order * splitSeconds))
                    val punchType = if (siCode == 99) SIRecordType.BEACON else SIRecordType.CONTROL
                    val punch = Punch(
                        siCode,
                        punchTime,
                        punchType,
                        order
                    )
                    punch.raceId = raceId
                    punch.resultId = resultId
                    punch.split = Duration.ofSeconds(splitSeconds)
                    AliasPunch(punch)
                }

                // Add finish punch
                val finishPunch = Punch(
                    0,
                    finishTime,
                    SIRecordType.FINISH,
                    cpCodes.size + 1
                )
                finishPunch.raceId = raceId
                finishPunch.resultId = resultId
                // Ensure the last split covers the remainder so it adds up exactly to runTime
                finishPunch.split =
                    Duration.ofSeconds(runTime.seconds - (cpCodes.size * splitSeconds))

                cpPunches + AliasPunch(finishPunch)
            } else {
                emptyList()
            }

            val readoutData = ReadoutData(result, punches)
            CompetitorData(CompetitorCategory(competitor, category), readoutData)
        }

        return RaceData(
            race,
            listOf(categoryData),
            emptyList(),
            competitorDataList,
            emptyList()
        )
    }

    @Test
    fun testFinalResultsUpload() = runBlocking {
        val raceData = getGeneratedMockRaceData()

        `when`(dataProcessor.getRaceData(org.mockito.kotlin.any()))
            .thenReturn(raceData)

        val httpClient = OkHttpClient.Builder().build()
        val resultService = ResultService(raceData.race.id)
        resultService.serviceType = ProviderType.ROBIS_TEST
        resultService.apiKey = BuildConfig.ROBIS_TEST_EXPORT_API_KEY

        if (
            !RobisWorker.uploadFinalResults(
                resultService,
                raceData.race,
                httpClient,
                dataProcessor,
                mock()
            )
        ) {
            throw Exception("Upload failed: ${resultService.errorText}")
        }
    }

    @Test
    fun testDataImport() = runBlocking {
        val raceData = ProviderClient.fetchRaceData(
            BuildConfig.ROBIS_TEST_IMPORT_API_KEY,
            ProviderType.ROBIS_TEST,
            dataProcessor,
            mock()
        )

        // Expected values, can be adjusted to specific test data
        if (raceData.competitorData.isEmpty()) throw Exception("Import data has empty competitor data!")
        if (raceData.categories.isEmpty()) throw Exception("Import data has empty categories!")
    }
}
