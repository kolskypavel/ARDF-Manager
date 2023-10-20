package kolskypavel.ardfmanager.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kolskypavel.ardfmanager.room.entitity.Event
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface EventDao {
    @Query("SELECT * FROM event")
    fun getEvents(): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE id=(:id) LIMIT 1")
    fun getEvent(id: UUID): Flow<List<Event>>

    @Insert
    suspend fun createEvent(event: Event)

    @Query("DELETE FROM event WHERE id =(:id)")
    suspend fun deleteEvent(id: UUID)
}