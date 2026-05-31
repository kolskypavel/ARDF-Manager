package kolskypavel.ardfmanager.backend.files.processors

import android.content.Context
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.files.xml.XmlHelper
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.wrappers.ResultWrapper
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import kotlin.collections.emptyList

/** Import/export processor for IOF XML interoperability. */
object IofXmlProcessor : FormatProcessor {

    /** Imports IOF XML data types currently supported by the app. */
    override suspend fun importData(
        inStream: InputStream,
        dataType: DataType,
        race: Race,
        dataProcessor: DataProcessor
    ): DataImportWrapper {
        val context = dataProcessor.getContext()

        if (context != null) {
            return when (dataType) {
                DataType.CATEGORIES -> importCategories(
                    inStream,
                    race,
                    context
                )

                else -> {
                    TODO()
                }
            }
        }
        return DataImportWrapper(emptyList(), emptyList(), ArrayList())
    }

    /** Exports IOF XML data types currently supported by the app. */
    override suspend fun exportData(
        outStream: OutputStream,
        dataType: DataType,
        format: DataFormat,
        dataProcessor: DataProcessor,
        race: Race
    ) {
        when (dataType) {
            DataType.COMPETITORS -> TODO()
            DataType.RESULTS_LIVE -> exportResults(
                outStream,
                race, ResultsProcessor.getResultWrapperFlowByRace(race.id, dataProcessor).first()
                    .filter { it.category != null },
                dataProcessor
            )

            else -> TODO()
        }
    }

    fun importCompetitorData(
        inStream: InputStream,
        race: Race,
        categories: HashSet<CategoryData>
    ): DataImportWrapper {
        // Competitor import is not implemented yet; return an empty wrapper for callers that probe it.
        return DataImportWrapper(emptyList(), emptyList(), arrayListOf())
    }

    /** Imports IOF XML course/category data into category aggregates. */
    fun importCategories(
        inStream: InputStream,
        race: Race,
        context: Context
    ): DataImportWrapper {

        val cats = XmlHelper.parseCategories(inStream, race, context)
        return DataImportWrapper(emptyList(), cats, arrayListOf())
    }

    /** Placeholder for future IOF XML category export support. */
    fun exportCategories(
        outStream: OutputStream,
        race: Race,
        dataProcessor: DataProcessor
    ) {
    }

    /** Exports an IOF XML start list for the supplied category data. */
    suspend fun exportStartList(
        outStream: OutputStream,
        race: Race,
        data: List<CategoryData>,
        dataProcessor: DataProcessor
    ) {
        var writer: OutputStreamWriter? = null
        try {
            val (serializer, w) = XmlHelper.createSerializer(outStream)
            writer = w

            XmlHelper.writeRootTag(serializer, race, "StartList", dataProcessor)

            for (res in data) {
                XmlHelper.writeCategoryStartList(serializer, res, race.startDateTime)
            }

            serializer.endTag(null, "StartList")
            XmlHelper.finishSerializer(serializer, writer)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to export IOF XML startlist: ${ex.message}", ex)
        }
    }

    /** Exports an IOF XML result list for categorized live results. */
    suspend fun exportResults(
        outStream: OutputStream,
        race: Race,
        results: List<ResultWrapper>,
        dataProcessor: DataProcessor
    ) {
        var writer: OutputStreamWriter? = null
        try {
            val (serializer, w) = XmlHelper.createSerializer(outStream)
            writer = w

            XmlHelper.writeRootTag(serializer, race, "ResultList", dataProcessor)

            for (res in results) {
                XmlHelper.writeCategoryResult(serializer, res, race.startDateTime)
            }

            serializer.endTag(null, "ResultList")
            XmlHelper.finishSerializer(serializer, writer)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to export IOF XML: ${ex.message}", ex)
        }
    }
}
