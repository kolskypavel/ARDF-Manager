package kolskypavel.ardfmanager.backend.sportident

/**
 * Current status of the SI Reader - used for the bottom status bar
 */
enum class SIReaderStatus {
    CONNECTED,
    DISCONNECTED,
    READING,
    ERROR,
    CARD_READ
}