package com.udacity.project4.ui.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.data.database.ReminderDataSource
import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.data.model.local.ReminderDataItem
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminderTitle by lazy { MutableLiveData<String>() }
    val reminderDescription by lazy { MutableLiveData<String>() }

    private val _reminderSelectedLocationStr by lazy { MutableLiveData<String>() }
    val reminderSelectedLocationStr: LiveData<String> get() = _reminderSelectedLocationStr

    private val _selectedPOI by lazy { MutableLiveData<PointOfInterest>() }
    val selectedPOI: LiveData<PointOfInterest> get() = _selectedPOI

    private val _latitude by lazy { MutableLiveData<Double>() }
    val latitude: LiveData<Double> get() = _latitude

    private val _longitude by lazy { MutableLiveData<Double>() }
    val longitude: LiveData<Double> get() = _longitude

    private val _saveCompleted by lazy { MutableLiveData<Boolean>() }
    val saveCompleted: LiveData<Boolean> get() = _saveCompleted

    private val _sendGeoFenceRequest by lazy { MutableLiveData<GeofencingRequest>() }
    val sendGeoFenceRequest: LiveData<GeofencingRequest> get() = _sendGeoFenceRequest

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        _reminderSelectedLocationStr.value = null
        _selectedPOI.value = null
        _latitude.value = null
        _longitude.value = null
        _sendGeoFenceRequest.value = null
        _saveCompleted.value = false
    }

    fun storePoi(poi: PointOfInterest) {
        _selectedPOI.value = poi
        _reminderSelectedLocationStr.value = poi.name
        _longitude.value = poi.latLng.longitude
        _latitude.value = poi.latLng.latitude
    }

    fun prepLocationForDb() {
        val reminder = ReminderDataItem(
            title = reminderTitle.value,
            description = reminderDescription.value,
            location = reminderSelectedLocationStr.value,
            latitude = latitude.value,
            longitude = longitude.value
        )
        validateAndSaveReminder(reminder)
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    private fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            val savedId = dataSource.saveReminder(
                ReminderEntity(
                    title = reminderData.title,
                    description = reminderData.description,
                    location = reminderData.location,
                    latitude = reminderData.latitude,
                    longitude = reminderData.longitude,
                )
            )
            geoFenceBuilder(savedId, reminderData)
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            _saveCompleted.postValue(true)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.description.isNullOrEmpty()) {
            showSnackBar.value = R.string.err_enter_desc.toString()
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    private fun geoFenceBuilder(id: Long, reminder: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(id.toString())
            .setExpirationDuration(TimeUnit.DAYS.toMillis(1))
            .setCircularRegion(
                reminder.latitude ?: 0.0,
                reminder.longitude ?: 0.0,
                100f
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        _sendGeoFenceRequest.value = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }
















}