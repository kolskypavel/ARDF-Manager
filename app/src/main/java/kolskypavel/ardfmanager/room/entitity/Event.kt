package kolskypavel.ardfmanager.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "event")
data class Event(
    @PrimaryKey var id: UUID,
    var name: String,
    var level: Int,
    @ColumnInfo(name = "start_time") var startTime: LocalDateTime,
    @ColumnInfo(name = "event_type") var eventType: EventType
)
