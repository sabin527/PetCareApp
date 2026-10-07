package np.com.edu.ismt.Sabinpokhrel.petcare.data.repository

import android.content.Context
import np.com.edu.ismt.Sabinpokhrel.petcare.PetCareApp

/**
 * Simple manual dependency provider (no DI framework) giving Activities/ViewModels
 * easy access to repositories backed by the single Room database instance.
 */
object RepositoryProvider {

    fun userRepository(context: Context): UserRepository {
        val db = (context.applicationContext as PetCareApp).database
        return UserRepository(db.userDao())
    }

    fun petRepository(context: Context): PetRepository {
        val db = (context.applicationContext as PetCareApp).database
        return PetRepository(db.petDao())
    }

    fun routineRepository(context: Context): RoutineRepository {
        val db = (context.applicationContext as PetCareApp).database
        return RoutineRepository(db.routineDao(), db.careTaskDao())
    }

    fun careTaskRepository(context: Context): CareTaskRepository {
        val db = (context.applicationContext as PetCareApp).database
        return CareTaskRepository(db.careTaskDao())
    }

    fun locationRepository(context: Context): PetLocationRepository {
        val db = (context.applicationContext as PetCareApp).database
        return PetLocationRepository(db.petLocationDao())
    }
}
