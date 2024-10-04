package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable



data class ReadoutResult(
    @Embedded var readout: Readout,
    // @Relation(
    //     entityColumn = "punch::readout_id",
    //     parentColumn = "id",
    //     entity = AliasPunch::class
    // ) var punches : List<AliasPunch>,
    @Relation(
        entityColumn = "readout_id",
        parentColumn = "id"
    ) var result: Result
) : Serializable
