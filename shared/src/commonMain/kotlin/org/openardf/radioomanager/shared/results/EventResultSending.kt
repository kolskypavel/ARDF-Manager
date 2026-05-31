package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.event.EventCompetitorData

object EventResultSending {
    fun unsentCompetitorIds(results: List<EventCompetitorData>): Set<String> {
        return results
            .filter { competitorData ->
                competitorData.readoutData?.result?.sent == false
            }
            .map { competitorData ->
                competitorData.competitorCategory.competitor.id
            }
            .toSet()
    }
}
