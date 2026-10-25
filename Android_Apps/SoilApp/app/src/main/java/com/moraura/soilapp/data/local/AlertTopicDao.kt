package com.moraura.soilapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertTopicDao {
    @Query("SELECT * FROM alert_topics")
    fun getAllAlertTopics(): Flow<List<AlertTopicEntity>>

    @Query("SELECT * FROM alert_topics")
    suspend fun getAllAlertTopicsList(): List<AlertTopicEntity>

    @Query("SELECT * FROM alert_topics WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getAlertTopicByDeviceId(deviceId: String): AlertTopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(alertTopic: AlertTopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alertTopics: List<AlertTopicEntity>)

    @Delete
    suspend fun delete(alertTopic: AlertTopicEntity)

    @Query("DELETE FROM alert_topics WHERE deviceId = :deviceId")
    suspend fun deleteByDeviceId(deviceId: String)
}
