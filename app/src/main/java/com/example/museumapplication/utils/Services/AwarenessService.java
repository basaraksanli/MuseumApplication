package com.example.museumapplication.utils.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.museumapplication.utils.NotificationUtils;
import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

public class AwarenessService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AwarenessService() {
        super("AwarenessService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Log.e("Barrier Service:", "Intent is null");
            return;
        }
        // Barrier information is transferred through intents. Parse the barrier information using the Barrier.extract method.
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        // Obtain the label and current status of the barrier through BarrierStatus.
        String barrierLabel = barrierStatus.getBarrierLabel();
        int status = barrierStatus.getPresentStatus();
        if (status == BarrierStatus.TRUE ) {
            NotificationUtils.sendNotification(this,"There is an awesome nearby Museum waiting for you to explore! " , barrierLabel);
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }
            NotificationUtils.createNotificationChannel(this);
            Notification notification = new Notification.Builder(this,NotificationUtils.CHANNEL_ID).build();
            startForeground(1,notification);
        }
    }
}

