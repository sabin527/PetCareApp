package np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.AuthResult
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityForgotPasswordBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.ValidationResult
import kotlinx.coroutines.launch

/**
 * Local, two-step "forgot password" flow.
 *
 * Step 1: the user enters their account email; we just confirm it belongs to
 * a registered account (no OTP/email link, since this app has no backend).
 * Step 2: the new-password fields appear, validated against the same
 * password policy as sign-up, and saved via UserRepository.resetPassword().
 *
 * Note: because there's no way to verify the requester actually owns that
 * email address, this is weaker than a real production reset flow (which
 * should email a one-time code or link). It's an intentional simplification
 * for this offline, local-storage app.
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private var emailConfirmed: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = RepositoryProvider.userRepository(this)

        binding.btnForgotContinue.setOnClickListener {
            if (emailConfirmed == null) {
                handleStep1(userRepository)
            } else {
                handleStep2(userRepository)
            }
        }

        binding.tvBackToLogin.setOnClickListener { finish() }
    }

    private fun handleStep1(userRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.UserRepository) {
        val email = binding.etEmail.text.toString()
        val emailCheck = ValidationUtils.validateEmail(email)
        if (emailCheck is ValidationResult.Invalid) {
            Toast.makeText(this, emailCheck.message, Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnForgotContinue.isEnabled = false
        lifecycleScope.launch {
            val user = userRepository.getUserByEmail(email)
            binding.btnForgotContinue.isEnabled = true
            if (user == null) {
                Toast.makeText(this@ForgotPasswordActivity, "No account found with this email", Toast.LENGTH_SHORT).show()
                return@launch
            }
            emailConfirmed = email.trim().lowercase()
            binding.etEmail.isEnabled = false
            binding.tvForgotStep1Hint.text = "Enter a new password for this account"
            binding.groupNewPassword.visibility = View.VISIBLE
            binding.btnForgotContinue.text = "Reset password"
        }
    }

    private fun handleStep2(userRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.UserRepository) {
        val email = emailConfirmed ?: return
        val newPassword = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmNewPassword.text.toString()

        val passwordCheck = ValidationUtils.validatePasswordStrength(newPassword)
        if (passwordCheck is ValidationResult.Invalid) {
            Toast.makeText(this, passwordCheck.message, Toast.LENGTH_LONG).show()
            return
        }
        val confirmCheck = ValidationUtils.validateConfirmPassword(newPassword, confirm)
        if (confirmCheck is ValidationResult.Invalid) {
            Toast.makeText(this, confirmCheck.message, Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnForgotContinue.isEnabled = false
        lifecycleScope.launch {
            when (val result = userRepository.resetPassword(email, newPassword)) {
                is AuthResult.Success -> {
                    Toast.makeText(this@ForgotPasswordActivity, "Password reset. Please log in.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is AuthResult.Error -> {
                    binding.btnForgotContinue.isEnabled = true
                    Toast.makeText(this@ForgotPasswordActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
