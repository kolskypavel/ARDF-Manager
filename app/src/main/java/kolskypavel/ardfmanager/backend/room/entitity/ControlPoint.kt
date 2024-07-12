package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
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
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "beacon") var type: ControlPointType,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "points") var points: Int = 1
) : Serializable {

    //Format: Code # Name # Type # Order # Points
    fun toCsvString(): String {
        return "${siCode}#${name ?: ""}#${type.value}#${order}#${points}"
    }

    companion object {
        fun getTestControlPoint(): ControlPoint {
            return ControlPoint(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                31,
                "TEST",
                ControlPointType.CONTROL,
                0
            )
        }

        @Throws(java.lang.IllegalArgumentException::class)
        fun parseControlPoint(string: String, raceId: UUID, categoryId: UUID): ControlPoint {
            val split = string.split('#')
            try {
                var points = 1

                //Check if points are present
                if (split.size == 5) {
                    points = split[4].toInt()
                }
                return ControlPoint(
                    UUID.randomUUID(),
                    raceId,
                    categoryId,
                    split[0].toInt(),
                    split[1],
                    ControlPointType.getByValue(split[2].toInt())!!,
                    split[3].toInt(),
                    points
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid control point format")
            }
        }
    }
}