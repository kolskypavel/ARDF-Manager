package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.io.Serializable
import java.time.Duration
import java.util.UUID

@Entity(
    tableName = "punch", foreignKeys = [ForeignKey(
        entity = Readout::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("readout_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Punch(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "readout_id") var readoutId: UUID?,
    @ColumnInfo(name = "competitor_id") var competitorId: UUID? = null,
    @ColumnInfo(name = "card_number") var cardNumber: Int? = null,
    @ColumnInfo(name = "punch_type") var punchType: SIRecordType,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "si_time") var siTime: SITime,
    @ColumnInfo(name = "orig_si_time") val origSiTime: SITime?,      //Holds the original SI Time in case a punch was modified
    @ColumnInfo(name = "punch_status") var punchStatus: PunchStatus,
    var split: Duration? = null
) : Serializable
