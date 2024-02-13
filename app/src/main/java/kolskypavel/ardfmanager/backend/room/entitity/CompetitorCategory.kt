package kolskypavel.ardfmanager.backend.room.entitity

import androidx.room.Embedded
import androidx.room.Relation

data class CompetitorCategory(
    @Embedded var competitor: Competitor,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    var category: Category?
)
