package np.com.edu.ismt.Sabinpokhrel.petcare.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val petNameProvider: (Long) -> String,
    private val onToggleCompleted: (CareTask, Boolean) -> Unit,
    private val onEdit: (CareTask) -> Unit,
    private val onDelete: (CareTask) -> Unit,
    private val onDelegate: (CareTask) -> Unit
) : ListAdapter<CareTask, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("EEE d MMM, h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: CareTask) {
            binding.tvTaskCategory.text = task.category.name
            binding.tvTaskPetName.text = petNameProvider(task.petId).uppercase()
            binding.tvTaskTitle.text = task.title
            binding.tvTaskSchedule.text = dateFormat.format(task.scheduledAt)

            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = task.isCompleted
            applyCompletedStyle(task.isCompleted)

            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                applyCompletedStyle(isChecked)
                onToggleCompleted(task, isChecked)
            }

            binding.btnEditTask.setOnClickListener { onEdit(task) }
            binding.btnDeleteTask.setOnClickListener { onDelete(task) }
            binding.btnDelegateTask.setOnClickListener { onDelegate(task) }
        }

        private fun applyCompletedStyle(completed: Boolean) {
            if (completed) {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(
                    binding.root.context.getColor(np.com.edu.ismt.Sabinpokhrel.petcare.R.color.completed)
                )
            } else {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(
                    binding.root.context.getColor(np.com.edu.ismt.Sabinpokhrel.petcare.R.color.text_primary)
                )
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CareTask>() {
            override fun areItemsTheSame(oldItem: CareTask, newItem: CareTask) = oldItem.taskId == newItem.taskId
            override fun areContentsTheSame(oldItem: CareTask, newItem: CareTask) = oldItem == newItem
        }
    }
}
