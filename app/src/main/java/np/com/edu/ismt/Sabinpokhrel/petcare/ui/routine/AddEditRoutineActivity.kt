package np.com.edu.ismt.Sabinpokhrel.petcare.ui.routine

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Frequency
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Routine
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.TaskCategory
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityAddEditRoutineBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ImageUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ReminderScheduler
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditRoutineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditRoutineBinding
    private var petId: Long = -1
    private var existingRoutine: Routine? = null
    private var photoUri: Uri? = null
    private var pendingCameraUri: Uri? = null
    private var hour = 8
    private var minute = 0

    private val dayNames = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            ImageUtils.takePersistablePermission(this, it)
            photoUri = it
            binding.ivRoutineImage.load(it) { error(R.drawable.ic_pet_placeholder) }
        }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingCameraUri != null) {
            photoUri = pendingCameraUri
            binding.ivRoutineImage.load(pendingCameraUri) { error(R.drawable.ic_pet_placeholder) }
        }
    }
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = ImageUtils.createImageUri(this)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        val routineId = intent.getLongExtra(EXTRA_ROUTINE_ID, -1L)
        val routineRepository = RepositoryProvider.routineRepository(this)
        val petRepository = RepositoryProvider.petRepository(this)

        val categories = TaskCategory.values().map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        binding.spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dayNames)

        updateTimeField()

        binding.rgFrequency.setOnCheckedChangeListener { _, checkedId ->
            val isWeekly = checkedId == binding.rbWeekly.id
            binding.tvDayLabel.visibility = if (isWeekly) View.VISIBLE else View.GONE
            binding.spinnerDay.visibility = if (isWeekly) View.VISIBLE else View.GONE
        }

        binding.etTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                hour = h; minute = m; updateTimeField()
            }, hour, minute, false).show()
        }

        binding.ivRoutineImage.setOnClickListener { showImagePickerOptions() }
        binding.btnChangePhoto.setOnClickListener { showImagePickerOptions() }

        if (routineId != -1L) {
            supportActionBar?.title = getString(R.string.edit_routine)
            binding.btnSave.text = getString(R.string.update)
            lifecycleScope.launch {
                existingRoutine = routineRepository.getRoutineById(routineId)
                existingRoutine?.let { populate(it) }
            }
        } else {
            supportActionBar?.title = getString(R.string.add_routine)
            binding.btnSave.text = getString(R.string.save)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val check = ValidationUtils.validateNotEmpty(title, "Routine title")
            if (check is ValidationUtils.ValidationResult.Invalid) {
                android.widget.Toast.makeText(this, check.message, android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = TaskCategory.values()[binding.spinnerCategory.selectedItemPosition]
            val frequency = if (binding.rbWeekly.isChecked) Frequency.WEEKLY else Frequency.DAILY
            val dayOfWeek = if (frequency == Frequency.WEEKLY) binding.spinnerDay.selectedItemPosition + 1 else null

            val routine = Routine(
                routineId = existingRoutine?.routineId ?: 0,
                petId = petId,
                title = title,
                category = category,
                frequency = frequency,
                dayOfWeek = dayOfWeek,
                hour = hour,
                minute = minute,
                requiredSupplies = binding.etSupplies.text.toString().trim(),
                notes = binding.etNotes.text.toString().trim(),
                imageUri = (photoUri ?: existingRoutine?.imageUri?.let { Uri.parse(it) })?.toString(),
                remindersEnabled = binding.cbReminder.isChecked,
                createdAt = existingRoutine?.createdAt ?: System.currentTimeMillis()
            )

            lifecycleScope.launch {
                val routineId2 = routineRepository.saveRoutineAndGenerateTasks(routine)
                val pet = petRepository.getPetByIdSync(petId)
                // Schedule reminders for all upcoming tasks belonging to this routine.
                scheduleRemindersForRoutine(routineId2, pet?.name ?: "")
                finish()
            }
        }
    }

    private suspend fun scheduleRemindersForRoutine(routineId: Long, petName: String) {
        val db = (application as np.com.edu.ismt.Sabinpokhrel.petcare.PetCareApp).database
        val tasks = db.careTaskDao().getTasksForRoutineSync(routineId)
        tasks.forEach { task ->
            ReminderScheduler.cancel(this, task.taskId)
            ReminderScheduler.schedule(this, task, petName)
        }
    }

    private fun populate(routine: Routine) {
        binding.etTitle.setText(routine.title)
        binding.spinnerCategory.setSelection(routine.category.ordinal)
        if (routine.frequency == Frequency.WEEKLY) {
            binding.rbWeekly.isChecked = true
            binding.tvDayLabel.visibility = View.VISIBLE
            binding.spinnerDay.visibility = View.VISIBLE
            binding.spinnerDay.setSelection((routine.dayOfWeek ?: 1) - 1)
        } else {
            binding.rbDaily.isChecked = true
        }
        hour = routine.hour
        minute = routine.minute
        updateTimeField()
        binding.etSupplies.setText(routine.requiredSupplies)
        binding.etNotes.setText(routine.notes)
        binding.cbReminder.isChecked = routine.remindersEnabled
        if (!routine.imageUri.isNullOrBlank()) {
            binding.ivRoutineImage.load(Uri.parse(routine.imageUri)) { error(R.drawable.ic_pet_placeholder) }
        }
    }

    private fun updateTimeField() {
        binding.etTime.setText(String.format("%02d:%02d", hour, minute))
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Routine photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission.launch(android.Manifest.permission.CAMERA)
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
        const val EXTRA_ROUTINE_ID = "extra_routine_id"
    }
}