package com.udacity.project4.ui.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.ui.authentication.AuthenticationActivity
import com.udacity.project4.utils.Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.utils.Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.utils.geofence.GeofenceBroadcastReceiver
import org.jetbrains.anko.alert


class RemindersActivity : AppCompatActivity() {

    private lateinit var geofencingClient: GeofencingClient
    private var newGeoFenceRequest: GeofencingRequest? = null
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = "ACTION_GEOFENCE_EVENT"
        PendingIntent
            .getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        geofencingClient = LocationServices.getGeofencingClient(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            android.R.id.home -> {
//                findNavController(R.id.nav_host_fragment).popBackStack()
//                return true
//            }
            R.id.logout -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnSuccessListener {
                        startActivity(Intent(this, AuthenticationActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Unknown Error. Please Try Again", Toast.LENGTH_LONG)
                            .show()
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }




    /**
     *  GeoFencing
     */

    fun addGeoFences(geoFenceRequest: GeofencingRequest) {
        if (!locationPermissionsApproved()) {
            requestLocationPermissions()
            newGeoFenceRequest = geoFenceRequest
        } else {
            approvedToAddGeoFence(geoFenceRequest)
        }
    }

    @SuppressLint("MissingPermission")
    private fun approvedToAddGeoFence(geoFenceRequest: GeofencingRequest) {
        geofencingClient.addGeofences(geoFenceRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(
                    this@RemindersActivity,
                    getString(R.string.new_geofence_saved), Toast.LENGTH_LONG
                ).show()
            }
            addOnFailureListener {
                alert {
                    title = getString(R.string.error)
                    message =
                        getString(R.string.error_adding_geofence) + " ${it.localizedMessage} \n ${it.printStackTrace()}"
                    isCancelable = false
                    positiveButton(getString(R.string.okay)) { dialog ->
                        dialog.dismiss()
                    }
                }.show()
            }
        }
    }


    /**
     * Location and GeoFence Permissions -- Only requested when needed from the "addGeoFences" method
     */

    @TargetApi(29)
    private fun locationPermissionsApproved() : Boolean {
        val fineLocationApproval = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                )

        val backgroundPermissionApproval =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }

        return fineLocationApproval && backgroundPermissionApproval
    }

    @TargetApi(29)
    private fun requestLocationPermissions() {
        if (locationPermissionsApproved()) return
        var permissionsArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        ActivityCompat.requestPermissions(
            this@RemindersActivity,
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (locationPermissionsApproved()) {
            newGeoFenceRequest?.let { approvedToAddGeoFence(it) }
        }
    }










}
