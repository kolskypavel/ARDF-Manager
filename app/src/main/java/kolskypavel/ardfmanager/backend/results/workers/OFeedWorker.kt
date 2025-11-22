package kolskypavel.ardfmanager.backend.results.workers

import android.content.Context
import android.util.Base64
import android.util.Log
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.processors.IofXmlProcessor
import kolskypavel.ardfmanager.backend.results.ResultsConstants
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import kolskypavel.ardfmanager.backend.room.enums.ResultServiceStatus
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.time.LocalTime
import java.util.zip.GZIPOutputStream

object OFeedWorker : ResultServiceWorker {
    override suspend fun init(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor
    ) {
        try {
            val stream = ByteArrayOutputStream()
            val data = dataProcessor.getCategoryDataFlowForRace(race.id).first()
            IofXmlProcessor.exportStartList(stream, race, data, dataProcessor)

            val xml = stream.toString()
            if (!sendFile(xml, resultService, httpClient, dataProcessor.getContext())) {
                resultService.init = true
            }
        } catch (e: Exception) {
            Log.e(OResultsWorker.LOG_TAG, "Exception when init: ${e.message}")
            resultService.errorText =
                dataProcessor.getContext().getString(R.string.result_service_status_error)
        }
    }

    override suspend fun exportResults(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor
    ) {
        val results =
            ResultsProcessor.getResultWrapperFlowByRace(resultService.raceId, dataProcessor)
                .first()
                .filter { it.category != null }

        val stream = ByteArrayOutputStream()
        IofXmlProcessor.exportResults(stream, race, results, dataProcessor)
        val xml = stream.toString()

        try {
            sendFile(xml, resultService, httpClient, dataProcessor.getContext())

        } catch (exception: Exception) {
            // Handle exceptions during the request
            resultService.status = ResultServiceStatus.ERROR
            resultService.errorText = exception.message ?: "Unknown error"
            Log.e(OResultsWorker.LOG_TAG, "Exception sending : ${exception.message}")
        }
    }

    @Throws(Exception::class)
    fun sendFile(
        data: String,
        resultService: ResultService,
        httpClient: OkHttpClient,
        context: Context
    ): Boolean {

        // Compress data with gzip
        val compressed = gzipStringToByteArray(data)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(ResultsConstants.OFEED_EVENT_ID, resultService.url)
            .addFormDataPart(
                "Content-Encoding",
                "uploaded_file.xml",
                compressed.toRequestBody(ResultsConstants.CONTENT_TYPE_GZIP)
            )
            .build()

        val auth = Base64.encodeToString(
            "${resultService.url}:${resultService.apiKey}".toByteArray(),
            Base64.NO_WRAP
        )

        val request = Request.Builder()
            .header(ResultsConstants.OFEED_API_AUTH_HEADER, "Basic $auth")
            .url(ResultsConstants.OFEED_API_URL)
            .post(multipartBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                resultService.errorText = ""
                resultService.status = ResultServiceStatus.RUNNING
                resultService.sentAt = LocalTime.now()
                return true
            }
            // Unauthorized
            else if (response.code == 401) {
                resultService.status = ResultServiceStatus.UNAUTHORIZED
                resultService.errorText = context.getString(R.string.result_service_invalid_api_key)
                return false
            }
            // Validation error
            else if (response.code == 422) {
                resultService.status = ResultServiceStatus.ERROR
                resultService.errorText =
                    context.getString(R.string.result_service_validation_error)
                return false
            }
            // Server error
            else if (response.code == 500) {
                resultService.status = ResultServiceStatus.ERROR
                resultService.errorText =
                    context.getString(R.string.result_service_server_error)
                return false
            }

            // Other error
            else {
                Log.e(
                    OResultsWorker.LOG_TAG,
                    "Error sending file, code ${response.code}, message ${response.message}"
                )
                resultService.status = ResultServiceStatus.ERROR
                resultService.errorText =
                    context.getString(R.string.result_service_unknown_response)
                return false
            }

        }
    }


    fun gzipStringToByteArray(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(input.toByteArray(Charsets.UTF_8))
            gzip.finish()
        }
        return bos.toByteArray()
    }

    const val LOG_TAG = "SERVICE OFEED"
}