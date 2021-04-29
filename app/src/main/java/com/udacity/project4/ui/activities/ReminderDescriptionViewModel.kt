package com.udacity.project4.ui.activities

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.data.database.ReminderDataSource
import com.udacity.project4.data.model.local.ReminderDataItem
import com.udacity.project4.utils.Constants.EXTRA_ReminderDataItem
import kotlinx.coroutines.launch

class ReminderDescriptionViewModel(
    val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    private val _errorMessage by lazy { MutableLiveData<String>() }
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _navigateToApp by lazy { MutableLiveData<Boolean>() }
    val navigateToApp: LiveData<Boolean> get() = _navigateToApp

    // Reminder Details Live Data exposed to layout xml

    private val _reminderId by lazy { MutableLiveData<Long>() }
    val reminderId: LiveData<Long> get() = _reminderId

    private val _reminderTitle by lazy { MutableLiveData<String>() }
    val reminderTitle: LiveData<String> get() = _reminderTitle

    private val _reminderDescription by lazy { MutableLiveData<String>() }
    val reminderDescription: LiveData<String> get() = _reminderDescription

    private val _reminderLocation by lazy { MutableLiveData<String>() }
    val reminderLocation: LiveData<String> get() = _reminderLocation

    private val _reminderLat by lazy { MutableLiveData<String>() }
    val reminderLat: LiveData<String> get() = _reminderLat

    private val _reminderLong by lazy { MutableLiveData<String>() }
    val reminderLong: LiveData<String> get() = _reminderLong

    fun addReminderToLiveData(reminderData: Bundle?) {
        if (reminderData?.containsKey(EXTRA_ReminderDataItem) == true) {
            val data = reminderData.get(EXTRA_ReminderDataItem) as ReminderDataItem
            _reminderId.value = data.uid
            _reminderTitle.value = data.title
            _reminderDescription.value = data.description
            _reminderLocation.value = data.location
            _reminderLat.value = "Latitude: ${data.latitude}"
            _reminderLong.value = "Longitude: ${data.longitude}"
        } else {
            _errorMessage.value = "Bundle does not contain key value"
        }
    }

    fun deleteReminder() {
        viewModelScope.launch {
            try {
                reminderId.value?.let { dataSource.deleteReminderById(it) }
                showToast.value = "Reminder Deleted"
                _navigateToApp.value = true
            } catch (e: Exception) {
                showToast.value = "ERROR! Something went wrong when deleting reminder"
            }

        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun returnToApp() {
        _navigateToApp.value = true
    }

}