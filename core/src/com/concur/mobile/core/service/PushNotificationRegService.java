package com.concur.mobile.core.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.receiver.AWSPushNotificationReceiver;
import com.concur.mobile.core.util.Const;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PushNotificationRegService extends Service {

    private final String CLS_TAG = PushNotificationRegService.class.getSimpleName();

    private GoogleCloudMessaging gcm;

    private AWSPushNotificationReceiver pushNotificationReceiver;

    private ConcurCore ctx;

    private IntentFilter pushNotificationFilter;

    private static final String PUSH_CONCUR_PROJECT_ID = "798723466041";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        super.onCreate();
        ctx = (ConcurCore) ConcurCore.getContext();
        gcm = GoogleCloudMessaging.getInstance(ctx);
        register();
    }

    private void register() {
        new AsyncTask<Object, Void, Object>() {

            @Override
            protected Object doInBackground(Object... params) {
                String token;
                try {
                    token = gcm.register(PUSH_CONCUR_PROJECT_ID);
                    ConcurService service = ctx.getService();
                    service.sendNotificationRegisterRequest(token, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            protected void onPostExecute(Object result) {
                if (pushNotificationReceiver == null) {
                    pushNotificationReceiver = AWSPushNotificationReceiver.getInstance();

                    if (pushNotificationFilter == null) {
                        pushNotificationFilter = new IntentFilter();
                        pushNotificationFilter.addAction("com.google.android.c2dm.intent.RECEIVE");
                        pushNotificationFilter.addAction("com.google.android.c2dm.intent.REGISTRATION");
                        pushNotificationFilter.addAction("com.google.android.c2dm.intent.REGISTER");
                        pushNotificationFilter.addCategory("com.concur.breeze");
                    }
                    ctx.registerReceiver(pushNotificationReceiver, pushNotificationFilter,
                            "com.google.android.c2dm.permission.SEND", null);
                    if (!AWSPushNotificationReceiver.isRegistered()) AWSPushNotificationReceiver.setRegistered(true);

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPostExecute: pushNotificationReceiver is *not* null!");
                }

            };

        }.execute(null, null, null);
    }

    @Override
    public void onDestroy() {
        if (pushNotificationReceiver != null) {
            if(AWSPushNotificationReceiver.isRegistered()) {
                AWSPushNotificationReceiver.setRegistered(false);
                ctx.unregisterReceiver(pushNotificationReceiver);
            }
            pushNotificationReceiver = null;
        }
        super.onDestroy();
    }
}
