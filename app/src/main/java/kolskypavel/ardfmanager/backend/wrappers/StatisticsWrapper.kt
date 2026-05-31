package kolskypavel.ardfmanager.backend.wrappers

/** Summary counts shown in race statistics. */
data class StatisticsWrapper(
    var competitors: Int,
    var startedCompetitors: Int,
    var inLimitCompetitors: Int,
    var finishedCompetitors: Int

)
