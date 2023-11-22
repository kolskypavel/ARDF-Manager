package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CompetitorDao {
    @Query("SELECT * FROM competitor WHERE event_id=(:eventId)")
    fun getCompetitorsForEvent(eventId: UUID): Flow<List<Competitor>>

    @Query("SELECT * FROM competitor WHERE id=(:id) LIMIT 1")
    fun getCompetitor(id: UUID): Competitor

    @Query("SELECT * FROM competitor WHERE si_number=(:siNumber) AND event_id = (:eventId) LIMIT 1")
    fun getCompetitorBySINumber(siNumber: Int, eventId: UUID): Competitor?

    @Query("SELECT COUNT(*) FROM competitor WHERE si_number=(:siNumber) AND event_id =(:eventId)  LIMIT 1")
    suspend fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Int

    @Insert
    suspend fun createCompetitor(competitor: Competitor)

    @Update
    suspend fun updateCompetitor(competitor: Competitor)

    @Query("DELETE FROM competitor WHERE id =(:id)")
    suspend fun deleteCompetitor(id: UUID)
}