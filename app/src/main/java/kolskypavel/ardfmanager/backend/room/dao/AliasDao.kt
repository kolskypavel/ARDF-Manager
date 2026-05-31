package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entity.Alias
import java.util.UUID

/** Room access methods for control-point aliases scoped to a race. */
@Dao
interface AliasDao {
    /** Returns all aliases configured for a race. */
    @Query("SELECT * FROM alias WHERE race_id= (:raceId)")
    suspend fun getAliasesByRace(raceId: UUID): List<Alias>

    /** Returns one alias by primary key. */
    @Query("SELECT * FROM alias WHERE id=(:id)")
    suspend fun getAlias(id: UUID): Alias

    /** Inserts a new alias or updates the existing row with the same key. */
    @Upsert
    fun createOrUpdateAlias(alias: Alias)

    /** Deletes one alias by primary key. */
    @Query("DELETE FROM alias WHERE id =(:id) ")
    suspend fun deleteAlias(id: UUID)

    /** Deletes all aliases belonging to a race. */
    @Query("DELETE FROM alias WHERE race_id=(:raceId)")
    suspend fun deleteAliasesByRace(raceId: UUID)
}
