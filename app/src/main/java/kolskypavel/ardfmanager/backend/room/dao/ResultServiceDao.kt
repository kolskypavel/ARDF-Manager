package kolskypavel.ardfmanager.backend.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entity.ResultService
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ResultServiceData
import java.util.UUID

/** Room access methods for configured live-result services. */
@Dao
interface ResultServiceDao {
    /** Returns one result-service configuration by primary key. */
    @Query("SELECT * FROM result_service WHERE id=(:id)")
    suspend fun getResultService(id: UUID): ResultService

    /** Observes one race's result service plus the number of result rows available to send. */
    @Query(
        """
    SELECT *, 
    (SELECT COUNT(*) FROM result WHERE result.race_id = :raceId) AS resultCount
    FROM result_service 
    WHERE race_id = :raceId 
    LIMIT 1"""
    )
    fun getResultServiceLiveDataWithCountByRaceId(raceId: UUID): LiveData<ResultServiceData>

    /** Returns the result-service configuration for a race, or null when absent. */
    @Query("SELECT * FROM result_service WHERE race_id = (:raceId) LIMIT 1")
    fun getResultServiceByRaceId(raceId: UUID): ResultService?

    /** Inserts a new result-service configuration or updates the existing row. */
    @Upsert
    suspend fun createOrUpdateResultService(resultService: ResultService)

    /** Deletes one result-service configuration by primary key. */
    @Query("DELETE FROM result_service WHERE id =(:id)")
    suspend fun deleteResultService(id: UUID)
}
