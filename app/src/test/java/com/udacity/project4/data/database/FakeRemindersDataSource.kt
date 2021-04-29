package com.udacity.project4.data.database

import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.utils.Result

class FakeRemindersDataSource(
    var reminders: MutableList<ReminderEntity> = mutableListOf()
) : ReminderDataSource {

    private var shouldReturnError = false
    fun setReturnError(error: Boolean) {
        shouldReturnError = error
    }


    override suspend fun getReminders(): Result<List<ReminderEntity>> {
        return when (shouldReturnError) {
            true -> Result.Error("Reminders Not Found")
            false -> Result.Success(reminders.toList())
        }
    }

    override suspend fun saveReminder(reminder: ReminderEntity): Long {
        reminders.add(reminder)
        return reminder.uid
    }

    override suspend fun getReminder(id: Long): Result<ReminderEntity> {
        return when (shouldReturnError) {
            true -> Result.Error("Error: Exception")
            false -> {
                return when (val reminder = reminders.find { it.uid == id }) {
                    null -> Result.Error("No Reminder Found With Provided ID")
                    else -> Result.Success(reminder)
                }
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    override suspend fun deleteReminderById(id: Long) {
        when (shouldReturnError) {
            true -> Result.Error("Error: Exception")
            false -> {
                val reminder = reminders.find { it.uid == id }
                if (reminder != null) reminders.remove(reminder)
            }
        }
    }


}