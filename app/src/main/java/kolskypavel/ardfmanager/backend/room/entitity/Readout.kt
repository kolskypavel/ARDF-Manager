package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.EvaluationStatus
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
    @ColumnInfo(name = "eval_status") var evaluationStatus: EvaluationStatus,
    @ColumnInfo(name = "points") var points: Int,
    @ColumnInfo(name = "run_time") var startTime: LocalDateTime,
    @ColumnInfo(name = "run_time") var finishTime: LocalDateTime,
    @ColumnInfo(name = "run_time") var runTime: Duration
) : Serializable