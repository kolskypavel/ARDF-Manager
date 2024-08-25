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
        entity = Race::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("race_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Readout(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "si_number") var siNumber: Int?,
    @ColumnInfo(name = "card_type") var cardType: Byte,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorID: UUID? = null,
    @ColumnInfo(name = "check_time") var checkTime: SITime?,
    @ColumnInfo(name = "orig_check_time") var origCheckTime: SITime?, // Immutable copy of original SI Time, used mainly for SI 5 cards
    @ColumnInfo(name = "start_time") var startTime: SITime?,
    @ColumnInfo(name = "orig_start_time") var origStartTime: SITime?, // Immutable copy of original SI Time, used mainly for SI 5 cards
    @ColumnInfo(name = "finish_time") var finishTime: SITime?,
    @ColumnInfo(name = "orig_finish_time") var origFinishTime: SITime?, // Immutable copy of original SI Time, used mainly for SI 5 cards
    @ColumnInfo(name = "readout_time") var readoutTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "modified") var modified: Boolean
) : Serializable