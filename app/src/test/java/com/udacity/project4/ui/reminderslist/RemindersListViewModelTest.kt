package com.udacity.project4.ui.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.database.FakeRemindersDataSource
import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.data.model.local.ReminderDataItem
import com.udacity.project4.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
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
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun loadReminders_addReminders_shouldReturnList() = runBlockingTest {
        // GIVEN -- reminders added to fakeDataSource in setUpViewModel
        // WHEN -- loadReminders is called
        remindersListViewModel.loadReminders()
        // THEN -- remindersList Live Data should return a list
        val list = remindersListViewModel.remindersList.getOrAwaitValue()

        assert(list.isNotEmpty())
    }

    @Test
    fun loadReminders_hasError_shouldUpdateSnackbar() {
        // GIVEN -- Load Reminders has an error
        fakeDataSource.setReturnError(true)
        // WHEN -- Load Reminders is called
        remindersListViewModel.loadReminders()
        // THEN -- snack bar message live data is updated
        val errorMsg = remindersListViewModel.showSnackBar.getOrAwaitValue()

        assertThat(errorMsg, `is`("Reminders Not Found"))
        fakeDataSource.setReturnError(false)
    }

    @Test
    fun invalidateShowNoData_removeAllReminders_showNoDataIsTrue() = runBlockingTest {

        // GIVEN -- reminders added to fakeDataSource are removed
        fakeDataSource.deleteAllReminders()
        // WHEN -- load data is called
        remindersListViewModel.loadReminders()
        // THEN -- loadNoData should return true
        val noDataTrue = remindersListViewModel.showNoData.getOrAwaitValue()

        assertThat(noDataTrue, `is`(true))
    }

    @Test
    fun invalidateShowNoData_removeAllReminders_showNoDataIsFalse() = runBlockingTest {
        // GIVEN -- reminders added to fakeDataSource are NOT removed
        // WHEN -- load data is called
        remindersListViewModel.loadReminders()
        // THEN -- loadNoData should return true
        val noDataTrue = remindersListViewModel.showNoData.getOrAwaitValue()

        assertThat(noDataTrue, `is`(false))
    }

    @Test
    fun showLoading_callLoadReminders_dataIsTrueOrFalse() {
        // GIVEN -- the data source has reminders, pause the dispatcher
        mainCoroutineRule.pauseDispatcher()
        // WHEN -- load reminders is called
        remindersListViewModel.loadReminders()
        // THEN -- show loading value should be true
        val loadingTrue = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingTrue, `is`(true))

        // WHEN -- dispatcher is resumed
        mainCoroutineRule.resumeDispatcher()
        // THEN -- show loading value should be false
        val loadingFalse = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingFalse, `is`(false))
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