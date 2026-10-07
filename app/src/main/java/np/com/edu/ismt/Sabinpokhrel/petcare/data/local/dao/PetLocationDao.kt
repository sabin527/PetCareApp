package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PetLocation

@Dao
interface PetLocationDao {
    @Insert
    suspend fun insert(location: PetLocation): Long

    @Update
    suspend fun update(location: PetLocation)

    @Delete
    suspend fun delete(location: PetLocation)

    @Query("SELECT * FROM pet_locations WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getLocationsForOwner(ownerId: Long): LiveData<List<PetLocation>>

    @Query("SELECT * FROM pet_locations WHERE locationId = :locationId LIMIT 1")
    suspend fun getLocationById(locationId: Long): PetLocation?

    @Query("SELECT * FROM pet_locations WHERE ownerId = :ownerId")
    suspend fun getLocationsForOwnerSync(ownerId: Long): List<PetLocation>
}
