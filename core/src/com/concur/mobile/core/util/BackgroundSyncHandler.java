/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.core.util;

import android.os.Handler;
import android.util.Log;

public class BackgroundSyncHandler {

    private final String CLS_TAG = BackgroundSyncHandler.class.getSimpleName();

    public final static int DEFAULT_INTERVAL = 1000 * 30;

    protected Handler refreshHandler;

    protected final int interval;

    protected boolean prevTaskCompleted = true;

    public interface SyncCallback {
        void doSync();
    }

    protected final SyncCallback syncCallback;

    public BackgroundSyncHandler(SyncCallback syncCallback, int interval) {
        this.interval = interval;
        this.syncCallback  = syncCallback;
    }

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshCallback();
            runRefresh();
        }
    };

    private void refreshCallback() {
        if (prevTaskCompleted) {
            //Start a new task
            prevTaskCompleted = false;
            //this call is done in the UI thread
            try {
                syncCallback.doSync();
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".refreshCallback. Error while callback is being called.");
                stop();
            }
            prevTaskCompleted = true;
        }
    }

    public void start() {
        //if not already started
        if (refreshHandler == null) {
            refreshHandler = new Handler();
            runRefresh();
        }
    }

    public void stop() {
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            refreshHandler = null;
        }
    }

    public void runRefresh() {
        if (refreshHandler != null) {
            refreshHandler.postDelayed(refreshRunnable, interval);
        }
    }

}
