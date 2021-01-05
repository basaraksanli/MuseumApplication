/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            getSystemService(NotificationManager::class.java) ?: return
            createNotificationChannel(this)
            val notification = Notification.Builder(this, NotificationUtils.CHANNEL_ID).build()
            startForeground(1, notification)
        }
    }
}