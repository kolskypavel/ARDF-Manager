package kolskypavel.ardfmanager.backend.room.enums

/**
 * Time source for the start
 */
enum class StartTimeSource(val value: Int) {
    DRAWN_TIME(0), //Time drawn for each competitor
    START_CONTROL(1),  //Start control - punched by the competitor
    FIRST_CONTROL(2);// First control punched by the competitor, other than start

    companion object {
        fun getByValue(value: Int) =
            StartTimeSource.entries.firstOrNull { it.value == value } ?: START_CONTROL
    }
}