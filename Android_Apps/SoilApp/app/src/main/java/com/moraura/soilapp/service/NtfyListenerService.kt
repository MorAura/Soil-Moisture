package com.moraura.soilapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.moraura.soilapp.MainActivity
import com.moraura.soilapp.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

class NtfyListenerService : Service() {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MINUTES)
        .build()

    private fun sendEventNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleLine(line: String) {
        try {
            val json = JSONObject(line)
            if (json.optString("event") != "message") return // skip "open" / "keepalive"

            val message = json.optString("message")
            // parse message json
            try {
                val data = JSONObject(message)
                val deviceId = data.optString("id", "Unknown")
                val moistLow = data.optString("moistLow", "0")
                val moistHigh = data.optString("moistHigh", "0")
                val tempLow = data.optString("tempLow", "0")
                val tempHigh = data.optString("tempHigh", "0")

                val title = "Plant: $deviceId"

                if (moistLow != "0") {
                    sendEventNotification(title, "Low Moisture")
                }
                if (moistHigh != "0") {
                    sendEventNotification(title, "High Moisture")
                }
                if (tempLow != "0") {
                    sendEventNotification(title, "Low Temperature")
                }
                if (tempHigh != "0") {
                    sendEventNotification(title, "High Temperature")
                }

            } catch (e: JSONException) {
                sendEventNotification("Unknown Alert", message)
            }
        } catch (e: JSONException) {
            println("Error parsing ntfy line: $line")
            // ignore malformed line
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "ntfy_sync_channel",
            "Background Sync",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // create alert notification channel
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                "alerts",
                "Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val notification = NotificationCompat.Builder(this, "ntfy_sync_channel")
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
                            handleLine(line)
                        }
                    }
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}