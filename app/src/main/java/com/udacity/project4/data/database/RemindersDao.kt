package com.udacity.project4.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.udacity.project4.data.model.entity.ReminderEntity

@Dao
interface RemindersDao {
    @Query("SELECT * FROM reminders")
    fun getReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE uid = :reminderId")
    fun getReminderById(reminderId: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReminder(reminder: ReminderEntity) : Long

    @Query("DELETE FROM reminders")
    fun deleteAllReminders()

    @Query("DELETE FROM reminders WHERE uid = :reminderId")
    fun deleteReminderById(reminderId: Long)
}