package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kolskypavel.ardfmanager.backend.room.entity.Race
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Room access methods for race/event records. */
@Dao
interface RaceDao {
    /** Observes all races available in the local database. */
    @Query("SELECT * FROM race")
    fun getRaces(): Flow<List<Race>>

    /** Returns one race by primary key, or null when absent. */
    @Query("SELECT * FROM race WHERE id=(:id)")
    suspend fun getRace(id: UUID): Race?

    /** Inserts a new race. */
    @Insert
    suspend fun createRace(race: Race)

    /** Updates an existing race. */
    @Update
    suspend fun updateRace(race: Race)

    /** Deletes one race by primary key. */
    @Query("DELETE FROM race WHERE id =(:id)")
    suspend fun deleteRace(id: UUID)
}
