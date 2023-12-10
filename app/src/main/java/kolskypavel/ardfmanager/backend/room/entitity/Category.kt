package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.EventType
import java.io.Serializable
import java.util.UUID

@Entity(
    tableName = "category", indices = [Index(
        value = ["name"],
        unique = true
    )]
)
data class Category(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "age_based") var ageBased: Boolean,
    @ColumnInfo(name = "min_year") var minYear: Int,
    @ColumnInfo(name = "max_year") var maxYear: Int,
    @ColumnInfo(name = "event_type") var eventType: EventType,
    @ColumnInfo(name = "limit") var limit: Int,
    var siCodes: String,
    var length: Float,
    var climb: Float
) : Serializable
