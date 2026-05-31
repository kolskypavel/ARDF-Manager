package org.openardf.radioomanager.shared.files

/** Logical event data groups that can be imported or exported. */
enum class DataType(var value: Int) {
    CATEGORIES(0),
    COMPETITORS(1),
    COMPETITOR_STARTS(2),
    RESULTS_LIVE(3),
    READOUT_DATA(4),
    RESULTS_FINAL(5);

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.value == value }
    }
}
