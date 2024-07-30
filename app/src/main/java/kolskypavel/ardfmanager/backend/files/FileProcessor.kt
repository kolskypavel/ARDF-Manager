package kolskypavel.ardfmanager.backend.files

import android.content.Context
import android.net.Uri
import android.util.Log
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.processors.FormatProcessorFactory
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kotlinx.coroutines.flow.first
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
        type: DataType,
        format: DataFormat,
        race: Race
    ): DataImportWrapper? {

        val inStream = openInputStream(uri)
        if (inStream != null) {
            val formatProcessorFactory = FormatProcessorFactory()
            val proc = formatProcessorFactory.getFormatProcessor(format)
            val categories = dataProcessor.getCategoryDataForRace(race.id)
            return proc.importData(inStream, type, race, categories)
        }
        return null
    }

    suspend fun exportData(
        uri: Uri,
        type: DataType,
        format: DataFormat,
        race: Race,
    ): Boolean {
        val outStream = openOutputStream(uri)
        if (outStream != null) {
            val formatProcessorFactory = FormatProcessorFactory()
            val proc = formatProcessorFactory.getFormatProcessor(format)
            val categories = dataProcessor.getCategoryDataForRace(race.id)
            val competitors = dataProcessor.getCompetitorDataFlowByRace(race.id).first()
            val results = dataProcessor.getResultDataFlowByRace(race.id).first()
            val readouts = dataProcessor.getReadoutDataFlowByRace(race.id).first()

            return proc.exportData(
                outStream,
                type,
                format,
                race,
                categories,
                competitors,
                readouts,
                results
            )
        }
        return false
    }
}