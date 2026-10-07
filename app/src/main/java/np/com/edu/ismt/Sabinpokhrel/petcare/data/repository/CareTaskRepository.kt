package np.com.edu.ismt.Sabinpokhrel.petcare.data.repository

import androidx.lifecycle.LiveData
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.CareTaskDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask

class CareTaskRepository(private val careTaskDao: CareTaskDao) {

    fun getTasksForPet(petId: Long): LiveData<List<CareTask>> = careTaskDao.getTasksForPet(petId)

    fun getTasksForOwner(ownerId: Long): LiveData<List<CareTask>> = careTaskDao.getTasksForOwner(ownerId)

    suspend fun getTaskById(taskId: Long): CareTask? = careTaskDao.getTaskById(taskId)

    suspend fun getTasksByIds(ids: List<Long>): List<CareTask> = careTaskDao.getTasksByIds(ids)

    suspend fun addTask(task: CareTask): Long = careTaskDao.insert(task)

    suspend fun updateTask(task: CareTask) = careTaskDao.update(task)

    suspend fun deleteTask(task: CareTask) = careTaskDao.delete(task)

    suspend fun setCompleted(taskId: Long, completed: Boolean) = careTaskDao.setCompleted(taskId, completed)
}
