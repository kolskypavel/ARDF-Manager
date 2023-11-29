package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import java.io.Serializable
import java.util.UUID

@Entity(tableName = "punch")
data class Punch(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "competitor_id") var competitorId: UUID?,
    @ColumnInfo(name = "card_number") var cardNumber: Int?,
    @ColumnInfo(name = "si_code") var siCode: Int?,
    var time: Long?,
    var punchStatus: PunchStatus
) : Serializable
