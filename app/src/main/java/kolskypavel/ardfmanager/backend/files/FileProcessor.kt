package kolskypavel.ardfmanager.backend.files

import android.content.Context
import android.net.Uri
import android.util.Log
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.processors.CsvProcessor
import kolskypavel.ardfmanager.backend.files.processors.FormatProcessorFactory
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference

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

    suspend fun importData(
        uri: Uri,
        dataType: DataType,
        format: DataFormat,
        race: Race
    ): DataImportWrapper? {

        val inStream = openInputStream(uri)
        if (inStream != null) {
            val formatProcessorFactory = FormatProcessorFactory()
            val proc = formatProcessorFactory.getFormatProcessor(format)
            val categories = dataProcessor.getCategoryDataForRace(race.id)
            return proc.importData(uri, dataType, race, categories)
        }
        return null
    }

    suspend fun exportData(
        uri: Uri,
        dataType: DataType,
        dataFormat: DataFormat,
        results: List<ReadoutData>
    ): Boolean {
        val outStream = openOutputStream(uri)
        if (outStream != null) {
            when (dataType) {
                DataType.RESULTS_SIMPLE -> {
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

                DataType.CATEGORIES -> TODO()
                DataType.C0MPETITORS -> TODO()
                DataType.COMPETITOR_STARTS_TIME -> TODO()
                DataType.COMPETITOR_STARTS_CATEGORIES -> TODO()
                DataType.COMPETITOR_STARTS_CLUBS -> TODO()
                DataType.RESULTS_SPLITS -> TODO()
            }
        }
        return false
    }


}