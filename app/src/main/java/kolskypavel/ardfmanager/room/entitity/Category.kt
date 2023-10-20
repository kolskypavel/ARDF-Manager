package kolskypavel.ardfmanager.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.UUID

@Entity(tableName = "category")
data class Category(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "event_id") var eventId: UUID,
    var name: String,
    @ColumnInfo(name = "min_year") var minYear: Int,
    @ColumnInfo(name = "max_year") var maxYear: Int
)
