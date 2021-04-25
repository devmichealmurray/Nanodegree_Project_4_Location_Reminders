package com.udacity.project4.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.udacity.project4.data.model.entity.ReminderEntity

/**
 * The Room Database that contains the reminders table.
 */
@Database(entities = [ReminderEntity::class], version = 2, exportSchema = false)
abstract class RemindersDatabase : RoomDatabase() {

    abstract fun reminderDao(): RemindersDao
}