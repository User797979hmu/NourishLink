package com.nourishlink.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.nourishlink.app.R
import com.nourishlink.app.ui.main.MainActivity

/**
 * Shows a system notification (reservation confirmed, new logistics message, ...). Fired by
 * MainActivity whenever a new in-app notification arrives for the signed-in user.
 */
object NotificationHelper {

    fun show(context: Context, title: String, body: String, listingId: Int? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            listingId?.let { putExtra("listingId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, listingId ?: 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return // user has not granted POST_NOTIFICATIONS (Android 13+); silently skip
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((listingId ?: System.currentTimeMillis()).toInt(), notification)
    }
}
