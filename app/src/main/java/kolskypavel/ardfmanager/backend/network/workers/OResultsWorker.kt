package kolskypavel.ardfmanager.backend.network.workers

import android.content.Context
import android.util.Log
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.processors.IofXmlProcessor
import kolskypavel.ardfmanager.backend.network.NetworkConstants
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

/** Live-result worker for the OResults IOF XML upload API. */
object OResultsWorker : ResultServiceWorker {

    /** Uploads the start list before result uploads begin. */
    override suspend fun init(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor,
        context: Context
    ) {
        resultService.status = ResultServiceStatus.RUNNING
        try {
            val stream = ByteArrayOutputStream()
            val data = dataProcessor.getCategoryDataFlowForRace(race.id).first()
            IofXmlProcessor.exportStartList(stream, race, data, dataProcessor)

            val xml = stream.toString()
            if (sendFile(xml, resultService, httpClient, "/start-lists")) {
                resultService.init = true
            } else {
                resultService.status = ResultServiceStatus.ERROR
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception when init: ${e.message}")
        }
    }

    /** Uploads current categorized results as compressed IOF XML. */
    override suspend fun exportResults(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor,
        context: Context
    ) {

        val results =
            ResultsProcessor.getResultWrapperFlowByRace(resultService.raceId, dataProcessor)
                .first()
                .filter { it.category != null }

        val stream = ByteArrayOutputStream()
        IofXmlProcessor.exportResults(stream, race, results, dataProcessor)
        val xml = stream.toString()

        try {
            if (sendFile(xml, resultService, httpClient, "/results")) {
                resultService.status = ResultServiceStatus.RUNNING
                resultService.sentAt = LocalTime.now()
            } else {
                resultService.status = ResultServiceStatus.ERROR
            }

        } catch (exception: Exception) {
            // Convert request failures into provider status so the UI can show the error.
            resultService.status = ResultServiceStatus.ERROR
            resultService.errorText = exception.message ?: "Unknown error"
            Log.e(LOG_TAG, "Exception sending : ${exception.message}")
        }
    }

    /** Sends one gzipped IOF XML payload to the OResults endpoint path. */
    @Throws(Exception::class)
    fun sendFile(
        data: String,
        resultService: ResultService,
        httpClient: OkHttpClient,
        path: String
    ): Boolean {
        val compressed = gzipStringToByteArray(data)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(NetworkConstants.ORESULTS_API_HEADER, resultService.apiKey)
            .addFormDataPart("Content-Encoding", "application/gzip")
            .addFormDataPart(
                "file",
                null,
                compressed.toRequestBody(NetworkConstants.CONTENT_TYPE_GZIP)
            ).build()

        val request = Request.Builder()
            .url(NetworkConstants.ORESULTS_RESULTS_API_URL + path)
            .post(multipartBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return true
            } else {
                Log.e(
                    LOG_TAG,
                    "Error sending file, code ${response.code}, message ${response.message}"
                )
                return false
            }

        }
    }

    /** Compresses XML text as UTF-8 gzip bytes for provider upload. */
    fun gzipStringToByteArray(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(input.toByteArray(Charsets.UTF_8))
            gzip.finish()
        }
        return bos.toByteArray()
    }

    const val LOG_TAG = "SERVICE ORESULTS"
}
