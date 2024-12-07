package kolskypavel.ardfmanager.backend.room.enums

enum class RaceBand(val value: Int) {
    M80(0),
    M2(1),
    COMBINED(2),
    NONE(3);

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.value == value } ?: M80
    }
}