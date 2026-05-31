package org.openardf.radioomanager.shared.event

data class EventProjectFile(
    val schemaVersion: Int = EventProjectFileFormat.CURRENT_SCHEMA_VERSION,
    val appName: String = EventProjectFileFormat.APP_NAME,
    val raceData: EventRaceData
) {
    fun isSupportedSchema(): Boolean =
        EventProjectFileFormat.isSupportedSchema(schemaVersion)
}

object EventProjectFileFormat {
    const val APP_NAME = "Radio-O-Manager"
    const val CURRENT_SCHEMA_VERSION = 1

    fun isSupportedSchema(schemaVersion: Int): Boolean =
        schemaVersion in 1..CURRENT_SCHEMA_VERSION
}
