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

    @Query("SELECT * FROM readout WHERE si_number=(:siNumber) LIMIT 1")
    fun getReadoutsForSINumber(siNumber: Int): Readout

    @Query("SELECT * FROM readout WHERE id=(:id)")
    fun getReadout(id: UUID): Readout

    @Insert
    suspend fun createReadout(readout: Readout)

    @Query("DELETE FROM readout WHERE id =(:id) ")
    fun deletePunch(id: UUID)
}