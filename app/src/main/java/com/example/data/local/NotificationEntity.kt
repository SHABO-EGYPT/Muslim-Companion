package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val time: String,
    val iconId: Int, // Resource ID or custom enum mapping
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
