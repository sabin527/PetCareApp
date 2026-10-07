package np.com.edu.ismt.Sabinpokhrel.petcare.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityWelcomeBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth.LoginActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth.RegisterActivity

/**
 * Shown right after the splash screen for users who are not yet logged in.
 * Offers a simple welcome greeting plus explicit Login / Sign up entry points.
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnWelcomeLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnWelcomeRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
