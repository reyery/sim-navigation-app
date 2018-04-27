package com.reynoldm.simnavigation;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationPublisher extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        String action = intent.getAction();

        switch(action) {
            case "com.reynoldm.simnavigation.NOTIFICATION":
                Notification notification = intent.getParcelableExtra("notification");
                int id = intent.getIntExtra("notification-id", 0);
                notificationManager.notify(id, notification);
                break;

            case "com.reynoldm.simnavigation.NAVIGATE":
                Intent intent1 = new Intent(context, MainActivity.class);
                intent1.putExtra("location", intent.getStringExtra("location"));
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent1);
        }
    }
}
