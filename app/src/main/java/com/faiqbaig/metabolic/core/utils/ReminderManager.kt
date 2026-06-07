package com.faiqbaig.metabolic.core.utils

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ── 1. The Background Worker ──
class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("TYPE") ?: return Result.failure()
        val notificationManager = MetabolicNotificationManager(context)

        if (type == "MEAL") {
            notificationManager.showTestNotification(
                type = "MEAL",
                title = "Evening Meal Check-in",
                message = "Time to log your evening meal and hit those macros!"
            )
            // Reschedule for tomorrow at 7:00 PM
            ReminderManager.scheduleReminder(context, "MEAL", 19, 0)

        } else if (type == "HYDRATION") {
            notificationManager.showTestNotification(
                type = "HYDRATION",
                title = "Hydration Check",
                message = "It's the afternoon! Keep up with your water goals today."
            )
            // Reschedule for tomorrow at 3:00 PM
            ReminderManager.scheduleReminder(context, "HYDRATION", 15, 0)
        }

        return Result.success()
    }
}

// ── 2. The Scheduler Helper ──
object ReminderManager {
    fun scheduleReminder(context: Context, type: String, targetHour: Int, targetMinute: Int = 0) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
        }

        // If the target time has already passed today, schedule it for tomorrow
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val workData = workDataOf("TYPE" to type)

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .build()

        // EnqueueUniqueWork ensures we don't accidentally schedule 5 alarms for the same time
        WorkManager.getInstance(context).enqueueUniqueWork(
            type,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context, type: String) {
        WorkManager.getInstance(context).cancelUniqueWork(type)
    }
}