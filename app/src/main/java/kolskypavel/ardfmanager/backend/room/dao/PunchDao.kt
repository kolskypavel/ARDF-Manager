package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import java.util.UUID

@Dao
interface PunchDao {
    @Query("SELECT * FROM punch WHERE card_number=(:cardNumber) AND event_id=(:eventId)")
    suspend fun getPunchesForSINumber(cardNumber: Int, eventId: UUID): List<Punch>

    @Query("SELECT * FROM punch WHERE competitor_id = (:competitorId)")
    suspend fun getPunchesForCompetitor(competitorId: UUID): List<Punch>

    @Query("SELECT * FROM punch WHERE id=(:id)")
    fun getPunch(id: UUID): Punch

    @Insert
    fun createPunch(punch: Punch)

    @Query("DELETE FROM punch WHERE id =(:id) ")
    fun deletePunch(id: UUID)

    @Query("DELETE FROM punch WHERE event_id=(:eventId)")
    fun deletePunchesByEvent(eventId: UUID)

    @Query("DELETE FROM punch WHERE readout_id=(:readoutId)")
    fun deletePunchesByReadoutId(readoutId: UUID)
}