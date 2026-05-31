package org.openardf.radioomanager.shared.alias

import org.openardf.radioomanager.shared.sportident.SportIdentCodes

object AliasRules {
    const val MAX_NAME_LENGTH = 6
    const val ALLOWED_NAME_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ/"

    fun validateName(name: String, existingNames: List<String>, position: Int): AliasValidationResult {
        if (name.isEmpty()) {
            return AliasValidationResult.Required
        }
        if (name.length > MAX_NAME_LENGTH || name.any { it !in ALLOWED_NAME_CHARACTERS }) {
            return AliasValidationResult.Invalid
        }
        if (existingNames.withIndex().any { (index, value) -> index != position && value == name }) {
            return AliasValidationResult.Duplicate
        }
        return AliasValidationResult.Valid
    }

    fun validateCode(code: String, existingCodes: List<Int>, position: Int): AliasValidationResult {
        if (code.isEmpty()) {
            return AliasValidationResult.Required
        }

        val codeValue = code.toIntOrNull() ?: return AliasValidationResult.Invalid

        if (!SportIdentCodes.isSICodeValid(codeValue)) {
            return AliasValidationResult.Invalid
        }
        if (existingCodes.withIndex().any { (index, value) -> index != position && value == codeValue }) {
            return AliasValidationResult.Duplicate
        }
        return AliasValidationResult.Valid
    }
}

enum class AliasValidationResult {
    Valid,
    Required,
    Invalid,
    Duplicate
}
