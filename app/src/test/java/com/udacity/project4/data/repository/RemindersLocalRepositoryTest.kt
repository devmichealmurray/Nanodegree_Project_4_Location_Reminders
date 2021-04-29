package com.udacity.project4.data.repository

import com.udacity.project4.data.database.FakeRemindersDataSource
import com.udacity.project4.data.model.entity.ReminderEntity
import org.junit.Before

class RemindersLocalRepositoryTest {

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
    
    private lateinit var fakeRemindersDataSource: FakeRemindersDataSource

    // Class Under Test
    private lateinit var remindersRepository: RemindersLocalRepository

    @Before
    fun createRepository() {
        fakeRemindersDataSource = FakeRemindersDataSource(remindersList.toMutableList())

//        remindersRepository = RemindersLocalRepository(fakeRemindersDataSource)
    }

}