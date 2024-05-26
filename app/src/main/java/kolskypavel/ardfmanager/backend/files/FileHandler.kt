package kolskypavel.ardfmanager.backend.files

import android.content.Context
import android.net.Uri
import android.util.Log
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.ResultDataFormat
import kolskypavel.ardfmanager.backend.files.constants.CompetitorDataFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.UUID

class FileHandler(val appContext: WeakReference<Context>) {
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

    fun importData(uri: Uri, format: CompetitorDataFormat): Boolean {
        var ret = true
        CoroutineScope(Dispatchers.IO).launch {
            val inStream = openInputStream(uri)
            if (inStream != null) {
                when (format) {
                    CompetitorDataFormat.CSV_OCM_COMPETITORS -> {
                        // val competitorData = CSVProcessor.parseCompetitorDataCsv(inStream)

                    }


                    else -> {
                        TODO()
                    }
                }
            } else {
                ret = false
            }
        }
        return ret
    }

    fun exportData(format: ResultDataFormat, eventId: UUID): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            // val competitorData = dataProcessor.getCompetitorDataFlowByEvent()
            when (format) {
                ResultDataFormat.IOF_XML -> {}


                else -> {
                    TODO()
                }
            }
        }
        return true
    }

}