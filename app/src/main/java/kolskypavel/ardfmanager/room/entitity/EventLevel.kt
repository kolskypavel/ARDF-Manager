package kolskypavel.ardfmanager.room.entitity

enum class EventLevel(val value: Int) {
    INTERNATIONAL(0),
    NATIONAL(1),
    REGIONAL(2),
    DISTRICT(3),
    PRACTICE(4);

    companion object {
        fun getByValue(value: Int) = EventLevel.values().firstOrNull { it.value == value }
    }
}