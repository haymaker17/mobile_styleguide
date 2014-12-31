package com.concur.mobile.core.util;

import com.concur.mobile.core.service.PushNotificationRegService;

import android.content.Context;
import android.content.Intent;

public class Notifications {

    private Context ctx;

    public Notifications(Context context) {
        this.ctx = context;
    };

    public void initAWSPushService() {
        ctx.startService(new Intent(ctx, PushNotificationRegService.class));
    }

    public void stopAWSPushService() {
        ctx.stopService(new Intent(ctx, PushNotificationRegService.class));
    }
}
