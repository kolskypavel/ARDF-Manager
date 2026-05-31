package org.openardf.radioomanager.shared.domain

/** Radio band classification for ARDF categories and races. */
enum class RaceBand(val value: Int) {
    M80(0),
    M2(1),
    COMBINED(2),
    NONE(3);

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.value == value } ?: M80
    }
}
