package kolskypavel.ardfmanager.backend.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.network.workers.ResultWorkerFactory
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.ResultServiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Duration
import java.util.UUID

// Used for result service communication - distributes work
object ResultServiceProcessor {
    private val MIN_SERVICE_DELAY: Duration = Duration.ofSeconds(1)

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

                // Get the result service from db
                resultService = dataProcessor.getResultServiceByRaceId(raceId)
                if (resultService != null) {
                    val serviceDelay = getServiceDelay(resultService)

                    // Test connection before sending.
                    if (!isNetworkConnected(context)) {
                        resultService.status = ResultServiceStatus.NO_NETWORK
                        updateResultService(dataProcessor, resultService)
                        delay(serviceDelay)
                        continue
                    }
                    dataProcessor.getRace(raceId)?.let { race ->
                        val worker = ResultWorkerFactory.getResultWorker(resultService.serviceType)

                        // Init the service
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

                            // Init the service
                            if (!resultService.init) {
                                worker.init(
                                    resultService,
                                    race,
                                    httpClient,
                                    dataProcessor,
                                    context
                                )
                            }

                            // Redo the check to prevent additional waiting
                            if (resultService.init) {
                                // Main result sending
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
                    delay(MIN_SERVICE_DELAY)     // Failsafe - should never occur
                }

            }
        }
    }

    private fun getServiceDelay(resultService: ResultService): Duration {
        return if (resultService.interval.isZero || resultService.interval.isNegative) {
            MIN_SERVICE_DELAY
        } else {
            resultService.interval
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        // Require both that the network advertises internet and that the system validated it.
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }


    fun filterCompetitorDataBySent(
        results: List<CompetitorData>
    ): ArrayList<CompetitorData> {
        val filtered = ArrayList<CompetitorData>()
        for (cd in results) {
            if (cd.readoutData != null && !cd.readoutData!!.result.sent) {
                filtered.add(cd)
            }
        }
        return filtered
    }

    // Mark the results as sent
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

    private fun updateResultService(
        dataProcessor: DataProcessor,
        resultService: ResultService
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dataProcessor.createOrUpdateResultService(resultService)
        }
    }

}
