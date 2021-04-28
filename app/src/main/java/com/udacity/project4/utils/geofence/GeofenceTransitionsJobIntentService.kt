package com.udacity.project4.utils.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.data.database.ReminderDataSource
import com.udacity.project4.data.model.local.ReminderDataItem
import com.udacity.project4.utils.Result
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java,
                JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        sendNotification(geofencingEvent.triggeringGeofences)
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = triggeringGeofences[0].requestId

        val remindersLocalRepository: ReminderDataSource by inject()

        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            val result = remindersLocalRepository.getReminder(requestId.toLong())
            when (result) {
                is Result.Success -> {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.uid,
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude
                        )
                    )
                }

                is Result.Error -> {
                    Log.e(
                        "GeofenceTransitionsJobIntentService",
                    "Result Error Status Code -> ${result.statusCode}"
                    )
                    Log.e(
                        "GeofenceTransitionsJobIntentService",
                        "Result Error Message -> ${result.message}"
                    )
                }
            }
        }
    }

}