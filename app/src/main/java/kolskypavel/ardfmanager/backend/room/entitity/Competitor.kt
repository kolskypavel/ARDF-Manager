package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "competitor")
data class Competitor(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "category_id") var categoryId: UUID?,
    var name: String,
    var club: String,
    var index: String,
    var isWoman: Boolean = false,
    @ColumnInfo(name = "birth_year") var birthYear: Int,
    @ColumnInfo(name = "si_number") var siNumber: Int,
    @ColumnInfo(name = "si_rent") var siRent: Boolean = false,
    @ColumnInfo(name = "automatic_category") var automaticCategory: Boolean,
    @ColumnInfo(name = "start_time") var startTime: LocalDateTime
) : Serializable