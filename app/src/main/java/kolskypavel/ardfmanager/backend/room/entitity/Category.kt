package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.database.DateTimeTypeConverter
import kolskypavel.ardfmanager.backend.room.enums.EventType
import java.io.Serializable
import java.time.Duration
import java.util.UUID

@Entity(
    tableName = "category", indices = [Index(
        value = ["name", "event_id"],
        unique = true
    )],
    foreignKeys = [ForeignKey(
        entity = Event::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("event_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(DateTimeTypeConverter::class)
data class Category(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "age_based") var ageBased: Boolean,
    @ColumnInfo(name = "min_year") var minYear: Int,
    @ColumnInfo(name = "max_year") var maxYear: Int,
    @ColumnInfo(name = "different") var differentProperties: Boolean,
    @ColumnInfo(name = "event_type") var eventType: EventType,
    @ColumnInfo(name = "limit") var timeLimit: Duration,
    @ColumnInfo(name = "cp_names") var controlPointsNames: String,
    @ColumnInfo(name = "cp_codes") var controlPointsCodes: String,
    @ColumnInfo(name = "length") var length: Float,
    @ColumnInfo(name = "climb") var climb: Float,
    @ColumnInfo(name = "order") var order: Int
) : Serializable
