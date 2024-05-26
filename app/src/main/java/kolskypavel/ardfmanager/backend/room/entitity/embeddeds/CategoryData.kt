package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint

data class CategoryData(
    @Embedded var category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id",
        entity = ControlPoint::class
    ) var controlPoints: List<ControlPoint>,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id",
        entity = Competitor::class
    ) var competitors: List<Competitor>
) {
}