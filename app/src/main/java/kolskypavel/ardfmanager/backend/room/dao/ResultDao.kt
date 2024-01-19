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

    @Query("SELECT * FROM result WHERE event_id=(:eventId)")
    suspend fun getResultsByEvent(eventId: UUID): List<Result>

    @Query("SELECT * FROM result WHERE si_number=(:siNumber) AND event_id=(:eventId) LIMIT 1")
    suspend fun getResultForSINumber(siNumber: Int, eventId: UUID): Result?

    @Query("SELECT * FROM result WHERE competitor_id=(:competitorId)")
    suspend fun getResultByCompetitor(competitorId: UUID): Result?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createResult(result: Result)

    @Query("DELETE FROM result WHERE id =(:id) ")
    suspend fun deleteResult(id: UUID)

    @Query("DELETE FROM result WHERE id=(:eventId)")
    suspend fun deleteResultsByEvent(eventId: UUID)

    @Query("SELECT COUNT(*) from result WHERE si_number=(:siNumber) AND event_id= (:eventId)")
    suspend fun checkIfResultExistsById(siNumber: Int, eventId: UUID): Int
}