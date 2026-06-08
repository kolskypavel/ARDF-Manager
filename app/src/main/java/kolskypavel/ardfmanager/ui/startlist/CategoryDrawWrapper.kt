package kolskypavel.ardfmanager.ui.startlist

import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import java.time.Duration

// Simple category model used by the adapter
data class CategoryDrawWrapper(
    val catData: CategoryData,
    val color: Int = 0xFF00FF00.toInt(),
    var order: Int = 0,
    var startPoint: Duration = Duration.ZERO
) {
    fun getCompetitorCount() = catData.competitors.size
    fun getCategoryId() = catData.category.id
    fun getCategoryName() = catData.category.name
}
