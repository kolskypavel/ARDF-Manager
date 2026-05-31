package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Punch
import java.io.Serializable

/** Room relation aggregate for a punch and its optional display alias. */
data class AliasPunch(
    @Embedded var punch: Punch,
    @Relation(
        parentColumn = "si_code",
        entityColumn = "si_code",
    )
    var alias: Alias?,
) : Serializable {

    /** Default constructor used by debug views and tooling that require defaults. */
    constructor() : this(
        Punch(),
        null
    )

    /** Convenience constructor for an unaliased punch. */
    constructor(punch: Punch) : this(
        punch = punch,
        alias = null
    )
}
