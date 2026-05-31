package kolskypavel.ardfmanager.backend.logging

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Hidden app-private debug log facade.
 *
 * The file log complements logcat for field diagnostics. It is deliberately not
 * exposed in normal UI and should contain only operational breadcrumbs that are
 * useful when reproducing device, import/export, or service problems.
 */
object DebugLog {
    private const val LOG_DIR_NAME = "debug-logs"

    @Volatile
    private var rollingLog: RollingDebugLog? = null

    /** Configures logging under the app's private files directory. */
    fun initialize(context: Context) {
        rollingLog = RollingDebugLog(File(context.filesDir, LOG_DIR_NAME))
        info("App", "Debug log initialized")
    }

    /** Writes a debug-level breadcrumb to the hidden file log. */
    fun debug(tag: String, message: String) {
        write("D", tag, message)
    }

    /** Writes an info-level breadcrumb to the hidden file log. */
    fun info(tag: String, message: String) {
        write("I", tag, message)
    }

    /** Writes a warning-level breadcrumb to the hidden file log. */
    fun warn(tag: String, message: String) {
        write("W", tag, message)
    }

    /** Writes an error-level breadcrumb to the hidden file log. */
    fun error(tag: String, message: String) {
        write("E", tag, message)
    }

    private fun write(level: String, tag: String, message: String) {
        try {
            rollingLog?.write(level, tag, message)
        } catch (exception: Exception) {
            Log.w("DebugLog", "Failed to write debug log: ${exception.message}")
        }
    }
}
