package np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityPetDetailBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.delegation.DelegationActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.routine.AddEditRoutineActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.routine.RoutineAdapter
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.task.AddEditTaskActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.task.TaskAdapter
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ReminderScheduler
import kotlinx.coroutines.launch

class PetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPetDetailBinding
    private var petId: Long = -1
    private var currentPet: Pet? = null

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var routineAdapter: RoutineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        if (petId == -1L) { finish(); return }

        val petRepository = RepositoryProvider.petRepository(this)
        val taskRepository = RepositoryProvider.careTaskRepository(this)
        val routineRepository = RepositoryProvider.routineRepository(this)

        setupTasksList(taskRepository)
        setupRoutinesList(routineRepository)

        petRepository.getPetById(petId).observe(this) { pet ->
            pet?.let {
                currentPet = it
                bindPet(it)
            }
        }

        taskRepository.getTasksForPet(petId).observe(this) { tasks ->
            taskAdapter.submitList(tasks)
        }

        routineRepository.getRoutinesForPet(petId).observe(this) { routines ->
            routineAdapter.submitList(routines)
        }

        binding.btnEditPet.setOnClickListener {
            startActivity(Intent(this, AddEditPetActivity::class.java).putExtra(AddEditPetActivity.EXTRA_PET_ID, petId))
        }

        binding.btnDeletePet.setOnClickListener { confirmDeletePet(petRepository) }

        binding.btnAddTask.setOnClickListener {
            startActivity(Intent(this, AddEditTaskActivity::class.java).putExtra(AddEditTaskActivity.EXTRA_PET_ID, petId))
        }

        binding.btnAddRoutine.setOnClickListener {
            startActivity(Intent(this, AddEditRoutineActivity::class.java).putExtra(AddEditRoutineActivity.EXTRA_PET_ID, petId))
        }

        binding.btnDelegate.setOnClickListener {
            startActivity(Intent(this, DelegationActivity::class.java).putExtra(DelegationActivity.EXTRA_PET_ID, petId))
        }
    }

    private fun setupTasksList(taskRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.CareTaskRepository) {
        taskAdapter = TaskAdapter(
            petNameProvider = { _ -> currentPet?.name ?: "" },
            onToggleCompleted = { task, checked ->
                lifecycleScope.launch {
                    taskRepository.setCompleted(task.taskId, checked)
                    if (checked) ReminderScheduler.cancel(this@PetDetailActivity, task.taskId)
                }
            },
            onEdit = { task ->
                startActivity(
                    Intent(this, AddEditTaskActivity::class.java)
                        .putExtra(AddEditTaskActivity.EXTRA_PET_ID, petId)
                        .putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.taskId)
                )
            },
            onDelete = { task ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete))
                    .setMessage(R.string.delete_task_confirm)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch {
                            taskRepository.deleteTask(task)
                            ReminderScheduler.cancel(this@PetDetailActivity, task.taskId)
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            },
            onDelegate = { task ->
                startActivity(
                    Intent(this, DelegationActivity::class.java)
                        .putExtra(DelegationActivity.EXTRA_PET_ID, task.petId)
                )
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = taskAdapter
    }

    private fun setupRoutinesList(routineRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RoutineRepository) {
        routineAdapter = RoutineAdapter(
            onClick = { routine ->
                startActivity(
                    Intent(this, AddEditRoutineActivity::class.java)
                        .putExtra(AddEditRoutineActivity.EXTRA_PET_ID, petId)
                        .putExtra(AddEditRoutineActivity.EXTRA_ROUTINE_ID, routine.routineId)
                )
            },
            onDelete = { routine ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete))
                    .setMessage(R.string.delete_routine_confirm)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch { routineRepository.deleteRoutine(routine) }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.rvRoutines.layoutManager = LinearLayoutManager(this)
        binding.rvRoutines.adapter = routineAdapter
    }

    private fun bindPet(pet: Pet) {
        supportActionBar?.title = pet.name
        binding.tvPetName.text = pet.name
        val subtitle = listOfNotNull(
            pet.breed.ifBlank { null },
            pet.age.ifBlank { null }?.let { "$it yrs" },
            pet.weight.ifBlank { null }?.let { "${it}kg" }
        ).joinToString(" · ")
        binding.tvPetSubtitle.text = subtitle

        binding.tvDiet.text = "Diet: ${pet.dietaryPreferences.ifBlank { "—" }}"
        binding.tvVaccination.text = "Vaccinations: ${pet.vaccinationHistory.ifBlank { "—" }}"
        binding.tvAllergies.text = "Allergies: ${pet.allergies.ifBlank { "None recorded" }}"
        binding.tvFavToys.text = "Favourite toys: ${pet.favouriteToys.ifBlank { "—" }}"
        binding.tvNotes.text = "Notes: ${pet.notes.ifBlank { "—" }}"

        if (!pet.photoUri.isNullOrBlank()) {
            binding.ivPetPhoto.load(Uri.parse(pet.photoUri)) { error(R.drawable.ic_pet_placeholder) }
        }
    }

    private fun confirmDeletePet(petRepository: np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.PetRepository) {
        val pet = currentPet ?: return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_pet))
            .setMessage(R.string.delete_pet_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    petRepository.deletePet(pet) // Room cascades: deletes its routines & tasks
                    finish()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
    }
}
