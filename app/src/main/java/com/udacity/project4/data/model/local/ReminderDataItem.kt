package com.udacity.project4.data.model.local

import java.io.Serializable

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(
    val uid: Long?,
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
//    val id: String = UUID.randomUUID().toString()
) : Serializable