package com.udacity.project4.data.repository

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.database.RemindersDao
import com.udacity.project4.data.database.RemindersDatabase
import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Class Under Test
    private lateinit var remindersRepository: RemindersLocalRepository

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao


    @Before
    fun createRepository() {
        stopKoin()
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersDao = remindersDatabase.reminderDao()
        remindersRepository = RemindersLocalRepository(remindersDao, Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }


    /**
     * Testing for getReminders and saveReminders methods
     */
    @Test
    fun saveReminders_getReminders_returnList() = mainCoroutineRule.runBlockingTest {
        // GIVEN -- call saveReminder to add list of reminders to database
        remindersList.forEach {
            remindersRepository.saveReminder(it)
        }
        // WHEN -- call getReminders to pull list from database
        val result = remindersRepository.getReminders() as Result.Success<List<ReminderEntity>>
        val data = result.data

        // THEN -- check that data was retrieved
        assert(data.isNotEmpty())
        assert(data.size == remindersList.size)
    }

    /**
     *  Testing for saveReminder and getReminder to return a single reminder by id
     */
    @Test
    fun saveReminder_getReminder_returnSingleReminder() = mainCoroutineRule.runBlockingTest {
        // GIVEN -- call saveReminder to save a single Reminder
        remindersRepository.saveReminder(reminder1)

        // WHEN -- call getReminderById to retrieve that reminder
        val result = remindersRepository.getReminder(reminder1.uid) as Result.Success<ReminderEntity>
        val savedReminder = result.data

        // THEN -- savedReminder should be match reminder1
        assertThat(savedReminder, notNullValue())
        assertThat(savedReminder.uid, `is`(reminder1.uid))
        assertThat(savedReminder.title, `is`(reminder1.title))
        assertThat(savedReminder.description, `is`(reminder1.description))
        assertThat(savedReminder.location, `is`(reminder1.location))
        assertThat(savedReminder.latitude, `is`(reminder1.latitude))
        assertThat(savedReminder.longitude, `is`(reminder1.longitude))
    }

    /**
     *  Testing for deleteAllReminders
     */
    @Test
    fun deleteAllReminders_addRemindersList_returnEmptyList() = mainCoroutineRule.runBlockingTest {
        // GIVEN -- call saveReminder to add list of reminders to database
        remindersList.forEach {
            remindersRepository.saveReminder(it)
        }

        // WHEN -- deleteAllReminders is called
        remindersRepository.deleteAllReminders()

        //THEN -- no reminders should return when getReminders is called
        val list = remindersRepository.getReminders() as Result.Success<List<ReminderEntity>>

        assert(list.data.isEmpty())
    }

    /**
     *  Testing for deleteReminderByID
     */
    @Test
    fun deleteReminderById_deleteSingleReminder_shouldNotReturnReminder() =
        mainCoroutineRule.runBlockingTest {

            // GIVEN -- add reminder to database
            remindersRepository.saveReminder(reminder1)

            // WHEN -- reminder is deleted
            remindersRepository.deleteReminderById(reminder1.uid)

            // THEN -- reminder should not be returned
            val reminder = remindersRepository.getReminder(reminder1.uid) as Result.Error

            assertThat(reminder.message, `is`("Reminder not found!"))
    }


    /**
     *  Fake Data
     */

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

    private val remindersList = listOf(reminder1, reminder2, reminder3)

}