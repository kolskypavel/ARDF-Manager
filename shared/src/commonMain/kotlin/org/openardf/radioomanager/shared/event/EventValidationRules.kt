package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.importing.ImportValidationRules
import org.openardf.radioomanager.shared.importing.ReadoutPunchValidationError

/** Shared validation service for complete event aggregates. */
object EventValidationRules {
    /** Returns all currently supported validation issues without localizing messages. */
    fun validateRaceData(raceData: EventRaceData): List<EventValidationIssue> {
        val issues = mutableListOf<EventValidationIssue>()

        if (raceData.race.name.isEmpty()) {
            issues.add(EventValidationIssue.BlankRaceName)
        }

        ImportValidationRules.duplicateCategoryNames(
            raceData.categories.map { it.category.name }
        ).takeIf { it.isNotEmpty() }?.let { duplicateNames ->
            issues.add(EventValidationIssue.DuplicateCategoryNames(duplicateNames))
        }

        ImportValidationRules.duplicateAliasNames(
            raceData.aliases.map { it.name }
        ).takeIf { it.isNotEmpty() }?.let { duplicateNames ->
            issues.add(EventValidationIssue.DuplicateAliasNames(duplicateNames))
        }

        ImportValidationRules.duplicateAliasCodes(
            raceData.aliases.map { it.siCode }
        ).takeIf { it.isNotEmpty() }?.let { duplicateCodes ->
            issues.add(EventValidationIssue.DuplicateAliasCodes(duplicateCodes))
        }

        validateCompetitors(raceData.competitorData).takeIf { it.isNotEmpty() }?.let(issues::addAll)
        validateReadouts(raceData.competitorData.mapNotNull { it.readoutData }, issues)
        validateReadouts(raceData.unmatchedReadoutData, issues)

        return issues
    }

    private fun validateCompetitors(competitors: List<EventCompetitorData>): List<EventValidationIssue> {
        val eventCompetitors = competitors.map { it.competitorCategory.competitor }
        return buildList {
            ImportValidationRules.duplicateStartNumbers(
                eventCompetitors.map { it.startNumber }
            ).takeIf { it.isNotEmpty() }?.let { add(EventValidationIssue.DuplicateStartNumbers(it)) }

            ImportValidationRules.duplicateSINumbers(
                eventCompetitors.map { it.siNumber }
            ).takeIf { it.isNotEmpty() }?.let { add(EventValidationIssue.DuplicateSINumbers(it)) }
        }
    }

    private fun validateReadouts(
        readouts: List<EventReadoutData>,
        issues: MutableList<EventValidationIssue>
    ) {
        readouts.forEach { readout ->
            val punchErrors = ImportValidationRules.validateReadoutPunchTypes(
                readout.punches.map { it.punch.punchType }
            )
            if (punchErrors.contains(ReadoutPunchValidationError.MULTIPLE_START)) {
                issues.add(EventValidationIssue.MultipleStartPunches(readout.result.siNumber))
            }
            if (punchErrors.contains(ReadoutPunchValidationError.MULTIPLE_FINISH)) {
                issues.add(EventValidationIssue.MultipleFinishPunches(readout.result.siNumber))
            }
        }
    }
}

/** Machine-readable event validation issue used by Android and future desktop UI layers. */
sealed interface EventValidationIssue {
    data object BlankRaceName : EventValidationIssue
    data class DuplicateCategoryNames(val names: Set<String>) : EventValidationIssue
    data class DuplicateAliasNames(val names: Set<String>) : EventValidationIssue
    data class DuplicateAliasCodes(val codes: Set<Int>) : EventValidationIssue
    data class DuplicateStartNumbers(val startNumbers: Set<Int>) : EventValidationIssue
    data class DuplicateSINumbers(val siNumbers: Set<Int>) : EventValidationIssue
    data class MultipleStartPunches(val siNumber: Int?) : EventValidationIssue
    data class MultipleFinishPunches(val siNumber: Int?) : EventValidationIssue
}
