package kolskypavel.ardfmanager.backend.network

import android.content.Context
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.processors.JsonProcessor
import kolskypavel.ardfmanager.backend.network.NetworkConstants.ROBIS_API_HEADER
import kolskypavel.ardfmanager.backend.network.NetworkConstants.ROBIS_PLAYGROUND_RACE_API_URL
import kolskypavel.ardfmanager.backend.network.NetworkConstants.ROBIS_RACE_API_URL
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.enums.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// Used for communicating with online data providers
object ProviderClient {

    // TODO: if more providers are supported in future, modify
    suspend fun fetchRaceData(
        apiKey: String,
        providerType: ProviderType,
        dataProcessor: DataProcessor,
        context: Context
    ): RaceData {

        val httpClient = OkHttpClient.Builder().build()
        val url = if (providerType == ProviderType.ROBIS)
            ROBIS_RACE_API_URL
        else ROBIS_PLAYGROUND_RACE_API_URL

        // Send the results to the ROBIS API
        val request: Request = Request.Builder()
            .url(url)
            .addHeader(ROBIS_API_HEADER, apiKey)
            .build()


        // Execute the blocking network call and parsing on Dispatchers.IO
        val raceData = withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body.string()

                when (response.code) {
                    in 200..299 -> {
                        JsonProcessor.importRaceData(bodyString, dataProcessor)
                    }

                    403 -> {
                        throw IllegalArgumentException(context.getString(R.string.result_service_invalid_api_key))
                    }

                    else -> {
                        throw Exception("${context.getString(R.string.general_unknown_error)} ${response.code}")
                    }
                }
            }
        }

        return raceData
    }

    const val LOG_TAG = "ROBIS_CLIENT"
}
