package com.udacity.project4.ui.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constants.FINE_LOCATION
import com.udacity.project4.utils.Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.utils.Constants.STARTING_ZOOM_LEVEL
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.jetbrains.anko.support.v4.alert
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)
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


    /**
     *  Map Methods
     */

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        map.uiSettings.isZoomControlsEnabled = true
        getCurrentLocation()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            viewModel.showErrorMessage.value = e.message
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
            setCurrentLocation() else requestLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val currentLocation =
            (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))?.let {
            LatLng(it.latitude, it.longitude)
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, STARTING_ZOOM_LEVEL))
        map.isMyLocationEnabled = true
        onLocationSelected()
    }

    private fun onLocationSelected() {
        map.setOnPoiClickListener { poi ->
            val newMarker = map.addMarker(MarkerOptions().position(poi.latLng))
            launchPoiConfirmation(poi, newMarker)
        }

        map.setOnMapLongClickListener {
            val newMarker = map.addMarker(MarkerOptions().position(it))
            launchLocationConfirmation(it, newMarker)
        }
    }

    private fun launchPoiConfirmation(poi: PointOfInterest, marker: Marker) {
        alert {
            title = getString(R.string.save_location)
            message = "Would You Like To Save The Location ${poi.name}?"
            isCancelable = false
            positiveButton(getString(R.string.save_location)) { dialog ->
                Toast.makeText(context, getString(R.string.location_stored), Toast.LENGTH_SHORT).show()
                viewModel.storePoi(poi)
                findNavController().navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
                dialog.dismiss()
            }
            negativeButton(getString(R.string.cancel)) { dialog ->
                marker.remove()
                dialog.dismiss()
            }
        }.show()
    }

    private fun launchLocationConfirmation(location: LatLng, marker: Marker) {
        alert {
            title = getString(R.string.save_location)
            message = "Would You Like To Save This Location?"
            isCancelable = false
            positiveButton(getString(R.string.save_location)) { dialog ->
                Toast.makeText(context, getString(R.string.location_stored), Toast.LENGTH_SHORT).show()
                viewModel.storeLocation(location)
                findNavController().navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
                dialog.dismiss()
            }
            negativeButton(getString(R.string.cancel)) { dialog ->
                marker.remove()
                dialog.dismiss()
            }
        }.show()
    }


    /**
     *  Location Permission Methods; Only requested when needed by this fragment
     */

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), FINE_LOCATION)) {
            alert {
                title = getString(R.string.current_location_required)
                message = getString(R.string.permission_rationale_message)
                isCancelable = false
                positiveButton(getString(R.string.permission_rationale_positive_btn)) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(FINE_LOCATION),
                        REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                    )
                }
                negativeButton(getString(R.string.permission_rationale_negative_btn)) {
                    it.dismiss()
                }
            }.show()
        } else {
            requestPermissions(
                arrayOf(FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    map.isMyLocationEnabled = true
                    setCurrentLocation()
                } else {
                    requestLocationPermission()
                }
            }
        }
    }

}


















