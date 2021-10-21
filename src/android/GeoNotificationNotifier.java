package com.cowbell.cordova.geofence;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import android.os.Build;
import android.util.Log;

public class GeoNotificationNotifier {
    private static final String CHANNEL_ID = "channel_geofence";
    private NotificationManager notificationManager;
    private Context context;
    private BeepHelper beepHelper;
    private Logger logger;

    public GeoNotificationNotifier(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
        this.beepHelper = new BeepHelper();
        this.logger = Logger.getLogger();
    }

    public void notify(Notification notification) {
        Log.d("TAG", "notify: ");
        notification.setContext(context);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GeofenceDemo";
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

            // Set the Notification Channel for the Notification Manager.
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setVibrate(notification.getVibrate())
            .setSmallIcon(notification.getSmallIcon())
            .setLargeIcon(notification.getLargeIcon())
            .setAutoCancel(true)
            .setContentTitle(notification.getTitle())
            .setContentText(notification.getText());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(CHANNEL_ID); // Channel ID
        }

        if (notification.openAppOnClick) {
            String packageName = context.getPackageName();
            Intent resultIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);

            if (notification.data != null) {
                resultIntent.putExtra("geofence.notification.data", notification.getDataJson());
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notification.id, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        try {
            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notificationSound);
            r.play();
        } catch (Exception e) {
        	beepHelper.startTone("beep_beep_beep");
            e.printStackTrace();
        }
        notificationManager.notify(notification.id, mBuilder.build());
        logger.log(Log.DEBUG, notification.toString());
    }
}
