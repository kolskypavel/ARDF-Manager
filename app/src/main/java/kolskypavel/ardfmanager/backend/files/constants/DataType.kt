package kolskypavel.ardfmanager.backend.files.constants

enum class DataType(var value: Int) {
    CATEGORIES(0),
    C0MPETITORS(1),
    COMPETITOR_STARTS_TIME(2),
    COMPETITOR_STARTS_CATEGORIES(3),
    COMPETITOR_STARTS_CLUBS(4);
    companion object {
        fun getByValue(value: Int) = DataType.entries.firstOrNull { it.value == value }
    }
}