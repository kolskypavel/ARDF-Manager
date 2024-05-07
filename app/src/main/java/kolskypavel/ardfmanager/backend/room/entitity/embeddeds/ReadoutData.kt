package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import java.io.Serializable

/**
 * Used for displaying readout data
 */
data class ReadoutData(
    @Embedded var readoutResult: ReadoutResult,
    @Relation(
        parentColumn = "competitor_id",
        entityColumn = "id",
        entity = Competitor::class
    ) var competitorCategory: CompetitorCategory?,
) : Serializable
