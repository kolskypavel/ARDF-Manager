package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kolskypavel.ardfmanager.backend.room.entity.Race
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RaceDao {
    @Query("SELECT * FROM race")
    fun getRaces(): Flow<List<Race>>

    @Query("SELECT * FROM race WHERE id=(:id) LIMIT 1")
    suspend fun getRace(id: UUID): Race

    @Insert
    suspend fun createRace(race: Race)

    @Update
    suspend fun updateRace(race: Race)

    @Query("DELETE FROM race WHERE id =(:id)")
    suspend fun deleteRace(id: UUID)
}