package org.openardf.radioomanager.shared.event

/** File envelope for future shared event project import/export. */
data class EventProjectFile(
    val schemaVersion: Int = EventProjectFileFormat.CURRENT_SCHEMA_VERSION,
    val appName: String = EventProjectFileFormat.APP_NAME,
    val raceData: EventRaceData
) {
    /** Returns true when this file schema can be read by the current shared code. */
    fun isSupportedSchema(): Boolean =
        EventProjectFileFormat.isSupportedSchema(schemaVersion)
}

/** Schema metadata for portable Radio-O-Manager project files. */
object EventProjectFileFormat {
    const val APP_NAME = "Radio-O-Manager"
    const val CURRENT_SCHEMA_VERSION = 1

    /** Returns true when the supplied schema version is within the supported range. */
    fun isSupportedSchema(schemaVersion: Int): Boolean =
        schemaVersion in 1..CURRENT_SCHEMA_VERSION
}
