package np.com.edu.ismt.Sabinpokhrel.petcare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.CareTaskDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.PetDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.PetLocationDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.RoutineDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.UserDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PetLocation
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Routine
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.User

@Database(
    entities = [User::class, Pet::class, Routine::class, CareTask::class, PetLocation::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun routineDao(): RoutineDao
    abstract fun careTaskDao(): CareTaskDao
    abstract fun petLocationDao(): PetLocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "petcare.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
