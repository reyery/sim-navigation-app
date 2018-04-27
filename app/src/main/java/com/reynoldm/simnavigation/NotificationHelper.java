package com.reynoldm.simnavigation;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;
    public static final String PRIMARY_CHANNEL = "default";

    public NotificationHelper(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL,
                    "Timetable", NotificationManager.IMPORTANCE_HIGH);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(chan1);
        }
    }

    public Notification getNotification(String title, String time, String location) {
        Intent navigateIntent = new Intent(this, NotificationPublisher.class);
        navigateIntent.setAction("com.reynoldm.simnavigation.NAVIGATE");
        navigateIntent.putExtra("location", location);
        PendingIntent navPendingInt = PendingIntent.getBroadcast(this, 0, navigateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), PRIMARY_CHANNEL)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_event_black_24dp)
                    .setStyle(new Notification.BigTextStyle().bigText(time+'\n'+location))
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_navigation_black_24dp, "Navigate to location", navPendingInt)
                    .build();
        } else {
            return new Notification.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_event_black_24dp)
                    .setStyle(new Notification.BigTextStyle().bigText(time+'\n'+location))
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_navigation_black_24dp, "Navigate to location", navPendingInt)
                    .build();
        }
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void scheduleNotification(Notification notification, long time, long delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.setAction("com.reynoldm.simnavigation.NOTIFICATION");
        notificationIntent.putExtra("notification-id", 1);
        notificationIntent.putExtra("notification", notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // TODO change back
        long futureInMillis = time + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
    }
}
