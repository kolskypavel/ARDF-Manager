package kolskypavel.ardfmanager.backend.room.enums

enum class StandardCategoryType(val value: Int) {
    INTERNATIONAL(0),
    CZECH(1);

    companion object {
        fun getByValue(value: Int) =
            StandardCategoryType.entries.firstOrNull { it.value == value } ?: INTERNATIONAL
    }
}