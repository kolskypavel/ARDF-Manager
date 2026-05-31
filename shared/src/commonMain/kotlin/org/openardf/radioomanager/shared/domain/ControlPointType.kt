package org.openardf.radioomanager.shared.domain

/** Role of a control point within an ARDF/orienteering course. */
enum class ControlPointType(val value: Int) {
    CONTROL(0),
    BEACON(1),
    SEPARATOR(2);

    companion object {
        fun getByValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: CONTROL
    }
}
