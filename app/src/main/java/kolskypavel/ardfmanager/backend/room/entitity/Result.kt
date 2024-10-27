package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "result", foreignKeys = [ForeignKey(
        entity = Race::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("race_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Result(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorID: UUID? = null,
    @ColumnInfo(name = "category_id") var categoryId: UUID? = null,
    @ColumnInfo(name = "si_number") var siNumber: Int?,
    @ColumnInfo(name = "card_type") var cardType: Byte,
    @ColumnInfo(name = "check_time") var checkTime: SITime?,
    @ColumnInfo(name = "orig_check_time") var origCheckTime: SITime?, // Immutable copy of original SI Time, used mainly for SI 5 cards
    @ColumnInfo(name = "start_time") var startTime: SITime?,
    @ColumnInfo(name = "orig_start_time") var origStartTime: SITime?, // Immutable copy of original SI Time, used mainly for SI 5 cards
    @ColumnInfo(name = "finish_time") var finishTime: SITime?,
    @ColumnInfo(name = "orig_finish_time") var origFinishTime: SITime?, // Immutable copy of original SI Time, used mainly for SI 5 cards
    @ColumnInfo(name = "readout_time") var readoutTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "automatic_status") var automaticStatus: Boolean,
    @ColumnInfo(name = "race_status") var raceStatus: RaceStatus,
    @ColumnInfo(name = "points") var points: Int = 0,
    @ColumnInfo(name = "run_time") var runTime: Duration,
    @ColumnInfo(name = "modified") var modified: Boolean
) : Serializable, Comparable<Result> {
    @Ignore
    var place: Int? = null
    override operator fun compareTo(other: Result): Int {

        //Compare race status
        return if (raceStatus != other.raceStatus) {
            raceStatus.compareTo(other.raceStatus)
        }
        //Compare points - more points are before less points
        else if (points != other.points) {
            points.compareTo(other.points) * -1
        }
        //Compare times
        else {
            runTime.compareTo(other.runTime)
        }
    }
}