package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Punch
import java.io.Serializable

data class AliasPunch(
    @Embedded var punch: Punch,
    @Relation(
        parentColumn = "si_code",
        entityColumn = "si_code",
    )
    var alias: Alias?,
) : Serializable