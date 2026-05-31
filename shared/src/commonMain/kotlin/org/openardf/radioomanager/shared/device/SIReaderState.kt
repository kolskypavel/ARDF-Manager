package org.openardf.radioomanager.shared.device

data class SIReaderState(
    var status: SIReaderStatus,
    var stationId: Int? = null,
    var cardId: Int? = null,
    var lastCard: Int? = null
)
