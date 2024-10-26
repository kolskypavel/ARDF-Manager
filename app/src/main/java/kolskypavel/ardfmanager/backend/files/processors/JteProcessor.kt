package kolskypavel.ardfmanager.backend.files.processors

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.io.path.Path

object JteProcessor : FormatProcessor {

    override suspend fun importData(
        inStream: InputStream,
        dataType: DataType,
        race: Race,
        dataProcessor: DataProcessor
    ): DataImportWrapper {
        throw NotImplementedError("Jte processor not intended for data import")
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

        val codeRes =
            DirectoryCodeResolver(Path("src/main/java/kolskypavel/ardfmanager/backend/files/templates"))
        val engine = TemplateEngine.create(codeRes, ContentType.Plain)
        val params = HashMap<String, Any>()

        params["race"] = dataProcessor.getCurrentRace()
        params["context"] = dataProcessor.getContext()
        params["results"] = dataProcessor.getResultDataFlowByRace(raceId).first()

        val templateOutput = StringOutput()
        engine.render(
            "textResultsTemplate.jte",
            params,
            templateOutput
        )
        var string = templateOutput.toString()
    }
}