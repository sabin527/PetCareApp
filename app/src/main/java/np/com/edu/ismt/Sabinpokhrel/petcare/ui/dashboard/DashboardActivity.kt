package np.com.edu.ismt.Sabinpokhrel.petcare.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityDashboardBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.home.HomeFragment
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.map.MapFragment
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet.PetsFragment
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.profile.ProfileFragment
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.task.TasksFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            switchFragment(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_pets -> PetsFragment()
                R.id.nav_tasks -> TasksFragment()
                R.id.nav_map -> MapFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            switchFragment(fragment)
            true
        }
    }

    /** Lets a fragment (e.g. Home's "More options" arrows) switch the selected bottom-nav tab. */
    fun navigateToTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
