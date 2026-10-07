package np.com.edu.ismt.Sabinpokhrel.petcare.data.repository

import androidx.lifecycle.LiveData
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.PetLocationDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PetLocation

class PetLocationRepository(private val locationDao: PetLocationDao) {

    fun getLocationsForOwner(ownerId: Long): LiveData<List<PetLocation>> =
        locationDao.getLocationsForOwner(ownerId)

    suspend fun getLocationById(locationId: Long): PetLocation? = locationDao.getLocationById(locationId)

    suspend fun addLocation(location: PetLocation): Long = locationDao.insert(location)

    suspend fun updateLocation(location: PetLocation) = locationDao.update(location)

    suspend fun deleteLocation(location: PetLocation) = locationDao.delete(location)
}
