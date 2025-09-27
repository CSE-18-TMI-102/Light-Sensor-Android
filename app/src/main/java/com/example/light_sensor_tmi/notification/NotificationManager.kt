// File: app/src/main/java/com/example/light_sensor_tmi/notification/NotificationManager.kt
package com.example.light_sensor_tmi.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.light_sensor_tmi.MainActivity
import com.example.light_sensor_tmi.R

class SensorNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val preferences: SharedPreferences = context.getSharedPreferences("sensor_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "street_light_status"
        const val NOTIFICATION_ID = 1001
        private const val PREF_LAST_STATUS = "last_status"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Street Light Status",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for street light status changes"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun checkAndNotifyStatusChange(newStatus: String) {
        val lastStatus = preferences.getString(PREF_LAST_STATUS, "UNKNOWN")

        if (lastStatus != newStatus && lastStatus != "UNKNOWN") {
            sendStatusNotification(lastStatus!!, newStatus)
        }

        // Save current status
        preferences.edit()
            .putString(PREF_LAST_STATUS, newStatus)
            .apply()
    }

    private fun sendStatusNotification(oldStatus: String, newStatus: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message, color) = when (newStatus) {
            "OFF" -> Triple(
                "🔴 STREET LIGHT OFF",
                "Street light status changed from $oldStatus to OFF",
                Color.RED
            )
            "ON" -> Triple(
                "🟢 STREET LIGHT ON",
                "Street light status changed from $oldStatus to ON",
                Color.GREEN
            )
            "FLICKER" -> Triple(
                "⚡ STREET LIGHT FLICKERING",
                "ALERT: Street light is flickering! (was $oldStatus)",
                Color.YELLOW
            )
            else -> Triple(
                "Street Light Status",
                "Status changed from $oldStatus to $newStatus",
                Color.GRAY
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(color)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun sendTestNotification() {
        sendStatusNotification("TEST_OLD", "TEST_NEW")
    }
}
