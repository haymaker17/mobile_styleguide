package com.concur.mobile.core.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.receiver.AWSPushNotificationReceiver;
import com.concur.mobile.core.util.Const;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.concur.mobile.niftyservice.*;

import org.apache.http.HttpStatus;


/**
 *  This class manages registration and deregistration from Nifty push notification
 */
public class NiftyPushNotificationService extends Service implements BaseAsyncRequestTask.AsyncReplyListener {

    private final String CLS_TAG = NiftyPushNotificationService.class.getSimpleName();

    /**
     * Contains the instance of GoogleCloudMessaging used to register with google for push notifications
     */
    private GoogleCloudMessaging gcm;

    /**
     * Contains the instance of ConcurCore for this application
     */
    private ConcurCore ctx;

    /**
     * Contains the SharedPreferences instance used by ConcurService
     */
    private SharedPreferences prefs;

    /**
     * Contains the receiver that intercepts and displays push notifications
     */
    private AWSPushNotificationReceiver pushNotificationReceiver;

    /**
     * Contains the IntentFilter for registering AWSPushNotificationReceiver
     */
    private IntentFilter pushNotificationFilter;

    /**
     * Contains an instance of NiftyService used to make Nifty calls
     */
    private NiftyService niftyService;

    /**
     * Contains the result of a call to Nifty
     */
    private AsyncRequestResult result;

    /**
     * Contains the protected id of the currently signed in user
     * - Needed for logout when prefs may be cleared before deregistration can complete
     */
    private String user;

    /**
     * True if service should be cancelled due to bad or missing requirements
     */
    private boolean shouldCancelService;

    /**
     * True if current service call is to register with Nifty, false otherwise
     */
    private boolean wasRegister;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        super.onCreate();
        ctx = (ConcurCore) ConcurCore.getContext();
        gcm = GoogleCloudMessaging.getInstance(ctx);
        niftyService = new NiftyService(this);
        register();
    }

    /**
     * Fill the NiftyProperties object with settings required for Nifty calls
     */
    private boolean fillNiftyProperties() {
        String apiKey = ConcurCore.getNiftyApiKey();
        String appId = ConcurCore.getNiftyAppId();
        String server = ConcurCore.getNiftyServer();
        boolean allPresent = (!TextUtils.isEmpty(apiKey) &&
                !TextUtils.isEmpty(appId) && !TextUtils.isEmpty(server));

        if (allPresent) {
            NiftyProperties.setContext(ConcurCore.getContext());
            NiftyProperties.setApiKey(apiKey);
            NiftyProperties.setAppID(appId);
            NiftyProperties.setServerAddress(server);
            NiftyProperties.setUseSSL(true);
            NiftyProperties.setPort(443);
            NiftyProperties.setVersionNumber("0.0.1");
        }

        return allPresent;
    }

    /**
     * Register for Nifty push notifications if information from login has been stored
     */
    private void register() {
        new AsyncTask<Object, Void, Object>() {

            @Override
            protected Object doInBackground(Object... params) {
                prefs = ctx.getService().prefs;

                // If NiftyProperties are not filled, try retrieving from storage
                if (!NiftyProperties.allPropertiesFilled()) {
                    shouldCancelService = !fillNiftyProperties();
                }

                // Proceed with register if properties are found
                if (!shouldCancelService) {
                    wasRegister = true;
                    String token;

                    try {
                        // Register with GCM for deviceToken
                        String server = NiftyProperties.getServerAddress();
                        String gcmEnv = (server.startsWith("us-west-2-prod")) ?
                                Const.NIFTY_CONCUR_PROD_ID : Const.NIFTY_CONCUR_TEST_ID;
                        token = gcm.register(gcmEnv);

                        // Register with Nifty
                        user = prefs.getString(Const.PREF_USER_ID, null);
                        result = new AsyncRequestResult();
                        niftyService.registerDevice(user, token);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // Properties for Nifty could not be found, log errors and destroy service
                    Log.e(Const.LOG_TAG, CLS_TAG + ".register.doInBackground(): Properties for Nifty not in storage.");
                    stopSelf();
                }

                return true;
            }

            // Register pushNotificationReceiver if registration for Nifty push notifications was not cancelled
            protected void onPostExecute(Object result) {
                if (pushNotificationReceiver == null && !shouldCancelService) {
                    pushNotificationReceiver = new AWSPushNotificationReceiver();
                    if (pushNotificationFilter == null) {
                        pushNotificationFilter = new IntentFilter();
                        pushNotificationFilter.addAction("com.google.android.c2dm.intent.RECEIVE");
                        pushNotificationFilter.addAction("com.google.android.c2dm.intent.REGISTRATION");
                        pushNotificationFilter.addAction("com.google.android.c2dm.intent.REGISTER");
                        pushNotificationFilter.addCategory("com.concur.breeze");
                    }
                    ctx.registerReceiver(pushNotificationReceiver, pushNotificationFilter,
                            "com.google.android.c2dm.permission.SEND", null);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPostExecute: pushNotificationReceiver is *not* null!");
                }
            }
        }.execute(null, null, null);
    }

    /**
     * Deregister from Nifty push notifications before destroying service
     */
    @Override
    public void onDestroy() {
        wasRegister = false;

        new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                // Get the Nifty deviceId from preferences
                String deviceId = prefs.getString(Const.PREF_DEVICE_ID, null);
                if (user == null)  user = prefs.getString(Const.PREF_USER_ID, null);

                // Deregister from Nifty
                if (deviceId != null && !shouldCancelService) {
                    result = new AsyncRequestResult();
                    niftyService.deregisterDevice(user, deviceId);
                }
                return true;
            }
        }.execute(null, null, null);

        super.onDestroy();
    }

    // Start implementation of BaseAsyncRequestTask.AsyncReplyListener interface methods

    @Override
    public void onRequestSuccess(Bundle resultData) {
        storeResult(resultData, BaseAsyncRequestTask.RESULT_OK);

        if (wasRegister) {
            // If register was successful
            if (result.resultCode == BaseAsyncRequestTask.RESULT_OK) {
                int httpCode = result.resultData.getInt(BaseAsyncRequestTask.HTTP_STATUS_CODE);
                if (httpCode == HttpStatus.SC_CREATED) {

                    // See if there is a deviceId already in storage
                    String oldDeviceId = prefs.getString(Const.PREF_DEVICE_ID, null);
                    String newDeviceId = result.resultData.getString(NiftyAsyncRequestTask.DEVICE_ID_KEY);
                    boolean oldIsEmpty = TextUtils.isEmpty(oldDeviceId);

                    // If oldDeviceId is null or different from newDeviceID, save newDeviceId to prefs
                    if (oldIsEmpty || !oldDeviceId.equals(newDeviceId)) {
                        SharedPreferences.Editor e = prefs.edit();
                        e.putString(Const.PREF_DEVICE_ID, newDeviceId);
                        e.commit();
                    }

                    // If oldDeviceId is not null and is different from newDeviceId, deregister oldDeviceId
                    if (!oldIsEmpty && !oldDeviceId.equals(newDeviceId)) {
                        if(user == null) user = prefs.getString(Const.PREF_USER_ID, null);
                        result = new AsyncRequestResult();
                        niftyService.deregisterDevice(user, oldDeviceId);
                    }

                } else {
                    String httpStatusMessage = result.resultData.getString(BaseAsyncRequestTask.HTTP_STATUS_MESSAGE);
                    Log.e(Const.LOG_TAG, CLS_TAG + ".register(): HTTP status("
                            + httpCode + ") - " + httpStatusMessage + ".");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".register(): BaseAsyncRequestTask Result Code("
                        + result.resultCode + ").");
            }
        } else {
            // If deregister was successful
            if (result.resultCode == BaseAsyncRequestTask.RESULT_OK) {
                int httpCode = result.resultData.getInt(BaseAsyncRequestTask.HTTP_STATUS_CODE);
                if (httpCode == HttpStatus.SC_OK) {
                    // Remove deviceId from storage
                    SharedPreferences.Editor e = prefs.edit();
                    e.remove(Const.PREF_DEVICE_ID);
                    e.commit();

                    // Remove push notification receiver
                    ctx.unregisterReceiver(pushNotificationReceiver);
                    pushNotificationReceiver = null;
                    niftyService = null;

                } else {
                    String httpStatusMessage = result.resultData.getString(BaseAsyncRequestTask.HTTP_STATUS_MESSAGE);
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onDestroy(): HTTP status("
                            + httpCode + ") - " + httpStatusMessage + ".");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onDestroy(): BaseAsyncRequestTask Result Code("
                        + result.resultCode + ").");
            }
        }
    }

    @Override
    public void onRequestFail(Bundle resultData) {
        storeResult(resultData, BaseAsyncRequestTask.RESULT_ERROR);
    }

    @Override
    public void onRequestCancel(Bundle resultData) {
        storeResult(resultData, BaseAsyncRequestTask.RESULT_CANCEL);
    }

    /**
     * Store the response of the Nifty call, close down Nifty handler thread and signal countdown latch
     */
    private void storeResult(Bundle responseData, int responseCode) {
        if (result == null) result = new AsyncRequestResult();

        result.resultCode = (responseData != null) ? responseCode : BaseAsyncRequestTask.RESULT_ERROR;
        result.resultData = responseData;

        // Quit the handler thread.
        niftyService.closeHandlerThread();
        result.countDown();
    }

    @Override
    public void cleanup() { }
}