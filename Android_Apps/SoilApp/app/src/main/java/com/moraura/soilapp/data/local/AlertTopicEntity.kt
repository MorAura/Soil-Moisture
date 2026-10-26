package com.moraura.soilapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_topics")
data class AlertTopicEntity(
    @PrimaryKey
    val deviceId: String, // MAC address (12 hex digits)
    val deviceName: String, // String up to ~32 characters
    val alertUrl: String // Alert URL or topic
)
