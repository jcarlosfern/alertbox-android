package app.alertbox.io.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import app.alertbox.io.R

object NotificationChannels {
    const val ALERTS = "alertbox_alerts"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            ALERTS,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}

