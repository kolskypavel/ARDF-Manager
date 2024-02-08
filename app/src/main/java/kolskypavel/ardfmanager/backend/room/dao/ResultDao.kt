package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entitity.Result
import java.util.UUID

@Dao
interface ResultDao {
    @Query("SELECT * FROM result WHERE id=(:id)")
    suspend fun getResult(id: UUID): Result

    @Query("SELECT * FROM result WHERE readout_id=(:readoutId)")
    suspend fun getResultByReadout(readoutId: UUID): Result

    @Query("SELECT * FROM result WHERE competitor_id=(:competitorId)")
    suspend fun getResultByCompetitor(competitorId: UUID): Result?

    @Query("SELECT * FROM result WHERE category_id = (:categoryId)")
    suspend fun getResultByCategory(categoryId: UUID?): List<Result>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createResult(result: Result)

    @Query("DELETE FROM result WHERE id =(:id) ")
    suspend fun deleteResult(id: UUID)
}