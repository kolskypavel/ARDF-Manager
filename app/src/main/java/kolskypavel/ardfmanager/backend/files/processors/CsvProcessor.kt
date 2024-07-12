package kolskypavel.ardfmanager.backend.files.processors

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import kolskypavel.ardfmanager.backend.files.constants.FileConstants
import kolskypavel.ardfmanager.backend.files.wrappers.CompetitorImportDataWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
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
object CsvProcessor {

    @Throws(IOException::class)
    suspend fun importCategories(
        inStream: InputStream,
        race: Race
    ) {
        val csvReader = CsvReader().readAll(inStream)
        if (csvReader.isNotEmpty()) {

            for (row in csvReader) {
                if (row.size == FileConstants.CATEGORY_CSV_COLUMNS) {
                    try {

                        val controPointString = row[10]

                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun exportCategories(categories: List<CategoryData>, outStream: OutputStream) {

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

                    //Separate control points by column
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
    suspend fun parseCompetitorData(
        inStream: InputStream,
        race: Race,
        categories: HashSet<Category>
    ): CompetitorImportDataWrapper {
        val csvReader = CsvReader().readAll(inStream)
        val competitors = ArrayList<Competitor>()

        if (csvReader.isNotEmpty()) {
            for (row in csvReader) {

                if (row.size == FileConstants.OCM_COMPETITOR_CSV_COLUMNS) {
                    try {
                        var category: Category? = null

                        //Check if category exists
                        if (row[3].isNotEmpty()) {
                            val catName = row[3]
                            val origCat = categories.find { it.name == catName }
                            if (origCat != null) {
                                category = origCat
                            } else {
                                category =
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
                                    )
                                categories.add(category)
                            }
                        }

                        val competitor =
                            Competitor(
                                UUID.randomUUID(),
                                race.id,
                                category?.id,
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
                        competitors.add(competitor)
                    } catch (ex: Exception) {
                        Log.e("CSV import", "Failed to import competitor")
                    }
                }
            }
        }
        return CompetitorImportDataWrapper(competitors, categories.toList())
    }

    @Throws(IOException::class)
    suspend fun exportCompetitors(
        competitorData: List<CompetitorData>,
        outStream: OutputStream
    ) {
        val writer = outStream.bufferedWriter()
        withContext(Dispatchers.IO) {

            for (com in competitorData) {
                writer.write(
                    com.competitorCategory.competitor.toCsvString(
                        com.competitorCategory.category?.name ?: ""
                    )
                )
                writer.newLine()
            }
            writer.flush()
        }
    }

    @Throws(IOException::class)
    suspend fun exportCompetitorStarts(
        competitorData: List<CompetitorData>,
        outStream: OutputStream
    ) {

    }

    /**
     * Format:
     */
    @Throws(IOException::class)
    suspend fun exportReadoutData(readoutData: List<ReadoutData>, outStream: OutputStream) {
        readoutData.forEach { readoutData ->

        }
    }


    @Throws(IOException::class)
    suspend fun exportsResults(results: List<ReadoutData>, outputStream: OutputStream) {

    }
}