package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ReadoutDao {
    @Query("SELECT * FROM readout WHERE id=(:id)")
    suspend fun getReadout(id: UUID): Readout

    @Query("SELECT * FROM readout WHERE race_id = (:raceId) ")
    @Transaction
    fun getReadoutDataByRace(raceId: UUID): Flow<List<ReadoutData>>

    @Query("SELECT * FROM readout WHERE id = (:readoutId) LIMIT 1 ")
    fun getReadoutDataByReadout(readoutId:UUID):ReadoutData?

    @Query("SELECT * FROM readout WHERE si_number=(:siNumber) AND race_id=(:raceId) LIMIT 1")
    suspend fun getReadoutForSINumber(siNumber: Int, raceId: UUID): Readout?

    @Query("SELECT * FROM readout WHERE competitor_id=(:competitorId)")
    suspend fun getReadoutByCompetitor(competitorId: UUID): Readout?

    @Upsert
    suspend fun createOrUpdateReadout(readout: Readout)

    @Query("DELETE FROM readout WHERE id =(:id) ")
    suspend fun deleteReadout(id: UUID)

    @Query("DELETE FROM readout WHERE race_id =(:raceId) ")
    suspend fun deleteAllReadoutsByRace(raceId: UUID)

    @Query("DELETE FROM readout WHERE competitor_id =(:competitorId) ")
    suspend fun deleteReadoutByCompetitor(competitorId: UUID)

    @Query("SELECT COUNT(*) from readout WHERE si_number=(:siNumber) AND race_id= (:raceId)")
    suspend fun checkIfReadoutExistsById(siNumber: Int, raceId: UUID): Int
}