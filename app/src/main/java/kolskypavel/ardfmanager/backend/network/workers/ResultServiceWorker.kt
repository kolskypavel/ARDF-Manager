package kolskypavel.ardfmanager.backend.network.workers

import android.content.Context
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import okhttp3.OkHttpClient

/** Worker contract implemented by each live-result service provider. */
interface ResultServiceWorker {

    /** Performs optional startup work, such as start-list upload. */
    suspend fun init(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor,
        context: Context
    )

    /**
     * Exports results with the provided HTTP client.
     *
     * Implementations fetch and update local results as needed and update the service status.
     */
    suspend fun exportResults(
        resultService: ResultService,
        race: Race,
        httpClient: OkHttpClient,
        dataProcessor: DataProcessor,
        context: Context
    )
}
