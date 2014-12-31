/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptShareStatusNotification;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.service.SaveReceiptReply;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.SessionManager;

/**
 * An extension of <code>Service</code> for performing background receipt "share" with the Receipt Store.
 * 
 * @author andy
 */
public class ReceiptShareService extends Service {

    private static final String CLS_TAG = ReceiptShareService.class.getSimpleName();

    // Contains the name of the directory within the private application files external directory
    // where receipt files are placed that are to be uploaded to the receipt store.
    public static final String RECEIPT_SHARE_DIRECTORY = "share";

    private final IBinder binder = new ReceiptShareLocalBinder();

    private final int SESSION_TIME_OUT_COUNT = 3;
    /**
     * Contains the intent action used to update current state of the Receipt Share service.
     */
    public static final String ACTION_RECEIPT_SHARE_SERVICE_UPDATE = "com.concur.mobile.action.RECEIPT_SHARE_SERVICE_UPDATE";
    /**
     * Contains the intent extra key used to retrive the Receipt Share service.
     */
    public static final String EXTRA_RECEIPT_SHARE_SERVICE_STATUS = "com.concur.mobile.extra.RECEIPT_SHARE_SERVICE_STATUS";
    /**
     * Contains the intent extra key used to retrieve the URI of the receipt currently being uploaded.
     */
    public static final String EXTRA_RECEIPT_SHARE_UPLOAD_URI = "com.concur.mobile.extra.RECEIPT_SHARE_UPLOAD_URI";
    /**
     * Contains the intent extra key used to retrieve the mime-type of the receipt currently being uploaded.
     */
    public static final String EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE = "com.concur.mobile.extra.RECEIPT_SHARE_UPLOAD_MIME_TYPE";
    /**
     * Contains the intent extra key used to retrieve the upload progress (number from 0 - 100) percent of the receipt currently
     * being uploaded. Progress reported via this extra will be in increments of 5 percent.
     */
    public static final String EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS = "com.concur.mobile.extra.RECEIPT_SHARE_UPLOAD_PROGRESS";

    /**
     * Contains the intent extra key used to retrieve the upload result (a boolean value) of the last receipt upload attempt.
     */
    public static final String EXTRA_RECEIPT_SHARE_UPLOAD_RESULT = "com.concur.mobile.extra.RECEIPT_SHARE_UPLOAD_RESULT";

    /**
     * Contains the intent extra key used to retrieve the upload result reason of the last receipt upload attempt if it failed.
     */
    public static final String EXTRA_RECEIPT_SHARE_UPLOAD_REASON = "com.concur.mobile.extra.RECEIPT_SHARE_UPLOAD_RESULT_REASON";

    /**
     * Contains the intent extra key indicating whether a retry attempt will be made.
     */
    public static final String EXTRA_RECEIPT_SHARE_UPLOAD_RETRY = "com.concur.mobile.extra.RECEIPT_SHARE_UPLOAD_RETRY";

    /**
     * Contains the intent extra key indicating whether the service has been started by the device boot receiver.
     */
    public static final String EXTRA_RECEIPT_SHARE_DEVICE_BOOT = "com.concur.mobile.extra.RECEIPT_SHARE_DEVICE_BOOT";

    /**
     * Contains whether the last call to 'onStartCommand' came from the device boot receiver.
     */
    protected boolean deviceBoot;

    /**
     * Contains a reference to the Notification Manager.
     */
    protected NotificationManager notMngr;

    /**
     * Contains a reference to a Notification that will be updated based on status.
     */
    protected Notification notification;

    /**
     * Contains the intent used to launch the Concur StartUp activity.
     */
    protected PendingIntent concurLoginIntent;

    /**
     * Contains the intent used to launch the receipt share status activity.
     */
    protected PendingIntent receiptShareStatusIntent;

    /**
     * Contains a reference to the receipt share handler.
     */
    protected ReceiptShareHandler receiptShareHandler;

    /**
     * Contains a reference to a thread running the receipt share handler.
     */
    protected Thread receiptShareHandlerThread;

    /**
     * Contains a reference to the receipt share connectivity receiver.
     */
    protected ReceiptShareConnectivityReceiver connectivityReceiver;

    /**
     * Contains a reference to a filter used to register the connectivity receiver.
     */
    protected IntentFilter connectivityFilter;

    /**
     * Contains whether or not the share service has connectivity.
     */
    protected boolean connectivityAvailable;

    /**
     * Contains a reference to the concur service bound receiver.
     */
    protected ConcurServiceBoundReceiver concurServiceBoundReceiver;

    /**
     * Contains a reference to a filter used to register the concur service bound receiver.
     */
    protected IntentFilter concurServiceBoundFilter;

    /**
     * Contains whether or not the concur service is current bound.
     */
    protected boolean concurServiceAvailable;

    /**
     * Contains whether or not there is a concur session available for use.
     */
    protected boolean sessionAvailable;

    /**
     * Contains a reference to an object used to notify the worker thread of when connectivity is available.
     */
    private Object connectivityAvailableSyncObj = new Object();

    /**
     * Contains a reference to an object used to notify the worker thread of when a session may be established.
     */
    private Object sessionAvailabilitySyncObj = new Object();

    /**
     * Contains a reference to an object used to notify the worker thread of when share items have been made available.
     */
    private Object shareItemAvailabilitySyncObj = new Object();

    /**
     * Contains a reference to an object used to notify the worker thread of when the Concur Service is available.
     */
    private Object concurServiceAvailableSyncObj = new Object();

    /**
     * Contains an intent sent as a sticky broadcast indicating the current state of the service.
     */
    protected Intent serviceStatusIntent = new Intent(ACTION_RECEIPT_SHARE_SERVICE_UPDATE);

    /**
     * Contains the external receipt share directory.
     */
    public static File externalCacheDirectory;

    static {
        File externalFilesDir = ViewUtil.getExternalFilesDir(ConcurCore.getContext());
        if (externalFilesDir != null) {
            externalCacheDirectory = new File(externalFilesDir, RECEIPT_SHARE_DIRECTORY);
            try {
                if (!externalCacheDirectory.exists()) {
                    if (!externalCacheDirectory.mkdirs()) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".<static>: unable to initialize receipt share cache directory '"
                                + externalCacheDirectory.getAbsolutePath() + "'.");
                        externalCacheDirectory = null;
                    }
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".<static>: exception initializing receipt share cache directory '"
                        + externalCacheDirectory.getAbsolutePath() + "'.", exc);
                externalCacheDirectory = null;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".<static>: unable to initialize receipt share cache directory!");
        }
    }

    /**
     * An enumeration that models the states of the Receipt Share service.
     */
    public enum Status {
        // Indicates the service is starting up.
        STARTING_UP("STARTING_UP"),
        // Indicates the service is running.
        RUNNING("RUNNING"),
        // Indicates the service is preparing to upload a receipt.
        PREPARING_UPLOAD("PREPARING_UPLOAD"),
        // Indicates the service is currently uploading a receipt.
        UPLOADING("UPLOADING"),
        // Indicates the service has finished uploading a receipt.
        FINISHED_UPLOAD("FINISHED_UPLOAD"),
        // Indicates the service is waiting on connectivity.
        WAITING_FOR_CONNECTIVITY("WAITING_FOR_CONNECTIVITY"),
        // Indicates the service is waiting to establish a session (user login)
        WAITING_FOR_SESSION("WAITING_FOR_SESSION"),
        // Indicates the service is idle for the moment and waiting on a new list of share items.
        WAITING_FOR_SHARE_ITEMS("WAITING_FOR_SHARE_ITEMS"),
        // Indicates the service is waiting for the Concur Service to become available.
        WAITING_FOR_CONCUR_SERVICE("WAITING_FOR_CONCUR_SERVICE"),
        // Indicates the service is shutting down.
        SHUTTING_DOWN("SHUTTING_DOWN");

        private String name;

        Status(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>Status</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>ReceiptShareService.Status</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static Status fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (Status st : Status.values()) {
                    if (st.name.equalsIgnoreCase(name)) {
                        return st;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    };

    /**
     * Contains the current status.
     */
    protected Status status;

    /**
     * Class used for the client Binder. Because we know this service always runs in the same process as its clients, we don't
     * need to deal with IPC.
     */
    public class ReceiptShareLocalBinder extends Binder {

        public ReceiptShareService getService() {
            return ReceiptShareService.this;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onBind:");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: initializing...");
        setStatus(Status.STARTING_UP);
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Const.LOG_TAG, CLS_TAG + ".onDestroy:");
        // Unregister the service bound receiver.
        if (concurServiceBoundReceiver != null) {
            unregisterReceiver(concurServiceBoundReceiver);
        }
        // Unregister the connectivity receiver.
        if (connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver);
        }
        // Remove the sticky service status broadcast.
        if (serviceStatusIntent != null) {
            removeStickyBroadcast(serviceStatusIntent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onStartCommand: ");
        // Set the device boot flag.
        if (intent != null && intent.hasExtra(EXTRA_RECEIPT_SHARE_DEVICE_BOOT)) {
            deviceBoot = intent.getBooleanExtra(EXTRA_RECEIPT_SHARE_DEVICE_BOOT, false);
        }
        Log.d(Const.LOG_TAG, CLS_TAG + ".onStartCommand: deviceBoot(" + deviceBoot + ")");
        // Construct and kick-off the worker thread, if needbe.
        if (receiptShareHandler == null) {
            receiptShareHandler = new ReceiptShareHandler();
            receiptShareHandlerThread = new Thread(receiptShareHandler);
            receiptShareHandlerThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Will initializes various components required by this service.
     */
    protected void init() {
        // Init the notification manager.
        initNotificationManager();
        // Init the connectivity receiver.
        initConnectivityReceiver();
        // Init the ConcurService availability receiver.
        initConcurServiceReceiver();
    }

    /**
     * Initializes the connectivity receiver.
     */
    protected void initConnectivityReceiver() {
        if (connectivityReceiver == null) {
            connectivityReceiver = new ReceiptShareConnectivityReceiver();
            connectivityFilter = new IntentFilter(
                    com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
            connectivityFilter
                    .addAction(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);
            Intent stickyIntent = registerReceiver(connectivityReceiver, connectivityFilter);
            if (stickyIntent != null) {
                connectivityAvailable = stickyIntent.getAction().equalsIgnoreCase(
                        com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
            }
        }
    }

    /**
     * Initializes the concur service receiver.
     */
    protected void initConcurServiceReceiver() {
        if (concurServiceBoundReceiver == null) {
            concurServiceBoundFilter = new IntentFilter(Const.ACTION_CONCUR_SERVICE_BOUND);
            concurServiceBoundFilter.addAction(Const.ACTION_CONCUR_SERVICE_UNBOUND);
            concurServiceBoundReceiver = new ConcurServiceBoundReceiver();
            Intent stickyIntent = registerReceiver(concurServiceBoundReceiver, concurServiceBoundFilter);
            if (stickyIntent != null) {
                concurServiceAvailable = stickyIntent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SERVICE_BOUND);
            }
        }
    }

    /**
     * Initializes the notification manager.
     */
    protected void initNotificationManager() {
        if (notMngr == null) {
            notMngr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notMngr != null) {
                // Set up the login intent.
                Intent notIntent = new Intent(Intent.ACTION_MAIN);
                notIntent.setPackage(getPackageName());
                notIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                // MOB-20508 - set FLAG_ACTIVITY_SINGLE_TOP so the "from notification" flag gets
                // passed to the Startup class.
                notIntent
                        .setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                notIntent.putExtra("from notification", true);
                concurLoginIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Set up the Receipt Share status intent.
                notIntent.setClassName(getPackageName(), ReceiptShareStatusNotification.class.getName());
                receiptShareStatusIntent = PendingIntent.getActivity(this, 0, notIntent, 0);
                notification = new Notification(R.drawable.icon_concur_status, getText(R.string.sharing_receipt),
                        System.currentTimeMillis());
                notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
                notification.contentView = new RemoteViews(getApplicationContext().getPackageName(),
                        R.layout.upload_progress_bar);
                notification.contentIntent = receiptShareStatusIntent;
                notification.contentView.setProgressBar(R.id.progress, 100, 0, false);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initNotificationManager: unable to access Notification Manager!");
            }
        }
    }

    /**
     * Gets the status of the Receipt Share service.
     * 
     * @return returns the Receipt Share service status.
     */
    public synchronized Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the Receipt Share service.
     * 
     * @param status
     *            contains the new status.
     */
    protected synchronized void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Will broadcast the current status of the receipt share service.
     */
    protected void broadcastStatus() {
        if (getStatus() != null) {
            removeStickyBroadcast(serviceStatusIntent);
            switch (getStatus()) {
            case RUNNING: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.RUNNING.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case SHUTTING_DOWN: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.SHUTTING_DOWN.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case STARTING_UP: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.STARTING_UP.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case UPLOADING: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.UPLOADING.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case WAITING_FOR_CONCUR_SERVICE: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS,
                        Status.WAITING_FOR_CONCUR_SERVICE.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case WAITING_FOR_CONNECTIVITY: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS,
                        Status.WAITING_FOR_CONNECTIVITY.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case WAITING_FOR_SESSION: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.WAITING_FOR_SESSION.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            case WAITING_FOR_SHARE_ITEMS: {
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS,
                        Status.WAITING_FOR_SHARE_ITEMS.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, "");
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                break;
            }
            }
            sendStickyBroadcast(serviceStatusIntent);
        } else {
            removeStickyBroadcast(serviceStatusIntent);
        }
    }

    /**
     * Sets whether the concur service is available.
     * 
     * @param available
     *            contains whether the concur service is available.
     */
    protected synchronized void setConcurServiceAvailable(boolean available) {
        concurServiceAvailable = available;
    }

    /**
     * Gets whether the concur service is available.
     * 
     * @return returns whether the concur service is available.
     */
    protected synchronized boolean isConcurServiceAvailable() {
        return concurServiceAvailable;
    }

    /**
     * Sets whether connectivitiy is available.
     * 
     * @param available
     *            contains whether connectivity is available.
     */
    protected synchronized void setConnectivityAvailable(boolean available) {
        connectivityAvailable = available;
    }

    /**
     * Gets whether connectivity is available.
     * 
     * @return returns whether connectivity is available.
     */
    protected synchronized boolean isConnectivityAvailable() {
        return connectivityAvailable;
    }

    /**
     * Sets whether or not there is a session available.
     * 
     * @param available
     *            contains whether there is a session available.
     */
    protected synchronized void setSessionAvailable(boolean available) {
        sessionAvailable = available;
    }

    /**
     * Gets whether or not there is a session available.
     * 
     * @return returns whether there is a session available.
     */
    protected synchronized boolean isSessionAvailable() {
        boolean isSessionExpire = SessionManager.isSessionExpire((ConcurCore) getApplication());
        if (isSessionExpire) {
            String sessionId = SessionManager.validateSessionId((ConcurCore) getApplication());
            return (sessionId != null && sessionId.length() > 0);
        } else {
            return true;
        }
    }

    /**
     * Gets the list of <code>ReceiptShareItem</code> to be shared to the Receipt Store.
     * 
     * @return returns the list of <code>ReceiptShareItem</code> objects that are pending shared to the ReceiptStore.
     */
    public List<ReceiptShareItem> getReceiptShareItems() {
        List<ReceiptShareItem> rsItems = null;

        ConcurCore core = (ConcurCore) getApplication();
        ConcurService concurService = core.getService();
        if (concurService != null) {
            rsItems = filterForPending(concurService.getReceiptShareList());
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".getReceiptShareItems: Concur Service is not available!");
        }
        return rsItems;
    }

    /**
     * Adds a list of <code>ReceiptShareItem</code> objects to be shared to the Receipt Store.
     * 
     * @param rsItems
     *            contains the list of <code>ReceiptShareItem</code> objects to be shared to the Receipt Store.
     */
    public void addReceiptShareItems(List<ReceiptShareItem> rsItems) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_SHARE_COUNT, ((rsItems != null) ? Integer.toString(rsItems.size()) : "0"));
        EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_SHARE, params);

        ConcurCore core = (ConcurCore) getApplication();
        ConcurService concurService = core.getService();
        if (concurService != null) {
            // Call the service to persist the items.
            concurService.addReceiptShares(rsItems);
            // Notify the worker thread if waiting for items.
            notifyAllOnObject(shareItemAvailabilitySyncObj, CLS_TAG + ".addReceiptShareItems: ");
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".addReceiptShareItems: Concur Service is not available!");
        }
    }

    /**
     * Removes a list of <code>ReceiptShareItem</code> objects from being shared with the Receipt Store.
     * 
     * @param rsItems
     */
    public void removeReceiptShareItems(List<ReceiptShareItem> rsItems) {
        ConcurCore core = (ConcurCore) getApplication();
        ConcurService concurService = core.getService();
        if (concurService != null) {
            // Call the service to remove the items from persistence.
            concurService.removeReceiptShares(rsItems);
            // Notify the worker thread if waiting for items.
            notifyAllOnObject(shareItemAvailabilitySyncObj, CLS_TAG + ".removeReceiptShareItems: ");
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".removeReceiptShareItems: Concur Service is not available!");
        }
    }

    /**
     * Provides a notification that a session is available for the service.
     */
    public void sessionAvailable() {
        // Notify the worker thread if waiting for a session.
        notifyAllOnObject(sessionAvailabilitySyncObj, CLS_TAG + ".sessionAvailable: ");
    }

    protected boolean isImage(String mimeType) {
        boolean retVal = false;
        if (mimeType != null) {
            retVal = (mimeType.startsWith("image/") ? true : false);
        }
        return retVal;
    }

    protected boolean isPDF(String mimeType) {
        boolean retVal = false;
        if (mimeType != null) {
            retVal = (mimeType.startsWith("application/pdf") ? true : false);
        }
        return retVal;
    }

    /**
     * Calls <code>notifyAll</code> on <code>syncObj</code>.
     * 
     * @param syncObj
     */
    private void notifyAllOnObject(Object syncObj, String caller) {
        synchronized (syncObj) {
            try {
                syncObj.notifyAll();
            } catch (IllegalMonitorStateException ilmse) {
                Log.e(Const.LOG_TAG, caller, ilmse);
            }
        }
    }

    /**
     * Calls <code>wait(long)</code> on <code>syncObj</code> with timeout <code>time</code>.
     * 
     * @param syncObj
     *            the object to call wait on.
     * @param time
     *            the time out in milliseconds.
     * @param caller
     *            the caller.
     */
    private void timedWaitOnObject(Object syncObj, long time, String caller) {
        synchronized (syncObj) {
            try {
                syncObj.wait(time);
            } catch (IllegalMonitorStateException ilmsExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + caller + ".waitOnObject: ", ilmsExc);
            } catch (InterruptedException intExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + caller + ".waitOnObject: ", intExc);
            }
        }
    }

    /**
     * Will filter all <code>ReceiptShareItem</code> objects out of <code>rsItems</code> that do not have a status of
     * <code>ReceiptShareItem.Status.PENDING</code> and return a new list.
     * 
     * @param rsItems
     *            contains the list of receipt share items to filter.
     * @return returns a new list of <code>ReceiptShareItem</code> objects with a status of
     *         <code>ReceiptShareItem.Status.PENDING</code>.
     */
    private List<ReceiptShareItem> filterForPending(List<ReceiptShareItem> rsItems) {
        List<ReceiptShareItem> retVal = null;
        if (rsItems != null && rsItems.size() > 0) {
            for (ReceiptShareItem rsItem : rsItems) {
                if (rsItem.status == ReceiptShareItem.Status.PENDING) {
                    if (retVal == null) {
                        retVal = new ArrayList<ReceiptShareItem>();
                    }
                    retVal.add(rsItem);
                }
            }
        }
        return retVal;
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to listen for changes in data connectivity.
     */
    private class ReceiptShareConnectivityReceiver extends BroadcastReceiver {

        private String CLS_TAG = ReceiptShareService.ReceiptShareConnectivityReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                setConnectivityAvailable(intent.getAction().equalsIgnoreCase(
                        com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE));
                notifyAllOnObject(connectivityAvailableSyncObj, CLS_TAG + ".onReceive: ");
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to listen for changes in concur service availablity.
     */
    private class ConcurServiceBoundReceiver extends BroadcastReceiver {

        private String CLS_TAG = ReceiptShareService.ConcurServiceBoundReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                setConcurServiceAvailable(intent.getAction().equalsIgnoreCase(Const.ACTION_CONCUR_SERVICE_BOUND));
                notifyAllOnObject(concurServiceAvailableSyncObj, CLS_TAG + ".onReceive: ");
            }
        }
    }

    /**
     * An implementation of <code>Runable, SaveReceiptRequest.SaveReceiptUploadListener</code> that will is the main worker thread
     * handling receipt save requests.
     */
    private class ReceiptShareHandler implements Runnable, SaveReceiptRequest.SaveReceiptUploadListener {

        private String CLS_TAG = ReceiptShareService.CLS_TAG + "." + ReceiptShareHandler.class.getSimpleName();

        private static final boolean DEBUG = true;

        // Contains the last reported percent complete upload.
        private int lastPercent;

        // Contains the receipt share item currently being uploaded.
        private ReceiptShareItem curShareItem;

        // Contains the time for each wait in between checks concur service availability.
        private static final long CONCUR_SERVICE_WAIT_TIME = (1000L * 10L);
        // Contains the time for one wait for receipt share items (receipts to be uploaded) are available.
        private static final long RECEIPT_SHARE_ITEM_WAIT_TIME = (1000L * 60L * 3L);
        // Contains the time for each wait in between checks for connectivity.
        private static final long CONNECTIVITY_WAIT_TIME = (1000L * 10L);
        // Contains the time for each wait in between checks for session availability.
        private static final long SESSION_WAIT_TIME = (1000L * 20L);
        // Contains the time for each sleep after an upload has failed.
        private static final long UPLOAD_FAIL_RETRY_WAIT_TIME = (1000L * 20L);
        // Contains the time for each sleep after an exception in the main try/catch block is experienced.
        private static final long EXCEPTION_WAIT_TIME = (1000L * 20L);

        private int count;

        @Override
        public void run() {

            while (true) {
                try {
                    // Set the status.
                    setStatus(ReceiptShareService.Status.RUNNING);
                    broadcastStatus();

                    // Check for Concur Service availability.
                    if (!isConcurServiceAvailable()) {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: waiting for Concur service.");
                        }
                        // Set the status.
                        setStatus(ReceiptShareService.Status.WAITING_FOR_CONCUR_SERVICE);
                        broadcastStatus();
                        while (!isConcurServiceAvailable()) {
                            // Wait for the service to become available for 'CONCUR_SERVICE_WAIT_TIME' seconds prior to
                            // checking again.
                            timedWaitOnObject(concurServiceAvailableSyncObj, CONCUR_SERVICE_WAIT_TIME, CLS_TAG + ".run");
                        }
                        if (DEBUG) {
                            if (isConcurServiceAvailable()) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".run: Concur service available.");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: Concur service available.");
                        }
                    }

                    // Set the status.
                    setStatus(ReceiptShareService.Status.RUNNING);
                    broadcastStatus();

                    // Check for whether the service has any receipts it needs to share.
                    ConcurCore concurCore = (ConcurCore) getApplication();
                    ConcurService concurService = concurCore.getService();
                    List<ReceiptShareItem> shareItems = filterForPending(concurService.getReceiptShareList());
                    if (deviceBoot) {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: device boot, cleaning unreferenced files.");
                        }
                        deleteUnreferencedFiles(shareItems);
                        deviceBoot = false;
                    }
                    if (shareItems == null || shareItems.size() == 0) {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: waiting for share items for a fixed period of time.");
                        }
                        // Hide the notification.
                        hideNotification();
                        // Set the status.
                        setStatus(ReceiptShareService.Status.WAITING_FOR_SHARE_ITEMS);
                        broadcastStatus();
                        // Wait for 'RECEIPT_SHARE_ITEM_WAIT_TIME' seconds prior to shutting down the service.
                        timedWaitOnObject(shareItemAvailabilitySyncObj, RECEIPT_SHARE_ITEM_WAIT_TIME, CLS_TAG + "run");
                        shareItems = filterForPending(concurService.getReceiptShareList());
                        if (shareItems == null || shareItems.size() == 0) {
                            if (DEBUG) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".run: no share items, shutting service down.");
                            }
                            // Set the status.
                            setStatus(ReceiptShareService.Status.SHUTTING_DOWN);
                            broadcastStatus();
                            // Stop the service.
                            stopSelf();
                            // Clear the reference to this handler.
                            receiptShareHandler = null;
                            receiptShareHandlerThread = null;
                            // Stop this running thread.
                            return;
                        } else {
                            if (DEBUG) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".run: share items added.");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: " + shareItems.size() + " share items.");
                        }
                    }

                    // Set the status.
                    setStatus(ReceiptShareService.Status.RUNNING);
                    broadcastStatus();

                    // Check for whether connectivity is available.
                    if (!isConnectivityAvailable()) {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: waiting for connectivity.");
                        }
                        // Set the notification subtitle and detail.
                        setNotificationSubTitle(getText(R.string.sharing_receipt_wait_on_connectivity_subtitle)
                                .toString());
                        setNotificationDetail(getText(R.string.sharing_receipt_wait_on_connectivity_detail).toString());
                        showNotificationDetail();
                        updateNotification();

                        // Set the status.
                        setStatus(ReceiptShareService.Status.WAITING_FOR_CONNECTIVITY);
                        broadcastStatus();
                        while (!isConnectivityAvailable()) {
                            // Wait for connectivity to become available for 'CONNECTIVITY_WAIT_TIME' seconds prior to
                            // checking again.
                            timedWaitOnObject(connectivityAvailableSyncObj, CONNECTIVITY_WAIT_TIME, CLS_TAG + ".run");
                        }
                        if (DEBUG) {
                            if (isConnectivityAvailable()) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".run: connectivity available.");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: connectivity available.");
                        }
                    }

                    // Set the status.
                    setStatus(ReceiptShareService.Status.RUNNING);
                    broadcastStatus();

                    // initialize count for endless loop
                    count = 0;

                    // Check for whether session can be established.
                    if (!isSessionAvailable()) {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: waiting for a session.");
                        }
                        // Set the notification subtitle, detail and pending intent.
                        setNotificationSubTitle(getText(R.string.sharing_receipt_wait_on_session_subtitle).toString());
                        setNotificationDetail(getText(R.string.sharing_receipt_wait_on_session_detail).toString());
                        setNotificationContentIntent(concurLoginIntent);
                        showNotificationDetail();
                        updateNotification();
                        // Set the status.
                        setStatus(ReceiptShareService.Status.WAITING_FOR_SESSION);
                        broadcastStatus();
                        while (!isSessionAvailable()) {
                            // Wait for a session to become available for 'SESSION_WAIT_TIME' seconds.
                            timedWaitOnObject(sessionAvailabilitySyncObj, SESSION_WAIT_TIME, CLS_TAG + ".run");
                            if (++count > SESSION_TIME_OUT_COUNT) {
                                break;
                            }
                        }
                        if (DEBUG) {
                            if (isConnectivityAvailable() && count <= SESSION_TIME_OUT_COUNT) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".run: session available.");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: session available.");
                        }
                    }

                    if (count <= SESSION_TIME_OUT_COUNT) {
                        // Set the status.
                        setStatus(ReceiptShareService.Status.RUNNING);
                        broadcastStatus();
                    }
                    // TODO: May want to encapsulate the above step about checking for receipt share items once more
                    // in the event the end-user cancelled some/all of them while waiting for connectivity/session.

                    // At this point, share items, connectivity and a session should be available to perform
                    // the receipt upload.
                    // NOTE: Currently, the code only actually uploads one receipt, then will go back and retrieve
                    // a new list, perform the various checks, then try to upload. Keeping an in-memory version of the list
                    // would probably be a better solution than going back to the database to get a new list each time.
                    curShareItem = shareItems.get(0);
                    int quantity = (shareItems.size() - 1);

                    if (count <= SESSION_TIME_OUT_COUNT) {
                        // Set the notification sub-title, show the progress bar and the pending intent.
                        showNotificationProgressBar();
                        setNotificationSubTitle(getResources().getQuantityString(
                                R.plurals.sharing_receipt_progress_subtitle, quantity, quantity));
                        setNotificationContentIntent(receiptShareStatusIntent);
                        updateNotification();
                    }

                    // Copy to an upload directory the receipt image file referenced by the uri.
                    boolean uploadResult = false;
                    // Init the last percent value.
                    lastPercent = 0;
                    boolean inputStreamUnavailable = false;
                    boolean noImagingConfiguration = false;
                    boolean receiptUser = isReceiptUser();

                    setPrepareUploadStatus(curShareItem);
                    setInitialUploadStatus(curShareItem);
                    String uploadFile = new File(externalCacheDirectory, curShareItem.fileName).getAbsolutePath();
                    // NOTE: We're passing 'false' for the 'deleteFile' argument as the service, started as the result
                    // of a device boot, will delete any unreferenced files. The files are not deleted upon successful
                    // upload as the Receipt Share Status activity may still want to view the files.
                    // MOB-13033 - Non Receipt Users fail upload immediately.
                    if (receiptUser && count <= SESSION_TIME_OUT_COUNT) {
                        uploadResult = uploadFile(uploadFile, false);
                    }

                    if (uploadResult) {
                        // Update the broadcast with a 100 percent.
                        setServiceStatusUploadProgressPercent(100);
                        setFinishedUploadStatus(curShareItem, true, false, getText(R.string.general_succeeded)
                                .toString());
                        List<ReceiptShareItem> deleteList = new ArrayList<ReceiptShareItem>(1);
                        deleteList.add(curShareItem);
                        concurService.removeReceiptShares(deleteList);
                        // Set the in-memory flag indicating a new receipt list should be
                        // be retrieved from the receipt store.
                        ReceiptStoreCache receiptStoreCache = concurCore.getReceiptStoreCache();
                        if (receiptStoreCache != null) {
                            receiptStoreCache.setShouldFetchReceiptList();
                        }
                        // Flurry event.
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_SHARE);
                        String uploadedUsing = Flurry.PARAM_VALUE_MWS;
                        String token = Preferences.getAccessToken();
                        if (token != null && token.length() > 0) {
                            uploadedUsing = Flurry.PARAM_VALUE_CONNECT;
                        }
                        params.put(Flurry.PARAM_NAME_UPLOADED_USING, uploadedUsing);
                        params.put(Flurry.PARAM_NAME_ADDED_TO, Flurry.PARAM_VALUE_RECEIPT_STORE);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_ADD, params);
                    } else {
                        if (!inputStreamUnavailable && !noImagingConfiguration && receiptUser) {
                            try {
                                // update offline queue
                                // Save it to the database
                                MobileDatabase mdb = concurService.getMobileDatabase();
                                // Kludge and store the time in the display name
                                String now = FormatUtil.XML_DF_LOCAL.format(Calendar.getInstance().getTime());
                                ReceiptShareItem rsi = new ReceiptShareItem(null, null, uploadFile, now,
                                        com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem.Status.HOLD);
                                ArrayList<ReceiptShareItem> rsList = new ArrayList<ReceiptShareItem>(1);
                                rsList.add(rsi);
                                mdb.insertReceiptShareItems(rsList);
                                setFinishedUploadStatus(curShareItem, false, true,
                                        getText(R.string.receipt_share_failed_retry).toString());
                                Thread.sleep(UPLOAD_FAIL_RETRY_WAIT_TIME);
                                if (DEBUG) {
                                    Log.d(Const.LOG_TAG, CLS_TAG
                                            + ".run: receipt uploaded to offline queue, waiting 20 seconds");
                                }

                                List<ReceiptShareItem> deleteList = new ArrayList<ReceiptShareItem>(1);
                                deleteList.add(curShareItem);
                                concurService.removeReceiptShares(deleteList);

                                if (quantity == 0) {
                                    // Set the status.
                                    setStatus(ReceiptShareService.Status.SHUTTING_DOWN);
                                    broadcastStatus();
                                    // hide notification bar
                                    hideNotification();
                                    // Stop the service.
                                    stopSelf();
                                    // Clear the reference to this handler.
                                    receiptShareHandler = null;
                                    receiptShareHandlerThread = null;
                                    // Stop this running thread.
                                    return;
                                }

                            } catch (InterruptedException intExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".run: interrupted while pausing betweeen receipt upload attempts.");
                            }
                        } else if (inputStreamUnavailable) {
                            if (DEBUG) {
                                Log.d(Const.LOG_TAG, CLS_TAG
                                        + ".run: receipt input stream unavailable, removing share item.");
                            }
                            setFinishedUploadStatus(curShareItem, false, false,
                                    getText(R.string.receipt_share_failed_content_unavailable).toString());
                            List<ReceiptShareItem> deleteList = new ArrayList<ReceiptShareItem>(1);
                            deleteList.add(curShareItem);
                            concurService.removeReceiptShares(deleteList);

                            // Flurry Notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
                            params.put(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_RECEIPT_SHARE_SERVICE);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                        } else if (noImagingConfiguration) {
                            if (DEBUG) {
                                Log.d(Const.LOG_TAG, CLS_TAG + ".run: no imaging configuration, removing share item.");
                            }
                            setFinishedUploadStatus(curShareItem, false, false,
                                    getText(R.string.receipt_share_failed_no_imaging_configuration).toString());
                            List<ReceiptShareItem> deleteList = new ArrayList<ReceiptShareItem>(1);
                            deleteList.add(curShareItem);
                            concurService.removeReceiptShares(deleteList);

                            // Flurry Notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
                            params.put(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_RECEIPT_SHARE_SERVICE);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);
                        } else if (!receiptUser) {
                            if (DEBUG) {
                                Log.d(Const.LOG_TAG, CLS_TAG
                                        + ".run: non-Expense user or no receipt store access, removing share item.");
                            }
                            // Set up upload failed message.
                            setFinishedUploadStatus(curShareItem, false, false,
                                    getText(R.string.receipt_share_failed_no_receipt_access).toString());
                            // Punt the share item.
                            List<ReceiptShareItem> deleteList = new ArrayList<ReceiptShareItem>(1);
                            deleteList.add(curShareItem);
                            concurService.removeReceiptShares(deleteList);
                        }
                    }

                    // Clear the current share item.
                    curShareItem = null;

                    // Set the status.
                    setStatus(ReceiptShareService.Status.RUNNING);
                    broadcastStatus();

                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".run: exception thrown in service thread", exc);
                    // TODO: Should send the stack trace of the exception to our exception logging service on the
                    // server.
                    // Perform a thread sleep to keep repeated issues from happening in a tight-loop.
                    try {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".run: exception thrown, waiting 20 seconds to try again");
                        }
                        Thread.sleep(EXCEPTION_WAIT_TIME);
                    } catch (InterruptedException intExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".run: interrupted while pausing after exception throw.");
                    }
                }
            }
        }

        /**
         * Gets whether or not the end-user is both an Expense user with permitted access to the Receipt Store.
         * 
         * @return returns whether or not the end-user is an Expense user with permitted access to the Receipt Store.
         */
        protected boolean isReceiptUser() {
            boolean retVal = true;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ReceiptShareService.this);
            boolean expenseUser = prefs.getBoolean(Const.PREF_CAN_EXPENSE, false);
            if (expenseUser) {
                // Check for Receipt Store access.
                retVal = !ViewUtil.isReceiptStoreHidden(ReceiptShareService.this);
            } else {
                retVal = false;
            }
            return retVal;
        }

        /**
         * Will delete any files left in the share directory that are not referenced by share items in <code>rsItems</code>.
         * 
         * @param rsItems
         *            contains the list of items to be shared.
         */
        protected void deleteUnreferencedFiles(List<ReceiptShareItem> rsItems) {
            if (externalCacheDirectory != null) {
                try {
                    String[] files = externalCacheDirectory.list();
                    if (files != null) {
                        int delFileCnt = 0;
                        for (String cacheFileName : files) {
                            boolean foundReference = false;
                            if (rsItems != null) {
                                for (ReceiptShareItem rsItem : rsItems) {
                                    if (rsItem.fileName != null && cacheFileName != null
                                            && rsItem.fileName.equalsIgnoreCase(cacheFileName)) {
                                        foundReference = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundReference) {
                                if (DEBUG) {
                                    Log.d(Const.LOG_TAG, CLS_TAG + ".deleteUnreferencedFile: deleting file '"
                                            + cacheFileName + "'.");
                                }
                                String absFilePath = new File(externalCacheDirectory, cacheFileName).getAbsolutePath();
                                ViewUtil.deleteFile(absFilePath);
                                ++delFileCnt;
                            }
                        }
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".deleteUnreferencedFile: deleted " + delFileCnt + " files.");
                        }
                    }
                } catch (SecurityException secExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".deleteUnreferencedFiles: security exception", secExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteUnreferencedFiles: externalCacheDirectory is null!");
            }
        }

        protected boolean uploadFile(String filePath, boolean deleteReceiptFile) throws NoImagingConfiguration {
            boolean retVal = false;
            SaveReceiptRequest request = null;

            String sessionId = Preferences.getSessionId();
            if (sessionId != null) {
                // Construct the request.
                String msgId = Long.toString(System.currentTimeMillis());
                request = new SaveReceiptRequest();
                request.filePath = filePath;
                request.deleteReceiptFile = deleteReceiptFile;
                request.listener = this;
                request.imageOrigin = "mobile";
                request.messageId = msgId;
                request.sessionId = sessionId;

                // If there's an access token available, then use Connect to perform the upload.
                request.accessToken = Preferences.getAccessToken();
                if (request.accessToken != null) {
                    request.receiptEndpoint = SaveReceiptRequest.SaveReceiptCall.CONNECT_POST_IMAGE;
                }

                // Send the request.
                SaveReceiptReply reply = null;
                try {
                    ConcurCore concurCore = (ConcurCore) getApplication();
                    ConcurService concurService = concurCore.getService();
                    reply = (SaveReceiptReply) request.process(concurService);
                    // Saving receipts to Connect can return an 'HttpStatus.SC_CREATE' when the
                    // resource has been created.
                    if (reply.httpStatusCode == HttpStatus.SC_CREATED) {
                        reply.httpStatusCode = HttpStatus.SC_OK;
                    }
                    if (reply.httpStatusCode == HttpStatus.SC_OK) {
                        if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                            retVal = true;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".uploadFile: MWS status(" + reply.mwsStatus + ") - "
                                    + reply.mwsErrorMessage + ".");
                        }
                    } else if (reply.httpStatusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                        // An internal server error on a save receipt call indicates the lack
                        // of an imaging configuration.
                        throw new NoImagingConfiguration();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".uploadFile: HTTP status(" + reply.httpStatusCode + ") - "
                                + reply.httpStatusText + ".");
                    }
                } catch (ServiceRequestException srvReqExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".uploadFile: " + srvReqExc.getMessage());
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".uploadFile: " + ioExc.getMessage());
                } finally {
                    // Ensure we punt the upload file if directed.
                    if (retVal && request.deleteReceiptFile) {
                        ViewUtil.deleteFile(request.filePath);
                    }
                }
            }
            return retVal;
        }

        /**
         * Sets the upload prepare status.
         * 
         * @param rsItem
         *            the receipt share item being uploaded.
         */
        protected void setPrepareUploadStatus(ReceiptShareItem rsItem) {
            if (rsItem != null) {
                setStatus(Status.PREPARING_UPLOAD);
                removeStickyBroadcast(serviceStatusIntent);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.PREPARING_UPLOAD.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, rsItem.uri);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, rsItem.mimeType);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                sendStickyBroadcast(serviceStatusIntent);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setPrepareUploadStatus: rsItem is null!");
            }
        }

        /**
         * Sets the initial upload status.
         * 
         * @param rsItem
         *            the receipt share item being uploaded.
         */
        protected void setInitialUploadStatus(ReceiptShareItem rsItem) {
            if (rsItem != null) {
                setStatus(Status.UPLOADING);
                removeStickyBroadcast(serviceStatusIntent);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.UPLOADING.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, rsItem.uri);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, rsItem.mimeType);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                sendStickyBroadcast(serviceStatusIntent);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setInitialUploadStatus: rsItem is null!");
            }
        }

        protected void setFinishedUploadStatus(ReceiptShareItem rsItem, boolean result, boolean retry, String reason) {
            if (rsItem != null) {
                setStatus(Status.FINISHED_UPLOAD);
                removeStickyBroadcast(serviceStatusIntent);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_SERVICE_STATUS, Status.FINISHED_UPLOAD.getName());
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_URI, rsItem.uri);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_MIME_TYPE, rsItem.mimeType);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_RETRY, retry);
                serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_REASON, reason);
                sendStickyBroadcast(serviceStatusIntent);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setPrepareUploadStatus: rsItem is null!");
            }
        }

        @Override
        public void onChange(int percent) {
            if (percent >= (lastPercent + 5)) {
                setNotificationProgressBarPercent(percent);
                setServiceStatusUploadProgressPercent(percent);
                lastPercent = percent;
            }
        }

        /**
         * Will set the notification progress value.
         * 
         * @param percent
         *            contains the notification progress percent value.
         */
        protected void setNotificationProgressBarPercent(int percent) {
            notification.contentView.setProgressBar(R.id.progress, 100, percent, false);
            updateNotification();
            lastPercent = percent;
        }

        /**
         * Will set the notification subtitle text.
         * 
         * @param subTitle
         *            contains the notification subtitle text.
         */
        protected void setNotificationSubTitle(String subTitle) {
            notification.contentView.setTextViewText(R.id.sub_title, subTitle);
        }

        /**
         * Will set the notification detail text.
         * 
         * @param detail
         *            contains the notification detail text.
         */
        protected void setNotificationDetail(String detail) {
            notification.contentView.setTextViewText(R.id.detail, detail);
        }

        /**
         * Sets the notification pending intent.
         * 
         * Sets the notification pending intent, the intent that will be resolved when the end-user clicks on the notification.
         * 
         * @param pendingIntent
         *            contains the notification pending intent.
         */
        protected void setNotificationContentIntent(PendingIntent pendingIntent) {
            notification.contentIntent = pendingIntent;
        }

        /**
         * Will first hide the notification detail and then show the notification progress bar.
         */
        protected void showNotificationProgressBar() {
            notification.contentView.setViewVisibility(R.id.detail, View.GONE);
            notification.contentView.setViewVisibility(R.id.progress, View.VISIBLE);
        }

        /**
         * Will first hide the notification progress bar, then show the notification detail.
         */
        protected void showNotificationDetail() {
            notification.contentView.setViewVisibility(R.id.progress, View.GONE);
            notification.contentView.setViewVisibility(R.id.detail, View.VISIBLE);
        }

        protected void updateNotification() {
            notMngr.notify(1, notification);
        }

        protected void hideNotification() {
            notMngr.cancel(1);
        }

        protected void setServiceStatusUploadProgressPercent(int percent) {
            removeStickyBroadcast(serviceStatusIntent);
            serviceStatusIntent.putExtra(EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, percent);
            sendStickyBroadcast(serviceStatusIntent);
        }

    }

}

/**
 * An extension of <code>Exception</code> indicating the lack of a receipt imaging configuration.
 */
class NoImagingConfiguration extends Exception {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = 1L;

}
