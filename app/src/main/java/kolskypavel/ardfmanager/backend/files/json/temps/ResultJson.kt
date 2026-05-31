package kolskypavel.ardfmanager.backend.files.json.temps

import java.time.LocalDateTime

/** JSON DTO for final result exports grouped with categories and aliases. */
data class FinalResultsJson(
    val categories: List<CategoryJson>,
    val aliases: List<AliasJson>,
    val competitors: List<CompetitorJson>,
)

/** JSON DTO for one competitor result in live-result exports. */
data class ResultCompetitorJson(
    val competitor_index: String?,
    val si_number: Int?,
    val last_name: String,
    val first_name: String,
    val competitor_category: String,
    val result: ResultJson
)

/** JSON DTO for one result/readout payload. */
data class ResultJson(
    val check_time: LocalDateTime?,
    val start_time: LocalDateTime?,
    val finish_time: LocalDateTime?,
    val run_time: String?,
    val place: Int?,
    val readoutTime: LocalDateTime?,
    val modified: Boolean?,
    val punch_count: Int?,
    val result_status: String,
    val automatic_status: Boolean?,
    val punches: List<PunchJson>
)

/** JSON DTO for one punch in a result payload. */
data class PunchJson(
    var code: String,
    var si_code: Int?,
    val control_type: String,
    val punch_status: String,
    val split_time: String
)

/** JSON DTO for a readout that is not matched to a competitor. */
data class UnmatchedResultJson(
    val si_number: Int?,
    val check_time: LocalDateTime?,
    val start_time: LocalDateTime,
    val finish_time: LocalDateTime,
    val run_time: String,
    val punches: List<PunchJson>
)
