package np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao

import androidx.room.*
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun findById(userId: Long): User?

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun countByEmail(email: String): Int

    @Update
    suspend fun update(user: User)
}
