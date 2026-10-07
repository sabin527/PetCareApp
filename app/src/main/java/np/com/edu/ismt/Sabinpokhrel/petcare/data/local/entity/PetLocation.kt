package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PlaceType {
    VET_CLINIC, GROOMING_SALON, DOG_PARK, PET_SUPPLY_STORE, ANIMAL_SHELTER, OTHER
}

@Entity(
    tableName = "pet_locations",
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
data class PetLocation(
    @PrimaryKey(autoGenerate = true) val locationId: Long = 0,
    val ownerId: Long,
    val name: String,
    val type: PlaceType,
    val address: String = "",
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val linkedTaskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
