package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "readout", foreignKeys = [ForeignKey(
        entity = Event::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("event_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Readout(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "si_number") var siNumber: Int?,
    @ColumnInfo(name = "card_type") var cardType: Byte,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorID: UUID? = null,
    @ColumnInfo(name = "check_time") var checkTime: SITime?,
    @ColumnInfo(name = "start_time") var startTime: SITime?,
    @ColumnInfo(name = "finish_time") var finishTime: SITime?,
    @ColumnInfo(name = "readout_time") var readoutTime: LocalDateTime = LocalDateTime.now(),
) : Serializable {}