package com.udacity.project4.data.model.local

import java.io.Serializable

data class ReminderDataItem(
    val uid: Long = -1L,
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
) : Serializable