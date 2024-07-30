package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import java.io.Serializable

data class CompetitorCategory(
    @Embedded var competitor: Competitor,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id",
        entity = Category::class
    ) var category: Category?
) : Serializable