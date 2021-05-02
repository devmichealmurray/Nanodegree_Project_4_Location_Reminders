package com.udacity.project4.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.data.database.RemindersDao
import com.udacity.project4.data.database.RemindersDatabase
import com.udacity.project4.data.model.entity.ReminderEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDao: RemindersDao
    private lateinit var db: RemindersDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, RemindersDatabase::class.java
        ).build()
        remindersDao = db.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        db.close()
    }

    // Tests GetReminders & Insert
    @Test
    fun getReminders_addRemindersList_returnsListOfReminders() = runBlockingTest {
        // GIVEN -- Adding a list of reminders
        remindersList.forEach { remindersDao.saveReminder(it) }
        // WHEN -- getReminders is called
        val list = remindersDao.getReminders()
        //THEN -- the lists should match
        assert(list.isNotEmpty())
        assert(list.size == remindersList.size)
        assertThat(list[0].uid, `is`(1000))
        assertThat(list[1].uid, `is`(1001))
        assertThat(list[2].uid, `is`(1002))

    }

    // Tests GetReminderByID and Insert
    @Test
    fun getReminderById_addAReminder_returnsSameReminder() = runBlockingTest {
        // GIVEN -- a reminders is saved
        val savedReminder = remindersDao.saveReminder(reminder1)
        // WHEN -- that reminder is retrieved by it's uid
        val retrievedReminder = remindersDao.getReminderById(savedReminder)
        // THEN -- the reminders should match
        assertThat(retrievedReminder?.uid, `is`(1000))
    }

    // Tests Insert and Delete All
    @Test
    fun deleteAll_addThenDelete_shouldReturnEmptyList() = runBlockingTest {
        // GIVEN -- Insert a list of reminders and assert the list was saved
        remindersList.forEach { remindersDao.saveReminder(it) }
        val list = remindersDao.getReminders()
        assert(list.size == remindersList.size)
        // WHEN -- delete reminders by calling delete all
        remindersDao.deleteAllReminders()
        // THEN -- all reminders should be deleted
        val list2 = remindersDao.getReminders()
        assert(list2.isEmpty())
    }

    // Tests Insert and DeleteById
    @Test
    fun deleteById_addDeleteReminder_reminderShouldNotReturn() = runBlockingTest {
        // GIVEN -- Insert reminder into database and verify it was saved
        val reminderUid = remindersDao.saveReminder(reminder1)
        val returnedReminder = remindersDao.getReminderById(reminderUid)
        assertThat(returnedReminder?.uid, `is`(1000))
        // WHEN -- reminder is deleted
        remindersDao.deleteReminderById(reminderUid)
        // THEN -- that reminder does not exist
        val deletedReminder = remindersDao.getReminderById(reminderUid)

        assertThat(deletedReminder, `is`(nullValue()))
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