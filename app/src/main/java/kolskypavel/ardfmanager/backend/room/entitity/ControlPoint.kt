package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Control point entity, used to define categories
 */
@Entity(tableName = "control_point")
data class ControlPoint(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "category_id") var categoryId: UUID,
    var siNumber: Int,
    var order: Int,
    var round: Int,
    var points: Int,
    var beacon: Boolean,
    var separator: Boolean   //Separates rounds - S control
) : Serializable
