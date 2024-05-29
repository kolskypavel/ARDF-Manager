package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.database.DateTimeTypeConverter
import kolskypavel.ardfmanager.backend.room.enums.FinishTimeSource
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.StartTimeSource
import java.io.Serializable
import java.time.Duration
import java.util.UUID

@Entity(
    tableName = "category", indices = [Index(
        value = ["name", "race_id", "order"],
        unique = true
    )],
    foreignKeys = [ForeignKey(
        entity = Race::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("race_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(DateTimeTypeConverter::class)
data class Category(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "is_woman") var isWoman: Boolean,
    @ColumnInfo(name = "max_age") var maxAge: Int?,
    @ColumnInfo(name = "different") var differentProperties: Boolean,
    @ColumnInfo(name = "race_type") var raceType: RaceType?,
    @ColumnInfo(name = "limit") var timeLimit: Duration?,
    @ColumnInfo(name = "start_source") var startTimeSource: StartTimeSource?,
    @ColumnInfo(name = "finish_source") var finishTimeSource: FinishTimeSource?,
    @ColumnInfo(name = "length") var length: Float,
    @ColumnInfo(name = "climb") var climb: Float,
    @ColumnInfo(name = "order") var order: Int
) : Serializable
