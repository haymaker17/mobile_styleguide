/**
 * 
 */
package com.concur.mobile.core.util.net;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.concur.mobile.core.util.Const;

/**
 * An abstract class for login response handling.
 * 
 * @author andy
 */
public abstract class AuthenticationResponseHandler implements Handler.Callback {

    /**
     * The activity to handle an authentication response.
     */
    protected Activity activity;

    /**
     * The message containing the results of the authentication.
     */
    protected Message msg;

    /**
     * Constructs an instance of <code>LoginResponseHandler</code> with a current login object.
     * 
     * @param login
     *            the current login handler.
     */
    public AuthenticationResponseHandler(Activity activity) {
        this.activity = activity;
    }

    /**
     * Sets the current activity reference associated with this handler callback.
     * 
     * @param login
     *            the current activity reference.
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
        if (msg != null) {
            handleMessage(msg);
        }
    }

    /**
     * Will clear any previously received message.
     */
    public void clearMessage() {
        msg = null;
    }

    /**
     * Will handle a successful authentication response.
     * 
     * @param responses
     *            the map of response information.
     */
    protected abstract void handleSuccess(Map<String, Object> responses);

    /**
     * Will handle a failed authentication response.
     * 
     * @param responses
     *            the map of response information.
     */
    protected abstract void handleFailure(Map<String, Object> responses);

    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {

        boolean retVal = false;

        if (msg.what == Const.MSG_LOGIN_RESULT) {

            if (activity != null) {

                HashMap<String, Object> responses = (HashMap<String, Object>) msg.obj;
                String sessionId = (String) responses.get(Const.LR_SESSION_ID);

                if (sessionId != null) {
                    handleSuccess(responses);
                } else {
                    handleFailure(responses);
                }
            } else {
                // The response came, but the new activity has not yet had a chance to set the new reference...so
                // cache the response message until we act on it.
                this.msg = msg;
            }
            retVal = true;
        }

        return retVal;
    }

}
