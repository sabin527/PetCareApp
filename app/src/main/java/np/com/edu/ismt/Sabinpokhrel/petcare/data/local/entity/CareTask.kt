package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "care_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["routineId"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId"), Index("routineId")]
)
data class CareTask(
    @PrimaryKey(autoGenerate = true) val taskId: Long = 0,
    val petId: Long,
    val routineId: Long? = null,
    val title: String,
    val category: TaskCategory,
    // Feeding times / medication schedule / grooming instructions / vet appointment date-time
    val scheduledAt: Long,
    val instructions: String = "",
    val requiredSupplies: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false,
    val remindersEnabled: Boolean = true,
    val linkedLocationId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
