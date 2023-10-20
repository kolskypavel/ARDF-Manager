package kolskypavel.ardfmanager.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.room.entitity.Readout
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ReadoutDao {

    @Query("SELECT * FROM readout WHERE event_id=(:eventId)")
    fun getReadoutsForEvent(eventId: UUID): Flow<List<Readout>>

    @Query("SELECT * FROM readout WHERE si_number=(:siNumber) LIMIT 1")
    fun getReadoutsForSINumber(siNumber: Int): Readout

    @Query("SELECT * FROM readout WHERE id=(:id)")
    fun getPunch(id: UUID): Readout

    @Insert
    suspend fun createReadout(punch: Readout)

    @Query("DELETE FROM readout WHERE id =(:id) ")
    fun deletePunch(id: UUID)
}