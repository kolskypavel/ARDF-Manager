package kolskypavel.ardfmanager.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "competitor")
data class Competitor(
    @PrimaryKey var id: UUID,
    var name: String,
    var surname: String,
    var gender: Boolean,
    @ColumnInfo(name = "birth_year") var birthYear: Int,
    @ColumnInfo(name = "si_number") var siNumber: Int,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "start_time") var startTime: LocalDateTime
)