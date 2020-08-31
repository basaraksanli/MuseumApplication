package com.example.museumapplication.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.museumapplication.R;
import com.example.museumapplication.ui.home.splash_screen.SplashActivity;

public class NotificationUtils {
    static final String CHANNEL_ID = "MuseumPush";

    static void sendNotification(Context context, String content, String label) {
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.view_notification);
        notificationLayout.setTextViewText(R.id.notification_content, content +" "  +label);
        RemoteViews notificationLayoutExpand = new RemoteViews(context.getPackageName(), R.layout.view_notification_expand);
        notificationLayoutExpand.setTextViewText(R.id.notification_content, content + " "  +label);

        Bundle extra = new Bundle();
        extra.putString("MuseumName", label);

        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtras(extra);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);



        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpand)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(0, builder.build());
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }
            CharSequence name = "Coupon Info";
            String description = "Preferential information of surrounding merchants";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }
    }
}
