package kolskypavel.ardfmanager.backend.sportident

/**
 * Current status of the SI reader with the necessary information
 *
 */
data class SIReaderState(
    var status: SIReaderStatus,
    var stationId: Int? = null,
    var cardId: Int? = null,
    var lastCard: Int? = null
)
