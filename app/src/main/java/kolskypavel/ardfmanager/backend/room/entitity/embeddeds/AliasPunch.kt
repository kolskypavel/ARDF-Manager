package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import java.io.Serializable

data class AliasPunch(
    @Embedded var punch: Punch,
    @Relation(
        parentColumn = "si_code",
        entityColumn = "si_code",
    )
    var alias: Alias?,
) : Serializable