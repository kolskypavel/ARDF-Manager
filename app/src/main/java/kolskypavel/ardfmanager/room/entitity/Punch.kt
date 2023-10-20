package kolskypavel.ardfmanager.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "punch")
data class Punch(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "si_number") var SINumber: Int,
    var time: LocalDateTime,
)
