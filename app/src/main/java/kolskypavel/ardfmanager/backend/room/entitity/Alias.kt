package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

@Entity(tableName = "alias", foreignKeys = [ForeignKey(
    entity = Race::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("race_id"),
    onDelete = ForeignKey.CASCADE
)])
data class Alias(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "name") var name: String
) : Serializable
