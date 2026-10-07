package np.com.edu.ismt.Sabinpokhrel.petcare.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.TaskCategory
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityAddEditTaskBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ReminderScheduler
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.FormSuggestions
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private var petId: Long = -1
    private var existingTask: CareTask? = null
    private val calendar = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
    private val displayFormat = SimpleDateFormat("EEE d MMM yyyy, h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskRepository = RepositoryProvider.careTaskRepository(this)
        val petRepository = RepositoryProvider.petRepository(this)

        val categories = TaskCategory.values().map {
            it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        }
        binding.spinnerCategory.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, categories
        )

        updateSuggestionAdapters(TaskCategory.values()[binding.spinnerCategory.selectedItemPosition])
        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateSuggestionAdapters(TaskCategory.values()[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        updateDateTimeField()
        binding.etDateTime.setOnClickListener { showDateTimePicker() }

        if (taskId != -1L) {
            supportActionBar?.title = getString(R.string.edit_task)
            binding.btnSave.text = getString(R.string.update)
            lifecycleScope.launch {
                existingTask = taskRepository.getTaskById(taskId)
                existingTask?.let { populate(it) }
            }
        } else {
            supportActionBar?.title = getString(R.string.add_task)
            binding.btnSave.text = getString(R.string.save)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val check = ValidationUtils.validateNotEmpty(title, "Task title")
            if (check is ValidationUtils.ValidationResult.Invalid) {
                binding.tilTitle.error = check.message
                return@setOnClickListener
            }
            binding.tilTitle.error = null

            val category = TaskCategory.values()[binding.spinnerCategory.selectedItemPosition]
            val task = CareTask(
                taskId = existingTask?.taskId ?: 0,
                petId = petId,
                routineId = existingTask?.routineId,
                title = title,
                category = category,
                scheduledAt = calendar.timeInMillis,
                instructions = binding.etInstructions.text.toString().trim(),
                requiredSupplies = binding.etSupplies.text.toString().trim(),
                notes = binding.etNotes.text.toString().trim(),
                isCompleted = existingTask?.isCompleted ?: false,
                remindersEnabled = binding.cbReminder.isChecked,
                createdAt = existingTask?.createdAt ?: System.currentTimeMillis()
            )

            lifecycleScope.launch {
                val savedId = if (existingTask != null) {
                    taskRepository.updateTask(task)
                    task.taskId
                } else {
                    taskRepository.addTask(task)
                }
                val pet = petRepository.getPetByIdSync(petId)
                ReminderScheduler.cancel(this@AddEditTaskActivity, savedId)
                ReminderScheduler.schedule(this@AddEditTaskActivity, task.copy(taskId = savedId), pet?.name ?: "")
                finish()
            }
        }
    }

    private fun updateSuggestionAdapters(category: TaskCategory) {
        binding.etTitle.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FormSuggestions.taskTitles(category))
        )
        binding.etSupplies.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FormSuggestions.taskSupplies(category))
        )
    }

    private fun populate(task: CareTask) {
        binding.etTitle.setText(task.title, false)
        binding.spinnerCategory.setSelection(task.category.ordinal)
        calendar.timeInMillis = task.scheduledAt
        updateDateTimeField()
        binding.etInstructions.setText(task.instructions)
        binding.etSupplies.setText(task.requiredSupplies, false)
        binding.etNotes.setText(task.notes)
        binding.cbReminder.isChecked = task.remindersEnabled
    }

    private fun showDateTimePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        updateDateTimeField()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTimeField() {
        binding.etDateTime.setText(displayFormat.format(calendar.time))
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
