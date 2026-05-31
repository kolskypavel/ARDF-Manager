package org.openardf.radioomanager.shared.files

import org.openardf.radioomanager.shared.event.EventCategory
import org.openardf.radioomanager.shared.event.EventCompetitor

data class TimedPunchCsvField(
    val siCode: Int,
    val timeText: String
)

object EventCsvRows {
    fun categoryRow(category: EventCategory): String {
        return "${category.name};${category.isMan.compareTo(false)};${category.maxAge ?: 0};" +
                "${category.lengthMeters};${category.climbMeters};${category.order};" +
                "${category.raceType?.value ?: ""};${category.timeLimitSeconds?.div(60) ?: ""}}"
    }

    fun competitorRow(competitor: EventCompetitor, categoryName: String): String {
        return "${competitor.siNumber ?: ""};${competitor.firstName};${competitor.lastName};" +
                "$categoryName;${competitor.isMan.compareTo(false)};${competitor.birthYear};;" +
                "${competitor.club};;${competitor.startNumber};${competitor.index}"
    }

    fun punchRow(cardNumber: Int?, siCode: Int, timeText: String): String {
        return "${cardNumber ?: ""};$siCode;$timeText"
    }

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
