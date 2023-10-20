package kolskypavel.ardfmanager.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.room.entitity.Competitor
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CompetitorDao {
    @Query("SELECT * FROM competitor WHERE event_id=(:eventId)")
    fun getCompetitorsForEvent(eventId: UUID): Flow<List<Competitor>>

    @Query("SELECT * FROM competitor WHERE id=(:id)")
     fun getCompetitor(id: UUID): Flow<List<Competitor>>

    @Insert
    fun createCompetitor(competitor: Competitor)

    @Query("DELETE FROM competitor WHERE id =(:id)")
    suspend fun deleteCompetitor(id: UUID)
}