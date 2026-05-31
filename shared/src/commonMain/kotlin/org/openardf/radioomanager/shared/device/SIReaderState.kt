package org.openardf.radioomanager.shared.device

/** Shared snapshot of the SportIdent reader state exposed by platform services. */
data class SIReaderState(
    /** Current reader connection/readout status. */
    var status: SIReaderStatus,
    /** Connected station identifier, when known. */
    var stationId: Int? = null,
    /** Card currently being read, when a readout is in progress. */
    var cardId: Int? = null,
    /** Most recently completed card number. */
    var lastCard: Int? = null
)
