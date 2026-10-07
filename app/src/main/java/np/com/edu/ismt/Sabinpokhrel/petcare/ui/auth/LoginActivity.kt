package np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.AuthResult
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityLoginBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.dashboard.DashboardActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils.ValidationResult
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val userRepository = RepositoryProvider.userRepository(this)

        val prefilledEmail = intent.getStringExtra(EXTRA_EMAIL)
        if (!prefilledEmail.isNullOrEmpty()) {
            binding.etEmail.setText(prefilledEmail)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Step 1: validate email (empty -> format). Stop and toast on first failure.
            val emailCheck = ValidationUtils.validateEmail(email)
            if (emailCheck is ValidationResult.Invalid) {
                Toast.makeText(this, emailCheck.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Step 2: validate password (empty check only on login; policy is enforced at sign-up).
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false
            lifecycleScope.launch {
                when (val result = userRepository.login(email, password)) {
                    is AuthResult.Success -> {
                        sessionManager.createSession(
                            result.user.userId, result.user.name, result.user.email
                        )
                        startActivity(
                            Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish()
                    }
                    is AuthResult.Error -> {
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
