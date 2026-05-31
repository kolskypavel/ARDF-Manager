package org.openardf.radioomanager.shared.sportident

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SportIdentCodesTest {
    @Test
    fun validatesSportIdentNumberRange() {
        assertFalse(SportIdentCodes.isSINumberValid(999))
        assertTrue(SportIdentCodes.isSINumberValid(1000))
        assertTrue(SportIdentCodes.isSINumberValid(9999999))
        assertFalse(SportIdentCodes.isSINumberValid(10000000))
    }

    @Test
    fun validatesControlCodeRange() {
        assertFalse(SportIdentCodes.isSICodeValid(0))
        assertTrue(SportIdentCodes.isSICodeValid(1))
        assertTrue(SportIdentCodes.isSICodeValid(255))
        assertFalse(SportIdentCodes.isSICodeValid(256))
    }
}
