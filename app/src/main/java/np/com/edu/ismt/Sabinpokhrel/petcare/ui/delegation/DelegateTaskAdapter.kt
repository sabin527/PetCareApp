package np.com.edu.ismt.Sabinpokhrel.petcare.ui.delegation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ItemDelegateTaskBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DelegateTaskAdapter(
    private val tasks: List<CareTask>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<DelegateTaskAdapter.ViewHolder>() {

    private val selectedIds = mutableSetOf<Long>()
    private val dateFormat = SimpleDateFormat("EEE d MMM, h:mm a", Locale.getDefault())

    fun getSelectedTasks(): List<CareTask> = tasks.filter { selectedIds.contains(it.taskId) }

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDelegateTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.binding.tvDelegateTaskTitle.text = task.title
        holder.binding.tvDelegateTaskSchedule.text = dateFormat.format(task.scheduledAt)
        holder.binding.cbSelectTask.setOnCheckedChangeListener(null)
        holder.binding.cbSelectTask.isChecked = selectedIds.contains(task.taskId)
        holder.binding.cbSelectTask.setOnCheckedChangeListener { _, checked ->
            if (checked) selectedIds.add(task.taskId) else selectedIds.remove(task.taskId)
            onSelectionChanged()
        }
        holder.binding.root.setOnClickListener { holder.binding.cbSelectTask.toggle() }
    }

    override fun getItemCount(): Int = tasks.size

    class ViewHolder(val binding: ItemDelegateTaskBinding) : RecyclerView.ViewHolder(binding.root)
}
