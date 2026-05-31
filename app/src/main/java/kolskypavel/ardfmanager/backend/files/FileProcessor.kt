package kolskypavel.ardfmanager.backend.files

import android.content.Context
import android.net.Uri
import android.util.Log
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.processors.CsvProcessor
import kolskypavel.ardfmanager.backend.files.processors.FormatProcessorFactory
import kolskypavel.ardfmanager.backend.files.processors.JsonProcessor
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.enums.StandardCategoryType
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.UUID

/** Android file import/export coordinator backed by the system content resolver. */
class FileProcessor(appContext: WeakReference<Context>) {
    private val dataProcessor = DataProcessor.get()
    private val contentResolver = appContext.get()?.contentResolver

    /** Opens a read stream for a user-selected document URI. */
    private fun openInputStream(uri: Uri): InputStream? {
        try {
            return contentResolver?.openInputStream(uri)
        } catch (exception: Exception) {
            Log.e("Failed to open file for read: ", exception.stackTrace.toString())
        }
        return null
    }

    /** Opens a write stream for a user-selected document URI. */
    private fun openOutputStream(uri: Uri): OutputStream? {
        try {
            return contentResolver?.openOutputStream(uri)
        } catch (exception: Exception) {
            Log.e("Failed to open file for write: ", exception.stackTrace.toString())
        }
        return null
    }

    /** Imports typed event data from the selected file using the requested format processor. */
    suspend fun importData(
        uri: Uri,
        type: DataType,
        format: DataFormat,
        race: Race,
        context: Context
    ): DataImportWrapper {
        val inStream = openInputStream(uri)
        if (inStream != null) {

            val proc = FormatProcessorFactory.getFormatProcessor(format)
            return proc.importData(inStream, type, race, dataProcessor)
        }
        throw RuntimeException(context.getString(R.string.data_import_file_error))
    }


    /** Creates built-in standard categories for a race. */
    suspend fun importStandardCategories(
        type: StandardCategoryType,
        race: Race
    ): List<Category> =
        CsvProcessor.importStandardCategories(type, race, dataProcessor)

    /** Exports typed event data to the selected file using the requested format processor. */
    suspend fun exportData(
        uri: Uri,
        type: DataType,
        format: DataFormat,
        race: Race,
    ) {
        val outStream = openOutputStream(uri)
        if (outStream != null) {
            val proc = FormatProcessorFactory.getFormatProcessor(format)

            proc.exportData(
                outStream,
                type,
                format,
                dataProcessor,
                race
            )

        } else {
            throw IOException(dataProcessor.getContext()?.getString(R.string.data_import_file_error))
        }
    }

    /** Imports a full race backup from JSON. */
    suspend fun importRaceData(uri: Uri, context: Context): RaceData {
        val inStream = openInputStream(uri)
        if (inStream != null) {
            return JsonProcessor.importRaceData(inStream, dataProcessor)
        }
        throw IOException(context.getString(R.string.data_import_file_error))
    }

    /** Exports a full race backup to JSON. */
    suspend fun exportRaceData(uri: Uri, raceId: UUID) {
        val outStream = openOutputStream(uri)
        if (outStream != null) {
            return JsonProcessor.exportRaceData(outStream, dataProcessor, raceId)
        }
        throw RuntimeException()
    }
}
