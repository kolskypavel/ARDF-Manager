package kolskypavel.ardfmanager.backend.files.json.temps

/** JSON DTO for one ROBIS result-service response row. */
data class RobisResultJson(
    var last_name: String,
    var first_name: String,
    var competitor_index: String,
    var si_number: Int,
    var reason: String?
)

/** JSON DTO for a ROBIS result-service response grouped by accepted and rejected rows. */
data class RobisResponseJson(
    var created_entries: List<RobisResultJson>,
    var invalid_data: List<RobisResultJson>,
)
