package com.udacity.project4.data.database

import com.udacity.project4.data.model.entity.ReminderEntity
import com.udacity.project4.utils.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderEntity>>
    suspend fun saveReminder(reminder: ReminderEntity) : Long
    suspend fun getReminder(id: Long): Result<ReminderEntity>
    suspend fun deleteAllReminders()
}