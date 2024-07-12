package kolskypavel.ardfmanager.backend.files

import android.content.Context
import android.net.Uri
import android.util.Log
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.processors.CsvProcessor
import kolskypavel.ardfmanager.backend.files.wrappers.CompetitorImportDataWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.UUID

class FileProcessor(private val appContext: WeakReference<Context>) {
    private val dataProcessor = DataProcessor.get()
    private val contentResolver = appContext.get()?.contentResolver

    private fun openInputStream(uri: Uri): InputStream? {
        try {
            return contentResolver?.openInputStream(uri)
        } catch (exception: Exception) {
            Log.e("Failed to open file for read: ", exception.stackTrace.toString())
        }
        return null
    }

    private fun openOutputStream(uri: Uri): OutputStream? {
        try {
            return contentResolver?.openOutputStream(uri)
        } catch (exception: Exception) {
            Log.e("Failed to open file for write: ", exception.stackTrace.toString())
        }
        return null
    }

    fun importCategories(
        uri: Uri,
        format: DataFormat,
        raceId: UUID, existing: List<Category>
    ): List<CategoryData> {
        val categories = emptyArray<CategoryData>()


        return categories.toList()
    }

    suspend fun importCompetitors(
        uri: Uri,
        format: DataFormat,
        raceId: UUID
    ): CompetitorImportDataWrapper? {

        CoroutineScope(Dispatchers.IO).launch {
            val inStream = openInputStream(uri)
            if (inStream != null) {
                when (format) {
                    DataFormat.CSV -> {
                        // val competitorData = CSVProcessor.parseCompetitorDataCsv(inStream)

                    }


                    else -> {
                        TODO()
                    }
                }
            } else {

            }
        }
        return null
    }

    fun exportCompetitors(uri: Uri, format: DataFormat, race: Race): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            when (format) {
                DataFormat.IOF_XML -> {}


                else -> {
                    TODO()
                }
            }
        }
        return true
    }

    suspend fun exportReadoutData(uri: Uri, readouts: List<ReadoutResult>): Boolean {

        return true
    }

    fun importData(uri: Uri, dataType: DataType, dataFormat: DataFormat) {

    }

    suspend fun exportData(
        uri: Uri,
        dataFormat: DataFormat,
        results: List<ReadoutData>
    ): Boolean {
        val outStream = openOutputStream(uri)
        if (outStream != null) {
            try {
                when (dataFormat) {
                    DataFormat.CSV -> CsvProcessor.exportsResults(results, outStream)
                    else -> {}
                }
                return true
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }


}