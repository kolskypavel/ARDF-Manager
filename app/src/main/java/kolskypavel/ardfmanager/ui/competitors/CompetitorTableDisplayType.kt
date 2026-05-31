package kolskypavel.ardfmanager.ui.competitors

/** Display modes supported by the competitor table. */
enum class CompetitorTableDisplayType(var value: Int) {
    OVERVIEW(0),
    START_LIST(1),
    FINISH_REACHED(2),
    ON_THE_WAY(3),
    SI_RENT(4);

    companion object {
        /** Maps a persisted integer preference value to a display mode. */
        fun getByValue(value: Int) = entries.firstOrNull { it.value == value } ?: OVERVIEW
    }
}
