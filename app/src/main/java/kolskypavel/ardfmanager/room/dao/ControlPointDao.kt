package kolskypavel.ardfmanager.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.room.entitity.ControlPoint
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ControlPointDao {
    @Query("SELECT * FROM control_point WHERE category_id=(:categoryId)")
    fun getControlPointsForCategory(categoryId: UUID): Flow<List<ControlPoint>>

    @Query("SELECT * FROM control_point WHERE id=(:id)")
    fun getControlPoint(id: UUID): Flow<List<ControlPoint>>

    @Insert
    suspend fun createControlPoint(controlPoint: ControlPoint)

    @Query("DELETE FROM control_point WHERE id =(:id) ")
    suspend fun deleteControlPoint(id: UUID)
}