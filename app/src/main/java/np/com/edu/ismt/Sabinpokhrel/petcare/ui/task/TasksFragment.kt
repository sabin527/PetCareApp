package np.com.edu.ismt.Sabinpokhrel.petcare.ui.task

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.TaskCategory
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.FragmentTasksBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.delegation.DelegationActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ReminderScheduler
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Shows the checklist of all pet care tasks across every pet the user owns,
 * with a category filter row and per-task Edit / Delete / Share(delegate) actions.
 */
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter

    private var allTasks: List<CareTask> = emptyList()
    private var petNamesById: Map<Long, String> = emptyMap()
    private var activeFilter: TaskCategory? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val session = SessionManager(context)
        val taskRepository = RepositoryProvider.careTaskRepository(context)
        val petRepository = RepositoryProvider.petRepository(context)

        adapter = TaskAdapter(
            petNameProvider = { petId -> petNamesById[petId] ?: "" },
            onToggleCompleted = { task, checked ->
                lifecycleScope.launch {
                    taskRepository.setCompleted(task.taskId, checked)
                    if (checked) ReminderScheduler.cancel(context, task.taskId)
                }
            },
            onEdit = { task ->
                startActivity(
                    Intent(context, AddEditTaskActivity::class.java)
                        .putExtra(AddEditTaskActivity.EXTRA_PET_ID, task.petId)
                        .putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.taskId)
                )
            },
            onDelete = { task ->
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.delete))
                    .setMessage(R.string.delete_task_confirm)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch {
                            taskRepository.deleteTask(task)
                            ReminderScheduler.cancel(context, task.taskId)
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            },
            onDelegate = { task ->
                startActivity(
                    Intent(context, DelegationActivity::class.java)
                        .putExtra(DelegationActivity.EXTRA_PET_ID, task.petId)
                )
            }
        )

        binding.rvAllTasks.layoutManager = LinearLayoutManager(context)
        binding.rvAllTasks.adapter = adapter

        petRepository.getPetsForOwner(session.getUserId()).observe(viewLifecycleOwner) { pets ->
            petNamesById = pets.associate { it.petId to it.name }
            adapter.notifyDataSetChanged()
        }

        taskRepository.getTasksForOwner(session.getUserId()).observe(viewLifecycleOwner) { tasks ->
            allTasks = tasks
            applyFilter()
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            activeFilter = when (checkedIds.firstOrNull()) {
                binding.chipFeeding.id -> TaskCategory.FEEDING
                binding.chipExercise.id -> TaskCategory.EXERCISE
                binding.chipGrooming.id -> TaskCategory.GROOMING
                binding.chipMedication.id -> TaskCategory.MEDICATION
                binding.chipHealthcare.id -> TaskCategory.HEALTHCARE
                else -> null
            }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = activeFilter?.let { cat -> allTasks.filter { it.category == cat } } ?: allTasks
        adapter.submitList(filtered)
        binding.emptyState.visibility = if (allTasks.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
