package kolskypavel.ardfmanager.backend.sportident

/**
 * Current status of the SI reader with the necessary information
 *
 */
data class SIReaderState(
    var status: SIReaderStatus,
    var stationId: Int?,
    var cardId: Int?
)
