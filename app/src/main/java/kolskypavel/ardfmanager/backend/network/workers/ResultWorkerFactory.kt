package kolskypavel.ardfmanager.backend.network.workers

import kolskypavel.ardfmanager.backend.room.enums.ProviderType

object ResultWorkerFactory {
    fun getResultWorker(type: ProviderType): ResultServiceWorker {
        return when (type) {
            ProviderType.ROBIS, ProviderType.ROBIS_TEST -> RobisWorker
            ProviderType.ORESULTS -> OResultsWorker
            ProviderType.OFEED -> OFeedWorker
        }
    }
}