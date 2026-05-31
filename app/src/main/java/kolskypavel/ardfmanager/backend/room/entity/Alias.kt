package kolskypavel.ardfmanager.backend.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/** User-defined display name for a control-point SI code within one race. */
@Entity(
    tableName = "alias", indices = [Index(
        value = ["name", "race_id", "si_code"],
        unique = true
    )], foreignKeys = [ForeignKey(
        entity = Race::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("race_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Alias(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "name") var name: String
) : Serializable {

    /** Convenience constructor used by tests and edit-list setup. */
    constructor(code: Int, name: String) : this(
        id = UUID.randomUUID(),
        raceId = UUID.randomUUID(),
        siCode = code,
        name = name
    )
}
