package np.com.edu.ismt.Sabinpokhrel.petcare.ui.routine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Frequency
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Routine
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ItemRoutineBinding

class RoutineAdapter(
    private val onClick: (Routine) -> Unit,
    private val onDelete: (Routine) -> Unit
) : ListAdapter<Routine, RoutineAdapter.RoutineViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoutineViewHolder(private val binding: ItemRoutineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(routine: Routine) {
            binding.tvRoutineTitle.text = routine.title
            val time = String.format("%02d:%02d", routine.hour, routine.minute)
            val freqText = if (routine.frequency == Frequency.DAILY) {
                "Daily at $time"
            } else {
                val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val dayName = routine.dayOfWeek?.let { days.getOrNull(it - 1) } ?: ""
                "Weekly on $dayName at $time"
            }
            binding.tvRoutineDetails.text = "${routine.category.name.lowercase().replaceFirstChar { it.uppercase() }} · $freqText"

            binding.root.setOnClickListener { onClick(routine) }
            binding.btnDeleteRoutine.setOnClickListener { onDelete(routine) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Routine>() {
            override fun areItemsTheSame(oldItem: Routine, newItem: Routine) = oldItem.routineId == newItem.routineId
            override fun areContentsTheSame(oldItem: Routine, newItem: Routine) = oldItem == newItem
        }
    }
}
