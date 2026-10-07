package np.com.edu.ismt.Sabinpokhrel.petcare.data.repository

import androidx.lifecycle.LiveData
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.PetDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet

class PetRepository(private val petDao: PetDao) {

    fun getPetsForOwner(ownerId: Long): LiveData<List<Pet>> = petDao.getPetsForOwner(ownerId)

    fun getPetById(petId: Long): LiveData<Pet?> = petDao.getPetById(petId)

    suspend fun getPetByIdSync(petId: Long): Pet? = petDao.getPetByIdSync(petId)

    suspend fun getPetsForOwnerSync(ownerId: Long): List<Pet> = petDao.getPetsForOwnerSync(ownerId)

    suspend fun addPet(pet: Pet): Long = petDao.insert(pet)

    suspend fun updatePet(pet: Pet) = petDao.update(pet)

    suspend fun deletePet(pet: Pet) = petDao.delete(pet)
}
