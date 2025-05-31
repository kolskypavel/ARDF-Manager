package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entity.Alias
import java.util.UUID

@Dao
interface AliasDao {
    @Query("SELECT * FROM alias WHERE race_id= (:raceId)")
    suspend fun getAliasesByRace(raceId: UUID): List<Alias>

    @Query("SELECT * FROM alias WHERE id=(:id)")
    suspend fun getAlias(id: UUID): Alias
    
    @Upsert
    fun createOrUpdateAlias(alias: Alias)

    @Query("DELETE FROM alias WHERE id =(:id) ")
    suspend fun deleteAlias(id: UUID)

    @Query("DELETE FROM alias WHERE race_id=(:raceId)")
    suspend fun deleteAliasesByRace(raceId: UUID)
}