package com.udacity.project4.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.ui.authentication.AuthenticationActivity
import com.udacity.project4.utils.geofence.GeofenceBroadcastReceiver
import org.jetbrains.anko.alert


class RemindersActivity : AppCompatActivity() {

    private lateinit var geofencingClient: GeofencingClient

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
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
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

    @SuppressLint("MissingPermission")
    fun addGeoFences(geoFenceRequest: GeofencingRequest) {
        geofencingClient.addGeofences(geoFenceRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(this@RemindersActivity,
                    getString(R.string.new_geofence_saved), Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                alert {
                    title = getString(R.string.error)
                    message = getString(R.string.error_adding_geofence) + " ${it.message}"
                    isCancelable = false
                    positiveButton(getString(R.string.okay)) {
                        dialog -> dialog.dismiss()
                    }
                }.show()
            }

        }
    }












}
