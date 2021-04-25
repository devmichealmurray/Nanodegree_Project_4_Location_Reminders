package com.udacity.project4.locationreminders.data

import com.udacity.project4.data.database.ReminderDataSource
import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.utils.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderEntity>> {
        TODO("Return the reminders")
    }

    override suspend fun saveReminder(reminder: ReminderEntity) {
        TODO("save the reminder")
    }

    override suspend fun getReminder(id: String): Result<ReminderEntity> {
        TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
    }


}