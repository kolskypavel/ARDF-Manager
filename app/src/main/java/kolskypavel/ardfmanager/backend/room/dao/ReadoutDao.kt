package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ReadoutDao {

    @Query("SELECT * FROM readout WHERE event_id=(:eventId)")
    fun getReadoutsForEvent(eventId: UUID): Flow<List<Readout>>

    @Query("SELECT * FROM readout WHERE si_number=(:siNumber) AND event_id=(:eventId) LIMIT 1")
    fun getReadoutsForSINumber(siNumber: Int, eventId: UUID): Readout?

    @Query("SELECT * FROM readout WHERE id=(:id)")
    fun getReadout(id: UUID): Readout

    @Insert
    fun createReadout(readout: Readout)

    @Query("DELETE FROM readout WHERE id =(:id) ")
    fun deleteReadout(id: UUID)

    @Query("DELETE FROM readout WHERE id=(:eventId)")
    fun deleteReadoutsByEvent(eventId: UUID)

    @Query("SELECT COUNT(*) from readout WHERE si_number=(:siNumber) AND event_id= (:eventId)")
    fun checkIfReadoutExistsById(siNumber: Int, eventId: UUID): Int
}