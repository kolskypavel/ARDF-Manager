package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.EvaluationStatus
import java.io.Serializable
import java.time.LocalTime
import java.util.UUID

@Entity
data class Readout(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "si_number") var siNumber: Int,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorID: UUID,
    @ColumnInfo(name = "eval_status") var evaluationStatus: EvaluationStatus,
    @ColumnInfo(name = "num_cp") var numOfControlPoints: Int,
    @ColumnInfo(name = "run_time") var runTime: LocalTime
) : Serializable