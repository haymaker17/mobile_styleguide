/**
 * 
 */
package com.concur.mobile.platform.service;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.service.parser.ActionResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

/**
 * An extension of <code>BaseAsyncRequestTask</code> for the purposes perform platform level asynchronous service requests.
 * 
 * NOTE: This class is basically a copy of <code>CoreAsyncRequestTask</code> except that the session id is passed in the
 * constructor rather than directly retrieved from a <code>Preferences</code> object.
 * 
 * @author andrewk
 */
public abstract class PlatformAsyncRequestTask extends BaseAsyncRequestTask {

    private static final String CLS_TAG = "PlatformAsyncRequestTask";

    /**
     * Contains the result data bundle key for obtaining a response message (TEXT) from a platform response.
     */
    public final static String EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY = "mws.response.status.response.message";

    /**
     * Contains the result data bundle key for obtaining a response success (BOOLEAN) from a platform response.
     */
    public final static String EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY = "mws.response.status.success";

    /**
     * Contains the result data bundle key for obtaining a response list of <code>Error</code> objects from a platform response.
     * 
     * @see com.concur.mobile.platform.service.parser.Error
     */
    public final static String EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY = "mws.response.status.errors";

    public final static String IS_SUCCESS = "success";
    public final static String ERROR_MESSAGE = "error_message";

    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

    public static final String HTTP_HEADER_MESSAGE_ID = "X-MsgID";

    public static final String HTTP_HEADER_XSESSION_ID = "X-SessionID";

    /**
     * Constructs an instance of <code>PlatformAsyncRequestTask</code> with <code>context</code>, <code>id</code> and
     * <code>receiver</code>.
     * 
     * @param context
     *            contains the application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the result receiver.
     */
    public PlatformAsyncRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    /**
     * Return the User-Agent header string for this request.
     * 
     * @return A string containing the user agent for this request
     */
    @Override
    protected String getUserAgent() {
        return PlatformProperties.getUserAgent();
    }

    /**
     * Gets the URL for this request.
     * 
     * @return the request URL.
     */
    @Override
    protected String getURL() {
        String url = null;
        // Grab the server address
        StringBuilder strBldr = new StringBuilder();
        String serverAddress = PlatformProperties.getServerAddress();
        if (!TextUtils.isEmpty(serverAddress)) {
            strBldr.append(Format.formatServerAddress(true, serverAddress));
            String serviceEndpoint = getServiceEndPoint();
            if (!TextUtils.isEmpty(serviceEndpoint)) {
                if (serviceEndpoint.charAt(0) != '/') {
                    strBldr.append('/');
                }
                strBldr.append(serviceEndpoint);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getURL: service end-point is not set, cancelling request.");
                resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getURL: server address is not set, cancelling request.");
            resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
        }
        url = strBldr.toString();
        return url;
    }

    /**
     * Gets the service end-point for this request.
     * 
     * @return returns the service end-point for this request.
     */
    protected abstract String getServiceEndPoint();

    /**
     * Gets whether this request requires a session id.
     * 
     * @return returns whether this request requires a session id. Defaults to <code>true</code>.
     */
    protected boolean requiresSessionId() {
        return true;
    }

    /**
     * Gets the message id for this request.
     * 
     * @return returns the message id for this request.
     */
    protected String getMessageId() {
        return null;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        // Set the session id.
        if (requiresSessionId()) {
            String sessionId = PlatformProperties.getSessionId();
            if (!TextUtils.isEmpty(sessionId)) {
                try {
                    connection.addRequestProperty(PlatformAsyncRequestTask.HTTP_HEADER_XSESSION_ID,
                            URLEncoder.encode(sessionId, "UTF-8"));
                } catch (UnsupportedEncodingException unsEncExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: ", unsEncExc);
                }
            }
        }
        // Set the access token.
        String accessToken = PlatformProperties.getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            connection.addRequestProperty(PlatformAsyncRequestTask.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
        }
        // Set the message id.
        String messageId = getMessageId();
        if (!TextUtils.isEmpty(messageId)) {
            try {
                connection.addRequestProperty(PlatformAsyncRequestTask.HTTP_HEADER_MESSAGE_ID,
                        URLEncoder.encode(messageId, "UTF-8"));
            } catch (UnsupportedEncodingException unsEncExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: ", unsEncExc);
            }
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {

        if (resultCode == RESULT_OK) {
            // Notify the platform manager of a request about to be executed.
            PlatformManager sessionManager = PlatformProperties.getPlatformSessionManager();
            if (sessionManager != null) {
                resultCode = sessionManager.onRequestStarted(getContext(), this, resultData);
                // If onRequestStarted indicates the request should not go forward, then
                // perform a cancellation.
                if (resultCode != BaseAsyncRequestTask.RESULT_OK) {
                    cancel(true);
                }
            }
        }

        return super.doInBackground(params);
    }

    @Override
    protected int parseStream(InputStream is) {
        return RESULT_OK;
    }

    @Override
    protected int onPostParse() {

        int result = BaseAsyncRequestTask.RESULT_OK;

        // Notify the platform manager of a completed request.
        PlatformManager sessionManager = PlatformProperties.getPlatformSessionManager();
        if (sessionManager != null) {
            sessionManager.onRequestCompleted(getContext(), this);
        }
        return result;
    }

    /**
     * Will initialize an instance of <code>CommonParser</code> to use with <code>is</code>.
     * 
     * @param is
     *            contains the input stream being parsed.
     * @return returns an instance of <code>CommonParser</code>.
     */
    protected CommonParser initCommonParser(InputStream is) {
        CommonParser parser = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, null);
            parser = new CommonParser(xpp);
        } catch (XmlPullParserException xppe) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initCommonParser: XPP exception initing common parser", xppe);
        }
        return parser;
    }

    /**
     * Will set into the result data bundle (<code>resultData</code>) information from <code>mwsResponseStatus</code> in the
     * following way:<br>
     * <li>response message (TEXT) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY</li>
     * <li>success (BOOLEAN) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY</li> <li>errors
     * (ArrayList<Error>) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY</li>
     * 
     * @param responseStatus
     *            contains a reference to an <code>MWSResponseStatus</code> object.
     */
    protected void setMWSResponseStatusIntoResultBundle(MWSResponseStatus responseStatus) {

        // Set any response message.
        if (!TextUtils.isEmpty(responseStatus.getResponseMessage())) {
            resultData.putString(EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY, responseStatus.getResponseMessage());
        }

        // Set the success boolean value.
        resultData.putBoolean(EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY, responseStatus.isSuccess());

        // Set the error list.
        if (responseStatus.getErrors() != null) {
            resultData.putSerializable(EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY, (Serializable) responseStatus.getErrors());
        }

    }

    /**
     * Will set into the result data bundle (<code>resultData</code>) information from <code>mwsResponseStatus</code> in the
     * following way:<br>
     * <li>response message (TEXT) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY</li>
     * <li>success (BOOLEAN) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY</li> <li>errors
     * (ArrayList<Error>) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY</li>
     * 
     * @param <T>
     * 
     * @param responseStatus
     *            contains a reference to an <code>MWSResponseStatus</code> object.
     */
    protected <T> void setMWSResponseStatusIntoResultBundle(MWSResponse<T> responseStatus) {

        List<String> responseMessage = responseStatus.getInfo();
        // Set any response message.
        if (responseMessage != null && responseMessage.size() > 0
                && !TextUtils.isEmpty((CharSequence) responseMessage.get(0))) {
            resultData.putString(EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY, (String) responseMessage.get(0));
        }

        // Set the success boolean value.

        // Set the error list.
        if (responseStatus.getErrors() != null) {
            resultData.putSerializable(EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY, (Serializable) responseStatus.getErrors());
        }
        // } else {
        // resultData.putBoolean(EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY, responseStatus.isSuccess());
        // }

    }

    /**
     * Will set into the result data bundle (<code>resultData</code>) information from <code>mwsResponseStatus</code> in the
     * following way:<br>
     * <li>response message (TEXT) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY</li>
     * <li>success (BOOLEAN) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY</li> <li>errors
     * (ArrayList<Error>) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY</li>
     * 
     * @param <T>
     * 
     * @param responseStatus
     *            contains a reference to an <code>MWSResponseStatus</code> object.
     */
    protected <T> void setMWSResponseStatusIntoResultBundle(MWSResponse<T> responseStatus) {

        List<String> responseMessage = responseStatus.getInfo();
        // Set any response message.
        if (responseMessage != null && responseMessage.size() > 0
                && !TextUtils.isEmpty((CharSequence) responseMessage.get(0))) {
            resultData.putString(EXTRA_MWS_RESPONSE_STATUS_RESPONSE_MESSAGE_KEY, (String) responseMessage.get(0));
        }

        // Set the success boolean value.

        // Set the error list.
        if (responseStatus.getErrors() != null) {
            resultData.putSerializable(EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY, (Serializable) responseStatus.getErrors());
        }
        // } else {
        // resultData.putBoolean(EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY, responseStatus.isSuccess());
        // }

    }

    /**
     * Will set into the result data bundle (<code>resultData</code>) information from <code>actionResult</code> in the following
     * way:<br>
     * <li>success (BOOLEAN) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY</li> <li>errors
     * (ArrayList<Error>) using the key <code>PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY</li>
     * 
     * @param responseStatus
     *            contains a reference to an <code>ActionResponseParser</code> object.
     */
    protected void setActionResultIntoResultBundle(ActionResponseParser actionResult) {

        // Set the success boolean value.
        resultData.putBoolean(EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY, actionResult.isSuccess());

        // Set the error list.
        String errorMessage = actionResult.getErrorMessage();
        if (errorMessage != null) {
            errorMessage = errorMessage.trim();
        }
        if (!TextUtils.isEmpty(errorMessage)) {
            com.concur.mobile.platform.service.parser.Error error = new com.concur.mobile.platform.service.parser.Error();
            error.setSystemMessage(errorMessage);
            error.setUserMessage(errorMessage);
            ArrayList<com.concur.mobile.platform.service.parser.Error> errors = new ArrayList<com.concur.mobile.platform.service.parser.Error>(
                    1);
            errors.add(error);
            resultData.putSerializable(EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY, errors);
        }
    }

}
