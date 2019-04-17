package com.maggie.smarthelmet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Build;
import android.widget.Toast;

public class NotificationsActivity extends AppCompatActivity {
    private static final int USER_BUILD_LEVEL = Build.VERSION.SDK_INT;
    private static final int TARGEST_BUILD_LEVEL = Build.VERSION_CODES.O;

    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder mNotification;  //makes a new notification
    private static final int mUID = 12345;  //will change later
    private static final String mChannelID = "Smart Helmet Notification";
    private boolean correctAPILevel = false;


    @Override
    @TargetApi(26)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        int notificationPolicyAccess = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY);


        if (USER_BUILD_LEVEL < TARGEST_BUILD_LEVEL) {
            Toast.makeText(this, "You are running API level "+USER_BUILD_LEVEL+
                    ", which is less than the recommended version.  The Notificatoin Filter " +
                    "may not work properly on this device.", Toast.LENGTH_LONG).show();
            return;
        } else {
            correctAPILevel = true;
        }
        /*
        if (USER_BUILD_LEVEL >= TARGEST_BUILD_LEVEL) {
            //Log.i("idk", "************** notif access value: "+ notificationPolicyAccess);
            if (notificationPolicyAccess != 0) {
                Intent intentNotif = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intentNotif);
            }
        }
        */

        // making onClick for temp button to simulate getting a notification from the API
        Button btn_notif = (Button) findViewById(R.id.notification_btn);
        btn_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNotification(view);
            }
        });

        Button btn_dnd_en = (Button) findViewById(R.id.notification_dnd_en);
        btn_dnd_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                //Log.i("ENABLE DND", "************** interruption filter: "+mNotificationManager.getCurrentInterruptionFilter());

            }
        });

        Button btn_dnd_dis = (Button) findViewById(R.id.notification_dnd_dis);
        btn_dnd_dis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);  //turn off DND
                //Log.i("DISABLE DND", "************** interruption filter: "+mNotificationManager.getCurrentInterruptionFilter());
            }
        });
    }

    @TargetApi(26)
    private void createNotificationChannel() {
        if (!correctAPILevel) {
            return;
        }
        CharSequence channelName = "Notification Channel";
        String channelDes = "All notifications for the Smart Helmet";
        int priority = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(mChannelID, channelName, priority);
        channel.setDescription(channelDes);
        channel.canBypassDnd();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    public void displayNotification(View view) {
        createNotificationChannel();

        mNotification = new NotificationCompat.Builder(this, mChannelID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.directions_icon)
                .setTicker("New Smart Helmet notification!")
                .setContentTitle("Smart Helmet")
                .setContentText("new notification!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.notify(mUID, mNotification.build());
    }
}