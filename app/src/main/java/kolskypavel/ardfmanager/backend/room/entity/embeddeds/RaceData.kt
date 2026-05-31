package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Race

/** Complete Room aggregate for one race, including categories, aliases, competitors, and readouts. */
data class RaceData(
    /** Race/event root entity. */
    var race: Race,
    /** Categories with their courses and assigned competitors. */
    val categories: List<CategoryData>,
    /** Control-point aliases scoped to the race. */
    val aliases: List<Alias>,
    /** Competitors with optional categories and readouts. */
    val competitorData: List<CompetitorData>,
    /** Readouts that are not matched to competitors. */
    val unmatchedReadoutData: List<ReadoutData>
)
