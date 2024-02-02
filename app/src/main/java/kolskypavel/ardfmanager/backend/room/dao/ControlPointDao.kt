package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import java.util.UUID

@Dao
interface ControlPointDao {
    @Query("SELECT * FROM control_point WHERE category_id=(:categoryId) ORDER BY `order`ASC")
    suspend fun getControlPointsByCategory(categoryId: UUID): List<ControlPoint>

    @Query("SELECT * FROM control_point WHERE id=(:id) LIMIT 1")
    suspend fun getControlPoint(id: UUID): ControlPoint


    @Query("SELECT * FROM control_point WHERE event_id=(:eventId) AND name=(:name) LIMIT 1")
    suspend fun getControlPointByName(eventId: UUID, name: String): ControlPoint?

    @Insert
    suspend fun createControlPoint(controlPoint: ControlPoint)

    @Query("DELETE FROM control_point WHERE id =(:id) ")
    suspend fun deleteControlPoint(id: UUID)

    @Query("DELETE FROM control_point WHERE category_id=(:categoryId)")
    suspend fun deleteControlPointsByCategory(categoryId: UUID)

    @Query("DELETE FROM control_point WHERE event_id=(:eventId)")
    suspend fun deleteControlPointsByEvent(eventId: UUID)

}