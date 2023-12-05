package kolskypavel.ardfmanager.backend.wrappers

import kolskypavel.ardfmanager.backend.room.enums.RecordType
import kolskypavel.ardfmanager.backend.sportident.SITime

data class PunchRecordWrapper(
    var siCode: Int?,
    var siTime: SITime?,
    var recordType: RecordType
)