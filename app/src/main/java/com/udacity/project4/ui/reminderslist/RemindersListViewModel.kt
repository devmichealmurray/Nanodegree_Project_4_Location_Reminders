package com.udacity.project4.ui.reminderslist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.data.database.ReminderDataSource
import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.data.model.local.ReminderDataItem
import com.udacity.project4.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI

    private val _remindersList by lazy { MutableLiveData<List<ReminderDataItem>>() }
    val remindersList: LiveData<List<ReminderDataItem>> get() = _remindersList

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    convertRoomData(result.data as List<ReminderEntity>)
                }
                is Result.Error ->
                    showSnackBar.value = result.message
            }
            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    private suspend fun convertRoomData(result: List<ReminderEntity>) =
        withContext(Dispatchers.Default) {
            val dataList = ArrayList<ReminderDataItem>()
            dataList.addAll(result.map { reminder ->
                //map the reminder data from the DB to the be ready to be displayed on the UI
                ReminderDataItem(
                    uid = reminder.uid,
                    title = reminder.title,
                    description = reminder.description,
                    location = reminder.location,
                    latitude = reminder.latitude,
                    longitude = reminder.longitude,
                )
            })
            _remindersList.value = dataList
        }

}