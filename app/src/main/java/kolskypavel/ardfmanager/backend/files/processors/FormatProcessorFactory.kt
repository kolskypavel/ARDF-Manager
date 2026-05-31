package kolskypavel.ardfmanager.backend.files.processors

import kolskypavel.ardfmanager.backend.files.constants.DataFormat

/** Selects the processor implementation for a requested file format. */
object FormatProcessorFactory {
    /** Returns the import/export processor responsible for the format. */
    fun getFormatProcessor(dataFormat: DataFormat): FormatProcessor {
        return when (dataFormat) {
            DataFormat.TXT, DataFormat.HTML -> TextProcessor
            DataFormat.CSV -> CsvProcessor
            DataFormat.JSON -> JsonProcessor
            DataFormat.IOF_XML -> IofXmlProcessor
        }
    }
}
