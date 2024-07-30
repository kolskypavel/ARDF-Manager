package kolskypavel.ardfmanager.backend.files.processors

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.constants.FileConstants
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Exports/Import result in CSV format
 * Category format: Name; Gender (0 = woman, 1 = man); Max age; *Length; *Climb; *Order in results; *Race type (); Time limit in minutes; Start source (0 = drawn, 1 = start control; 2 = first control); Finish source (0 = finish control, 1 = last control); Number of control points; Control points
 * Control points format: Code,
 * Competitor format: SI, Name, Last Name, Category, Gender, Birth year, Callsign (not used), Club;;;Start no, Index
 * Competitor starts format:
 * Result format:
 */
object CsvProcessor : FormatProcessor {

    override suspend fun importData(
        inStream: InputStream,
        dataType: DataType,
        race: Race,
        categories: List<CategoryData>
    ): DataImportWrapper {
        return when (dataType) {
            DataType.CATEGORIES -> return importCategories(inStream, race)
            DataType.C0MPETITORS -> return importCompetitorData(
                inStream,
                race,
                categories.toHashSet()
            )

            DataType.COMPETITOR_STARTS_TIME -> return importCompetitorStarts(inStream, race)
            else -> DataImportWrapper(emptyList(), emptyList())
        }
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
        try {

            when (dataType) {
                DataType.CATEGORIES -> exportCategories(outStream, categories)
                DataType.C0MPETITORS -> exportCompetitors(outStream, competitors)
                DataType.COMPETITOR_STARTS_TIME,
                DataType.COMPETITOR_STARTS_CATEGORIES,
                DataType.COMPETITOR_STARTS_CLUBS -> exportStarts(outStream, competitors, race)

                DataType.RESULTS_SIMPLE, DataType.RESULTS_SPLITS -> exportsResults(
                    outStream,
                    results
                )

                DataType.READOUT_DATA -> exportReadoutData(outStream, readouts)
            }
            return true
        } catch (e: Exception) {
            Log.e("EXPORT", e.stackTraceToString())
            return false
        }
    }


    //Use reader with semicolon separator
    private fun getReader(): CsvReader {
        val context = CsvReaderContext()
        context.delimiter = ';'
        return CsvReader(context)
    }

    @Throws(IOException::class)
    suspend fun importCategories(
        inStream: InputStream,
        race: Race
    ): DataImportWrapper {
        val readData = getReader().readAll(inStream)
        val data = DataImportWrapper(emptyList(), emptyList())
        if (readData.isNotEmpty()) {

            for (row in readData) {
                if (row.size == FileConstants.CATEGORY_CSV_COLUMNS) {
                    try {

                        val controPointString = row[10]

                    } catch (e: Exception) {

                    }
                }
            }
        }
        return data
    }

    @Throws(IOException::class)
    suspend fun exportCategories(outStream: OutputStream, categories: List<CategoryData>) {

        withContext(Dispatchers.IO) {
            val writer = outStream.bufferedWriter()
            for (data in categories) {

                writer.write(data.category.toCSVString())
                writer.write(";")
                writer.write(data.controlPoints.size.toString())
                writer.write(";")

                //Write all control points
                for (cp in data.controlPoints.withIndex()) {
                    writer.write(cp.value.toCsvString())

                    //Separate control points by comma
                    if (cp.index < data.controlPoints.size - 1) {
                        writer.write(",")
                    }
                }
                writer.newLine()
            }
            writer.flush()
        }
    }

    @Throws(IOException::class)
    fun importCompetitorData(
        inStream: InputStream,
        race: Race,
        categories: HashSet<CategoryData>
    ): DataImportWrapper {


        val csvReader = getReader().readAll(inStream)
        val competitors = ArrayList<CompetitorCategory>()

        if (csvReader.isNotEmpty()) {
            for (row in csvReader) {

                if (row.size == FileConstants.OCM_COMPETITOR_CSV_COLUMNS) {
                    try {
                        var category: CategoryData? = null

                        //Check if category exists
                        if (row[3].isNotEmpty()) {
                            val catName = row[3]
                            val origCat = categories.find { it.category.name == catName }
                            if (origCat != null) {
                                category = origCat
                            } else {
                                category = CategoryData(
                                    Category(
                                        UUID.randomUUID(),
                                        race.id,
                                        row[3],
                                        false,
                                        null,
                                        0F,
                                        0F,
                                        0,
                                        false,
                                        race.raceType, race.timeLimit,
                                        race.startTimeSource, race.finishTimeSource
                                    ), emptyList(), emptyList()
                                )
                                categories.add(category)
                            }
                        }

                        val competitor =
                            Competitor(
                                UUID.randomUUID(),
                                race.id,
                                category?.category?.id,
                                row[1],
                                row[2],
                                row[7],
                                row[10],
                                row[4].toInt() == 0,
                                row[5].toInt(),
                                row[0].toInt(),
                                false,
                                row[9].toInt(),
                                null
                            )
                        if (category != null) {
                            competitors.add(CompetitorCategory(competitor, category.category))
                        }
                    } catch (ex: Exception) {
                        Log.e("CSV import", "Failed to import competitor")
                    }
                }
            }
        }
        return DataImportWrapper(competitors, categories.toList())
    }

    @Throws(IOException::class)
    suspend fun exportCompetitors(
        outStream: OutputStream,
        competitorData: List<CompetitorData>
    ) {
        val writer = outStream.bufferedWriter()
        withContext(Dispatchers.IO) {

            for (com in competitorData) {
                writer.write(
                    com.competitorCategory.competitor.toSimpleCsvString(
                        com.competitorCategory.category?.name ?: ""
                    )
                )
                writer.newLine()
            }
            writer.flush()
        }
    }

    suspend fun importCompetitorStarts(
        inStream: InputStream,
        race: Race
    ): DataImportWrapper {
        return DataImportWrapper(emptyList(), emptyList())
    }

    @Throws(IOException::class)
    suspend fun exportStarts(
        outStream: OutputStream,
        competitorData: List<CompetitorData>,
        race: Race
    ) {
        val writer = outStream.bufferedWriter()
        withContext(Dispatchers.IO) {
            for (com in competitorData) {
                val category = com.competitorCategory.category
                writer.write(
                    com.competitorCategory.competitor.toStartCsvString(
                        category?.name ?: "",
                        race.startDateTime
                    )
                )
                writer.newLine()
            }
            writer.flush()
        }
    }

    @Throws(IOException::class)
    suspend fun exportReadoutData(outStream: OutputStream, readoutData: List<ReadoutData>) {
        val writer = outStream.bufferedWriter()
        withContext(Dispatchers.IO) {
            for (rd in readoutData) {

                writer.newLine()
            }
            writer.flush()
        }
    }

    @Throws(IOException::class)
    suspend fun exportsResults(outStream: OutputStream, results: List<ResultDisplayWrapper>) {
        val writer = outStream.bufferedWriter()
        withContext(Dispatchers.IO) {
            for (res in results) {

                writer.newLine()
            }
            writer.flush()
        }
    }
}