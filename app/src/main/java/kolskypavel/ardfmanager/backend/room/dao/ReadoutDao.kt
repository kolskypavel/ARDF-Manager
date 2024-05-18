package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ReadoutDao {
    @Query("SELECT * FROM readout WHERE id=(:id)")
    suspend fun getReadout(id: UUID): Readout

    @Query("SELECT * FROM readout WHERE event_id = (:eventId) ")
    @Transaction
    fun getReadoutDataByEvent(eventId: UUID): Flow<List<ReadoutData>>

    @Query("SELECT * FROM readout WHERE si_number=(:siNumber) AND event_id=(:eventId) LIMIT 1")
    suspend fun getReadoutForSINumber(siNumber: Int, eventId: UUID): Readout?

    @Query("SELECT * FROM readout WHERE competitor_id=(:competitorId)")
    suspend fun getReadoutByCompetitor(competitorId: UUID): Readout?

    @Upsert
    suspend fun createReadout(readout: Readout)

    @Query("DELETE FROM readout WHERE id =(:id) ")
    suspend fun deleteReadout(id: UUID)

    @Query("DELETE FROM readout WHERE competitor_id =(:competitorId) ")
    suspend fun deleteReadoutByCompetitor(competitorId: UUID)

    @Query("SELECT COUNT(*) from readout WHERE si_number=(:siNumber) AND event_id= (:eventId)")
    suspend fun checkIfReadoutExistsById(siNumber: Int, eventId: UUID): Int
}