package org.openardf.radioomanager.shared.domain

enum class PunchStatus(val value: Int) {
    VALID(0),
    INVALID(1),
    DUPLICATE(2),
    UNKNOWN(3);

    companion object {
        fun getByValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: VALID
    }
}
