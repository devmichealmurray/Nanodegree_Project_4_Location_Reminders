package com.udacity.project4.ui.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.jetbrains.anko.support.v4.alert
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectLocationBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // Initialize Map
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)

       return binding.root
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // add style to the map
        setMapStyle(map)
        map.uiSettings.isZoomControlsEnabled = true
        enableMyLocation()
        // zoom to the user location after taking his permission
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val latLang = currentLocation?.let {
            com.google.android.gms.maps.model.LatLng(
                currentLocation.latitude,
                currentLocation.longitude
            )
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 15f))
        onLocationSelected()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
    }

    private fun isPermissionGranted() : Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        } catch (e:Resources.NotFoundException) {
            viewModel.showErrorMessage.value = e.message
        }
    }

    private fun onLocationSelected() {
        map.setOnPoiClickListener {
            launchPoiConfirmation(it)
        }
    }


    //  When the user confirms on the selected location, send back the selected location details
    //  to the viewModel and navigate back to the previous fragment to save the reminder and add
    //  the geofence
    private fun launchPoiConfirmation(poi: PointOfInterest) {
        alert {
            title = getString(R.string.save_location)
            message = "Would You Like To Save The Location ${poi.name}? \n ${poi.latLng.latitude} \n ${poi.latLng.longitude}"
            isCancelable = false
            positiveButton(getString(R.string.save_location)) { dialog ->
                Toast.makeText(context, "Location Stored!", Toast.LENGTH_SHORT).show()
                viewModel.storePoi(poi)
                findNavController().navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
                dialog.dismiss()
            }
            negativeButton(getString(R.string.cancel)) { dialog ->
                dialog.dismiss()
            }
        }.show()
    }


}


















