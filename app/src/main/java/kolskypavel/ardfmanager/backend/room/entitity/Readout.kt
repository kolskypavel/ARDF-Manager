package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class Readout(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "si_number") var siNumber: Int,
    @ColumnInfo(name = "card_type") var cardType: Byte,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorID: UUID?,
    @ColumnInfo(name = "check_time") var checkTime: LocalDateTime,
    @ColumnInfo(name = "start_time") var startTime: LocalDateTime,
    @ColumnInfo(name = "finish_time") var finishTime: LocalDateTime,
    @ColumnInfo(name = "run_time") var runTime: Duration,
    @ColumnInfo(name = "readout_time") var readoutTime: LocalDateTime = LocalDateTime.now()
) : Serializable