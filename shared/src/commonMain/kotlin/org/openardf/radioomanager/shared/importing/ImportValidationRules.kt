package org.openardf.radioomanager.shared.importing

import org.openardf.radioomanager.shared.domain.SIRecordType

object ImportValidationRules {
    fun duplicateCategoryNames(names: List<String>): Set<String> = duplicateValues(names)

    fun duplicateAliasNames(names: List<String>): Set<String> = duplicateValues(names)

    fun duplicateAliasCodes(codes: List<Int>): Set<Int> = duplicateValues(codes)

    fun duplicateStartNumbers(startNumbers: List<Int>): Set<Int> = duplicateValues(startNumbers)

    fun duplicateSINumbers(siNumbers: List<Int?>): Set<Int> = duplicateValues(siNumbers.filterNotNull())

    fun validateReadoutPunchTypes(punchTypes: List<SIRecordType>): Set<ReadoutPunchValidationError> {
        val errors = LinkedHashSet<ReadoutPunchValidationError>()
        if (punchTypes.count { it == SIRecordType.START } > 1) {
            errors.add(ReadoutPunchValidationError.MULTIPLE_START)
        }
        if (punchTypes.count { it == SIRecordType.FINISH } > 1) {
            errors.add(ReadoutPunchValidationError.MULTIPLE_FINISH)
        }
        return errors
    }

    private fun <T> duplicateValues(values: List<T>): Set<T> {
        return values.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
    }
}

enum class ReadoutPunchValidationError {
    MULTIPLE_START,
    MULTIPLE_FINISH
}
