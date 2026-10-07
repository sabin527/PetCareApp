package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask

@Dao
interface CareTaskDao {
    @Insert
    suspend fun insert(task: CareTask): Long

    @Insert
    suspend fun insertAll(tasks: List<CareTask>)

    @Update
    suspend fun update(task: CareTask)

    @Delete
    suspend fun delete(task: CareTask)

    @Query("SELECT * FROM care_tasks WHERE petId = :petId ORDER BY isCompleted ASC, scheduledAt ASC")
    fun getTasksForPet(petId: Long): LiveData<List<CareTask>>

    @Query("""
        SELECT ct.* FROM care_tasks ct
        INNER JOIN pets p ON ct.petId = p.petId
        WHERE p.ownerId = :ownerId
        ORDER BY ct.isCompleted ASC, ct.scheduledAt ASC
    """)
    fun getTasksForOwner(ownerId: Long): LiveData<List<CareTask>>

    @Query("SELECT * FROM care_tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): CareTask?

    @Query("SELECT * FROM care_tasks WHERE taskId IN (:ids)")
    suspend fun getTasksByIds(ids: List<Long>): List<CareTask>

    @Query("UPDATE care_tasks SET isCompleted = :completed WHERE taskId = :taskId")
    suspend fun setCompleted(taskId: Long, completed: Boolean)

    @Query("SELECT * FROM care_tasks WHERE isCompleted = 0 AND scheduledAt > :now")
    suspend fun getAllUpcomingIncompleteTasks(now: Long = System.currentTimeMillis()): List<CareTask>

    @Query("SELECT * FROM care_tasks WHERE routineId = :routineId")
    suspend fun getTasksForRoutineSync(routineId: Long): List<CareTask>

    @Query("""
        SELECT ct.* FROM care_tasks ct
        INNER JOIN pets p ON ct.petId = p.petId
        WHERE p.ownerId = :ownerId
        ORDER BY ct.scheduledAt ASC
    """)
    suspend fun getTasksForOwnerSync(ownerId: Long): List<CareTask>
}
