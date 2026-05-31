package org.openardf.radioomanager.shared.device

/** Shared high-level connection/readout state for a SportIdent reader. */
enum class SIReaderStatus {
    CONNECTED,
    DISCONNECTED,
    READING,
    ERROR,
    CARD_READ
}
