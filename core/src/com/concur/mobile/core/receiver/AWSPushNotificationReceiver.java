package com.concur.mobile.core.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;

public class AWSPushNotificationReceiver extends BroadcastReceiver {

    public static final String CLS_TAG = AWSPushNotificationReceiver.class.getSimpleName();

    public AWSPushNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            buildNotificationBadge(context, extras);
        }
    }

    private void buildNotificationBadge(Context context, Bundle extras) {
        if (extras != null) {

            String title = extras.getString(Const.PUSH_CONCUR_NOTIF_SUBJECT_FIELD);
            String message = extras.getString(Const.PUSH_CONCUR_NOTIF_MESSAGE_FIELD);
            String type = extras.getString(Const.PUSH_CONCUR_NOTIF_TYPE_FIELD);

            Log.v(Const.LOG_TAG, CLS_TAG + " AWSpush : title " + title + " message: " + message + " type: " + type);

            if (Const.PUSH_CONCUR_NOTIF_TYPE_REPORT_APPR.equalsIgnoreCase(type)
                    || Const.PUSH_CONCUR_NOTIF_TYPE_TRIP_APPR.equalsIgnoreCase(type)) {
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
