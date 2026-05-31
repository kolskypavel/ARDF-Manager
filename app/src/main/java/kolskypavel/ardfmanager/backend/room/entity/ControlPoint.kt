package kolskypavel.ardfmanager.backend.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import org.openardf.radioomanager.shared.course.ControlPointDefinition
import org.openardf.radioomanager.shared.course.ControlPointRules
import java.io.Serializable
import java.util.UUID

/** Room entity for one ordered control point in a category course. */
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
    @ColumnInfo(name = "category_id") var categoryId: UUID,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "type") var type: ControlPointType,
    @ColumnInfo(name = "order") var order: Int
) : Serializable {

    /** Formats this single control point as the compact CSV token used by exports. */
    fun toCsvString(): String {
        return ControlPointRules.formatControlPoints(
            listOf(ControlPointDefinition(siCode, type, order))
        )
    }

    /** Default constructor used by tests and Room/tooling support. */
    constructor() : this(
        UUID.randomUUID(),
        UUID.randomUUID(),
        31,
        ControlPointType.CONTROL,
        0
    )

    /** Convenience constructor for tests that only need a control SI code. */
    constructor(siCode: Int) : this(
        UUID.randomUUID(),
        UUID.randomUUID(),
        siCode,
        ControlPointType.CONTROL,
        0
    )
}
