package com.maggie.smarthelmet;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    private Receiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Notification");
        registerReceiver(receiver, intentFilter);
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.i(TAG, "********** onNotificationPosted *******");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent intent = new Intent("Notification");
        intent.putExtra("notification_event", "onNotificationPosted: "+sbn.getPackageName()+"\n");
        sendBroadcast(intent);
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.i(TAG, "********** onNotificationRemoved *******");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("command").equals("clearall")) {
                NotificationListener.this.cancelAllNotifications();
            } else if (intent.getStringExtra("command").equals("list")) {
                Intent intent1 = new Intent("Notification");
                intent1.putExtra("notification_event", "=============");
                sendBroadcast(intent1);
            }

            int i = 1;
            for (StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
                Intent intent2 = new Intent("Notification");
                intent2.putExtra("notification_event ",i+": "+sbn.getPackageName()+"\n");
                sendBroadcast(intent2);
                i++;
            }

            Intent intent3 = new Intent("Notification");
            intent3.putExtra("notification_event","===== Notification List ====");
            sendBroadcast(intent3);
        }

}


}
