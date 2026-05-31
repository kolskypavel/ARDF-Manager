package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entity.Result
import java.io.Serializable

/** Room aggregate used by competitor tables and result workflows. */
data class CompetitorData(
    @Embedded var competitorCategory: CompetitorCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "competitor_id",
        entity = Result::class
    )
    var readoutData: ReadoutData?,
) : Serializable
