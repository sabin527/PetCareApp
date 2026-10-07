package np.com.edu.ismt.Sabinpokhrel.petcare.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import np.com.edu.ismt.Sabinpokhrel.petcare.BuildConfig
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.AuthResult
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.FragmentProfileBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth.LoginActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.AppPreferences
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private lateinit var prefs: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        session = SessionManager(context)
        prefs = AppPreferences(context)
        val userRepository = RepositoryProvider.userRepository(context)

        binding.tvUserName.text = session.getUserName()
        binding.tvUserEmail.text = session.getUserEmail()

        // --- Dark mode ---
        binding.switchDarkMode.isChecked = prefs.isDarkModeEnabled()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDarkModeEnabled(isChecked)
            requireActivity().recreate()
        }

        // --- Task reminders (opens per-category settings) ---
        binding.rowNotifications.setOnClickListener { showNotificationsDialog() }

        // --- Weight unit ---
        binding.tvUnitValue.text = prefs.getWeightUnit()
        binding.rowUnits.setOnClickListener {
            binding.tvUnitValue.text = prefs.toggleWeightUnit()
        }

        // --- Edit profile ---
        binding.rowEditProfile.setOnClickListener { showEditProfileDialog(userRepository) }

        // --- Change password ---
        binding.rowChangePassword.setOnClickListener { showChangePasswordDialog(userRepository) }

        // --- Account details ---
        binding.rowAccountDetails.setOnClickListener { showAccountDetailsDialog(userRepository) }

        // --- Add new account ---
        binding.rowAddAccount.setOnClickListener {
            startActivity(
                Intent(context, np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth.RegisterActivity::class.java)
                    .putExtra(
                        np.com.edu.ismt.Sabinpokhrel.petcare.ui.auth.RegisterActivity.EXTRA_ADDING_ACCOUNT,
                        true
                    )
            )
        }

        // --- Help & support ---
        binding.rowHelp.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Help & support")
                .setMessage(
                    "Need help using PetCare?\n\n" +
                        "• Add a pet from the Pets tab, then create routines or tasks from its profile.\n" +
                        "• Delegate a task via SMS from a pet's profile or the Tasks tab.\n" +
                        "• Save vet clinics, groomers and parks from the Places tab.\n\n" +
                        "For further support, contact your app administrator."
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // --- About ---
        binding.rowAbout.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("About PetCare")
                .setMessage("PetCare v${BuildConfig.VERSION_NAME}\nA simple way to manage your pets' care routines, tasks, and important places.")
                .setPositiveButton("OK", null)
                .show()
        }

        // --- Logout ---
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout") { _, _ ->
                    session.logout()
                    startActivity(
                        Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showEditProfileDialog(userRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.UserRepository) {
        val context = requireContext()
        val input = EditText(context).apply {
            setText(session.getUserName())
            hint = "Enter Full Name"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(input)
        }

        AlertDialog.Builder(context)
            .setTitle("Edit profile")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(context, "Name is empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    userRepository.updateName(session.getUserId(), newName)
                    session.updateUserName(newName)
                    binding.tvUserName.text = newName
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog(userRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.UserRepository) {
        val context = requireContext()

        fun passwordField(hint: String) = EditText(context).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val currentField = passwordField("Enter Current Password")
        val newField = passwordField("Enter New Password")
        val confirmField = passwordField("Enter Confirm New Password")

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(currentField)
            addView(newField)
            addView(confirmField)
        }

        AlertDialog.Builder(context)
            .setTitle("Change password")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val current = currentField.text.toString()
                val newPassword = newField.text.toString()
                val confirm = confirmField.text.toString()

                if (newPassword != confirm) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    when (val result = userRepository.changePassword(session.getUserId(), current, newPassword)) {
                        is AuthResult.Success -> Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                        is AuthResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationsDialog() {
        val context = requireContext()
        val categories = np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.TaskCategory.values()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        val masterRow = android.widget.Switch(context).apply {
            text = "All reminders"
            isChecked = prefs.areRemindersEnabled()
        }
        container.addView(masterRow)
        container.addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(-1, 24) })

        val categorySwitches = categories.map { category ->
            android.widget.Switch(context).apply {
                text = category.name.lowercase().replaceFirstChar { it.uppercase() }
                isChecked = prefs.isCategoryReminderEnabled(category.name)
                isEnabled = masterRow.isChecked
            }.also { container.addView(it) }
        }

        masterRow.setOnCheckedChangeListener { _, isChecked ->
            categorySwitches.forEach { it.isEnabled = isChecked }
        }

        AlertDialog.Builder(context)
            .setTitle("Notifications")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                prefs.setRemindersEnabled(masterRow.isChecked)
                categories.forEachIndexed { i, category ->
                    prefs.setCategoryReminderEnabled(category.name, categorySwitches[i].isChecked)
                }
                Toast.makeText(context, "Notification settings saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAccountDetailsDialog(userRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.UserRepository) {
        val context = requireContext()
        val petRepository = RepositoryProvider.petRepository(context)
        val dateFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())

        lifecycleScope.launch {
            val user = userRepository.getUser(session.getUserId())
            val petCount = petRepository.getPetsForOwnerSync(session.getUserId()).size

            val message = buildString {
                append("Name: ${user?.name ?: session.getUserName()}\n")
                append("Email: ${user?.email ?: session.getUserEmail()}\n")
                if (user != null) append("Member since: ${dateFormat.format(user.createdAt)}\n")
                append("Pets registered: $petCount")
            }

            AlertDialog.Builder(context)
                .setTitle("Account details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
