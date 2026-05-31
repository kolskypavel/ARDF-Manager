package org.openardf.radioomanager.shared.sportident

/** Shared SportIdent numeric limits and range-checking helpers. */
object SportIdentCodes {
    const val SI_MIN_NUMBER = 1000
    const val SI_MAX_NUMBER = 9999999

    const val SI_MIN_CODE = 1
    const val SI_MAX_CODE = 255

    const val SECONDS_DAY = 86400L
    const val SECONDS_WEEK = 604800L

    /** Returns true when the supplied SI card number is within the supported card range. */
    fun isSINumberValid(siNumber: Int): Boolean {
        return siNumber in SI_MIN_NUMBER..SI_MAX_NUMBER
    }

    /** Returns true when the supplied control code is within the supported station-code range. */
    fun isSICodeValid(siCode: Int): Boolean {
        return siCode in SI_MIN_CODE..SI_MAX_CODE
    }
}
