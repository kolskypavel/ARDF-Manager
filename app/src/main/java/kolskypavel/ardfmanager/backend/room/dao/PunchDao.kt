package kolskypavel.ardfmanager.backend.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kolskypavel.ardfmanager.backend.room.entity.Punch
import java.util.UUID

/** Room access methods for SportIdent punches. */
@Dao
interface PunchDao {

    /** Returns punches for one result in recorded order. */
    @Query("SELECT * FROM punch WHERE result_id= (:resultId) ORDER BY `order` ASC")
    suspend fun getPunchesByResult(resultId: UUID): List<Punch>

    /** Returns one punch by primary key. */
    @Query("SELECT * FROM punch WHERE id=(:id)")
    suspend fun getPunch(id: UUID): Punch

    /** Inserts a new punch or updates the existing row with the same key. */
    @Upsert
    fun createOrUpdatePunch(punch: Punch)

    /** Deletes one punch by primary key. */
    @Query("DELETE FROM punch WHERE id =(:id) ")
    suspend fun deletePunch(id: UUID)

    /** Deletes all punches belonging to a race. */
    @Query("DELETE FROM punch WHERE race_id=(:raceId)")
    suspend fun deletePunchesByRace(raceId: UUID)

    /** Deletes all punches belonging to a result. */
    @Query("DELETE FROM punch WHERE result_id=(:resultId)")
    suspend fun deletePunchesByResult(resultId: UUID)

}
