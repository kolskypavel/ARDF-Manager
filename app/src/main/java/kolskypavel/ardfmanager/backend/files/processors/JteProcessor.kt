package kolskypavel.ardfmanager.backend.files.processors

import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import java.io.InputStream
import java.io.OutputStream

object JteProcessor : FormatProcessor {

    override suspend fun importData(
        inStream: InputStream,
        dataType: DataType,
        race: Race,
        categories: List<CategoryData>
    ): DataImportWrapper {
        TODO("Not yet implemented")
    }

    override suspend fun exportData(
        outStream: OutputStream,
        dataType: DataType,
        format: DataFormat,
        race: Race,
        categories: List<CategoryData>,
        competitors: List<CompetitorData>,
        readouts: List<ReadoutData>,
        results: List<ResultDisplayWrapper>
    ): Boolean {
        TODO("Not yet implemented")
    }


}