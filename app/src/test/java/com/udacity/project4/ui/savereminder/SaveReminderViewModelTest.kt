package com.udacity.project4.ui.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.data.database.FakeRemindersDataSource
import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.data.model.local.ReminderDataItem
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.ui.reminderslist.ReminderListFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeRemindersDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        stopKoin()
        fakeDataSource = FakeRemindersDataSource()
        fakeDataSource.reminders = remindersList.toMutableList()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun onClear_shouldReturnCleared() {
        // GIVEN -- a poi, call storePoi to add data to live data
        saveReminderViewModel.storePoi(poi)
        // WHEN -- onClear is called
        saveReminderViewModel.onClear()
        // THEN -- corresponding LiveData should be reset
        val poiLD = saveReminderViewModel.selectedPOI.getOrAwaitValue()
        val nameLD = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val longLD = saveReminderViewModel.longitude.getOrAwaitValue()
        val latLD = saveReminderViewModel.latitude.getOrAwaitValue()

        assertThat(poiLD, `is`(nullValue()))
        assertThat(nameLD, `is`(nullValue()))
        assertThat(longLD, `is`(0.00))
        assertThat(latLD, `is`(0.00))

        // GIVEN -- a location, call storeLocation to add data to live data
        saveReminderViewModel.storeLocation(latLong)
        // WHEN -- onClear is called
        saveReminderViewModel.onClear()
        // THEN -- corresponding LiveData should be reset
        val locLocation = saveReminderViewModel.selectedLocation.getOrAwaitValue()
        val locName = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val locLat = saveReminderViewModel.latitude.getOrAwaitValue()
        val locLong = saveReminderViewModel.longitude.getOrAwaitValue()

        assertThat(locLocation, `is`(nullValue()))
        assertThat(locName, `is`(nullValue()))
        assertThat(locLat, `is`(0.00))
        assertThat(locLong, `is`(0.00))
    }

    @Test
    fun storePoi_addPoi_shouldReturnPoiData() {
        // GIVEN -- a poi; poi below
        // WHEN -- storePoi is called
        saveReminderViewModel.storePoi(poi)
        // THEN -- corresponding LiveData should hold the POI
        val poiLD = saveReminderViewModel.selectedPOI.getOrAwaitValue()
        val nameLD = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val longLD = saveReminderViewModel.longitude.getOrAwaitValue()
        val latLD = saveReminderViewModel.latitude.getOrAwaitValue()

        assertThat(poiLD, `is`(poi))
        assertThat(nameLD, `is`("Name"))
        assertThat(longLD, `is`(-38.00))
        assertThat(latLD, `is`(38.00))
    }

    @Test
    fun storeLocation_addLocation_shouldReturnLocationData() {
        // GIVEN -- a location; location below
        // WHEN -- storeLocation is called
        saveReminderViewModel.storeLocation(latLong)
        //THEN -- corresponding LiveData should hold Location
        val locLocation = saveReminderViewModel.selectedLocation.getOrAwaitValue()
        val locName = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val locLat = saveReminderViewModel.latitude.getOrAwaitValue()
        val locLong = saveReminderViewModel.longitude.getOrAwaitValue()

        assertThat(locLocation, `is`(latLong))
        assertThat(locName, `is`("Unknown Location"))
        assertThat(locLat, `is`(38.00))
        assertThat(locLong, `is`(-38.00))
    }

    @Test
    fun geoFenceBuilder_requestNewGeoFence_sendGeofenceRequestNOTNull() {
        // GIVEN -- a reminder to request geofence
        saveReminderViewModel.geoFenceBuilder(1000, reminder4)
        // WHEN -- poi is prepped, validated, saved, the geofence request live data is stored
        val geofencingRequestLD =
            saveReminderViewModel.sendGeoFenceRequest.getOrAwaitValue().initialTrigger
        // Check that data is stored in NOT null
        assertThat(geofencingRequestLD, `is`(not(nullValue())))
    }

    @Test
    fun clearGeofenceRequest_addRemoveGeofence_shouldReturnNull() = runBlockingTest {
        // GIVEN -- a reminder to request geofence
        saveReminderViewModel.geoFenceBuilder(1000, reminder4)
        // WHEN -- poi is prepped, validated, saved, the geofence request live data is stored
        val geofencingRequestLD =
            saveReminderViewModel.sendGeoFenceRequest.getOrAwaitValue().initialTrigger
        // Check that data is stored in NOT null
        assertThat(geofencingRequestLD, `is`(not(nullValue())))
        // THEN -- clearGeofenceRequest is called
        saveReminderViewModel.clearGeofenceRequest()
        val clearedGeoRequest = saveReminderViewModel.sendGeoFenceRequest.getOrAwaitValue()
        assertThat(clearedGeoRequest, `is`(nullValue()))
    }

    @Test
    fun validateEnteredData_givenIncompleteData_showSnackBarShouldReturn() {
        // GIVEN -- Data to validate a complete reminder is cleared
        saveReminderViewModel.onClear()
        // WHEN -- attempt to prepare and validate a reminder to be saved
        saveReminderViewModel.prepLocationForDb()
        // THEN -- show snackbar live data should return a value to request user fill in data
        val showSnackbarLD = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        assertThat(showSnackbarLD, `is`(not(nullValue())))
    }

    @Test
    fun singleLiveEvents_givenData_shouldNotReturnNull() {
        // GIVEN -- Live Data from Base View Model
        // WHEN -- data is added
        saveReminderViewModel.navigationCommand.value = NavigationCommand.To(
            ReminderListFragmentDirections.toSaveReminder()
        )
        saveReminderViewModel.showErrorMessage.value = "Test"
        saveReminderViewModel.showSnackBar.value = "Test"
        saveReminderViewModel.showSnackBarInt.value = 1
        saveReminderViewModel.showToast.value = "Test"
        saveReminderViewModel.showLoading.value = true
        saveReminderViewModel.showNoData.value = true

        // THEN -- Live Data should NOT return null
        val navCommand = saveReminderViewModel.navigationCommand.getOrAwaitValue()
        val errorMsg = saveReminderViewModel.showErrorMessage.getOrAwaitValue()
        val snackbar = saveReminderViewModel.showSnackBar.getOrAwaitValue()
        val snackbarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        val showToast = saveReminderViewModel.showToast.getOrAwaitValue()
        val showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()
        val showNoData = saveReminderViewModel.showNoData.getOrAwaitValue()

        assertThat(navCommand, `is`(not(nullValue())))
        assertThat(errorMsg, `is`(not(nullValue())))
        assertThat(snackbar, `is`(not(nullValue())))
        assertThat(snackbarInt, `is`(not(nullValue())))
        assertThat(showToast, `is`(not(nullValue())))
        assertThat(showLoading, `is`(not(nullValue())))
        assertThat(showNoData, `is`(not(nullValue())))

    }


    @Test
    fun saveReminder_addReminder_returnLong() = runBlockingTest {
        // GIVEN a reminder -- reminder1
        // WHEN the saveReminder method is called
        val saveReturn = fakeDataSource.saveReminder(reminder1)
        // THEN saveReminder returns a long uid
        assertThat(saveReturn, `is`(1000))
    }


    /**
     *  Fake Data
     */

    private val latLong = LatLng(38.00, -38.00)

    private val poi = PointOfInterest(
        latLong,
        "PlaceId",
        "Name"
    )

    private val reminder1 = ReminderEntity(
        1000,
        "Title1",
        "Description1",
        "LocationName1",
        38.00,
        -38.00
    )

    private val reminder2 = ReminderEntity(
        1001,
        "Title2",
        "Description2",
        "LocationName2",
        38.00,
        -38.00
    )

    private val reminder3 = ReminderEntity(
        1002,
        "Title3",
        "Description3",
        "LocationName3",
        38.00,
        -38.00
    )

    private val reminder4 = ReminderDataItem(
        1003,
        "Title4",
        "Description4",
        "LocationName4",
        38.00,
        -38.00
    )

    private val remindersList = listOf(reminder1, reminder2, reminder3)
}

