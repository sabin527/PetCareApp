package np.com.edu.ismt.Sabinpokhrel.petcare.data.repository

import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.dao.UserDao
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.User
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.PasswordUtils

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository(private val userDao: UserDao) {

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val existing = userDao.countByEmail(email.trim().lowercase())
        if (existing > 0) {
            return AuthResult.Error("An account with this email already exists")
        }
        val salt = PasswordUtils.generateSalt()
        val hash = PasswordUtils.hashPassword(password, salt)
        val user = User(
            name = name.trim(),
            email = email.trim().lowercase(),
            passwordHash = hash,
            salt = salt
        )
        val id = userDao.insert(user)
        return AuthResult.Success(user.copy(userId = id))
    }

    suspend fun login(email: String, password: String): AuthResult {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return AuthResult.Error("No account found with this email")
        val valid = PasswordUtils.verifyPassword(password, user.salt, user.passwordHash)
        return if (valid) {
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Incorrect password")
        }
    }

    suspend fun getUser(userId: Long): User? = userDao.findById(userId)

    suspend fun getUserByEmail(email: String): User? = userDao.findByEmail(email.trim().lowercase())

    suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): AuthResult {
        val user = userDao.findById(userId) ?: return AuthResult.Error("User not found")
        val valid = PasswordUtils.verifyPassword(oldPassword, user.salt, user.passwordHash)
        if (!valid) return AuthResult.Error("Current password is incorrect")

        val passwordCheck = np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.validatePasswordStrength(newPassword)
        if (passwordCheck is np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.ValidationResult.Invalid) {
            return AuthResult.Error(passwordCheck.message)
        }

        val newSalt = PasswordUtils.generateSalt()
        val newHash = PasswordUtils.hashPassword(newPassword, newSalt)
        val updated = user.copy(passwordHash = newHash, salt = newSalt)
        userDao.update(updated)
        return AuthResult.Success(updated)
    }

    suspend fun updateName(userId: Long, newName: String): User? {
        val user = userDao.findById(userId) ?: return null
        val updated = user.copy(name = newName)
        userDao.update(updated)
        return updated
    }

    /**
     * Simplified "forgot password" reset for this local-storage app: since there is no
     * email/SMS backend to verify identity, we only confirm the email matches a registered
     * account, then let the user set a new password directly. In a production app backed by
     * a server, this should instead send a one-time verification code or reset link to the
     * user's email before allowing the change.
     */
    suspend fun resetPassword(email: String, newPassword: String): AuthResult {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return AuthResult.Error("No account found with this email")

        val passwordCheck = np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.validatePasswordStrength(newPassword)
        if (passwordCheck is np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.ValidationResult.Invalid) {
            return AuthResult.Error(passwordCheck.message)
        }

        val newSalt = PasswordUtils.generateSalt()
        val newHash = PasswordUtils.hashPassword(newPassword, newSalt)
        val updated = user.copy(passwordHash = newHash, salt = newSalt)
        userDao.update(updated)
        return AuthResult.Success(updated)
    }
}
