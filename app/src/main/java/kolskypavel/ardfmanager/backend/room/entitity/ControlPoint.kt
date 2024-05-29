package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * Control point entity, used to define categories
 */
@Entity(
    tableName = "control_point",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("category_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class ControlPoint(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "category_id") var categoryId: UUID,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "round") var round: Int,
    @ColumnInfo(name = "points") var points: Int,
    @ColumnInfo(name = "beacon") var beacon: Boolean,
    @ColumnInfo(name = "separator") var separator: Boolean   //Separates rounds - S control){}
) : Serializable
