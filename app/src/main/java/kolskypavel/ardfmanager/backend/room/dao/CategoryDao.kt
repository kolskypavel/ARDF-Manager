package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE event_id=(:eventId) ORDER BY `order`")
    fun getCategoriesFlowForEvent(eventId: UUID): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE event_id=(:eventId) ORDER BY `order`")
    fun getCategoriesForEvent(eventId: UUID): List<Category>

    @Query("SELECT * FROM category WHERE id=(:id) LIMIT 1")
    suspend fun getCategory(id: UUID?): Category

    @Query("SELECT * FROM category WHERE name=(:name) AND event_id = (:eventId) LIMIT 1")
    suspend fun getCategoryByName(name: String, eventId: UUID): Category?

    @Query("SELECT * FROM category WHERE max_age = (:maxAge) AND event_id = (:eventId) LIMIT 1")
    suspend fun getCategoryByMaxAge(maxAge: Int, eventId: UUID): Category?

    @Query("SELECT * FROM category WHERE event_id=(:eventId) AND is_woman = (:woman) AND (:age) <= max_age ORDER BY max_age ASC LIMIT 1 ")
    suspend fun getCategoryByAge(age: Int, woman: Boolean, eventId: UUID): Category?

    @Insert
    suspend fun createCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Query("DELETE FROM category WHERE id=(:id) ")
    suspend fun deleteCategory(id: UUID)


}