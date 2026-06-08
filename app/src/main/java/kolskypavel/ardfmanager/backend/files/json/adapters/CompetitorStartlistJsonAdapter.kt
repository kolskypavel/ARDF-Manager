package kolskypavel.ardfmanager.backend.files.json.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kolskypavel.ardfmanager.backend.files.json.temps.CompetitorStartlistJson
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData

class CompetitorStartlistJsonAdapter {
    @ToJson
    fun toJson(competitorData: CompetitorData): CompetitorStartlistJson {
        val competitor = competitorData.competitorCategory.competitor
        val category = competitorData.competitorCategory.category
        return CompetitorStartlistJson(
            first_name = competitor.firstName,
            last_name = competitor.lastName,
            competitor_category = category?.name ?: "",
            competitor_index = competitor.index,
            si_number = competitor.siNumber,
            start_number = competitor.startNumber,
            competitor_start_time = competitor.drawnRelativeStartTime?.let {
                TimeProcessor.durationToFormattedString(it, true)
            } ?: ""
        )
    }

    // Dummy implementation - won't be used
    @FromJson
    fun fromJson(json: CompetitorStartlistJson): CompetitorData {
        return CompetitorData(CompetitorCategory(Competitor(), null), null)
    }
}
