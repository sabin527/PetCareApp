package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PlaceType

object PlaceTypeUtils {
    fun label(type: PlaceType): String = when (type) {
        PlaceType.VET_CLINIC -> "Vet Clinic"
        PlaceType.GROOMING_SALON -> "Grooming Salon"
        PlaceType.DOG_PARK -> "Dog Park"
        PlaceType.PET_SUPPLY_STORE -> "Pet Supply Store"
        PlaceType.ANIMAL_SHELTER -> "Animal Shelter"
        PlaceType.OTHER -> "Other"
    }

    fun allLabels(): List<String> = PlaceType.values().map { label(it) }
}
