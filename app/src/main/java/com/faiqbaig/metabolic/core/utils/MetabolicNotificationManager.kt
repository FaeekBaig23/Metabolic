package com.faiqbaig.metabolic.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.faiqbaig.metabolic.R // Ensure this matches your package name

class MetabolicNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // Android 8.0+ requires Notification Channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mealChannel = NotificationChannel(
                "MEAL_CHANNEL",
                "Meal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to log your meals"
            }

            val hydrationChannel = NotificationChannel(
                "HYDRATION_CHANNEL",
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications to remind you to drink water"
            }

            notificationManager.createNotificationChannel(mealChannel)
            notificationManager.createNotificationChannel(hydrationChannel)
        }
    }

    fun showTestNotification(type: String, title: String, message: String) {
        val channelId = if (type == "MEAL") "MEAL_CHANNEL" else "HYDRATION_CHANNEL"

        // NOTE: Replace R.drawable.ic_launcher_foreground with your app's actual icon if you have a custom one!
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_m) // ── YOUR NEW M ICON ──
            .setColor(android.graphics.Color.parseColor("#00C896")) // ── TINTS IT METABOLIC GREEN ──
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Using a random ID so multiple test notifications don't overwrite each other
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}