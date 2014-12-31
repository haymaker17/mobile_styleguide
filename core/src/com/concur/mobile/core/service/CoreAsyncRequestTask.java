package com.concur.mobile.core.service;

import java.io.InputStream;
import java.net.HttpURLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Format;

public abstract class CoreAsyncRequestTask extends BaseAsyncRequestTask {

    public final static String IS_SUCCESS = "success";
    public final static String ERROR_MESSAGE = "error_message";
    public final static String ERROR = "error";

    public CoreAsyncRequestTask(Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, id, receiver);
    }

    /**
     * Return the User-Agent header string for this request.
     * 
     * @return A string containing the user agent for this request
     */
    @Override
    protected String getUserAgent() {
        return Const.HTTP_HEADER_USER_AGENT_VALUE;
    }

    /**
     * Gets the URL for this request.
     * 
     * @return the request URL.
     */
    @Override
    protected String getURL() {
        // Grab the server address
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        enableSpdy = Preferences.shouldEnableSpdy();
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(serverAdd);
        strBldr.append(getServiceEndpoint());
        return strBldr.toString();
    }

    protected abstract String getServiceEndpoint();

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        // The current session ID is stored in the shared preferences.
        String sessionId = Preferences.getSessionId();
        connection.addRequestProperty(Const.HTTP_HEADER_XSESSION_ID, sessionId);
    }

    @Override
    protected int parseStream(InputStream is) {
        int result = RESULT_ERROR;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, null);

            result = parse(new CommonParser(xpp));
        } catch (XmlPullParserException xppe) {
            Log.e(Const.LOG_TAG, "XPP exception parsing response", xppe);
        }

        return result;
    }

    protected abstract int parse(CommonParser parser);

}
