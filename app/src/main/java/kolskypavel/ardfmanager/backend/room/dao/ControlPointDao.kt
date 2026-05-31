package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ControlPointAlias
import java.util.UUID

/** Room access methods for category control points and alias joins. */
@Dao
interface ControlPointDao {
    /** Returns ordered control points for a category. */
    @Query("SELECT * FROM control_point WHERE category_id=(:categoryId) ORDER BY `order`ASC")
    suspend fun getControlPointsByCategory(categoryId: UUID): List<ControlPoint>

    /** Returns ordered control points joined with any matching aliases. */
    @Query("SELECT * FROM control_point WHERE category_id=(:categoryId) ORDER BY `order`ASC")
    suspend fun getControlPointAliasesByCategory(categoryId: UUID): List<ControlPointAlias>

    /** Returns one control point by primary key. */
    @Query("SELECT * FROM control_point WHERE id=(:id) LIMIT 1")
    suspend fun getControlPoint(id: UUID): ControlPoint

    /** Inserts one control point. */
    @Insert
    suspend fun createControlPoint(controlPoint: ControlPoint)

    /** Deletes one control point by primary key. */
    @Query("DELETE FROM control_point WHERE id =(:id) ")
    suspend fun deleteControlPoint(id: UUID)

    /** Deletes all control points belonging to a category. */
    @Query("DELETE FROM control_point WHERE category_id=(:categoryId)")
    suspend fun deleteControlPointsByCategory(categoryId: UUID)

}
