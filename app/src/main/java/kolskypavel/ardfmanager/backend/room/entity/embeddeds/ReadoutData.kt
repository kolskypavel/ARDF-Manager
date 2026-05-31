package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Result

/** Room aggregate for a result and the punches recorded during that readout. */
data class ReadoutData(
    @Embedded var result: Result,

    @Relation(
        entityColumn = "result_id",
        parentColumn = "id",
        entity = Punch::class
    )
    var punches: List<AliasPunch>,
)
