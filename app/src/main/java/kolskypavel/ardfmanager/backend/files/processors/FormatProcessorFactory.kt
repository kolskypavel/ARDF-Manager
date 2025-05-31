package kolskypavel.ardfmanager.backend.files.processors

import kolskypavel.ardfmanager.backend.files.constants.DataFormat

class FormatProcessorFactory {
    fun getFormatProcessor(dataFormat: DataFormat): FormatProcessor {
        return when (dataFormat) {
            DataFormat.TXT, DataFormat.HTML, DataFormat.PDF -> TextProcessor
            DataFormat.CSV -> CsvProcessor
            DataFormat.JSON -> JsonProcessor
            DataFormat.IOF_XML -> IofXmlProcessor
        }
    }
}