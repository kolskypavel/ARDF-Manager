package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.time.Duration

/**
 * Wrapper class for displaying punches in recycler views
 */
data class PunchWrapper(
    var cardNumber: Int? = null,
    var siCode: Int?,
    var order: Int? = null,
    var siTime: SITime,
    var punchStatus: PunchStatus,
    var split: Duration? = null,
    var siRecordType: SIRecordType
)