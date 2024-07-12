package kolskypavel.ardfmanager.backend.files.constants

enum class DataFormat(var value: Int) {
    TXT(0),
    CSV(1),
    JSON(2),
    IOF_XML(3),
    PDF(4),
    HTML(5);

    companion object {
        fun getByValue(value: Int) = DataFormat.entries.firstOrNull { it.value == value }
    }
}