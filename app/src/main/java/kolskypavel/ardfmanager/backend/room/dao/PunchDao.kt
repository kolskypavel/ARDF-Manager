package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PunchDao {
    @Query("SELECT * FROM punch WHERE si_number=(:siNumber)")
    fun getPunchesForSINumber(siNumber: Int): Flow<List<Punch>>

    @Query("SELECT * FROM punch WHERE id=(:id)")
    fun getPunch(id: UUID): Punch

    @Insert
    suspend fun createPunch(punch: Punch)

    @Query("DELETE FROM punch WHERE id =(:id) ")
    fun deletePunch(id: UUID)

    @Query("DELETE FROM punch WHERE event_id=(:eventId)")
    fun deletePunchesForEvent(eventId: UUID)
}