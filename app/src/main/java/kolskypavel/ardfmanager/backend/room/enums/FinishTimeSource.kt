package kolskypavel.ardfmanager.backend.room.enums

/**
 * Time source for the finish
 */
enum class FinishTimeSource(val value: Int) {
    FINISH_CONTROL(0),  // Finish punched by the competitor
    LAST_CONTROL(1);    // Last control punched by the competitor

    companion object {
        fun getByValue(value: Int) = FinishTimeSource.entries.firstOrNull { it.value == value }
    }
}