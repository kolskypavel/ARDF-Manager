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
    @Query("SELECT * FROM category WHERE event_id=(:id)")
    fun getCategoriesForEvent(id: UUID): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id=(:id)")
    fun getCategory(id: UUID): Flow<List<Category>>

    @Insert
    suspend fun createCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Query("DELETE FROM category WHERE id=(:id) ")
    fun deleteCategory(id: UUID)
}