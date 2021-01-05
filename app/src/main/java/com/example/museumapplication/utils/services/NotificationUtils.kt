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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.museumapplication.R
import com.example.museumapplication.ui.splash_screen.SplashActivity

object NotificationUtils {
    const val CHANNEL_ID = "MuseumPush"

    /**
     * Creation of the notification
     * Sends notification
     */
    @JvmStatic
    fun sendNotification(context: Context, content: String?, label: String?) {
        val notificationLayout = RemoteViews(context.packageName, R.layout.view_notification)
        notificationLayout.setTextViewText(R.id.notification_content, content)
        val notificationLayoutExpand = RemoteViews(context.packageName, R.layout.view_notification_expand)
        notificationLayoutExpand.setTextViewText(R.id.notification_content, content)
        notificationLayoutExpand.setTextViewText(R.id.notification_museum_name, label)
        notificationLayoutExpand.setTextViewText(R.id.notification_museum_description, "Click Explore to see the details!")
        val extra = Bundle()
        extra.putString("MuseumName", label)
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtras(extra)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpand)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        val managerCompat = NotificationManagerCompat.from(context)
        managerCompat.notify(0, builder.build())
    }

    /**
     * Creates Notification Channel
     */
    @JvmStatic
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
                    ?: return
            val name: CharSequence = "Coupon Info"
            val description = "Preferential information of surrounding merchants"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            manager.createNotificationChannel(channel)
        }
    }
}