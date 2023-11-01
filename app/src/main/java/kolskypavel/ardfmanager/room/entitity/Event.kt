package kolskypavel.ardfmanager.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "event")
data class Event(
    @PrimaryKey var id: UUID,
    var name: String,
    @ColumnInfo(name = "date") var date: LocalDate,
    @ColumnInfo(name = "start_time") var startTime: LocalTime,
    @ColumnInfo(name = "event_type") var eventType: EventType,
    @ColumnInfo(name = "event_level") var level: EventLevel,
    @ColumnInfo(name = "event_band") var eventBand: EventBand
)
