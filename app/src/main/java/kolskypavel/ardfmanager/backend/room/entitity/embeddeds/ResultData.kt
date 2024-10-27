package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.io.Serializable


data class ResultData(
    @Embedded var result: Result,

    @Relation(
        entityColumn = "result_id",
        parentColumn = "id",
        entity = Punch::class
    )
    var punches: List<AliasPunch>,
    @Relation(
        parentColumn = "competitor_id",
        entityColumn = "id",
        entity = Competitor::class
    ) var competitorCategory: CompetitorCategory?
) : Serializable {
    fun getPunchList(): List<Punch> {
        return punches.map { p -> p.punch }
    }
}
