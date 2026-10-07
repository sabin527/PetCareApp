package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet

@Dao
interface PetDao {
    @Insert
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getPetsForOwner(ownerId: Long): LiveData<List<Pet>>

    @Query("SELECT * FROM pets WHERE petId = :petId LIMIT 1")
    fun getPetById(petId: Long): LiveData<Pet?>

    @Query("SELECT * FROM pets WHERE petId = :petId LIMIT 1")
    suspend fun getPetByIdSync(petId: Long): Pet?

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY name ASC")
    suspend fun getPetsForOwnerSync(ownerId: Long): List<Pet>

    @Query("SELECT * FROM pets")
    suspend fun getAllPetsSync(): List<Pet>
}
