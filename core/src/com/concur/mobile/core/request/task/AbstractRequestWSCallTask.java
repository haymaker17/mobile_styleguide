/**
 *
 */
package com.concur.mobile.core.request.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

/**
 * @author OlivierB
 */
public abstract class AbstractRequestWSCallTask extends BaseAsyncRequestTask {

    public AbstractRequestWSCallTask(Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, id, receiver);
    }

    private static final String CLS_TAG = "AbstractRequestWSCallTask";

    /**
     * Contains parsed json
     */
    private String jsonRes = null;

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
        final StringBuilder strBldr = new StringBuilder();
        final String serverAddress = PlatformProperties.getServerAddress();
        if (!TextUtils.isEmpty(serverAddress)) {
            strBldr.append(Format.formatServerAddress(true, serverAddress));
            String serviceEndpoint;
            try {
                serviceEndpoint = getServiceEndPoint();
                if (!TextUtils.isEmpty(serviceEndpoint)) {
                    if (serviceEndpoint.charAt(0) != '/') {
                        strBldr.append('/');
                    }
                    strBldr.append(serviceEndpoint);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getURL: service end-point is not set, cancelling request.");
                    resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
                }
            } catch (ServiceRequestException e) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getURL: request cancelled. Following error met: " + (e.getMessage() != null ?
                                e.getMessage() :
                                "no message."));
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
     * @throws ServiceRequestException
     */
    protected abstract String getServiceEndPoint() throws ServiceRequestException;

    /**
     * Gets whether this request requires a session id.
     *
     * @return returns whether this request requires a session id. Defaults to
     * <code>true</code>.
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
        return Long.toString(System.currentTimeMillis());
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        // Set the accept header to JSON.
        connection.setRequestProperty(HEADER_ACCEPT, CONTENT_TYPE_JSON);
        connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);

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
        final String accessToken = PlatformProperties.getAccessToken();
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
    protected int parseStream(InputStream is) {
        try {
            jsonRes = readStream(is, "UTF-8");
            return BaseAsyncRequestTask.RESULT_OK;
        } catch (IOException e) {
            // nothing
            e.printStackTrace();
        }
        return BaseAsyncRequestTask.RESULT_ERROR;
    }

    @Override
    protected int onPostParse() {
        if (jsonRes != null) {
            resultData.putString(HTTP_RESPONSE, jsonRes);
            return RESULT_OK;
        } else {
            resultData.putString(HTTP_RESPONSE, "");
            return RESULT_ERROR;
        }
    }

    private static String readStream(InputStream is, String encoding) throws IOException {
        final char[] buffer = new char[8192];
        StringBuilder out = new StringBuilder();

        Reader in;
        try {
            in = new InputStreamReader(is, encoding);
        } catch (UnsupportedEncodingException e) {
            in = new InputStreamReader(is);
        }

        int readCount;
        do {
            readCount = in.read(buffer, 0, buffer.length);
            if (readCount > 0) {
                out.append(buffer, 0, readCount);
            }
        } while (readCount >= 0);

        return out.toString();
    }
}
