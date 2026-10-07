package np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet

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
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.FragmentPetsBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import kotlinx.coroutines.launch

class PetsFragment : Fragment() {

    private var _binding: FragmentPetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val session = SessionManager(context)
        val petRepository = RepositoryProvider.petRepository(context)

        adapter = PetAdapter(
            onClick = { pet ->
                startActivity(
                    Intent(context, PetDetailActivity::class.java)
                        .putExtra(PetDetailActivity.EXTRA_PET_ID, pet.petId)
                )
            },
            onEdit = { pet ->
                startActivity(
                    Intent(context, AddEditPetActivity::class.java)
                        .putExtra(AddEditPetActivity.EXTRA_PET_ID, pet.petId)
                )
            },
            onDelete = { pet ->
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.delete_pet))
                    .setMessage(R.string.delete_pet_confirm)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch { petRepository.deletePet(pet) }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            },
            onShare = { pet ->
                val details = listOfNotNull(
                    pet.breed.ifBlank { null }?.let { "Breed: $it" },
                    pet.age.ifBlank { null }?.let { "Age: $it yrs" },
                    pet.weight.ifBlank { null }?.let { "Weight: ${it}kg" },
                    pet.allergies.ifBlank { null }?.let { "Allergies: $it" }
                ).joinToString("\n")
                val shareText = "Meet ${pet.name}!\n$details".trim()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, pet.name)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "Share ${pet.name}'s profile"))
            }
        )
        binding.rvPets.layoutManager = LinearLayoutManager(context)
        binding.rvPets.adapter = adapter

        petRepository.getPetsForOwner(session.getUserId()).observe(viewLifecycleOwner) { pets ->
            adapter.submitList(pets)
            binding.emptyState.visibility = if (pets.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddPet.setOnClickListener {
            startActivity(Intent(context, AddEditPetActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
