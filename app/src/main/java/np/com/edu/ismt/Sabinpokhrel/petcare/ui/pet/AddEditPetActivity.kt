package np.com.edu.ismt.Sabinpokhrel.petcare.ui.pet

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Pet
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityAddEditPetBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.FormSuggestions
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ImageUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import kotlinx.coroutines.launch

class AddEditPetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPetBinding
    private var petId: Long = -1
    private var existingPet: Pet? = null
    private var photoUri: Uri? = null
    private var pendingCameraUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            ImageUtils.takePersistablePermission(this, it)
            photoUri = it
            binding.ivPetPhoto.load(it) { error(R.drawable.ic_pet_placeholder) }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingCameraUri != null) {
            photoUri = pendingCameraUri
            binding.ivPetPhoto.load(pendingCameraUri) { error(R.drawable.ic_pet_placeholder) }
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
        binding = ActivityAddEditPetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        val petRepository = RepositoryProvider.petRepository(this)
        val session = SessionManager(this)

        setupAutocompleteSuggestions()

        binding.ivPetPhoto.setOnClickListener { showImagePickerOptions() }
        binding.btnChangePhoto.setOnClickListener { showImagePickerOptions() }

        if (petId != -1L) {
            supportActionBar?.title = getString(R.string.edit_pet)
            binding.btnSave.text = getString(R.string.update)
            lifecycleScope.launch {
                existingPet = petRepository.getPetByIdSync(petId)
                existingPet?.let { populate(it) }
            }
        } else {
            supportActionBar?.title = getString(R.string.add_pet)
            binding.btnSave.text = getString(R.string.save)
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etPetName.text.toString().trim()
            val check = ValidationUtils.validateNotEmpty(name, "Pet name")
            if (check is ValidationUtils.ValidationResult.Invalid) {
                binding.tilPetName.error = check.message
                return@setOnClickListener
            }
            binding.tilPetName.error = null

            val pet = Pet(
                petId = existingPet?.petId ?: 0,
                ownerId = existingPet?.ownerId ?: session.getUserId(),
                name = name,
                breed = binding.etBreed.text.toString().trim(),
                age = binding.etAge.text.toString().trim(),
                weight = binding.etWeight.text.toString().trim(),
                dietaryPreferences = binding.etDiet.text.toString().trim(),
                vaccinationHistory = binding.etVaccination.text.toString().trim(),
                allergies = binding.etAllergies.text.toString().trim(),
                favouriteToys = binding.etFavToys.text.toString().trim(),
                notes = binding.etNotes.text.toString().trim(),
                photoUri = (photoUri ?: existingPet?.photoUri?.let { Uri.parse(it) })?.toString(),
                createdAt = existingPet?.createdAt ?: System.currentTimeMillis()
            )

            lifecycleScope.launch {
                if (existingPet != null) {
                    petRepository.updatePet(pet)
                } else {
                    petRepository.addPet(pet)
                }
                finish()
            }
        }
    }

    private fun setupAutocompleteSuggestions() {
        binding.etBreed.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FormSuggestions.breeds)
        )
        binding.etDiet.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FormSuggestions.dietaryPreferences)
        )
        binding.etAllergies.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FormSuggestions.allergies)
        )
        binding.etFavToys.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, FormSuggestions.favouriteToys)
        )
    }

    private fun populate(pet: Pet) {
        binding.etPetName.setText(pet.name)
        binding.etBreed.setText(pet.breed, false)
        binding.etAge.setText(pet.age)
        binding.etWeight.setText(pet.weight)
        binding.etDiet.setText(pet.dietaryPreferences, false)
        binding.etVaccination.setText(pet.vaccinationHistory)
        binding.etAllergies.setText(pet.allergies, false)
        binding.etFavToys.setText(pet.favouriteToys, false)
        binding.etNotes.setText(pet.notes)
        if (!pet.photoUri.isNullOrBlank()) {
            binding.ivPetPhoto.load(Uri.parse(pet.photoUri)) { error(R.drawable.ic_pet_placeholder) }
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Pet photo")
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
    }
}
