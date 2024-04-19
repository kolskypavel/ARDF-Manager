package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable

/**
 * Used for collapsable recycler view for result display
 */
data class ReadoutData(
    @Embedded var readout: Readout,
    @Relation(parentColumn = "id", entityColumn = "readout_id") var result: Result?,
    @Relation(
        parentColumn = "competitor_id",
        entityColumn = "id",
        entity = Competitor::class
    ) var competitorCategory: CompetitorCategory?,
) : Serializable
