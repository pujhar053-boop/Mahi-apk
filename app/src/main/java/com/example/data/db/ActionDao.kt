package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LiveActionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query("SELECT * FROM live_actions ORDER BY timestamp DESC LIMIT 20")
    fun getRecentActions(): Flow<List<LiveActionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: LiveActionItem): Long

    @Query("DELETE FROM live_actions")
    suspend fun clearActions()
}
