package kolskypavel.ardfmanager.backend.files.constants

enum class FileExportFormat(var value: Int) {
    CSV_OCM_RESULTS(0),
    PDF_SIMPLE(1),
    PDF_SPLITS(2),
    IOF_XML(3),
    HTML_SIMPLE(4),
    HTML_SPLITS(5),
}