package io.github.josephdalughut.sidelock.android.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import io.github.josephdalughut.sidelock.android.core.Application;

/**
 * Created by Joseph Dalughut on 20/07/2018
 * Copyright © 2018
 *
 * Manager for notification channels
 */
public class NotificationChannelManager {

    //wrap channels support into a handy static boolean
    public static boolean CHANNELS_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    /*
        Make sure we don't have to access these fields from the NotificationManager since they
        won't be available in android versions before oreo
     */
    public static int IMPORTANCE_HIGH = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_HIGH : 0;
    public static int IMPORTANCE_DEFAULT = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_DEFAULT : 0;
    public static int IMPORTANCE_LOW = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_LOW : 0;
    public static int IMPORTANCE_MAX = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_MAX : 0;
    public static int IMPORTANCE_MIN = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_MIN : 0;
    public static int IMPORTANCE_NONE = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_NONE : 0;
    public static int IMPORTANCE_UNSPECIFIED = CHANNELS_SUPPORTED ? NotificationManager.IMPORTANCE_UNSPECIFIED : 0;


    /*
        Channels
     */
    public static NotificationChannel CHANNEL_CONTROLS = new NotificationChannel(
            "controls", "Controls", "Notifications for controlling application.",
            IMPORTANCE_LOW
    );

    public static class NotificationChannel {

        public String channelId, channelName, description;
        public int importance;

        public NotificationChannel(String channelId, String channelName, String channelDesc) {
            this(channelId, channelName, channelDesc, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    NotificationManager.IMPORTANCE_DEFAULT : 0);
        }

        public NotificationChannel(String channelId, String channelName, String channelDesc, int importance) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.description = channelDesc;
            this.importance = importance;
            initializeChannel(Application.getInstance());
        }

        private void initializeChannel(Context context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, channelName, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }

        public NotificationCompat.Builder newBuilder(){
            return new NotificationCompat.Builder(Application.getInstance(), channelId);
        }

        public void notify(int id, Notification notification){
            getNotificationMgr(Application.getInstance()).notify(id, notification);
        }
    }

    public static void init(){
        // just call instances of this, it would initialize from its constructor.
        NotificationChannel chat = CHANNEL_CONTROLS;
    }

    public static NotificationManagerCompat getNotificationMgr(){
        return getNotificationMgr(Application.getInstance());
    }

    private static NotificationManagerCompat getNotificationMgr(Context context){
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        return notificationManager;
    }

}
