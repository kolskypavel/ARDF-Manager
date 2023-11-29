package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.enums.PunchType
import java.time.LocalTime

data class PunchRecordsWrapper(
    var siCode: Int?,
    var time: LocalTime?,
    var punchType: PunchType
)