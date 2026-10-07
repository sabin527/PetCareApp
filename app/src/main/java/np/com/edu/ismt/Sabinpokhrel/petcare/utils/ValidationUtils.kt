package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import android.util.Patterns

object ValidationUtils {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    // 8-30 characters, at least 1 uppercase letter, 1 number, and 1 special character.
    private val PASSWORD_POLICY_REGEX =
        Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]).{8,30}$")

    fun validateName(name: String): ValidationResult {
        if (name.trim().isEmpty()) return ValidationResult.Invalid("Name is empty")
        if (name.trim().length < 2) return ValidationResult.Invalid("Name is too short")
        return ValidationResult.Valid
    }

    /** Step 1: empty check. Step 2: format check. Matches the spec's two-stage email validation. */
    fun validateEmail(email: String): ValidationResult {
        if (email.trim().isEmpty()) return ValidationResult.Invalid("Email is empty")
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return ValidationResult.Invalid("Invalid email format")
        }
        return ValidationResult.Valid
    }

    /**
     * Password policy: 8-30 characters, at least 1 special character, 1 number,
     * and 1 uppercase letter.
     */
    fun validatePasswordStrength(password: String): ValidationResult {
        if (password.isEmpty()) return ValidationResult.Invalid("Password is empty")
        if (!PASSWORD_POLICY_REGEX.matches(password)) {
            return ValidationResult.Invalid(
                "Password must be 8-30 characters and include at least 1 uppercase letter, 1 number, and 1 special character"
            )
        }
        return ValidationResult.Valid
    }

    fun validateConfirmPassword(password: String, confirm: String): ValidationResult {
        if (confirm.isEmpty()) return ValidationResult.Invalid("Confirm password is empty")
        if (password != confirm) return ValidationResult.Invalid("Passwords do not match")
        return ValidationResult.Valid
    }

    fun validateNotEmpty(value: String, fieldName: String): ValidationResult {
        if (value.trim().isEmpty()) return ValidationResult.Invalid("$fieldName is empty")
        return ValidationResult.Valid
    }

    fun validatePhoneNumber(number: String): ValidationResult {
        val cleaned = number.trim()
        if (cleaned.isEmpty()) return ValidationResult.Invalid("Phone number is empty")
        if (!Patterns.PHONE.matcher(cleaned).matches() || cleaned.length < 7) {
            return ValidationResult.Invalid("Invalid phone number")
        }
        return ValidationResult.Valid
    }
}
