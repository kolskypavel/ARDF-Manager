package org.openardf.radioomanager.shared.domain

/** Built-in standard category preset groups. */
enum class StandardCategoryType(val value: Int) {
    INTERNATIONAL(0),
    CZECH(1);

    companion object {
        fun getByValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: INTERNATIONAL
    }
}
