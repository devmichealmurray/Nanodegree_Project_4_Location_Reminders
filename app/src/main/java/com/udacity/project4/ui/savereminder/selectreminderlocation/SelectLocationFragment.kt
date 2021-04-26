package com.udacity.project4.ui.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PointOfInterest
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

private const val TAG = "* * SelectLocationFragment * *"

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

    override fun onStart() {
        super.onStart()
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady Called")
        map = googleMap
        setMapStyle(map)
        map.uiSettings.isZoomControlsEnabled = true
        getCurrentLocation()
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
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val latLang = location?.let { LatLng(it.latitude, it.longitude) }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, STARTING_ZOOM_LEVEL))
        map.isMyLocationEnabled = true
        onLocationSelected()
    }


    private fun requestLocationPermission() {
        Log.d(TAG, "requestLocationPermission Called")
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), FINE_LOCATION)) {
            alert {
                title = "Current Location Required"
                message = "Location Permission Needed To Show Current Location On Map"
                isCancelable = false
                positiveButton("Grant Permission") {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(FINE_LOCATION),
                        REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                    )
                }
                negativeButton("Cancel Request") {
                    it.dismiss()
                }
            }.show()
        } else {
            Log.d(TAG, "requestLocationPermission ELSE Called")
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
        Log.d(TAG, "onRequestPermissionsResult Called")
        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult GRANTED Called")
                    map.isMyLocationEnabled = true
                    setCurrentLocation()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult ELSE Called")
                    requestLocationPermission()
                }
            }
        }
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
            message = "Would You Like To Save The Location ${poi.name}?"
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


    /**
     *  Permissions; Only called from SelectLocationFragment when the permission is needed
     */

//    private fun checkLocationPermissions() {
//        if (!fineLocationApproval()) {
//            requestFineLocationPermission()
//        }
//    }
//
//    private fun fineLocationApproval(): Boolean {
//        return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//                )
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (
//            grantResults.isEmpty() ||
//            grantResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
//            (requestCode == Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
//                    grantResults[Constants.BACKGROUND_LOCATION_PERMISSION_INDEX] ==
//                    PackageManager.PERMISSION_DENIED)
//        ) {
//            Snackbar.make(
//                requireActivity().findViewById(R.id.activity_reminders),
//                getString(R.string.location_required_error),
//                Snackbar.LENGTH_INDEFINITE
//            )
//                .setAction(getString(R.string.settings)) {
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }
//        } else {
//            setUpMap()
//            Toast.makeText(context, getString(R.string.location_granted_toast), Toast.LENGTH_LONG)
//                .show()
//        }
//    }
//
//
//    private fun requestFineLocationPermission() {
//        val permissionsArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
//
//        ActivityCompat.requestPermissions(
//            requireActivity(),
//            permissionsArray,
//            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
//        )
//    }


}


















