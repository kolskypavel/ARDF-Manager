package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import java.io.Serializable
import java.time.Duration
import java.util.UUID

@Entity(
    tableName = "result", foreignKeys = [ForeignKey(
        entity = Readout::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("readout_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Result(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "readout_id") var readoutId: UUID,
    @ColumnInfo(name = "category_id") var categoryId: UUID?,
    @ColumnInfo(name = "competitor_id") var competitorID: UUID? = null,
    @ColumnInfo(name = "race_status") var raceStatus: RaceStatus,
    @ColumnInfo(name = "points") var points: Int,
    @ColumnInfo(name = "run_time") var runTime: Duration,

    ) : Serializable, Comparable<Result> {
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