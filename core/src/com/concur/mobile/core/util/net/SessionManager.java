/**
 * 
 */
package com.concur.mobile.core.util.net;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.platform.authentication.AutoLoginRequestTask;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.LoginResponseKeys;
import com.concur.mobile.platform.authentication.PPLoginLightRequestTask;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.platform.PlatformProperties;

/**
 * A component supporting session management actions, i.e., validating session id, etc.
 * 
 * @author AndrewK
 */
public class SessionManager {

    private static final String CLS_TAG = SessionManager.class.getSimpleName();

    /**
     * Interface invoked when the auto-login request completes or failes.
     * 
     * @author Chris N. Diaz
     *
     */
    public interface AutoLoginListener {

        /**
         * Invoked when the auto-login request returns back with an HTTP status 200.
         * 
         * @param the
         *            Session ID returned from the auto-login, can be <code>null</code> if authentication failed (e.g. a remote
         *            wipe was sent down).
         */
        public void onSuccess(String sessionId);

        /**
         * Invoked when something bad happened during the request (either on the server side or the actual network connection).
         * 
         * @param the
         *            error message sent from the server, can be <code>null</code>.
         */
        public void onFailure(String errorMessage);

        /**
         * Invoked when the server returns the remote wipe flag during authentication.
         */
        public void onRemoteWipe();

    }

    private static final SessionManager sessionManager;

    private static BaseAsyncResultReceiver sessionAutoLoginReceiver;
    private final static int AUTO_LOGIN_REQUEST_ID = 1;
    private final static int VALIDATE_PASSWORD_REQUEST_ID = 2;

    private static SessionInfo sessionInfo;

    private static SessionAutoLoginResult loginResult;

    private static HandlerThread handlerThread;
    static {
        sessionManager = new SessionManager();
    }

    /**
     * Will validate the current session id by checking whether one exists or has expired. If a new session needs to be
     * established, then this method will block until that session has been created or fails doing so. If a new session id is
     * created, it and its expiration information will be persistently stored in preferences information.
     * 
     * @param context
     *            an application context.
     * 
     * @return the current valid session id upon success; <code>null</code> upon failure.
     */
    @SuppressLint("NewApi")
    public synchronized static String validateSessionId(ConcurCore concurMobile) {
        return SessionManager.validateSessionId(concurMobile,null);
    }

    /**
     * Will validate the current session id by checking whether one exists or has expired. If a new session needs to be
     * established, then this method will block until that session has been created or fails doing so. If a new session id is
     * created, it and its expiration information will be persistently stored in preferences information.
     * 
     * @param context
     *            an application context.
     * @param replyListener
     *            an <code>AsyncReplyListener</code> that is invoked when the Login Request Task has completed; that means if it
     *            has completed successfully, failed, or cancled.
     * 
     * @return the current valid session id upon success; <code>null</code> upon failure.
     */
    @SuppressLint("NewApi")
    public synchronized static String validateSessionId(ConcurCore concurMobile, final AutoLoginListener replyListener) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        String sessionId = PlatformProperties.getSessionId();
        int sessionDuration = prefs.getInt(Const.PREF_SESSION_DURATION, -1);
        sessionInfo = ConfigUtil.getSessionInfo(concurMobile.getApplicationContext());
        if (sessionInfo == null) {
            // This can happen if the user has logged out (which calls ConfigUtil.removeLoginInfo()).
            Log.i(CLS_TAG, CLS_TAG
                    + ".validateSessionId(): SessionInfo is null!  This may be caused by the user logging out.");
            //reset userclick time and autologin time
            ConcurCore.resetAutloLoginTimes();
            return null;
        }
        // If session expire time is not set, has expired or is within 5 minutes of expiring, then create
        // a new session id.
        if (isSessionExpire(concurMobile)) {

            if (ConcurCore.isConnected()) {
                Log.d(Const.LOG_TAG, CLS_TAG
                        + ".validateSessionId: session is expired or not set, re-establishing the session.");

                boolean autoLogin = prefs.getBoolean(Const.PREF_AUTO_LOGIN, false);
                boolean disableAutoLogin = prefs.getBoolean(Const.PREF_DISABLE_AUTO_LOGIN, false);

                if(disableAutoLogin) {
                    autoLogin = false;
                }

                // If auto-login is enabled and company sign-on is being used, then force autoLogin to 'false'.
                // Company Sign-on auto-login is not currently supported.
                if (autoLogin) {
                    String loginMethod = sessionInfo.getSignInMethod();
                    if ((!(TextUtils.isEmpty(loginMethod)))) {
                        if (loginMethod.equalsIgnoreCase("SSO")) {
                            autoLogin = false;
                        }
                    } else {
                        autoLogin = false;
                    }
                }

                if (autoLogin) {
                    // Utilize a 'LoginRequest' object to repeat the login attempt. The 'handleMessage'
                    // callback below will contain the response.
                    // in LoginPassword.onClick() we are storing loginid from EmailLookUpFragment to preferences.
                    String loginId = sessionInfo.getLoginId();
                    String pinOrPassword = Preferences.getPin(prefs, null);
                    String accessToken = sessionInfo.getAccessToken();

                    // Make sure that we have a login and pin. This prevents multi-method recursion
                    // if we are here because of the message sent to clear the local data.
                    if (loginId != null) {

                        // First check if the user has an existing oAuth token.
                        if (accessToken != null) {
                            // Init the login result.
                            loginResult = sessionManager.new SessionAutoLoginResult();
                            loginResult.sessionId = null;

                            // Init the handler thread with associated looper as this thread will be
                            // blocked in the 'loginResult.await' call below.
                            handlerThread = new HandlerThread("PPLoginLightRequestTask");
                            handlerThread.start();

                            Locale locale = Locale.getDefault();
                            sessionAutoLoginReceiver = new BaseAsyncResultReceiver(new Handler(
                                    handlerThread.getLooper()));
                            sessionAutoLoginReceiver.setListener(sessionManager.new SessionAutoLoginListener(
                                    concurMobile.getApplicationContext(), replyListener));

                            UserAndSessionInfoUtil.setServerAddress(sessionInfo.getServerUrl());

                            AutoLoginRequestTask autoLoginRequestTask = new AutoLoginRequestTask(
                                    concurMobile.getApplicationContext(), AUTO_LOGIN_REQUEST_ID,
                                    sessionAutoLoginReceiver, locale);
                            // Check for HoneyComb or later to execute login request on separate thread pool. Otherwise,
                            // this thread will be blocked by 'loginResult.await' call below.
                            long startTimeMillis = System.currentTimeMillis();
                            concurMobile.startAutologinTime = startTimeMillis;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                autoLoginRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                autoLoginRequestTask.execute();
                            }
                            // Acquire the login result if ready.
                            try {
                                // Wait for the latch to be decremented.
                                loginResult.await();
                                long stopTimeMillis = System.currentTimeMillis();
                                concurMobile.stopAutoLoginTime = stopTimeMillis;
                                logUserWaitingTime();

                                // Set the result code in the return value and in the bundle.
                                sessionId = loginResult.sessionId;
                            } catch (InterruptedException intExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".renewPPLoginSession: interrupted while acquiring result", intExc);
                                sessionId = null;
                                // MOB-19366 - Invoke the listener. It is the implementation's responsibility
                                // to handle null Session IDs (e.g. show expire dialog and log out).
                                if (replyListener != null) {
                                    replyListener.onSuccess(null);
                                }
                                //reset user click and autologin times
                                ConcurCore.resetAutloLoginTimes();
                            }
                        }  else {
                            Log.d(Const.LOG_TAG, CLS_TAG
                                    + ".validateSessionId: no credentials available to re-establish session");
                            sessionId = null;
                            // MOB-19366 - Invoke the listener. It is the implementation's responsibility
                            // to handle null Session IDs (e.g. show expire dialog and log out).
                            if (replyListener != null) {
                                replyListener.onSuccess(null);
                            }
                            //reset user click and autologin times
                            ConcurCore.resetAutloLoginTimes();
                        }

                    } else {
                        Log.d(Const.LOG_TAG, CLS_TAG
                                + ".validateSessionId: no credentials available to re-establish session");
                        sessionId = null;
                        // MOB-19366 - Invoke the listener. It is the implementation's responsibility
                        // to handle null Session IDs (e.g. show expire dialog and log out).
                        if (replyListener != null) {
                            replyListener.onSuccess(null);
                        }
                        //reset user click and autologin times
                        ConcurCore.resetAutloLoginTimes();
                    }
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG
                            + ".validateSessionId: auto-login disabled, session not re-established");
                    sessionId = null;
                    // MOB-19366 - Invoke the listener. It is the implementation's responsibility
                    // to handle null Session IDs (e.g. show expire dialog and log out).
                    if (replyListener != null) {
                        replyListener.onSuccess(null);
                    }
                    //reset user click and autologin times
                    ConcurCore.resetAutloLoginTimes();
                }

            } else {
                // Session has expired or is not set, but there's no connectivity to reset it,
                // just return the session id we have now.
                Log.d(Const.LOG_TAG, CLS_TAG + ".validateSessionId: session is expired but there is no connectivity.");
                sessionId = null;
                // MOB-19366 - Invoke the listener. It is the implementation's responsibility
                // to handle null Session IDs (e.g. show expire dialog and log out).
                if (replyListener != null) {
                    replyListener.onSuccess(null);
                }
                //reset user click and autologin times
                ConcurCore.resetAutloLoginTimes();
            }
        } else {
            // Otherwise, just bump the session expiration
            // Previous clients will not have the duration in prefs so don't attempt to extend.
            // This will cause the session to expire normally and reset after the first time at which
            // point we'll get the duration in prefs and start using this extend mechanism.
            if (sessionDuration >= 0) {
                Preferences.extendSesssionExpiration(prefs, sessionDuration);
            }
            //reset user click and autologin times
            ConcurCore.resetAutloLoginTimes();
        }
        return sessionId;
    }

    private static void logUserWaitingTime() {
        long totalWaitTime = 0L;
        if(ConcurCore.userClickTime>0){
            totalWaitTime = ConcurCore.stopAutoLoginTime -ConcurCore.userClickTime;
            Log.e("logUserWaitingTime : ", " user click time is > 0");
        }
        if(totalWaitTime<=0){
            totalWaitTime=0;
            Log.e("logUserWaitingTime : ", " total wait time is = 0");
        }
        // Statistics Notification
        EventTracker.INSTANCE.trackTimings(Flurry.CATEGORY_WAIT_TIME, Flurry.ACTION_AUTO_LOGIN_WAIT,
                Flurry.LABEL_WAIT_TIME, totalWaitTime);
        ConcurCore.resetAutloLoginTimes();
    }
    /**
     * Will verify the current session id expiration by checking
     * 
     * @return true : the current valid session id expiration and false : upon failure.
     */
    public static boolean isSessionExpire(ConcurCore concurMobile) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile.getApplicationContext());
        String sessionId = PlatformProperties.getSessionId();

        long sessionExpireTime = prefs.getLong(Const.PREF_SESSION_EXPIRATION, 0L);
        long curTimeMillis = System.currentTimeMillis();
        // If session expire time is not set, has expired or is within 5 minutes of expiring, then create
        // a new session id.
        // MOB-21741 check session id in addition to session expire time
        if (TextUtils.isEmpty(sessionId) || sessionExpireTime == 0L || (curTimeMillis > sessionExpireTime)
                || (sessionExpireTime - curTimeMillis) <= 300000L) {
            return true;
        } else {
            return false;
        }
    }

    protected class SessionAutoLoginListener implements AsyncReplyListener {

        private Context context;
        private AutoLoginListener replyListener;

        public SessionAutoLoginListener(Context context, AutoLoginListener replyListener) {
            super();
            this.context = context;
            this.replyListener = replyListener;
        }

        public void onRequestSuccess(Bundle resultData) {
            int RequestID = resultData.getInt(BaseAsyncRequestTask.REQUEST_ID);

            switch (RequestID) {
            case AUTO_LOGIN_REQUEST_ID: {

                boolean isRemoteWipe = resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, false);
                if (isRemoteWipe) {
                    // MOB-18782 - Check for remote wipe and send a Broadcast
                    // to indicate a remote wipe was sent down.
                    if (context != null) {

                        Log.d(Const.LOG_TAG,
                                "SessionAutoLoginListener.onRequestSuccess: Remote Wipe sent down from auto-login.");

                        // Set the result information.
                        loginResult.sessionId = null;
                        cleanup();

                        // Analytics stuff.
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Type", Flurry.EVENT_REMOTE_WIPE);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_FAILURE, params);

                    }
                } else {

                    Bundle emailBundle = new Bundle();

                    String loginId = sessionInfo.getLoginId();
                    String serverUrl = sessionInfo.getServerUrl();
                    String signInMethod = sessionInfo.getSignInMethod();
                    String ssoUrl = sessionInfo.getSSOUrl();
                    // Set the login id.
                    emailBundle.putString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY, loginId);
                    // Set the server url.
                    emailBundle.putString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY, serverUrl);
                    // Set the sign-in method.
                    emailBundle.putString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, signInMethod);
                    // Set the sso url.
                    emailBundle.putString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY, ssoUrl);

                    UserAndSessionInfoUtil.updateUserAndSessionInfo(context, emailBundle);

                    // Save the login response information.
                    Log.d(Const.LOG_TAG, CLS_TAG + ".validateSessionId: successfully created new session id.");

                    // Flurry Notification
                    Map<String, String> autoLoginParams = new HashMap<String, String>();
                    autoLoginParams.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_AUTO_LOGIN);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_AUTHENTICATION,
                            autoLoginParams);

                    // Set the result information.
                    loginResult.sessionId = PlatformProperties.getSessionId();

                }

                // Decrement the latch.
                loginResult.countDown();
                // Quit the handler thread.
                if (handlerThread != null) {
                    handlerThread.quit();
                    handlerThread = null;
                }

                // Invoke any other listeners.
                if (replyListener != null) {
                    if (isRemoteWipe) {
                        replyListener.onRemoteWipe();
                    } else {
                        replyListener.onSuccess(loginResult.sessionId);
                    }
                }

                break;
            }
            case VALIDATE_PASSWORD_REQUEST_ID: {
                AutoLoginRequestTask autoLoginRequestTask = new AutoLoginRequestTask(context.getApplicationContext(),
                        AUTO_LOGIN_REQUEST_ID, sessionAutoLoginReceiver, Locale.getDefault());
                autoLoginRequestTask.execute();

                break;
            }
            }
        }

        public void onRequestFail(Bundle resultData) {
            // Set the result information.
            loginResult.sessionId = null;
            // Decrement the latch.
            loginResult.countDown();
            // Quit the handler thread.
            if (handlerThread != null) {
                handlerThread.quit();
                handlerThread = null;
            }
            cleanup();

            // Invoke any other listeners.
            if (replyListener != null) {
                replyListener.onFailure(resultData.getString("request.http.status.message"));
            }

            return;

        }

        public void onRequestCancel(Bundle resultData) {
            // Set the result information.
            loginResult.sessionId = null;
            // Decrement the latch.
            loginResult.countDown();
            // Quit the handler thread.
            if (handlerThread != null) {
                handlerThread.quit();
                handlerThread = null;
            }
            cleanup();
            return;

        }

        public void cleanup() {
            sessionAutoLoginReceiver = null;
            return;

        }
    }

    /**
     * An extension of <code>Semaphore</code> used to acquire the login result.
     * 
     * @author andrewk
     */
    class SessionAutoLoginResult extends CountDownLatch {

        @SuppressWarnings("unused")
        private static final long serialVersionUID = 1L;

        /**
         * Contains the session Id;
         */
        String sessionId;

        /**
         * Constructs an instance of <code>LoginResultWaiter</code>
         */
        public SessionAutoLoginResult() {
            super(1);
        }

    }
}
