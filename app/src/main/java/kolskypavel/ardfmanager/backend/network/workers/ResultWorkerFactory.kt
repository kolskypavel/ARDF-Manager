package kolskypavel.ardfmanager.backend.network.workers

import kolskypavel.ardfmanager.backend.room.enums.ProviderType

/** Selects the live-result worker implementation for a provider type. */
object ResultWorkerFactory {
    /** Returns the worker responsible for publishing to the requested provider. */
    fun getResultWorker(type: ProviderType): ResultServiceWorker {
        return when (type) {
            ProviderType.ROBIS, ProviderType.ROBIS_TEST -> RobisWorker
            ProviderType.ORESULTS -> OResultsWorker
            ProviderType.OFEED -> OFeedWorker
        }
    }
}
