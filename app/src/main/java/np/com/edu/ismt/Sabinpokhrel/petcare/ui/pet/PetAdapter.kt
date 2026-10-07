package np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ItemPetBinding

class PetAdapter(
    private val onClick: (Pet) -> Unit,
    private val onEdit: (Pet) -> Unit,
    private val onDelete: (Pet) -> Unit,
    private val onShare: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(private val binding: ItemPetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            binding.tvPetName.text = pet.name
            val details = listOfNotNull(
                pet.breed.ifBlank { null },
                pet.age.ifBlank { null }?.let { "$it yrs" }
            ).joinToString(" · ")
            binding.tvPetBreed.text = details.ifBlank { "Tap to view details" }

            if (!pet.photoUri.isNullOrBlank()) {
                binding.ivPetPhoto.visibility = android.view.View.VISIBLE
                binding.ivPetIcon.visibility = android.view.View.GONE
                binding.ivPetPhoto.load(Uri.parse(pet.photoUri)) {
                    crossfade(true)
                    error(np.com.edu.ismt.Sabinpokhrel.petcare.R.drawable.ic_pet_placeholder)
                    placeholder(np.com.edu.ismt.Sabinpokhrel.petcare.R.drawable.ic_pet_placeholder)
                }
            } else {
                binding.ivPetPhoto.visibility = android.view.View.GONE
                binding.ivPetIcon.visibility = android.view.View.VISIBLE
                binding.ivPetIcon.setImageResource(speciesIcon(pet.breed))
            }

            binding.petCardHeader.setOnClickListener { onClick(pet) }
            binding.btnEditPetItem.setOnClickListener { onEdit(pet) }
            binding.btnDeletePetItem.setOnClickListener { onDelete(pet) }
            binding.btnSharePetItem.setOnClickListener { onShare(pet) }
        }
    }

    private fun speciesIcon(breed: String): Int {
        val b = breed.lowercase()
        return when {
            listOf("cat", "kitten", "feline", "tabby", "persian", "siamese").any { b.contains(it) } ->
                np.com.edu.ismt.Sabinpokhrel.petcare.R.drawable.ic_species_cat
            listOf("dog", "puppy", "retriever", "shepherd", "terrier", "bulldog", "poodle", "labrador").any { b.contains(it) } ->
                np.com.edu.ismt.Sabinpokhrel.petcare.R.drawable.ic_species_dog
            else -> np.com.edu.ismt.Sabinpokhrel.petcare.R.drawable.ic_species_paw
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(oldItem: Pet, newItem: Pet) = oldItem.petId == newItem.petId
            override fun areContentsTheSame(oldItem: Pet, newItem: Pet) = oldItem == newItem
        }
    }
}
