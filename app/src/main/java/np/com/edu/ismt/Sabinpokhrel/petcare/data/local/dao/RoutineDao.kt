package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Routine

@Dao
interface RoutineDao {
    @Insert
    suspend fun insert(routine: Routine): Long

    @Update
    suspend fun update(routine: Routine)

    @Delete
    suspend fun delete(routine: Routine)

    @Query("SELECT * FROM routines WHERE petId = :petId ORDER BY createdAt DESC")
    fun getRoutinesForPet(petId: Long): LiveData<List<Routine>>

    @Query("SELECT * FROM routines WHERE routineId = :routineId LIMIT 1")
    suspend fun getRoutineById(routineId: Long): Routine?
}
