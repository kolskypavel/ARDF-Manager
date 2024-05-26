package kolskypavel.ardfmanager.backend.files.constants

enum class ResultDataFormat(var value: Int) {
    CSV_OCM_RESULTS(0),
    PDF_SIMPLE(1),
    PDF_SPLITS(2),
    IOF_XML(3),
    JSON(4),
    HTML_SIMPLE(5),
    HTML_SPLITS(6);

    companion object {
        fun getByValue(value: Int) = ResultDataFormat.entries.firstOrNull { it.value == value }
    }
}