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
            return proc.importData(inStream, type, race, dataProcessor)
        }
        return null
    }

    suspend fun exportData(
        uri: Uri,
        type: DataType,
        format: DataFormat,
        raceId: UUID,
    ): Boolean {
        val outStream = openOutputStream(uri)
        if (outStream != null) {
            val formatProcessorFactory = FormatProcessorFactory()
            val proc = formatProcessorFactory.getFormatProcessor(format)

            return proc.exportData(
                outStream,
                type,
                format,
                dataProcessor,
                raceId
            )
        }
        return false
    }
}