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

    @Query("SELECT * FROM reminders where uid = :reminderId")
    fun getReminderById(reminderId: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders")
    fun deleteAllReminders()

}