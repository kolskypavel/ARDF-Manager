package kolskypavel.ardfmanager.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.UUID

/**
 * Control point entity, used to define categories
 */
@Entity(tableName = "control_point")
data class ControlPoint(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "category_id") var categoryId: UUID,
    @ColumnInfo(name = "si_number") var SINumber: Int,
    var order: Int,
    var round: Int,
    var points: Int,
    var beacon: Boolean
)
