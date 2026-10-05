package com.moraura.soilapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class NtfyListenerService : Service() {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MINUTES)
        .build()

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        val channelId = "ntfy_sync_channel"

        val channel = NotificationChannel(
            channelId,
            "Background Sync",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Listening for alerts")
            .setContentText("Background connection active")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1, notification)
        }

        startListening()
    }

    private fun startListening() {
        val request = Request.Builder()
            .url("https://ntfy.sh/oiiaioiiiai/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle reconnection logic
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.bufferedReader()?.use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isNotEmpty()) {
                            println("New alert received: $line")
                        }
                    }
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}