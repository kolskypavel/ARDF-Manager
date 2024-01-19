package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import java.util.UUID

@Dao
interface PunchDao {

    @Query("SELECT * FROM punch WHERE result_id = (:resultId) ORDER BY `order` ASC")
    suspend fun getPunchesByResult(resultId: UUID): List<Punch>

    @Query("SELECT * FROM punch WHERE competitor_id = (:competitorId)")
    suspend fun getPunchesByCompetitor(competitorId: UUID): List<Punch>


    @Query("SELECT * FROM punch WHERE id=(:id)")
    suspend fun getPunch(id: UUID): Punch

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPunch(punch: Punch)

    @Query("DELETE FROM punch WHERE id =(:id) ")
    suspend fun deletePunch(id: UUID)

    @Query("DELETE FROM punch WHERE event_id=(:eventId)")
    suspend fun deletePunchesByEvent(eventId: UUID)

    @Query("DELETE FROM punch WHERE result_id=(:resultId)")
    suspend fun deletePunchesByResultId(resultId: UUID)
}