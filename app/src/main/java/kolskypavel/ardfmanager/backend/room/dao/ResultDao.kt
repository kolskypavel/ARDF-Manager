package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ResultData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Room access methods for SI readout results and result aggregates. */
@Dao
interface ResultDao {
    /** Returns one result entity by primary key. */
    @Query("SELECT * FROM result WHERE id=(:id)")
    suspend fun getResult(id: UUID): Result

    /** Returns one result aggregate by primary key. */
    @Query("SELECT * FROM result WHERE id=(:id)")
    suspend fun getResultData(id: UUID): ResultData

    /** Observes result aggregates for a race ordered by readout time. */
    @Query("SELECT * FROM result WHERE race_id=(:raceId) ORDER BY readout_time ASC")
    fun getResultDataFlowByRace(raceId: UUID): Flow<List<ResultData>>

    /** Returns the result matched to a competitor, or null when absent. */
    @Query("SELECT * FROM result WHERE competitor_id=(:competitorId) LIMIT 1")
    suspend fun getResultByCompetitor(competitorId: UUID): Result?

    /** Finds a result by SI card number within one race. */
    @Query("SELECT * FROM result WHERE si_number=(:siNumber) AND race_id=(:raceId) LIMIT 1")
    suspend fun getResultForSINumber(siNumber: Int, raceId: UUID): Result?

    /** Inserts a new result or updates the existing row with the same key. */
    @Upsert
    suspend fun createOrUpdateResult(result: Result)

    /** Marks all results in a race as unsent for live-result publishing. */
    @Query("UPDATE result SET sent = 0 WHERE race_id =(:raceId)")
    suspend fun setAllResultsUnsent(raceId: UUID)

    /** Deletes one result by primary key. */
    @Query("DELETE FROM result WHERE id =(:id)")
    suspend fun deleteResult(id: UUID)

    /** Deletes the result matched to a competitor. */
    @Query("DELETE FROM result WHERE competitor_id =(:competitorId)")
    suspend fun deleteResultByCompetitor(competitorId: UUID)

    /** Deletes all results belonging to a race. */
    @Query("DELETE FROM result WHERE race_id =(:raceId)")
    suspend fun deleteAllResultsByRace(raceId: UUID)
}
