package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kolskypavel.ardfmanager.backend.room.database.DateTimeTypeConverter
import kolskypavel.ardfmanager.backend.room.enums.EventBand
import kolskypavel.ardfmanager.backend.room.enums.EventLevel
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.FinishTimeSource
import kolskypavel.ardfmanager.backend.room.enums.StartTimeSource
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "event"
)
@TypeConverters(DateTimeTypeConverter::class)
data class Event(
    @PrimaryKey var id: UUID,
    var name: String,
    @ColumnInfo(name = "external_id") var externalId: Long?,
    @ColumnInfo(name = "start_date_time") var startDateTime: LocalDateTime,
    @ColumnInfo(name = "event_type") var eventType: EventType,
    @ColumnInfo(name = "event_level") var eventLevel: EventLevel,
    @ColumnInfo(name = "event_band") var eventBand: EventBand,
    @ColumnInfo(name = "time_limit") var timeLimit: Duration,
    @ColumnInfo(name = "start_source") var startTimeSource: StartTimeSource,
    @ColumnInfo(name = "finish_source") var finishTimeSource: FinishTimeSource
) : Serializable
