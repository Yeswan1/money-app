package com.example.moneymap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.moneymap.data.model.BudgetAlertDto
import com.example.moneymap.R

object NotificationHelper {
    private const val CHANNEL_ID = "budget_alerts_channel"
    private const val CHANNEL_NAME = "Budget Alerts"
    private const val PREFS_NAME = "moneymap_notifications"
    private const val KEY_NOTIFIED_ALERTS = "notified_alerts"

    fun showNotificationsForAlerts(context: Context, alerts: List<BudgetAlertDto>) {
        if (alerts.isEmpty()) return

        // Check if we have permission (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (context.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifiedSet = sharedPreferences.getStringSet(KEY_NOTIFIED_ALERTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        var newAlertsFound = false

        for (alert in alerts) {
            // Generate a unique key for the alert (title + message)
            val alertKey = "${alert.title}_${alert.message}"
            if (!notifiedSet.contains(alertKey)) {
                // Send local push notification
                sendNotification(context, alert.title, alert.message, alert.isCritical)
                notifiedSet.add(alertKey)
                newAlertsFound = true
            }
        }

        if (newAlertsFound) {
            sharedPreferences.edit().putStringSet(KEY_NOTIFIED_ALERTS, notifiedSet).apply()
        }
    }

    private fun sendNotification(context: Context, title: String, message: String, isCritical: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                if (isCritical) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for budget thresholds and alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationId = (title.hashCode() + message.hashCode()).hashCode()
        notificationManager.notify(notificationId, builder.build())
    }
}
