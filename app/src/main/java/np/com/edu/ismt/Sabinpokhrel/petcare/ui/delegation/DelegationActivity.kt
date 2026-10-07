package np.com.edu.ismt.Sabinpokhrel.petcare.ui.delegation

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityDelegationBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SmsUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DelegationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDelegationBinding
    private lateinit var delegateAdapter: DelegateTaskAdapter
    private var petId: Long = -1
    private var petName: String = "your pet"
    private val dateFormat = SimpleDateFormat("EEE d MMM, h:mm a", Locale.getDefault())

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { resolveContactPhoneNumber(it) }
    }

    private val requestContactsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) pickContactLauncher.launch(null) }

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) performSend() }

    // Some phones/emulators check the Phone permission when an app sends an SMS and
    // fail with errors such as "getGroupIdLevel1" without it. We only ask for it
    // if a send actually fails for that reason, then retry automatically.
    private val requestPhoneStatePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            performSend()
        } else {
            showMessageDialog(
                "Couldn't send",
                "This phone needs the Phone permission to send the message from the app. " +
                        "Allow it in Settings > Apps > PetCare > Permissions, then try again."
            )
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDelegationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        val taskRepository = RepositoryProvider.careTaskRepository(this)
        val petRepository = RepositoryProvider.petRepository(this)

        binding.rvDelegateTasks.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val pet = petRepository.getPetByIdSync(petId)
            petName = pet?.name ?: "your pet"

            taskRepository.getTasksForPet(petId).observe(this@DelegationActivity) { tasks ->
                val incomplete = tasks.filter { !it.isCompleted }
                binding.tvNoTasks.visibility = if (incomplete.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                delegateAdapter = DelegateTaskAdapter(incomplete) { updateMessagePreview() }
                binding.rvDelegateTasks.adapter = delegateAdapter
            }
        }

        binding.btnPickContact.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                pickContactLauncher.launch(null)
            } else {
                requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        binding.btnSendSms.setOnClickListener {
            updateMessagePreview(forceIfEmpty = true)
            val phoneCheck = ValidationUtils.validatePhoneNumber(binding.etPhoneNumber.text.toString())
            if (phoneCheck is ValidationUtils.ValidationResult.Invalid) {
                Toast.makeText(this, phoneCheck.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.etMessage.text.toString().isBlank()) {
                Toast.makeText(this, "Please select at least one task or write a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                performSend()
            } else {
                requestSmsPermission.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    private fun updateMessagePreview(forceIfEmpty: Boolean = false) {
        if (!::delegateAdapter.isInitialized) return
        val selected = delegateAdapter.getSelectedTasks()
        if (selected.isEmpty() && !forceIfEmpty) {
            if (binding.etMessage.text.isNullOrBlank()) return
        }
        if (selected.isNotEmpty()) {
            binding.etMessage.setText(composeMessage(selected))
        }
    }

    private fun composeMessage(tasks: List<CareTask>): String {
        val sb = StringBuilder()
        sb.append("Hi! Could you help take care of $petName? Here's the checklist:\n\n")
        tasks.sortedBy { it.scheduledAt }.forEach { task ->
            sb.append("• [${task.category.name}] ${task.title} — ${dateFormat.format(task.scheduledAt)}\n")
            if (task.instructions.isNotBlank()) sb.append("   Instructions: ${task.instructions}\n")
            if (task.requiredSupplies.isNotBlank()) sb.append("   Supplies: ${task.requiredSupplies}\n")
        }
        sb.append("\nThanks so much!")
        return sb.toString()
    }

    private fun showMessageDialog(title: String, message: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun performSend() {
        val phone = binding.etPhoneNumber.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        // Stop double taps while we wait for the system to confirm the send.
        binding.btnSendSms.isEnabled = false

        SmsUtils.sendSms(this, phone, message) { result, detail ->
            if (isDestroyed) return@sendSms
            binding.btnSendSms.isEnabled = true
            when (result) {
                SmsUtils.Result.SENT -> {
                    // Sent straight from the app: clear the form and confirm.
                    clearForm()
                    showMessageDialog("Message Sent", "Delegation message sent to $phone successfully!")
                }
                SmsUtils.Result.NOT_CONFIRMED ->
                    showMessageDialog("Not confirmed", detail ?: "The message could not be confirmed.")
                SmsUtils.Result.PERMISSION_DENIED -> {
                    if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
                        requestPhoneStatePermission.launch(Manifest.permission.READ_PHONE_STATE)
                    } else {
                        showMessageDialog("Couldn't send", detail ?: "The phone refused to send the message.")
                    }
                }
                SmsUtils.Result.FAILED ->
                    showMessageDialog("Couldn't send", detail ?: "The message could not be sent.")
            }
        }
    }

    private fun clearForm() {
        binding.etMessage.text?.clear()
        binding.etPhoneNumber.text?.clear()
        if (::delegateAdapter.isInitialized) {
            delegateAdapter.clearSelection()
        }
    }

    private fun resolveContactPhoneNumber(contactUri: Uri) {
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(contactUri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                if (idIndex < 0) return
                val contactId = cursor.getString(idIndex)
                val hasPhone = hasPhoneIndex >= 0 && cursor.getInt(hasPhoneIndex) > 0
                if (hasPhone) {
                    val phones = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    phones?.use { p ->
                        if (p.moveToFirst()) {
                            val numberIndex = p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex >= 0) {
                                binding.etPhoneNumber.setText(p.getString(numberIndex))
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Selected contact has no phone number", Toast.LENGTH_SHORT).show()
                }
            }
        } finally {
            cursor?.close()
        }
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
    }
}