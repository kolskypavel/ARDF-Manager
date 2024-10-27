package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import java.util.UUID

@Dao
interface PunchDao {

    @Query("SELECT * FROM punch WHERE result_id= (:resultId) ORDER BY `order` ASC")
    suspend fun getPunchesByResult(resultId: UUID): List<Punch>

    @Query("SELECT * FROM punch WHERE id=(:id)")
    suspend fun getPunch(id: UUID): Punch

    @Upsert
    fun createOrUpdatePunch(punch: Punch)

    @Query("DELETE FROM punch WHERE id =(:id) ")
    suspend fun deletePunch(id: UUID)

    @Query("DELETE FROM punch WHERE race_id=(:raceId)")
    suspend fun deletePunchesByRace(raceId: UUID)

    @Query("DELETE FROM punch WHERE result_id=(:resultId)")
    suspend fun deletePunchesByResult(resultId: UUID)

}