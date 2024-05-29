package kolskypavel.ardfmanager.backend.files.processors

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.FileConstants
import kolskypavel.ardfmanager.backend.files.wrappers.CompetitorImportDataWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Exports/Import result in CSV format
 * Competitor format: SI, Name, Last Name, Category, Gender, Birth year, Callsign (not used), Club;;;Start no, Index
 * Result format:
 */
object CSVProcessor {
    val dataProcessor = DataProcessor.get()

    suspend fun parseCompetitorDataCsv(
        inStream: InputStream,
        race: Race
    ): List<CompetitorImportDataWrapper> {
        val csvReader = CsvReader().readAll(inStream)
        val competitors = ArrayList<Competitor>()
        val categories = HashMap<String, Category>()

        if (csvReader.isNotEmpty()) {
            for (row in csvReader) {
                if (row.size == FileConstants.CHEB_IMPORT_CSV_COLUMNS) {
                    try {
                        var category: Category? = null

                        //Check if category exists
                        if (row[3].isNotEmpty()) {
                            val dbCat = dataProcessor.getCategoryByName(row[3], race.id)

                            if (dbCat != null) {
                                category = dbCat
                            } else if (categories[row[3]] != null) {
                                category = categories[row[3]]
                            } else {
                                category =
                                    Category(
                                        UUID.randomUUID(),
                                        race.id,
                                        row[3],
                                        false,
                                        null,
                                        false,
                                        race.raceType,
                                        race.timeLimit,
                                        race.startTimeSource,
                                        race.finishTimeSource, 0F,
                                        0F, 0
                                    )
                                categories[row[3]] = category
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

                    }
                }
            }
        }
        // return
        return emptyList()
    }

    suspend fun exportCompetitorDataCsv(
        competitors: List<CompetitorData>,
        outputStream: OutputStream
    ): Boolean {

        for (com in competitors) {

        }
        return true
    }

    suspend fun exportCompetitorStartsCsv() {

    }


    suspend fun exportsResultsCsv(outputStream: OutputStream): Boolean {

        return true
    }
}