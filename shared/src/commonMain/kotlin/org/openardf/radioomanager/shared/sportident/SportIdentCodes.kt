package org.openardf.radioomanager.shared.sportident

object SportIdentCodes {
    const val SI_MIN_NUMBER = 1000
    const val SI_MAX_NUMBER = 9999999

    const val SI_MIN_CODE = 1
    const val SI_MAX_CODE = 255

    fun isSINumberValid(siNumber: Int): Boolean {
        return siNumber in SI_MIN_NUMBER..SI_MAX_NUMBER
    }

    fun isSICodeValid(siCode: Int): Boolean {
        return siCode in SI_MIN_CODE..SI_MAX_CODE
    }
}
