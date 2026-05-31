package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Race

/** Complete Room aggregate for one race, including categories, aliases, competitors, and readouts. */
data class RaceData(
    var race: Race,
    val categories: List<CategoryData>,
    val aliases: List<Alias>,
    val competitorData: List<CompetitorData>,
    val unmatchedReadoutData: List<ReadoutData>
)
