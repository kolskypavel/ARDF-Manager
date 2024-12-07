package kolskypavel.ardfmanager.backend.room.enums

enum class CompetitorTableDisplayType(var value: Int) {
    OVERVIEW(0),
    START_LIST(1),
    FINISH_REACHED(2),
    ON_THE_WAY(3);

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.value == value } ?: OVERVIEW
    }
}