package kolskypavel.ardfmanager.backend.logging

import java.io.File
import java.time.Instant

/**
 * Writes a small rolling debug log into app-private storage.
 *
 * The logger is intentionally simple and synchronous because it is only used
 * for low-volume diagnostic breadcrumbs. Callers must avoid raw payloads,
 * credentials, and personally identifying race data.
 */
class RollingDebugLog(
    private val directory: File,
    private val clock: () -> Instant = { Instant.now() },
    private val maxFileBytes: Long = DEFAULT_MAX_FILE_BYTES,
    private val retainedFileCount: Int = DEFAULT_RETAINED_FILE_COUNT,
    private val fileName: String = DEFAULT_FILE_NAME
) {

    init {
        require(maxFileBytes > 0) { "maxFileBytes must be positive" }
        require(retainedFileCount > 0) { "retainedFileCount must be positive" }
        require(fileName.isNotBlank()) { "fileName must not be blank" }
    }

    /** Appends one sanitized line and rotates files when the active log is full. */
    @Synchronized
    fun write(level: String, tag: String, message: String) {
        directory.mkdirs()

        val line = "${clock()} ${sanitize(level)} ${sanitize(tag)} ${sanitize(message)}\n"
        val bytes = line.toByteArray(Charsets.UTF_8)
        rotateIfNeeded(bytes.size.toLong())
        activeFile().appendBytes(bytes)
    }

    /** Returns the active log followed by retained archives that currently exist. */
    fun logFiles(): List<File> =
        (listOf(activeFile()) + (1 until retainedFileCount).map { archiveFile(it) })
            .filter { it.exists() }

    /** Sanitizes diagnostic text so each log event stays on a single readable line. */
    fun sanitize(value: String): String =
        value
            .replace(Regex("[\\r\\n\\t]+"), " ")
            .replace(Regex("[\\p{Cntrl}&&[^ ]]"), "?")
            .trim()

    private fun rotateIfNeeded(incomingBytes: Long) {
        val activeFile = activeFile()
        if (!activeFile.exists() || activeFile.length() + incomingBytes <= maxFileBytes) {
            return
        }

        archiveFile(retainedFileCount - 1).delete()
        for (index in retainedFileCount - 2 downTo 1) {
            val source = archiveFile(index)
            if (source.exists()) {
                source.renameTo(archiveFile(index + 1))
            }
        }
        activeFile.renameTo(archiveFile(1))
    }

    private fun activeFile(): File = File(directory, fileName)

    private fun archiveFile(index: Int): File = File(directory, "$fileName.$index")

    companion object {
        const val DEFAULT_FILE_NAME = "debug.log"
        const val DEFAULT_MAX_FILE_BYTES = 512L * 1024L
        const val DEFAULT_RETAINED_FILE_COUNT = 3
    }
}
