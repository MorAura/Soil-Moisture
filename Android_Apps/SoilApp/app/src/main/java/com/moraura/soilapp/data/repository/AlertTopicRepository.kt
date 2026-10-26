package com.moraura.soilapp.data.repository

import android.content.Context
import com.moraura.soilapp.data.local.AlertTopicDao
import com.moraura.soilapp.data.local.AlertTopicEntity
import com.moraura.soilapp.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow

class AlertTopicRepository(context: Context) {
    private val alertTopicDao: AlertTopicDao = AppDatabase.getDatabase(context).alertTopicDao()

    val allAlertTopics: Flow<List<AlertTopicEntity>> = alertTopicDao.getAllAlertTopics()

    suspend fun getAllAlertTopicsList(): List<AlertTopicEntity> = alertTopicDao.getAllAlertTopicsList()

    suspend fun getAlertTopicByDeviceId(deviceId: String): AlertTopicEntity? =
        alertTopicDao.getAlertTopicByDeviceId(deviceId)

    suspend fun saveAlertTopic(deviceId: String, deviceName: String, alertUrl: String) {
        val truncatedName = if (deviceName.length > 32) deviceName.take(32) else deviceName
        val entity = AlertTopicEntity(
            deviceId = deviceId,
            deviceName = truncatedName,
            alertUrl = alertUrl
        )
        alertTopicDao.insertOrUpdate(entity)
    }

    suspend fun insertAll(alertTopics: List<AlertTopicEntity>) {
        alertTopicDao.insertAll(alertTopics)
    }

    suspend fun deleteAlertTopic(deviceId: String) {
        alertTopicDao.deleteByDeviceId(deviceId)
    }
}
