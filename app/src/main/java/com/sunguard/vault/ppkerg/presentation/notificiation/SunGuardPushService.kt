package com.sunguard.vault.ppkerg.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sunguard.vault.R
import com.sunguard.vault.SunGuardActivity
import com.sunguard.vault.ppkerg.presentation.app.SunGuardApplication

private const val SUN_GUARD_CHANNEL_ID = "sun_guard_notifications"
private const val SUN_GUARD_CHANNEL_NAME = "SunGuard Notifications"
private const val SUN_GUARD_NOT_TAG = "SunGuard"

class SunGuardPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                sunGuardShowNotification(it.title ?: SUN_GUARD_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                sunGuardShowNotification(it.title ?: SUN_GUARD_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            sunGuardHandleDataPayload(remoteMessage.data)
        }
    }

    private fun sunGuardShowNotification(title: String, message: String, data: String?) {
        val sunGuardNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SUN_GUARD_CHANNEL_ID,
                SUN_GUARD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            sunGuardNotificationManager.createNotificationChannel(channel)
        }

        val sunGuardIntent = Intent(this, SunGuardActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val sunGuardPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sunGuardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sunGuardNotification = NotificationCompat.Builder(this, SUN_GUARD_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.sun_guard_noti_icon)
            .setAutoCancel(true)
            .setContentIntent(sunGuardPendingIntent)
            .build()

        sunGuardNotificationManager.notify(System.currentTimeMillis().toInt(), sunGuardNotification)
    }

    private fun sunGuardHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(SunGuardApplication.SUN_GUARD_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}