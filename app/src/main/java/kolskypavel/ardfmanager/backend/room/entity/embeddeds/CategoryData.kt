package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint

/** Room aggregate for a category with its control points and assigned competitors. */
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
)
