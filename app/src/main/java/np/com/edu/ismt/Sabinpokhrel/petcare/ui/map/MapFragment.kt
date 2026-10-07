package np.com.edu.ismt.Sabinpokhrel.petcare.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import np.com.edu.ismt.Sabinpokhrel.petcare.R
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PetLocation
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PlaceType
import np.com.edu.ismt.Sabinpokhrel.petcare.data.repository.RepositoryProvider
import np.com.edu.ismt.Sabinpokhrel.petcare.databinding.FragmentMapBinding
import np.com.edu.ismt.Sabinpokhrel.petcare.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Displays saved pet-related places (vet clinics, grooming salons, dog parks,
 * pet supply stores, animal shelters) on a Google Map — the app's chosen
 * "Geotagging" desirable feature.
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var locationsById: Map<Long, PetLocation> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.googleMap) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.fabAddLocation.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditLocationActivity::class.java))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false

        map.setOnMarkerClickListener { marker ->
            val locationId = marker.tag as? Long
            val location = locationId?.let { locationsById[it] }
            if (location != null) showLocationDetails(location)
            true
        }

        loadLocations()
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case a location was added/edited/deleted while this fragment was hidden.
        if (googleMap != null) loadLocations()
    }

    private fun loadLocations() {
        val context = context ?: return
        val session = SessionManager(context)
        val locationRepository = RepositoryProvider.locationRepository(context)

        locationRepository.getLocationsForOwner(session.getUserId()).observe(viewLifecycleOwner) { locations ->
            locationsById = locations.associateBy { it.locationId }
            renderMarkers(locations)
        }
    }

    private fun renderMarkers(locations: List<PetLocation>) {
        val map = googleMap ?: return
        map.clear()

        if (locations.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        locations.forEach { loc ->
            val position = LatLng(loc.latitude, loc.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(loc.name)
                    .snippet(placeTypeLabel(loc.type))
                    .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(markerHue(loc.type)))
            )
            marker?.tag = loc.locationId
            boundsBuilder.include(position)
        }

        try {
            if (locations.size == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locations[0].latitude, locations[0].longitude), 13f))
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
            }
        } catch (e: IllegalStateException) {
            // Map not laid out yet; safe to ignore.
        }
    }

    private fun showLocationDetails(location: PetLocation) {
        val context = context ?: return
        val message = buildString {
            append(placeTypeLabel(location.type)).append("\n")
            if (location.address.isNotBlank()) append(location.address).append("\n")
            if (location.notes.isNotBlank()) append("\nNotes: ${location.notes}")
        }

        AlertDialog.Builder(context)
            .setTitle(location.name)
            .setMessage(message)
            .setPositiveButton(R.string.navigate) { _, _ -> navigateTo(location) }
            .setNeutralButton(R.string.edit) { _, _ ->
                startActivity(
                    Intent(context, AddEditLocationActivity::class.java)
                        .putExtra(AddEditLocationActivity.EXTRA_LOCATION_ID, location.locationId)
                )
            }
            .setNegativeButton(R.string.delete) { _, _ -> confirmDelete(location) }
            .show()
    }

    private fun confirmDelete(location: PetLocation) {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.delete))
            .setMessage(R.string.delete_place_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    RepositoryProvider.locationRepository(context).deleteLocation(location)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun navigateTo(location: PetLocation) {
        val uri = Uri.parse(
            "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${Uri.encode(location.name)})"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun placeTypeLabel(type: PlaceType): String = np.com.edu.ismt.Sabinpokhrel.petcare.utils.PlaceTypeUtils.label(type)

    private fun markerHue(type: PlaceType): Float = when (type) {
        PlaceType.VET_CLINIC -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
        PlaceType.GROOMING_SALON -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
        PlaceType.DOG_PARK -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
        PlaceType.PET_SUPPLY_STORE -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
        PlaceType.ANIMAL_SHELTER -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ROSE
        PlaceType.OTHER -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
