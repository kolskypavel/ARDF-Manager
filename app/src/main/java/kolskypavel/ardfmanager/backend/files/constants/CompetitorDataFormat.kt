package kolskypavel.ardfmanager.backend.files.constants

enum class CompetitorDataFormat(var value: Int) {
    CSV_OCM_COMPETITORS(0),
    FJW_TLN(1),
    JSON(3),
    IOF_XML(2);

    companion object {
        fun getByValue(value: Int) = CompetitorDataFormat.entries.firstOrNull { it.value == value }
    }
}