package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable


data class ReadoutResult(
    @Embedded var readout: Readout,
    @Relation(
        entityColumn = "readout_id",
        parentColumn = "id"
    ) var result: Result,

    @Relation(
        entityColumn = "readout_id",
        parentColumn = "id",
        entity = Punch::class
    )
    var punches: List<AliasPunch>
) : Serializable {
    fun getPunchList(): List<Punch> {
        return punches.map { p -> p.punch }
    }
}
