package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskCategory {
    FEEDING, EXERCISE, GROOMING, MEDICATION, HEALTHCARE
}

enum class Frequency {
    DAILY, WEEKLY
}

@Entity(
    tableName = "routines",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId")]
)
data class Routine(
    @PrimaryKey(autoGenerate = true) val routineId: Long = 0,
    val petId: Long,
    val title: String,
    val category: TaskCategory,
    val frequency: Frequency,
    val dayOfWeek: Int? = null, // 1=Mon..7=Sun, null if daily
    val hour: Int,
    val minute: Int,
    val requiredSupplies: String = "",
    val notes: String = "",
    val imageUri: String? = null,
    val remindersEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
