package np.com.edu.ismt.Sabinpokhrel.petcare.data.repository

import androidx.lifecycle.LiveData
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.CareTaskDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.RoutineDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Frequency
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Routine
import java.util.Calendar

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val careTaskDao: CareTaskDao
) {

    fun getRoutinesForPet(petId: Long): LiveData<List<Routine>> = routineDao.getRoutinesForPet(petId)

    suspend fun getRoutineById(routineId: Long): Routine? = routineDao.getRoutineById(routineId)

    /**
     * Saves the routine and auto-generates its checklist of upcoming CareTasks.
     * Daily routines generate the next 7 occurrences; weekly routines generate the next 4
     * occurrences (one per week) on the chosen day of week.
     */
    suspend fun saveRoutineAndGenerateTasks(routine: Routine, petName: String = ""): Long {
        val routineId = if (routine.routineId == 0L) {
            routineDao.insert(routine)
        } else {
            routineDao.update(routine)
            routine.routineId
        }
        val savedRoutine = routine.copy(routineId = routineId)
        regenerateTasksForRoutine(savedRoutine)
        return routineId
    }

    private suspend fun regenerateTasksForRoutine(routine: Routine) {
        val occurrences = mutableListOf<Long>()
        val now = Calendar.getInstance()

        if (routine.frequency == Frequency.DAILY) {
            for (i in 0 until 7) {
                val cal = now.clone() as Calendar
                cal.add(Calendar.DAY_OF_YEAR, i)
                cal.set(Calendar.HOUR_OF_DAY, routine.hour)
                cal.set(Calendar.MINUTE, routine.minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                if (cal.timeInMillis > System.currentTimeMillis()) {
                    occurrences.add(cal.timeInMillis)
                }
            }
        } else {
            val targetDay = routine.dayOfWeek ?: 1
            for (i in 0 until 4) {
                val cal = now.clone() as Calendar
                cal.add(Calendar.WEEK_OF_YEAR, i)
                // Calendar.DAY_OF_WEEK: Sunday=1..Saturday=7. Our targetDay: Monday=1..Sunday=7.
                val calendarDay = if (targetDay == 7) Calendar.SUNDAY else targetDay + 1
                cal.set(Calendar.DAY_OF_WEEK, calendarDay)
                cal.set(Calendar.HOUR_OF_DAY, routine.hour)
                cal.set(Calendar.MINUTE, routine.minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                if (cal.timeInMillis > System.currentTimeMillis()) {
                    occurrences.add(cal.timeInMillis)
                }
            }
        }

        val tasks = occurrences.map { time ->
            CareTask(
                petId = routine.petId,
                routineId = routine.routineId,
                title = routine.title,
                category = routine.category,
                scheduledAt = time,
                requiredSupplies = routine.requiredSupplies,
                notes = routine.notes,
                remindersEnabled = routine.remindersEnabled
            )
        }
        if (tasks.isNotEmpty()) {
            careTaskDao.insertAll(tasks)
        }
    }

    suspend fun deleteRoutine(routine: Routine) = routineDao.delete(routine)
}
