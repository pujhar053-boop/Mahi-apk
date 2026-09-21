package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "live_actions")
data class LiveActionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionName: String,
    val status: ActionStatus,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActionStatus {
    SUCCESS,
    RUNNING,
    ERROR
}
