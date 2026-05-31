package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.event.EventCompetitorData
import org.openardf.radioomanager.shared.event.EventReadoutData
import org.openardf.radioomanager.shared.event.EventResult

object EventResultPlacement {
    fun sortByPlace(competitors: List<EventCompetitorData>): List<EventCompetitorData> {
        val sorted = competitors.sortedWith(::compareCompetitorData)
        var place = 0

        return sorted.mapIndexed { index, competitorData ->
            val current = competitorData.readoutData
            if (current == null) {
                competitorData
            } else {
                val previous = sorted.getOrNull(index - 1)?.readoutData
                place = if (previous != null &&
                    current.result.runTimeSeconds == previous.result.runTimeSeconds &&
                    current.result.points == previous.result.points
                ) {
                    place
                } else {
                    place + 1
                }
                competitorData.withResultPlace(place)
            }
        }
    }

    fun groupByCategoryAndSortByPlace(
        competitors: List<EventCompetitorData>
    ): Map<String?, List<EventCompetitorData>> {
        return competitors
            .groupBy { it.competitorCategory.category?.id }
            .mapValues { (_, categoryCompetitors) -> sortByPlace(categoryCompetitors) }
    }

    private fun compareCompetitorData(first: EventCompetitorData, second: EventCompetitorData): Int {
        val firstReadout = first.readoutData
        val secondReadout = second.readoutData

        return when {
            firstReadout == null && secondReadout == null -> 0
            firstReadout == null -> 1
            secondReadout == null -> -1
            else -> firstReadout.result.compareRanking(secondReadout.result)
        }
    }

    private fun EventResult.compareRanking(other: EventResult): Int {
        return when {
            resultStatus != other.resultStatus -> resultStatus.compareTo(other.resultStatus)
            points != other.points -> other.points.compareTo(points)
            else -> runTimeSeconds.compareTo(other.runTimeSeconds)
        }
    }

    private fun EventCompetitorData.withResultPlace(place: Int): EventCompetitorData {
        val readoutData = readoutData ?: return this
        return copy(readoutData = readoutData.withPlace(place))
    }

    private fun EventReadoutData.withPlace(place: Int): EventReadoutData =
        copy(result = result.copy(place = place))
}
