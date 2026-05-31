package org.openardf.radioomanager.shared.device

/** Shared snapshot of the SportIdent reader state exposed by platform services. */
data class SIReaderState(
    var status: SIReaderStatus,
    var stationId: Int? = null,
    var cardId: Int? = null,
    var lastCard: Int? = null
)
