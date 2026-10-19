package com.moraura.soilapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("myapp", "Application created")
        createNotificationChannel(this)
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "background_alerts", "Background Alerts", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts from the public ntfy server" }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}