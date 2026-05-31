package org.openardf.radioomanager.shared.importing

import org.openardf.radioomanager.shared.domain.SIRecordType

/** Shared low-level validation helpers for import and project-file data. */
object ImportValidationRules {
    /** Returns category names that occur more than once. */
    fun duplicateCategoryNames(names: List<String>): Set<String> = duplicateValues(names)

    /** Returns alias names that occur more than once. */
    fun duplicateAliasNames(names: List<String>): Set<String> = duplicateValues(names)

    /** Returns alias SI codes that occur more than once. */
    fun duplicateAliasCodes(codes: List<Int>): Set<Int> = duplicateValues(codes)

    /** Returns start numbers that occur more than once. */
    fun duplicateStartNumbers(startNumbers: List<Int>): Set<Int> = duplicateValues(startNumbers)

    /** Returns non-null SI numbers that occur more than once. */
    fun duplicateSINumbers(siNumbers: List<Int?>): Set<Int> = duplicateValues(siNumbers.filterNotNull())

    /** Detects unsupported readout punch combinations, such as multiple starts or finishes. */
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

/** Machine-readable readout punch validation failure. */
enum class ReadoutPunchValidationError {
    MULTIPLE_START,
    MULTIPLE_FINISH
}
