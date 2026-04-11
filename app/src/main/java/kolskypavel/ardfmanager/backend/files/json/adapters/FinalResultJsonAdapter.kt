package kolskypavel.ardfmanager.backend.files.json.adapters;

import com.squareup.moshi.ToJson;
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.json.temps.AliasJson
import kolskypavel.ardfmanager.backend.files.json.temps.FinalResultsJson
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData

class FinalResultJsonAdapter(val dataProcessor: DataProcessor) {
    @ToJson
    fun toJson(raceData: RaceData): FinalResultsJson {
        val categoryAdapter = CategoryJsonAdapter(raceData.race.id)
        val competitorAdapter = CompetitorJsonAdapter(raceData.race, dataProcessor)

        return FinalResultsJson(
            categories = raceData.categories.map { cat -> categoryAdapter.toJson(cat) },
            aliases = raceData.aliases.map { al -> AliasJson(al.siCode, al.name) },
            competitors = raceData.competitorData.map { cd -> competitorAdapter.toJson(cd) },
        )
    }
}
