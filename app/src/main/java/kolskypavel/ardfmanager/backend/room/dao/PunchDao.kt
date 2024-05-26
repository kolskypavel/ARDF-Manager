package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import java.util.UUID

@Dao
interface PunchDao {

    @Query("SELECT * FROM punch WHERE readout_id= (:readoutId) ORDER BY `order` ASC")
    suspend fun getPunchesByReadout(readoutId: UUID): List<Punch>

    @Query("SELECT * FROM punch WHERE competitor_id = (:competitorId)")
    suspend fun getPunchesByCompetitor(competitorId: UUID): List<Punch>


    @Query("SELECT * FROM punch WHERE id=(:id)")
    suspend fun getPunch(id: UUID): Punch

    @Upsert
    fun createPunch(punch: Punch)

    @Query("DELETE FROM punch WHERE id =(:id) ")
    suspend fun deletePunch(id: UUID)

    @Query("DELETE FROM punch WHERE event_id=(:eventId)")
    suspend fun deletePunchesByEvent(eventId: UUID)

}