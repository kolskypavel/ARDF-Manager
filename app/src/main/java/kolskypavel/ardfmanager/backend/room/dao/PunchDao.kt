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

    @Query("SELECT * FROM punch WHERE readout_id = (:readoutId) ORDER BY `order` ASC")
    suspend fun getPunchesForReadout(readoutId: UUID): List<Punch>

    @Query("SELECT * FROM punch WHERE id=(:id)")
    suspend fun getPunch(id: UUID): Punch

    @Insert
    suspend fun createPunch(punch: Punch)

    @Query("DELETE FROM punch WHERE id =(:id) ")
    suspend fun deletePunch(id: UUID)

    @Query("DELETE FROM punch WHERE event_id=(:eventId)")
    suspend fun deletePunchesByEvent(eventId: UUID)

    @Query("DELETE FROM punch WHERE readout_id=(:readoutId)")
    suspend fun deletePunchesByReadoutId(readoutId: UUID)
}