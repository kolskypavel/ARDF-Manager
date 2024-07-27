package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE race_id=(:raceId) ORDER BY `order`")
    fun getCategoryFlowForRace(raceId: UUID): Flow<List<CategoryData>>

    @Query("SELECT * FROM category WHERE race_id=(:raceId) ORDER BY `order`")
    fun getCategoriesForRace(raceId: UUID): List<Category>

    @Query("SELECT * FROM category WHERE id=(:id) LIMIT 1")
    suspend fun getCategory(id: UUID): Category?

    @Query("SELECT * FROM category WHERE id=(:id) AND race_id=(:raceId) LIMIT 1")
    suspend fun getCategoryData(id: UUID?, raceId: UUID): CategoryData?

    @Query("SELECT * FROM category WHERE  race_id=(:raceId) ")
    suspend fun getCategoryDataForRace(raceId: UUID): List<CategoryData>

    @Query("SELECT `order` FROM category WHERE race_id =(:raceId) ORDER BY `order` DESC LIMIT 1")
    suspend fun getHighestCategoryOrder(raceId: UUID): Int

    @Query("SELECT * FROM category WHERE name=(:name) AND race_id = (:raceId) LIMIT 1")
    suspend fun getCategoryByName(name: String, raceId: UUID): Category?

    @Query("SELECT * FROM category WHERE max_age = (:maxAge) AND race_id = (:raceId) LIMIT 1")
    suspend fun getCategoryByMaxAge(maxAge: Int, raceId: UUID): Category?

    @Query("SELECT * FROM category WHERE race_id=(:raceId) AND is_woman = (:woman) AND (:age) <= max_age ORDER BY max_age ASC LIMIT 1 ")
    suspend fun getCategoryByAge(age: Int, woman: Boolean, raceId: UUID): Category?

    @Upsert
    suspend fun createOrUpdateCategory(category: Category): Long

    @Query("DELETE FROM category WHERE id=(:id) ")
    suspend fun deleteCategory(id: UUID)
}