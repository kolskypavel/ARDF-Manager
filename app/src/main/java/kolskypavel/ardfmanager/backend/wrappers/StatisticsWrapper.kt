package kolskypavel.ardfmanager.backend.wrappers

/** Summary counts shown in race statistics. */
data class StatisticsWrapper(
    /** Total registered competitors. */
    var competitors: Int,
    /** Competitors whose start time has passed. */
    var startedCompetitors: Int,
    /** Started competitors still inside their race time limit. */
    var inLimitCompetitors: Int,
    /** Competitors with a finish/readout. */
    var finishedCompetitors: Int

)
