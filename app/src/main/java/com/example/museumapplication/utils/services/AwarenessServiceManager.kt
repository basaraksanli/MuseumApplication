package com.example.museumapplication.utils.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.museumapplication.utils.services.NotificationUtils.createNotificationChannel
import com.example.museumapplication.utils.services.NotificationUtils.sendNotification
import com.huawei.hms.kit.awareness.barrier.BarrierStatus

class AwarenessServiceManager
/**
 * Creates an IntentService.  Invoked by the subclass's constructor.
 */
    : IntentService("AwarenessService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            Log.e("Barrier Service:", "Intent is null")
            return
        }
        // Barrier information is transferred through intents. Parse the barrier information using the Barrier.extract method.
        val barrierStatus = BarrierStatus.extract(intent)
        // Obtain the label and current status of the barrier through BarrierStatus.
        val barrierLabel = barrierStatus.barrierLabel
        val status = barrierStatus.presentStatus
        if (status == BarrierStatus.TRUE) {
            sendNotification(this, "There is an awesome nearby Museum waiting for you to explore! ", barrierLabel)
        }
    }

    /**
     * Create notification channel for Awareness Barriers
     */
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return
            createNotificationChannel(this)
            val notification = Notification.Builder(this, NotificationUtils.CHANNEL_ID).build()
            startForeground(1, notification)
        }
    }
}