package com.concur.mobile.core.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.niftyservice.NiftyAsyncRequestTask;

public class AWSPushNotificationReceiver extends BroadcastReceiver {

    public static final String CLS_TAG = AWSPushNotificationReceiver.class.getSimpleName();

    protected static AWSPushNotificationReceiver instance;

    private static boolean registered;

    protected AWSPushNotificationReceiver() { }

    static {
        instance = new AWSPushNotificationReceiver();
        registered = false;
    }

    public static AWSPushNotificationReceiver getInstance() {
        return instance;
    }

    public static Boolean isRegistered() {
        return registered;
    }

    public static void setRegistered(Boolean isRegistered) {
        registered = isRegistered;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            buildNotificationBadge(context, extras);
        }
    }

    protected void buildNotificationBadge(Context context, Bundle extras) {
        if (extras != null) {

            String title = extras.getString(Const.PUSH_CONCUR_NOTIF_SUBJECT_FIELD);
            String message = extras.getString(Const.PUSH_CONCUR_NOTIF_MESSAGE_FIELD);
            String type = extras.getString(Const.PUSH_CONCUR_NOTIF_TYPE_FIELD);

            String notificationSource = "AWSpush";
            String notificationId = null;
            Boolean containsId = (extras.containsKey(NiftyAsyncRequestTask.NOTIFICATION_ID_KEY));
            if (containsId) {
                notificationId = extras.getString(NiftyAsyncRequestTask.NOTIFICATION_ID_KEY);
                notificationSource = "Nifty Push Notification";
            }

            Log.v(Const.LOG_TAG, CLS_TAG + " " + notificationSource + " title: " + title + " message: " + message + " type: " + type);

            if (Const.PUSH_CONCUR_NOTIF_TYPE_REPORT_APPR.equalsIgnoreCase(type)
                    || Const.PUSH_CONCUR_NOTIF_TYPE_TRIP_APPR.equalsIgnoreCase(type)) {

                /*
                * Work around android 4.4 bug in launching intent from push notification.
                * Issue report: https://code.google.com/p/android/issues/detail?id=61850
                * Fix used: https://github.com/phonegap-build/PushPlugin/issues/192
                * */
                // we do this once...
                Intent notificationIntent_forclear = new Intent(context, Approval.class);
                notificationIntent_forclear.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                notificationIntent_forclear.putExtra("pushBundle", extras);

                // Clear pending Intents
                PendingIntent contentIntent_forclear = PendingIntent.getActivity(context, 0, notificationIntent_forclear, PendingIntent.FLAG_UPDATE_CURRENT);
                contentIntent_forclear.cancel();
                /*
                * End work around
                * */

                NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
                nb.setSmallIcon(R.drawable.icon_notify);
                nb.setContentTitle(title);
                nb.setContentText(message);
                nb.setAutoCancel(true);
                // Set vibration pattern if enabled.
                if (Preferences.shouldVibrateNotifications()) {
                    nb.setVibrate(Const.NOTIFICATION_VIBRATION_PATTERN);
                }

                Intent nfyIntent = new Intent(context, Approval.class);
                nfyIntent.putExtra(ConcurCore.FROM_NOTIFICATION, true);
                nfyIntent.putExtra(Flurry.EXTRA_FLURRY_CATEGORY, Flurry.CATEGORY_PUSH_NOTIFICATION);
                nfyIntent.putExtra(Flurry.EXTRA_FLURRY_ACTION_PARAM_VALUE, type);

                if (!TextUtils.isEmpty(notificationId)) {
                    nfyIntent.putExtra(NiftyAsyncRequestTask.NOTIFICATION_ID_KEY, notificationId);
                }

                TaskStackBuilder sb = TaskStackBuilder.create(context);
                sb.addParentStack(Approval.class);
                sb.addNextIntent(nfyIntent);
                PendingIntent pi = sb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                nb.setContentIntent(pi);

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                nm.notify(1, nb.build());
            } else if (Const.PUSH_CONCUR_NOTIF_TYPE_CREDIT_CARD.equalsIgnoreCase(type)) {
                // MOB-14075 - Disable handling of card charges (server is not currently sending them as
                // of the July SU release) until the client has support for handling re-authentication
                // at the point of the end-user clicking the notification.
                //
                // NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
                // nb.setSmallIcon(R.drawable.icon_notify);
                // nb.setContentTitle(title);
                // nb.setContentText(message);
                // nb.setAutoCancel(true);
                // Set vibration pattern if enabled.
                // if (Preferences.shouldVibrateNotifications()) {
                // nb.setVibrate(NotificationsPreference.getVibrationPattern(context));
                // }

                //
                // Intent nfyIntent = new Intent(context, ExpensesAndReceipts.class);
                // nfyIntent.putExtra(ConcurCore.FROM_NOTIFICATION, true);
                // nfyIntent.putExtra(Flurry.EXTRA_FLURRY_CATEGORY, Flurry.CATEGORY_PUSH_NOTIFICATION);
                // nfyIntent.putExtra(Flurry.EXTRA_FLURRY_ACTION_PARAM_VALUE, PUSH_CONCUR_NOTIF_TYPE_CREDIT_CARD);

                // TaskStackBuilder sb = TaskStackBuilder.create(context);
                // sb.addParentStack(ExpensesAndReceipts.class);
                // sb.addNextIntent(nfyIntent);
                // PendingIntent pi = sb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                // nb.setContentIntent(pi);
                //
                // NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                //
                // nm.notify(1, nb.build());
            }

        } else {
            Log.v(Const.LOG_TAG, CLS_TAG + " bundle is null from server. Can not create notification");
        }
    }
}
