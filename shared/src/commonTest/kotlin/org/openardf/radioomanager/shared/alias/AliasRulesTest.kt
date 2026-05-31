package org.openardf.radioomanager.shared.alias

import kotlin.test.Test
import kotlin.test.assertEquals

class AliasRulesTest {
    @Test
    fun validatesAliasNames() {
        assertEquals(AliasValidationResult.Required, AliasRules.validateName("", emptyList(), 0))
        assertEquals(AliasValidationResult.Valid, AliasRules.validateName("F1", listOf("F1"), 0))
        assertEquals(AliasValidationResult.Valid, AliasRules.validateName("A/B", emptyList(), 0))
        assertEquals(AliasValidationResult.Invalid, AliasRules.validateName("TOOLONG", emptyList(), 0))
        assertEquals(AliasValidationResult.Invalid, AliasRules.validateName("F-1", emptyList(), 0))
        assertEquals(AliasValidationResult.Duplicate, AliasRules.validateName("F1", listOf("F1", "F1"), 1))
    }

    @Test
    fun validatesAliasControlCodes() {
        assertEquals(AliasValidationResult.Required, AliasRules.validateCode("", emptyList(), 0))
        assertEquals(AliasValidationResult.Invalid, AliasRules.validateCode("abc", emptyList(), 0))
        assertEquals(AliasValidationResult.Invalid, AliasRules.validateCode("0", emptyList(), 0))
        assertEquals(AliasValidationResult.Invalid, AliasRules.validateCode("256", emptyList(), 0))
        assertEquals(AliasValidationResult.Valid, AliasRules.validateCode("31", listOf(31), 0))
        assertEquals(AliasValidationResult.Duplicate, AliasRules.validateCode("31", listOf(31, 31), 1))
    }
}
