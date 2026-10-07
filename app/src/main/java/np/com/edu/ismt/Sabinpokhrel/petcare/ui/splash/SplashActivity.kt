package np.com.edu.ismt.Sabinpokhrel.petcare.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivitySplashBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.dashboard.DashboardActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.welcome.WelcomeActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val destination = if (session.isLoggedIn()) {
                Intent(this, DashboardActivity::class.java)
            } else {
                Intent(this, WelcomeActivity::class.java)
            }
            startActivity(destination)
            finish()
        }, 900)
    }
}
