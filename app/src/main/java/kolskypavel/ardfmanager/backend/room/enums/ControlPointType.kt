package kolskypavel.ardfmanager.backend.room.enums

enum class ControlPointType(val value: Int) {
    CONTROL(0),
    BEACON(1),
    SEPARATOR(2);

    companion object {
        fun getByValue(value: Int) = ControlPointType.entries.firstOrNull { it.value == value }
    }
}