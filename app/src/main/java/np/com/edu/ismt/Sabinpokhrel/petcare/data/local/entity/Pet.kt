package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class Pet(
    @PrimaryKey(autoGenerate = true) val petId: Long = 0,
    val ownerId: Long,
    val name: String,
    val breed: String = "",
    val age: String = "",
    val weight: String = "",
    val dietaryPreferences: String = "",
    val vaccinationHistory: String = "",
    val allergies: String = "",
    val favouriteToys: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
