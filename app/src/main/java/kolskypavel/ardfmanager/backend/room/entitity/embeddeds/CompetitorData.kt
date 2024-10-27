package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable

/**
Used to get data for the competitor table + results
 */
data class CompetitorData(
    @Embedded var competitorCategory: CompetitorCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "competitor_id",
        entity = Result::class
    )
    var resultData: ResultData?,
) : Serializable
