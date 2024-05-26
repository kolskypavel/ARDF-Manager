package kolskypavel.ardfmanager.backend.files.constants

enum class CategoryDataFormat(var value: Int) {
    CSV(0),
    JSON(1),
    XML(2);

    companion object {
        fun getByValue(value: Int) = CategoryDataFormat.entries.firstOrNull { it.value == value }
    }
}