package com.concur.mobile.platform.service;

import java.sql.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.LoginResponseKeys;
import com.concur.mobile.platform.authentication.PPLoginRequestTask;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;
import com.concur.platform.PlatformProperties;

/**
 * Provides an implementation of <code>PlatformManager</code> for handling session management. <br>
 * <br>
 * <b>NOTE:</b> Only <code>MWSPlatformManager.PPLogin</code> authentication type is currently supported for re-authentication.
 * 
 * @author andrewk
 */
public class MWSPlatformManager implements PlatformManager {

    private static final String CLS_TAG = "MWSPlatformManager";

    private static final Boolean DEBUG = Boolean.TRUE;

    /**
     * Contains the key name to retrieve whether re-authentication was required.
     */
    public static final String REAUTHENTICATION_NEEDED_KEY = "reauthentication.needed";

    /**
     * Contains the key name to retrieve whether or not re-authentication was attempted.
     */
    public static final String REAUTHENTICATION_ATTEMPTED_KEY = "reauthentication.attempted";

    /**
     * Contains the key name to retrieve the re-authentication result code.
     */
    public static final String REAUTHENTICATION_RESULT_CODE_KEY = "reauthentication.result.code";

    /**
     * Contains the key name to retrive the re-authentication result data.
     */
    public static final String REAUTHENTICATION_RESULT_DATA_KEY = "reauthentication.result.data";

    /**
     * An enumeration containing authentication types.
     */
    public static enum AuthenticationType {
        PPLogin, SSO
    };

    /**
     * Contains the type of authentication.
     */
    protected AuthenticationType authType = AuthenticationType.PPLogin;

    /**
     * Contains the login id for the <code>AuthenticationType.PPLogin</code> authentication type.
     */
    protected String ppLoginId;

    /**
     * Contains the pin/password for the <code>AuthenticationType.PPLogin</code> authentication type.
     */
    protected String ppLoginPinPassword;

    /**
     * Contains whether or not auto-login is enabled.
     */
    protected boolean autoLoginEnabled;

    /**
     * Contains a reference to the login reply receiver.
     */
    private BaseAsyncResultReceiver loginReplyReceiver;

    /**
     * Contains a reference to an instance of <code>LoginResult</code>.
     */
    protected LoginResult loginResult;

    /**
     * Contains a reference to a <code>HandlerThread</code> used to run a looper in order to receive the result of an
     * authentication request.
     */
    protected HandlerThread handlerThread;

    /**
     * Will set whether or not auto-login is enabled.
     * 
     * @param autoLoginEnabled
     *            contains whether or not auto-login is enabled.
     */
    public void setAutoLoginEnabled(boolean autoLoginEnabled) {
        this.autoLoginEnabled = autoLoginEnabled;
    }

    /**
     * Will set the authentication type.
     * 
     * @param authType
     *            contains a reference to the authentication type.
     */
    public void setAuthenticationType(AuthenticationType authType) {
        this.authType = authType;
    }

    /**
     * Will set the authentication credentials for a pin/password authentication type.
     * 
     * @param ppLoginId
     *            contains the login id.
     * @param ppLoginPinPassword
     *            contains the pin/password.
     */
    public void setPPLoginAuthenticationInfo(String ppLoginId, String ppLoginPinPassword) {
        this.ppLoginId = ppLoginId;
        this.ppLoginPinPassword = ppLoginPinPassword;
    }

    @Override
    public int onRequestStarted(Context context, PlatformAsyncRequestTask request, Bundle resultData) {

        int retVal = BaseAsyncRequestTask.RESULT_OK;

        // If no session management is required for 'request', then immediately return.
        if (request != null && !request.requiresSessionId()) {
            return retVal;
        }

        // Determine whether the session is about to expire, if so, then perform synchronous re-authentication
        // if needed.
        String sessionId = PlatformProperties.getSessionId();
        if (!TextUtils.isEmpty(sessionId)) {
            SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
            if (isSessionExpired(sessionInfo)) {

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestStarted: session has expired.");
                }

                if (autoLoginEnabled) {
                    switch (authType) {
                    case PPLogin: {

                        // Set the re-authentication is required flag.
                        resultData.putBoolean(REAUTHENTICATION_NEEDED_KEY, Boolean.TRUE);

                        if (!TextUtils.isEmpty(ppLoginId) && !TextUtils.isEmpty(ppLoginPinPassword)) {
                            Log.d(Const.LOG_TAG,
                                    CLS_TAG
                                            + ".onRequestStarted: session expired, auth-type is PPLogin, attempting re-authentication.");
                            retVal = renewPPLoginSession(context, resultData);
                        } else {

                            // Set the re-authentication attempted flag.
                            resultData.putBoolean(REAUTHENTICATION_ATTEMPTED_KEY, Boolean.FALSE);

                            if (DEBUG) {
                                Log.d(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".onRequestStarted: session expired, auth-type is PPLogin, missing credentials.");
                            }
                            retVal = BaseAsyncRequestTask.RESULT_CANCEL;
                        }
                        break;
                    }
                    case SSO: {
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG
                                    + ".onRequestStarted: session expired, auth-type is SSO, auto-login not supported.");
                        }
                        // Set the re-authentication is required flag.
                        resultData.putBoolean(REAUTHENTICATION_NEEDED_KEY, Boolean.TRUE);
                        // Set the re-authentication attempted flag.
                        resultData.putBoolean(REAUTHENTICATION_ATTEMPTED_KEY, Boolean.FALSE);

                        // SSO is not currently supported for re-authentication.
                        retVal = BaseAsyncRequestTask.RESULT_CANCEL;
                        break;
                    }
                    }
                } else {

                    if (DEBUG) {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestStarted: session expired, but auto-login turned-off.");
                    }
                    // Set the re-authentication is required flag.
                    resultData.putBoolean(REAUTHENTICATION_NEEDED_KEY, Boolean.TRUE);
                    // Set the re-authentication attempted flag.
                    resultData.putBoolean(REAUTHENTICATION_ATTEMPTED_KEY, Boolean.FALSE);

                    // Auto-login is disabled, cancel the request.
                    retVal = BaseAsyncRequestTask.RESULT_CANCEL;
                }
            } else {
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestStarted: session current.");
                }
                resultData.putBoolean(REAUTHENTICATION_NEEDED_KEY, Boolean.FALSE);
                // Set the re-authentication attempted flag.
                resultData.putBoolean(REAUTHENTICATION_ATTEMPTED_KEY, Boolean.FALSE);
            }
        } else {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestStarted: no session id.");
            }
            // NO session id, unable to determine whether the session has expired.
            retVal = BaseAsyncRequestTask.RESULT_CANCEL;
        }
        return retVal;
    }

    /**
     * Will determine whether a session has expired.
     * 
     * @param sessionInfo
     *            contains a reference to a <code>SessionInfo</code> object.
     * @return returns <code>true</code> if the session has expired; <code>false</code> otherwise.
     */
    public boolean isSessionExpired(SessionInfo sessionInfo) {
        boolean retVal = false;
        if (sessionInfo != null) {
            Long sessionExpirationTime = sessionInfo.getSessionExpirationTime();
            Long curTimeMillis = System.currentTimeMillis();
            if (sessionExpirationTime != null) {
                if (sessionExpirationTime == 0L || (curTimeMillis > sessionExpirationTime)
                        || (sessionExpirationTime - curTimeMillis) <= 300000L) {
                    retVal = true;
                } else {
                    retVal = false;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".isSessionExpired: sessionExpirationTime is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".isSessionExpired: sessionInfo is null!");
        }
        return retVal;
    }

    /**
     * Will renew a session based on executing a PPLogin request.
     * 
     * @param resultData
     *            contains a reference to the result data.
     * @return contains whether or not the session was renewed.
     */
    @SuppressLint("NewApi")
    protected int renewPPLoginSession(Context context, Bundle resultData) {

        int resultCode = BaseAsyncRequestTask.RESULT_OK;

        // Set the re-authentication is required flag.
        resultData.putBoolean(REAUTHENTICATION_NEEDED_KEY, Boolean.TRUE);
        // Set the re-authentication attempted flag.
        resultData.putBoolean(REAUTHENTICATION_ATTEMPTED_KEY, Boolean.TRUE);

        // Init the login result.
        loginResult = new LoginResult();
        loginResult.resultCode = BaseAsyncRequestTask.RESULT_OK;
        loginResult.resultData = null;

        // Init the handler thread with associated looper as this thread will be
        // blocked in the 'loginResult.await' call below.
        handlerThread = new HandlerThread("PPLoginResultThread");
        handlerThread.start();

        // Initiate the login request.
        loginReplyReceiver = new BaseAsyncResultReceiver(new Handler(handlerThread.getLooper()));
        loginReplyReceiver.setListener(new LoginReplyListener());
        Locale locale = context.getResources().getConfiguration().locale;
        PPLoginRequestTask reqTask = new PPLoginRequestTask(context, loginReplyReceiver, 1, locale, ppLoginId,
                ppLoginPinPassword);

        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: executing login request.");
        }

        // Check for HoneyComb or later to execute login request on separate thread pool. Otherwise,
        // this thread will be blocked by 'loginResult.await' call below.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: executeOnExecutor.");
            }
            reqTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: execute.");
            }
            reqTask.execute();
        }

        // Acquire the login result if ready.
        try {

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: waiting for login result.");
            }
            // Wait for the latch to be decremented.
            loginResult.await();
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: got login result.");
            }

            // Set the result code in the return value and in the bundle.
            resultCode = loginResult.resultCode;
            resultData.putInt(REAUTHENTICATION_RESULT_CODE_KEY, loginResult.resultCode);

            // Set the result data.
            resultData.putBundle(REAUTHENTICATION_RESULT_DATA_KEY, loginResult.resultData);

        } catch (InterruptedException intExc) {

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: interrupted while acquiring login result.");
            }

            Log.e(Const.LOG_TAG, CLS_TAG + ".renewPPLoginSession: interrupted while acquiring result", intExc);

            resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
        }

        return resultCode;
    }

    @Override
    public void onRequestCompleted(Context context, PlatformAsyncRequestTask request) {

        // If no session management is required for 'request', then immediately return.
        if (request != null && !request.requiresSessionId()) {
            return;
        }

        // Update the session expiration time.
        String sessionId = PlatformProperties.getSessionId();
        if (!TextUtils.isEmpty(sessionId)) {
            SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
            if (sessionInfo != null) {
                Integer sessionTimeout = sessionInfo.getSessionTimeout();
                if (sessionTimeout != null) {
                    // Update the session timeout.
                    Long sessionExpirationTime = System.currentTimeMillis() + (sessionTimeout * 60 * 1000);
                    ConfigUtil.updateSessionExpirationTime(context, sessionId, sessionExpirationTime);
                    if (DEBUG) {
                        Log.d(Const.LOG_TAG,
                                CLS_TAG + ".onRequestCompleted: updated session timeout to "
                                        + Format.safeFormatDate(Parse.XML_DF_LOCAL, new Date(sessionExpirationTime)));
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestCompleted: sessionInfo is null!");
            }
        }
    }

    /**
     * An extension of <code>BaseAsyncResultReceiver</code> for handling the result of a login attempt.
     */
    class LoginReplyListener implements AsyncReplyListener {

        private static final String CLS_TAG = MWSPlatformManager.CLS_TAG + ".LoginReplyListener";

        @Override
        public void onRequestSuccess(Bundle resultData) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: ");
            }
            Boolean remoteWipe = resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, false);
            int resultCode = BaseAsyncRequestTask.RESULT_OK;
            if (remoteWipe) {
                // NOTE: If the remote wipe flag has been set on the re-authentication attempt, then
                // set the result to cancel.
                resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
            }
            if (loginResult != null) {

                // Set the result information.
                loginResult.resultCode = resultCode;
                loginResult.resultData = resultData;

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: releasing login result.");
                }

                // Decrement the latch.
                loginResult.countDown();

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: released login result.");
                }

                // Quit the handler thread.
                if (handlerThread != null) {
                    if (DEBUG) {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: quitting handler thread.");
                    }
                    handlerThread.quit();
                    if (DEBUG) {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: quit handler thread.");
                    }
                    handlerThread = null;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: handlerThread is null!");
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: loginResult is null!");
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: ");
            }
            if (loginResult != null) {

                // Set the result information.
                loginResult.resultCode = BaseAsyncRequestTask.RESULT_ERROR;
                loginResult.resultData = resultData;

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: releasing login result.");
                }

                // Decrement the latch.
                loginResult.countDown();

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: released login result.");
                }

                // Quit the handler thread.
                if (handlerThread != null) {
                    if (DEBUG) {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: quitting handler thread.");
                    }
                    handlerThread.quit();
                    if (DEBUG) {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail: quit handler thread.");
                    }
                    handlerThread = null;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail: handlerThread is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail: loginResult is null!");
            }
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: ");
            }
            if (loginResult != null) {

                // Set the result information.
                loginResult.resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
                loginResult.resultData = resultData;

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: releasing login result.");
                }

                // Decrement the latch.
                loginResult.countDown();

                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: released login result.");
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: loginResult is null!");
            }
            // Quit the handler thread.
            if (handlerThread != null) {
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: quitting handler thread.");
                }
                handlerThread.quit();
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: quit handler thread.");
                }
                handlerThread = null;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: handlerThread is null!");
            }
        }

        @Override
        public void cleanup() {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".cleanup: ");
            }
        }
    }

    /**
     * An extension of <code>Semaphore</code> used to acquire the login result.
     * 
     * @author andrewk
     */
    class LoginResult extends CountDownLatch {

        @SuppressWarnings("unused")
        private static final long serialVersionUID = 1L;

        /**
         * Contains the result data from a login attempt.
         */
        Bundle resultData;

        /**
         * Contains the result code from a login attempt.
         */
        int resultCode;

        /**
         * Constructs an instance of <code>LoginResultWaiter</code>
         */
        public LoginResult() {
            super(1);
        }

    }

}
