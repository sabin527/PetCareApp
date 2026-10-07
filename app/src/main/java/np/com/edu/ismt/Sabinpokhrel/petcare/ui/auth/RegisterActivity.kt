package np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.AuthResult
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityRegisterBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.ValidationResult
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    /**
     * When true, this screen was opened from Profile > "Add new account" while
     * already logged in. On success we do NOT switch the active session — we
     * just create the account and let the user know they can log into it later
     * from the Welcome screen. This app has no simultaneous multi-account
     * switcher; only one account can be signed in on the device at a time.
     */
    private var isAddingAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = RepositoryProvider.userRepository(this)

        isAddingAccount = intent.getBooleanExtra(EXTRA_ADDING_ACCOUNT, false)
        if (isAddingAccount) {
            binding.tvGoToLogin.visibility = android.view.View.INVISIBLE
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            // Sequential validation, stop and toast on the first failure.
            val nameCheck = ValidationUtils.validateName(name)
            if (nameCheck is ValidationResult.Invalid) {
                Toast.makeText(this, nameCheck.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Email: empty check, then format check.
            val emailCheck = ValidationUtils.validateEmail(email)
            if (emailCheck is ValidationResult.Invalid) {
                Toast.makeText(this, emailCheck.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password: empty check, then policy check (8-30 chars, 1 uppercase, 1 number, 1 special char).
            val passwordCheck = ValidationUtils.validatePasswordStrength(password)
            if (passwordCheck is ValidationResult.Invalid) {
                Toast.makeText(this, passwordCheck.message, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val confirmCheck = ValidationUtils.validateConfirmPassword(password, confirm)
            if (confirmCheck is ValidationResult.Invalid) {
                Toast.makeText(this, confirmCheck.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnRegister.isEnabled = false
            lifecycleScope.launch {
                when (val result = userRepository.register(name, email, password)) {
                    is AuthResult.Success -> {
                        if (isAddingAccount) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Account created. You can log in as ${result.user.name} from the Welcome screen.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration successful! Please log in to continue.",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(
                                Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                                    putExtra(LoginActivity.EXTRA_EMAIL, result.user.email)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                            finish()
                        }
                    }
                    is AuthResult.Error -> {
                        binding.btnRegister.isEnabled = true
                        // e.g. duplicate account
                        Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_ADDING_ACCOUNT = "extra_adding_account"
    }
}
