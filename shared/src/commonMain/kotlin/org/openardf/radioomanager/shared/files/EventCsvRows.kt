package org.openardf.radioomanager.shared.files

import org.openardf.radioomanager.shared.event.EventCategory
import org.openardf.radioomanager.shared.event.EventCompetitor

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
}
