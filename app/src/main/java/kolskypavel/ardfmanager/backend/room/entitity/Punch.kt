package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.io.Serializable
import java.time.Duration
import java.util.UUID

@Entity(tableName = "punch")
data class Punch(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "readout_id") var readoutId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorId: UUID? = null,
    @ColumnInfo(name = "card_number") var cardNumber: Int? = null,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "si_time") var siTime: SITime,
    @ColumnInfo(name = "punch_status") var punchStatus: PunchStatus,
    var split: Duration? = null
) : Serializable
