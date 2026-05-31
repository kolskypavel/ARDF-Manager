package org.openardf.radioomanager.shared.files

import org.openardf.radioomanager.shared.event.EventCategory
import org.openardf.radioomanager.shared.event.EventCompetitor

/** A control punch rendered as a pair of SI code and already formatted time text. */
data class TimedPunchCsvField(
    val siCode: Int,
    val timeText: String
)

/** Shared CSV row formatter for event import/export formats used by Android and desktop. */
object EventCsvRows {
    /** Formats a category row in the legacy semicolon-delimited category export shape. */
    fun categoryRow(category: EventCategory): String {
        return "${category.name};${category.isMan.compareTo(false)};${category.maxAge ?: 0};" +
                "${category.lengthMeters};${category.climbMeters};${category.order};" +
                "${category.raceType?.value ?: ""};${category.timeLimitSeconds?.div(60) ?: ""}}"
    }

    /** Formats a competitor row in the existing simple competitor CSV export shape. */
    fun competitorRow(competitor: EventCompetitor, categoryName: String): String {
        return "${competitor.siNumber ?: ""};${competitor.firstName};${competitor.lastName};" +
                "$categoryName;${competitor.isMan.compareTo(false)};${competitor.birthYear};;" +
                "${competitor.club};;${competitor.startNumber};${competitor.index}"
    }

    /** Formats a start-list row, using caller-provided absolute start time text when available. */
    fun competitorStartRow(
        competitor: EventCompetitor,
        categoryName: String,
        startTimeText: String?
    ): String {
        return "${competitor.startNumber};${competitor.lastName};${competitor.firstName};" +
                "$categoryName;;${startTimeText ?: ""};${competitor.index};;" +
                "${competitor.club};${competitor.siNumber ?: ""}"
    }

    /** Formats one raw punch row for readout debugging/export. */
    fun punchRow(cardNumber: Int?, siCode: Int, timeText: String): String {
        return "${cardNumber ?: ""};$siCode;$timeText"
    }

    /** Formats one full readout row with header times followed by control code/time pairs. */
    fun readoutRow(
        siNumber: Int?,
        checkTimeText: String?,
        startTimeText: String?,
        finishTimeText: String?,
        controlPunches: List<TimedPunchCsvField>
    ): String {
        val punchFields = controlPunches.joinToString(";") { punch ->
            "${punch.siCode};${punch.timeText}"
        }

        val header = listOf(
            siNumber ?: "",
            checkTimeText ?: "",
            startTimeText ?: "",
            finishTimeText ?: "",
            controlPunches.size
        ).joinToString(";")

        return header + if (punchFields.isNotEmpty()) ";$punchFields" else ""
    }
}
