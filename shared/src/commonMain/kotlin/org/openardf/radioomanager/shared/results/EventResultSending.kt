package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.event.EventCompetitorData

/** Shared result-service policy helpers that do not perform network or persistence work. */
object EventResultSending {
    /** Returns competitor ids for readouts that exist and have not yet been marked sent. */
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
