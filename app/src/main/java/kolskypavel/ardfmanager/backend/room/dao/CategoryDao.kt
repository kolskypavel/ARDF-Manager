package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Room access methods for categories and their related course data. */
@Dao
interface CategoryDao {

    /** Observes category aggregates for a race in display order. */
    @Transaction
    @Query("SELECT * FROM category WHERE race_id=(:raceId) ORDER BY `order`")
    fun getCategoryFlowForRace(raceId: UUID): Flow<List<CategoryData>>

    /** Returns category entities for a race in display order. */
    @Query("SELECT * FROM category WHERE race_id=(:raceId) ORDER BY `order`")
    suspend fun getCategoriesForRace(raceId: UUID): List<Category>

    /** Returns one category entity by primary key, or null when absent. */
    @Query("SELECT * FROM category WHERE id=(:id) LIMIT 1")
    suspend fun getCategory(id: UUID): Category?

    /** Returns one category aggregate by primary key, or null when absent. */
    @Query("SELECT * FROM category WHERE id=(:id) LIMIT 1")
    suspend fun getCategoryData(id: UUID): CategoryData?

    /** Returns all category aggregates for a race. */
    @Query("SELECT * FROM category WHERE  race_id=(:raceId) ")
    suspend fun getCategoryDataForRace(raceId: UUID): List<CategoryData>

    /** Returns the highest display-order value currently used in a race. */
    @Query("SELECT `order` FROM category WHERE race_id =(:raceId) ORDER BY `order` DESC LIMIT 1")
    suspend fun getHighestCategoryOrder(raceId: UUID): Int

    /** Finds a category by name within one race. */
    @Query("SELECT * FROM category WHERE name=(:name) AND race_id = (:raceId) LIMIT 1")
    suspend fun getCategoryByName(name: String, raceId: UUID): Category?

    /** Inserts a new category or updates the existing row with the same key. */
    @Upsert
    suspend fun createOrUpdateCategory(category: Category)

    /** Deletes one category by primary key. */
    @Query("DELETE FROM category WHERE id=(:id) ")
    suspend fun deleteCategory(id: UUID)
}
