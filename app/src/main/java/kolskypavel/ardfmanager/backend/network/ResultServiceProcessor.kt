package kolskypavel.ardfmanager.backend.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.network.workers.ResultWorkerFactory
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.ResultServiceStatus
import kolskypavel.ardfmanager.backend.shared.toEventCompetitorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.openardf.radioomanager.shared.results.EventResultSending
import java.time.Duration
import java.util.UUID

/** Coordinates background publishing to configured live-result services. */
object ResultServiceProcessor {
    private val MIN_SERVICE_DELAY: Duration = Duration.ofSeconds(1)

    /** Starts the long-running result-service loop for one race. */
    fun resultServiceJob(
        raceId: UUID,
        dataProcessor: DataProcessor,
        context: Context
    ): Job {
        return CoroutineScope(Dispatchers.IO).launch {

            val inter = HttpLoggingInterceptor()
            inter.setLevel(HttpLoggingInterceptor.Level.BODY)
            val httpClient = OkHttpClient.Builder().addInterceptor(inter).build()
            var resultService: ResultService?

            while (true) {

                // Reload service settings each cycle so UI changes take effect without restarting the job.
                resultService = dataProcessor.getResultServiceByRaceId(raceId)
                if (resultService != null) {
                    val serviceDelay = getServiceDelay(resultService)

                    // Avoid marking provider failures when the device has no validated network.
                    if (!isNetworkConnected(context)) {
                        resultService.status = ResultServiceStatus.NO_NETWORK
                        updateResultService(dataProcessor, resultService)
                        delay(serviceDelay)
                        continue
                    }
                    dataProcessor.getRace(raceId)?.let { race ->
                        val worker = ResultWorkerFactory.getResultWorker(resultService.serviceType)

                        // Some providers need a one-time start-list upload before result uploads.
                        if (!resultService.init) {
                            worker.init(
                                resultService,
                                race,
                                httpClient,
                                dataProcessor,
                                context
                            )
                        }
                        dataProcessor.getRace(raceId)?.let { race ->
                            val worker =
                                ResultWorkerFactory.getResultWorker(resultService.serviceType)

                            // Some providers need a one-time start-list upload before result uploads.
                            if (!resultService.init) {
                                worker.init(
                                    resultService,
                                    race,
                                    httpClient,
                                    dataProcessor,
                                    context
                                )
                            }

                            // Send immediately after a successful init instead of waiting for another cycle.
                            if (resultService.init) {
                                worker.exportResults(
                                    resultService,
                                    race,
                                    httpClient,
                                    dataProcessor,
                                    context
                                )
                            }
                        }
                        updateResultService(dataProcessor, resultService)
                        delay(resultService.interval)
                    }
                    updateResultService(dataProcessor, resultService)
                    delay(serviceDelay)
                } else {
                    delay(MIN_SERVICE_DELAY) // Failsafe for races without a result-service row.
                }

            }
        }
    }

    /** Enforces a minimum loop delay so invalid intervals cannot spin the worker. */
    private fun getServiceDelay(resultService: ResultService): Duration {
        return if (resultService.interval.isZero || resultService.interval.isNegative) {
            MIN_SERVICE_DELAY
        } else {
            resultService.interval
        }
    }

    /** Returns true only when Android reports a validated internet connection. */
    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        // Require both that the network advertises internet and that the system validated it.
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }


    /** Keeps only competitor results that have not already been marked as sent. */
    fun filterCompetitorDataBySent(
        results: List<CompetitorData>
    ): ArrayList<CompetitorData> {
        val unsentCompetitorIds = EventResultSending.unsentCompetitorIds(
            results.map { it.toEventCompetitorData() }
        )
        return results
            .filter { competitorData ->
                unsentCompetitorIds.contains(competitorData.competitorCategory.competitor.id.toString())
            }
            .toCollection(ArrayList())
    }

    /** Marks successfully accepted results as sent in the local database. */
    fun updateSentResults(
        dataProcessor: DataProcessor,
        resultData: List<CompetitorData>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            for (r in resultData) {
                val result = r.readoutData?.result
                if (result != null) {
                    result.sent = true
                    dataProcessor.createOrUpdateResult(result)
                }
            }
        }
    }

    /** Persists the latest result-service status asynchronously. */
    private fun updateResultService(
        dataProcessor: DataProcessor,
        resultService: ResultService
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createOrUpdateResultService(resultService)
        }
    }

}
