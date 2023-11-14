package kolskypavel.ardfmanager.backend.room.enums

enum class EventBand(val value: Int) {
    M80(0),
    M2(1),
    COMBINED(2);

    companion object {
        fun getByValue(value: Int) = EventBand.values().firstOrNull { it.value == value }
    }
}