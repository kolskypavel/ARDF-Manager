
package kolskypavel.ardfmanager.backend.network.workers

import android.content.Context
import android.util.Log
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.json.temps.RobisResultJson
import kolskypavel.ardfmanager.backend.files.processors.JsonProcessor

import kolskypavel.ardfmanager.backend.network.ResultServiceProcessor
import kolskypavel.ardfmanager.backend.network.ResultServiceProcessor.updateSentResults
import kolskypavel.ardfmanager.backend.network.NetworkConstants
import kolskypavel.ardfmanager.backend.network.NetworkConstants.CONTENT_TYPE_JSON
import kolskypavel.ardfmanager.backend.network.NetworkConstants.ROBIS_API_HEADER
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.ResultServiceStatus
import kolskypavel.ardfmanager.backend.room.enums.ProviderType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.time.LocalTime

/** Live-result worker for the ROBIS JSON result API. */
object RobisWorker : ResultServiceWorker {

    /** Marks ROBIS as initialized because it does not require a separate startup request. */
    override suspend fun init(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor,
        context: Context
    ) {
        resultService.init = true
    }

    /** Exports unsent live results to ROBIS and updates local sent/error state. */
    override suspend fun exportResults(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor,
        context: Context
    ) {
        Log.i(LOG_TAG, "Starting to export results")

        // Only unsent results should be counted as accepted after a successful ROBIS response.
        val filteredResults = ResultServiceProcessor.filterCompetitorDataBySent(
            ResultsProcessor.getCompetitorDataByRace(
                resultService.raceId,
                dataProcessor
            )
        )

        if (filteredResults.isEmpty()) {
            Log.i(LOG_TAG, "  nothing to send, exiting")
            return
        }

        val outStream = ByteArrayOutputStream()
        JsonProcessor.exportLiveResults(outStream, race, dataProcessor)
        val resultString = outStream.toString("UTF-8")
        Log.i(LOG_TAG, "Export JSON payload:\n$resultString")
        val body: RequestBody = resultString.toRequestBody(CONTENT_TYPE_JSON)

        val request: Request = Request.Builder()
            .url(
                if (resultService.serviceType == ProviderType.ROBIS_TEST) {
                    NetworkConstants.ROBIS_PLAYGROUND_RESULTS_API_URL
               } else {
                    NetworkConstants.ROBIS_RESULTS_API_URL
                }
            )
            .addHeader(ROBIS_API_HEADER, resultService.apiKey)
            .put(body)
            .build()


        try {
            // TODO: Replace verbose payload logging with a structured debug-level policy.
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body.string()

                Log.i(LOG_TAG, "ROBIS response code=${response.code}, body=$bodyString")

                when (response.code) {
                    in 200..299 -> {

                        filterInvalidResults(
                            filteredResults,
                            bodyString,
                            resultService,
                            dataProcessor.getContext()
                        )
                        updateSentResults(dataProcessor, filteredResults)
                        resultService.status = ResultServiceStatus.RUNNING
                        resultService.sentAt = LocalTime.now()
                        resultService.sent += filteredResults.size
                    }

                    401 -> {
                        resultService.status = ResultServiceStatus.UNAUTHORIZED
                        resultService.errorText = dataProcessor.getContext()
                            ?.getString(R.string.result_service_invalid_api_key) ?: "Error"

                        Log.e(
                            LOG_TAG,
                            "Error ${response.code} sending results to ROBis: ${response.message}"
                        )
                    }

                    else -> {
                        resultService.status = ResultServiceStatus.ERROR
                        resultService.errorText = bodyString
                        Log.e(
                            LOG_TAG,
                            "Error ${response.code} sending results to ROBis: ${response.message}"
                        )
                    }
                }
            }
        } catch (exception: Exception) {
            resultService.status = ResultServiceStatus.ERROR
            resultService.errorText = exception.message ?: "Unknown error"
            Log.e(LOG_TAG, "Exception sending results to ROBis: ${exception.message}")
        }
    }

    /** Removes ROBIS-rejected results from the accepted set and builds a localized error summary. */
    private fun filterInvalidResults(
        results: ArrayList<CompetitorData>,
        robisResponse: String,
        resultService: ResultService,
        context: Context?
    ) {
        val response = JsonProcessor.parseRobisResponse(robisResponse)

        if (response != null) {

            var invalidString = ""
            for (invalid in response.invalid_data) {
                findAndRemoveMatchingResult(results, invalid)
                val fullName = "${invalid.last_name.uppercase()} ${invalid.first_name}"
                invalidString += context?.getString(
                    R.string.result_service_invalid_result,
                    fullName,
                    invalid.si_number,
                    invalid.competitor_index,
                    invalid.reason
                )
                invalidString += "\n"
            }
            resultService.errorText = invalidString
        }
    }

    /**
     * Finds the invalid result and removes it from the array list.
     *
     * Matching tries competitor index first, then SI number, then first and last name.
     */
    fun findAndRemoveMatchingResult(
        results: ArrayList<CompetitorData>,
        response: RobisResultJson
    ) {
        val found = results.find {
            it.competitorCategory.competitor.index == response.competitor_index
        } ?: results.find {
            it.readoutData?.result?.siNumber == response.si_number
        } ?: results.find {
            it.competitorCategory.competitor.firstName == response.first_name &&
                    it.competitorCategory.competitor.lastName == response.last_name
        }
        results.remove(found)
    }

    const val LOG_TAG = "SERVICE ROBIS"
}
