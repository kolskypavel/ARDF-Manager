package kolskypavel.ardfmanager.backend.files.processors

import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entity.Race
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

object TextProcessor : FormatProcessor {

    override suspend fun importData(
        inStream: InputStream,
        dataType: DataType,
        race: Race,
        dataProcessor: DataProcessor
    ): DataImportWrapper {
        throw NotImplementedError("Text processor not intended for data import")
    }

    override suspend fun exportData(
        outStream: OutputStream,
        dataType: DataType,
        format: DataFormat,
        dataProcessor: DataProcessor,
        raceId: UUID
    ): Boolean {
        exportTxtResults(outStream, dataType, raceId, dataProcessor)
        return true
    }

    suspend fun exportTxtResults(
        outStream: OutputStream,
        dataType: DataType,
        raceId: UUID,
        dataProcessor: DataProcessor
    ) {


    }
}