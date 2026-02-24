package com.kaeru.app.data

import com.kaeru.app.tracking.database.TrackingEntity

data class BackupData(
    val version: Int = 1,
    val userName: String,
    val userBio: String,
    val history: List<TrackingEntity>,
    val timestamp: Long = System.currentTimeMillis()
)