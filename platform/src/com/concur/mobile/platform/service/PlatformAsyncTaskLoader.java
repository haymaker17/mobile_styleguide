package com.concur.mobile.platform.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.loader.BaseAsyncTaskLoader;
import com.concur.mobile.base.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

/**
 * An extension of <code>BaseAsyncTaskLoader</code> for the purpose to perform platform level asynchronous task loads.
 * 
 * NOTE: The implemented methods are a copy of <code>BaseAsyncRequestTask</code>
 * 
 * @author RatanK
 * 
 * @param <T>
 */
public abstract class PlatformAsyncTaskLoader<T> extends BaseAsyncTaskLoader<T> {

    private static final String CLS_TAG = "PlatformAsyncTaskLoader";

    /**
     * A generic result indicating that no failures occurred during the request processing.
     */
    public static final int RESULT_OK = 0;

    /**
     * A generic result indicating that some failure occurred during the request processing. Refer to the specific request and the
     * result bundle for details.
     */
    public static final int RESULT_ERROR = -1;

    /**
     * A generic result indicating that the request was cancelled prior to completion.
     */
    public static final int RESULT_CANCEL = -2;

    public final static String IS_SUCCESS = "success";
    public final static String ERROR_MESSAGE = "error_message";

    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

    public static final String HTTP_HEADER_MESSAGE_ID = "X-MsgID";

    public static final String HTTP_HEADER_XSESSION_ID = "X-SessionID";

    protected static final String HEADER_USER_AGENT = "User-Agent";
    protected static final String HEADER_CONTENT_TYPE = "Content-Type";
    protected static final String HEADER_CONTENT_LENGTH = "Content-Length";
    protected static final String HEADER_ACCEPT = "Accept";
    protected static final String HEADER_ETAG = "ETag";
    protected static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    protected static final String CONTENT_TYPE_XML = "text/xml";
    protected static final String CONTENT_TYPE_JSON = "application/json";

    protected static final String REQUEST_METHOD_DELETE = "DELETE";
    protected static final String REQUEST_METHOD_POST = "POST";
    protected static final String REQUEST_METHOD_PUT = "PUT";
    protected static final String REQUEST_METHOD_GET = "GET";

    /**
     * Contains the captured response.
     */
    protected ByteArrayInputStream response;

    public PlatformAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public T loadInBackground() {

        T t = null;

        int res = RESULT_OK;

        // Get the URL
        URL url = null;
        try {
            String urlStr = getURL();
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // unable to parse URL // " + getURL(), e);
            res = RESULT_ERROR;
        }
        if (res == RESULT_OK) {

            HttpURLConnection connection = null;

            try {
                // Start the connection
                try {
                    // from KITKAT default impl is OkHttpUrlConnection
                    if (Build.VERSION.SDK_INT < 19) {
                        OkHttpClient client = new OkHttpClient();
                        OkUrlFactory factory = new OkUrlFactory(client);
                        connection = factory.open(url);
                        Log.d(Const.LOG_TAG, getClass().getSimpleName() + " // SPDY is enabled // ");

                    } else {
                        connection = (HttpURLConnection) url.openConnection();
                    }
                } catch (IOException e) {
                    Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // error opening connection // " + getURL(), e);
                    res = RESULT_ERROR;
                }

                if (res == RESULT_OK) {

                    configureConnection(connection);

                    // Check the HTTP status
                    int httpStatusCode = -1;
                    String httpStatusMessage = null;
                    try {
                        httpStatusCode = connection.getResponseCode();
                        httpStatusMessage = connection.getResponseMessage();
                    } catch (IOException e) {
                        Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // error determine HTTP response code // "
                                + getURL(), e);
                        res = RESULT_ERROR;
                    }
                    if (res == RESULT_OK) {
                        // If HTTP was successful then pull the input stream
                        // and proceed to parsing
                        if (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < HttpStatus.SC_BAD_REQUEST) {
                            InputStream in = null;
                            try {
                                in = new BufferedInputStream(connection.getInputStream());
                            } catch (IOException e) {
                                Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // error opening input stream // "
                                        + getURL(), e);
                                res = RESULT_ERROR;
                            }

                            if (res == RESULT_OK) {
                                // do the json parsing here...
                                t = parseStream(connection, in);

                            }
                        } else {
                            res = RESULT_ERROR;
                            Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // HTTP was unsuccessful // "
                                    + getURL() + " // HTTP_STATUS_CODE //" + httpStatusCode);
                        }
                    }

                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        return t;
    }

    @Override
    protected void releaseResources(T data) {
        // TODO Auto-generated method stub

    }

    /**
     * Return the User-Agent header string for this request. The default implementation creates a generic UA value with version. A
     * product should produce its own value.
     * 
     * @return A string containing the user agent for this request
     */
    // protected String getUserAgent() {
    // PlatformProperties.getUserAgent();
    //
    // StringBuilder ua = new StringBuilder();
    // ua.append("Concur/");
    //
    // Context ctx = getContext();
    //
    // if (ctx != null) {
    // String versionName;
    // try {
    // versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
    // } catch (NameNotFoundException e) {
    // versionName = "0.0.0";
    // }
    // ua.append(versionName);
    // }
    // ua.append(" (Android, ").append(Build.MODEL).append(", ").append(Build.VERSION.RELEASE).append(")");
    //
    // return ua.toString();
    // }

    /**
     * Configure connection properties. The default implementation sets the user agent, content type to type/xml, connect timeout
     * to 10 seconds, and read timeout to 30 seconds.
     * 
     * @param connection
     *            The open but not yet connected {@link HttpURLConnection} to the server
     */
    protected void configureConnection(HttpURLConnection connection) {
        // Configure the headers
        connection.setRequestProperty(HEADER_USER_AGENT, PlatformProperties.getUserAgent());
        connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_XML);

        // MOB-18841 - There seems to be a bug in JellyBean+ where re-using
        // the connection will cause an EOFException.
        // For more details check out the following links:
        // https://code.google.com/p/libs-for-android/issues/detail?id=14
        // https://code.google.com/p/google-http-java-client/issues/detail?id=116
        // http://stackoverflow.com/questions/15411213/android-httpsurlconnection-eofexception
        if (Build.VERSION.SDK_INT > 13) {
            connection.setRequestProperty("Connection", "close");
        }

        // Set timeout values
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
    }

    /**
     * Parse the returned stream using the desired parser. Be sure to close the stream when done parsing.
     * 
     * @param is
     *            An {@link InputStream} of the data returned by the server.
     * 
     * @return The int result code of the operation.
     */
    protected abstract T parseStream(InputStream is);

    /**
     * Parse the returned stream using the desired parser. Be sure to close the stream when done parsing.
     * 
     * @param connection
     *            Contains the {@link HttpURLConnection} connection.
     * @param is
     *            An {@link InputStream} of the data returned by the server.
     * 
     * @return The int result code of the operation.
     */
    protected T parseStream(HttpURLConnection connection, InputStream is) {
        return parseStream(is);
    }

    /**
     * Gets the URL for this request.
     * 
     * @return the request URL.
     */
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
                // resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getURL: server address is not set, cancelling request.");
            // resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
        }
        url = strBldr.toString();
        return url;
    }

    /**
     * Return the post body for the request. If the body is null then the request will be executed as a GET. If a blank POST body
     * is needed then return a blank string.
     * 
     * @return A string containing the POST body or null if the request is a GET request
     */
    protected String getPostBody() {
        return null;
    }

    /**
     * Gets the service end-point for this request.
     * 
     * @return returns the service end-point for this request.
     */
    protected abstract String getServiceEndPoint();

    /**
     * Will return the response from the server as an input stream.
     * 
     * @return returns any captured response as an input stream.
     */
    public InputStream getResponse() {
        return response;
    }
}
