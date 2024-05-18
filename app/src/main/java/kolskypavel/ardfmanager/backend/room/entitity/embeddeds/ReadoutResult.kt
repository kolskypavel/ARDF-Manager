package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result

data class ReadoutResult(
    @Embedded var readout: Readout,
    @Relation(
        entityColumn = "readout_id",
        parentColumn = "id"
    ) var result: Result,
)
