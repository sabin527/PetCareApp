package np.com.edu.ismt.Sabinpokhrel.petcare.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.CareTask
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PetLocation
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PlaceType
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.ActivityAddEditLocationBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.PlaceTypeUtils
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.ValidationUtils
import kotlinx.coroutines.launch
import java.util.Locale

class AddEditLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddEditLocationBinding
    private var googleMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null
    private var existingLocation: PetLocation? = null
    private var linkableTasks: List<CareTask> = emptyList()

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) useCurrentLocation() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)
        val locationRepository = RepositoryProvider.locationRepository(this)
        val taskRepository = RepositoryProvider.careTaskRepository(this)
        val session = SessionManager(this)

        binding.spinnerType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, PlaceTypeUtils.allLabels()
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.miniMap) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        lifecycleScope.launch {
            val allTasks = taskRepository.getTasksForOwner(session.getUserId())
            allTasks.observe(this@AddEditLocationActivity) { tasks ->
                linkableTasks = tasks.filter { !it.isCompleted }
                val labels = mutableListOf("None") + linkableTasks.map { it.title }
                binding.spinnerLinkedTask.adapter = ArrayAdapter(
                    this@AddEditLocationActivity, android.R.layout.simple_spinner_dropdown_item, labels
                )
                existingLocation?.linkedTaskId?.let { linkedId ->
                    val index = linkableTasks.indexOfFirst { it.taskId == linkedId }
                    if (index >= 0) binding.spinnerLinkedTask.setSelection(index + 1)
                }
            }
        }

        if (locationId != -1L) {
            supportActionBar?.title = getString(R.string.edit_place)
            binding.btnSave.text = getString(R.string.update)
            lifecycleScope.launch {
                existingLocation = locationRepository.getLocationById(locationId)
                existingLocation?.let { populate(it) }
            }
        } else {
            supportActionBar?.title = getString(R.string.add_place)
            binding.btnSave.text = getString(R.string.save)
        }

        binding.btnUseCurrentLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                useCurrentLocation()
            } else {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.btnSearchAddress.setOnClickListener { searchAddress() }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val nameCheck = ValidationUtils.validateNotEmpty(name, "Place name")
            if (nameCheck is ValidationUtils.ValidationResult.Invalid) {
                binding.tilName.error = nameCheck.message
                return@setOnClickListener
            }
            binding.tilName.error = null

            val latLng = selectedLatLng
            if (latLng == null) {
                Toast.makeText(this, "Please set a location on the map", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = PlaceType.values()[binding.spinnerType.selectedItemPosition]
            val linkedTaskIndex = binding.spinnerLinkedTask.selectedItemPosition
            val linkedTaskId = if (linkedTaskIndex > 0) linkableTasks.getOrNull(linkedTaskIndex - 1)?.taskId else null

            val location = PetLocation(
                locationId = existingLocation?.locationId ?: 0,
                ownerId = session.getUserId(),
                name = name,
                type = type,
                address = binding.etAddress.text.toString().trim(),
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                notes = binding.etNotes.text.toString().trim(),
                linkedTaskId = linkedTaskId,
                createdAt = existingLocation?.createdAt ?: System.currentTimeMillis()
            )

            lifecycleScope.launch {
                if (existingLocation != null) {
                    locationRepository.updateLocation(location)
                } else {
                    locationRepository.addLocation(location)
                }
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (existingLocation != null || intent.getLongExtra(EXTRA_LOCATION_ID, -1L) != -1L) {
            menuInflater.inflate(R.menu.menu_delete, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            confirmDelete()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDelete() {
        val location = existingLocation ?: return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(R.string.delete_place_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    RepositoryProvider.locationRepository(this@AddEditLocationActivity).deleteLocation(location)
                    finish()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.setOnMapClickListener { latLng -> setMarker(latLng) }

        existingLocation?.let {
            setMarker(LatLng(it.latitude, it.longitude), moveCamera = true)
        } ?: run {
            // Default view (world-ish) until the user picks a location.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))
        }
    }

    private fun populate(location: PetLocation) {
        binding.etName.setText(location.name)
        binding.spinnerType.setSelection(location.type.ordinal)
        binding.etAddress.setText(location.address)
        binding.etNotes.setText(location.notes)
        selectedLatLng = LatLng(location.latitude, location.longitude)
        binding.tvCoordinates.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
        googleMap?.let { setMarker(LatLng(location.latitude, location.longitude), moveCamera = true) }
        invalidateOptionsMenu()
    }

    private fun setMarker(latLng: LatLng, moveCamera: Boolean = false) {
        selectedLatLng = latLng
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(latLng))
        if (moveCamera) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        binding.tvCoordinates.text = String.format(Locale.getDefault(), "Lat: %.5f, Lng: %.5f", latLng.latitude, latLng.longitude)
    }

    private fun useCurrentLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    setMarker(latLng, moveCamera = true)
                } else {
                    Toast.makeText(this, "Couldn't get current location. Try tapping the map instead.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchAddress() {
        val query = binding.etSearchAddress.text.toString().trim()
        if (query.isEmpty()) return
        try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(query, 1)
            if (!results.isNullOrEmpty()) {
                val result = results[0]
                val latLng = LatLng(result.latitude, result.longitude)
                setMarker(latLng, moveCamera = true)
                if (binding.etAddress.text.isNullOrBlank()) {
                    binding.etAddress.setText(result.getAddressLine(0) ?: query)
                }
            } else {
                Toast.makeText(this, "No results found for \"$query\"", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Search unavailable right now. Try tapping the map instead.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_LOCATION_ID = "extra_location_id"
    }
}
