package np.com.edu.ismt.Sabinpokhrel.petcare.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import coil.load
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.FragmentHomeBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ItemHomePetCardBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ItemHomeTaskCardBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.dashboard.DashboardActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet.AddEditPetActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet.PetDetailActivity
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Home tab: greeting/summary, a small slice of the Pets and Tasks pages,
 * and an empty state that lets the user add their first pet directly.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val dateFormat = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())

    private var petsById: Map<Long, Pet> = emptyMap()
    private var petsEmpty = true
    private var tasksEmpty = true
    private var incompleteTaskCount = 0
    private var petCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val session = SessionManager(context)
        val petRepository = RepositoryProvider.petRepository(context)
        val taskRepository = RepositoryProvider.careTaskRepository(context)

        val firstName = session.getUserName().trim().split(" ").firstOrNull().orEmpty()
        binding.tvGreeting.text = if (firstName.isNotBlank()) "Hi, $firstName \uD83D\uDC4B" else "Hi there \uD83D\uDC4B"

        binding.btnMorePets.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToTab(R.id.nav_pets)
        }
        binding.btnMoreTasks.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToTab(R.id.nav_tasks)
        }
        binding.btnEmptyAddPet.setOnClickListener {
            startActivity(Intent(context, AddEditPetActivity::class.java))
        }

        petRepository.getPetsForOwner(session.getUserId()).observe(viewLifecycleOwner) { pets ->
            petsById = pets.associateBy { it.petId }
            petCount = pets.size
            petsEmpty = pets.isEmpty()
            renderPets(pets)
            refreshSummaryAndEmptyState()
        }

        taskRepository.getTasksForOwner(session.getUserId()).observe(viewLifecycleOwner) { tasks ->
            tasksEmpty = tasks.isEmpty()
            val incomplete = tasks.filter { !it.isCompleted }
            incompleteTaskCount = incomplete.size
            renderTasks(incomplete)
            refreshSummaryAndEmptyState()
        }
    }

    private fun renderPets(pets: List<Pet>) {
        binding.sectionPets.visibility = if (pets.isEmpty()) View.GONE else View.VISIBLE
        binding.homePetCardsContainer.removeAllViews()

        pets.take(2).forEach { pet ->
            val cardBinding = ItemHomePetCardBinding.inflate(
                LayoutInflater.from(requireContext()), binding.homePetCardsContainer, false
            )
            cardBinding.tvHomePetName.text = pet.name
            if (!pet.photoUri.isNullOrBlank()) {
                cardBinding.ivHomePetPhoto.visibility = View.VISIBLE
                cardBinding.ivHomePetIcon.visibility = View.GONE
                cardBinding.ivHomePetPhoto.load(Uri.parse(pet.photoUri)) {
                    error(R.drawable.ic_pet_placeholder)
                }
            }
            cardBinding.root.setOnClickListener {
                startActivity(
                    Intent(requireContext(), PetDetailActivity::class.java)
                        .putExtra(PetDetailActivity.EXTRA_PET_ID, pet.petId)
                )
            }
            binding.homePetCardsContainer.addView(cardBinding.root)
        }
        // Keep two-column balance even with a single pet.
        if (pets.size == 1) {
            val spacer = View(requireContext())
            spacer.layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
            binding.homePetCardsContainer.addView(spacer)
        }
    }

    private fun renderTasks(incompleteTasks: List<CareTask>) {
        binding.sectionTasks.visibility = if (incompleteTasks.isEmpty()) View.GONE else View.VISIBLE
        binding.homeTaskCardsContainer.removeAllViews()
        val context = requireContext()

        incompleteTasks.sortedBy { it.scheduledAt }.take(2).forEach { task ->
            val cardBinding = ItemHomeTaskCardBinding.inflate(
                LayoutInflater.from(context), binding.homeTaskCardsContainer, false
            )
            val petName = petsById[task.petId]?.name ?: ""
            cardBinding.tvHomeTaskCategory.text = task.category.name
            cardBinding.tvHomeTaskTitle.text = task.title
            cardBinding.tvHomeTaskMeta.text = if (petName.isNotBlank()) {
                "$petName · ${dateFormat.format(task.scheduledAt)}"
            } else {
                dateFormat.format(task.scheduledAt)
            }

            cardBinding.root.setOnClickListener {
                startActivity(
                    Intent(context, PetDetailActivity::class.java)
                        .putExtra(PetDetailActivity.EXTRA_PET_ID, task.petId)
                )
            }
            binding.homeTaskCardsContainer.addView(cardBinding.root)
        }
    }

    private fun refreshSummaryAndEmptyState() {
        binding.tvSummarySubtitle.text = when {
            petCount == 0 && incompleteTaskCount == 0 -> "Let's get started"
            else -> "You have $petCount ${if (petCount == 1) "pet" else "pets"} and $incompleteTaskCount upcoming ${if (incompleteTaskCount == 1) "task" else "tasks"}"
        }
        binding.emptyState.visibility = if (petsEmpty && tasksEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
