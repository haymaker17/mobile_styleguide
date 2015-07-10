package com.concur.mobile.base.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.base.util.Const;
import com.concur.mobile.base.util.DataConnectivityManager;
import com.concur.mobile.base.util.DataConnectivityManager.ConnectivityListener;
import com.concur.mobile.base.util.IOUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public abstract class BaseAsyncRequestTask extends AsyncTask<Void, Void, Integer> {

    private static final String CLS_TAG = "BaseAsyncRequestTask";

    /**
     * An interface used to allow request tasks to communicate their results back to their owner.
     */
    public interface AsyncReplyListener {

        void onRequestSuccess(Bundle resultData);

        void onRequestFail(Bundle resultData);

        void onRequestCancel(Bundle resultData);

        void cleanup();
    }

    // -------------------------------------------------

    protected static final boolean LOG_TRAFFIC = false;

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
     * The results bundle key for the creator-defined ID of this request.
     */
    public static final String REQUEST_ID = "request.id";

    /**
     * The results bundle key for the HTTP status code
     */
    public static final String HTTP_STATUS_CODE = "request.http.status.code";

    /**
     * The results bundle key for the HTTP status message
     */
    public static final String HTTP_STATUS_MESSAGE = "request.http.status.message";

    /**
     * The results bundle key for the HTTP status message
     */
    public static final String HTTP_RESPONSE = "request.response";

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

    /**
     * flag to enable Spdy protocol
     */
    public static boolean enableSpdy;
    // -------------------------------------------------

    protected DataConnectivityManager dcm;

    protected WeakReference<Context> contextRef;
    
    private ConnectivityListener connectivityListener;

    /**
     * A {@link BaseAsyncResultReceiver} that will receive the result post from this request.
     */
    protected WeakReference<BaseAsyncResultReceiver> receiverRef;

    /**
     * A request id from the creator. If one is not provided then a default value of -1 will be used.
     */
    protected int id;

    protected int resultCode;

    /**
     * The {@link Bundle} of data containing request results. This should generally not contain the full request result body. That
     * data should be cached or stored elsewhere before the request completes.
     */
    protected Bundle resultData;

    /**
     * Contains the captured response.
     */
    protected ByteArrayInputStream response;

    // Contains whether or not a server response is retained.
    protected boolean retainServerResponse = false;

    // -------------------------------------------------

    /**
     * The standard constructor.
     * 
     * @param context
     *            A {@link Context} object needed for various operations. The application context is recommended.
     * 
     * @param receiver
     *            A {@link BaseAsyncResultReceiver} to receive a result message from this request.
     * @param id
     *            An identifier used by the creator for this request. This value will be returned in the results bundle with the
     *            key of {@link REQUEST_ID}.
     */
    public BaseAsyncRequestTask(Context context, int id, BaseAsyncResultReceiver receiver) {
        this.contextRef = new WeakReference<Context>(context);
        this.receiverRef = new WeakReference<BaseAsyncResultReceiver>(receiver);
        this.id = id;

        resultData = new Bundle();
        resultData.putInt(REQUEST_ID, id);

        resultCode = RESULT_OK;
    }

    public void setRetainResponse(boolean retainResponse) {
        this.retainServerResponse = retainResponse;
    }
    /**
     * Safely dereference the {@link WeakReference} for the context
     * 
     * @return The {@link Context} object if still available, null otherwise
     */
    protected Context getContext() {
        if (contextRef != null) {
            return contextRef.get();
        }

        return null;
    }

    /**
     * Safely dereference the {@link WeakReference} for the receiver
     * 
     * @return The {@link BaseAsyncResultReceiver} object if still available, null otherwise
     */
    protected BaseAsyncResultReceiver getReceiver() {
        if (receiverRef != null) {
            return receiverRef.get();
        }

        return null;
    }

    /**
     * Return the URL for the request
     * 
     * @return A string containing the full URL used for this connection
     */
    protected abstract String getURL();

    /**
     * Return the User-Agent header string for this request. The default implementation creates a generic UA value with version. A
     * product should produce its own value.
     * 
     * @return A string containing the user agent for this request
     */
    protected String getUserAgent() {
        StringBuilder ua = new StringBuilder();
        ua.append("Concur/");

        Context ctx = getContext();

        if (ctx != null) {
            String versionName;
            try {
                versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                versionName = "0.0.0";
            }
            ua.append(versionName);
        }
        ua.append(" (Android, ").append(Build.MODEL).append(", ").append(Build.VERSION.RELEASE).append(")");

        return ua.toString();
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

    public int getId() {
        return id;
    }

    public int getResultCode() {
        return resultCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // If we still have context then proceed
        Context ctx = getContext();
        if (ctx != null) {
            // Grab a DCM to monitor connectivity
            connectivityListener = new ConnectivityListener() {
                public void connectionLost(int connectionType) {
                    // Do nothing
                }

                public void connectionEstablished(int connectionType) {
                    // Do nothing
                }
            };
            dcm = DataConnectivityManager.getInstance(ctx, connectivityListener);
        } else {
            connectivityListener = null;
            // Otherwise, cancel
            cancel(true);
        }

        // Only proceed if connected
        if (!dcm.isConnected()) {
            cancel(true);
        }

    }

    @Override
    protected Integer doInBackground(Void... params) {

        // Check for cancel
        if (isCancelled()) {
            return RESULT_CANCEL;
        }

        if (!dcm.isConnected()) {
            // Do not proceed without a connection
            return RESULT_ERROR;
        }

        int res = RESULT_OK;

        // Get the URL
        URL url = null;
        try {
            String urlStr = getURL();
            if (LOG_TRAFFIC) {
                Log.d(Const.LOG_TAG, getClass().getSimpleName() + ": MWS call to - " + urlStr);
            }
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // unable to parse URL // " + getURL(), e);
            res = RESULT_ERROR;
        }

        // Check for cancel
        if (isCancelled()) {
            res = RESULT_CANCEL;
        }

        if (res == RESULT_OK) {
            long start = System.currentTimeMillis();

            HttpURLConnection connection = null;

            try {
                // Start the connection
                try {
                    // from KITKAT default impl is OkHttpUrlConnection
                    if (enableSpdy && Build.VERSION.SDK_INT < 19) {
                        OkHttpClient client = new OkHttpClient();
                        // client.setCache(new Cache(getApplicationContext().getCacheDir(), 10 * 1024 * 1024));
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

                // Check for cancel
                if (isCancelled()) {
                    res = RESULT_CANCEL;
                }

                if (res == RESULT_OK) {

                    configureConnection(connection);

                    // Do a post if needed
                    res = doPost(connection);

                    // Check for cancel
                    if (isCancelled()) {
                        res = RESULT_CANCEL;
                    }

                    if (res == RESULT_OK) {

                        // Check the HTTP status
                        int httpStatusCode = -1;
                        String httpStatusMessage = null;
                        try {
                            httpStatusCode = connection.getResponseCode();
                            httpStatusMessage = connection.getResponseMessage();
                        } catch (IOException e) {
                            Log.e(Const.LOG_TAG, getClass().getSimpleName()
                                    + " // error determine HTTP response code // " + getURL(), e);
                            res = RESULT_ERROR;
                        }

                        resultData.putInt(HTTP_STATUS_CODE, httpStatusCode);
                        resultData.putString(HTTP_STATUS_MESSAGE, httpStatusMessage);

                        if (res == RESULT_OK) {
                            Log.d(Const.LOG_TAG,
                                    getClass().getSimpleName() + " // request complete // "
                                            + (System.currentTimeMillis() - start));

                            // If HTTP was successful then pull the input stream
                            // and proceed to parsing
                            if (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode < HttpStatus.SC_BAD_REQUEST) {
                                InputStream in = null;
                                try {
                                    in = logResponse(retainResponse(new BufferedInputStream(connection.getInputStream())));
                                } catch (IOException e) {
                                    Log.e(Const.LOG_TAG, getClass().getSimpleName()
                                            + " // error opening input stream // " + getURL(), e);
                                    res = RESULT_ERROR;
                                }

                                // Check for cancel
                                if (isCancelled()) {
                                    res = RESULT_CANCEL;
                                }

                                if (res == RESULT_OK) {
                                    // Let the derived request do the work
                                    res = parseStream(connection, in);
                                }
                            } else {
                                res = RESULT_ERROR;
                                String reqMeth = (connection != null) ? connection.getRequestMethod() : "";
                                Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // HTTP " + reqMeth
                                        + " was unsuccessful // " + getURL() + " // HTTP_STATUS_CODE //"
                                        + httpStatusCode);
                            }
                        }
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            if (res == RESULT_OK) {
                res = onPostParse();
            }
        }

        return res;
    }

    /**
     * Configure connection properties. The default implementation sets the user agent, content type to type/xml, connect timeout
     * to 10 seconds, and read timeout to 30 seconds.
     * 
     * @param connection
     *            The open but not yet connected {@link HttpURLConnection} to the server
     */
    protected void configureConnection(HttpURLConnection connection) {
        // Configure the headers
        connection.setRequestProperty(HEADER_USER_AGENT, getUserAgent());
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
     * Perform a POST operation to the server if needed. The default implementation gets the POST body as a string and converts to
     * bytes for transfer. This approach will work for most all POSTs. When using this task to send large amounts of data then
     * this method needs to be overridden to use streams for the whole process and probably setting ChunkedStreamingMode.
     * 
     * @param connection
     *            The open {@link HttpURLConnection} to the server
     * 
     * @return The int result code of the operation.
     */
    protected int doPost(HttpURLConnection connection) {

        int res = RESULT_OK;

        // Configure the post body
        String post = getPostBody();
        if (post != null) {

            if (LOG_TRAFFIC) {
                Log.d(Const.LOG_TAG, getClass().getSimpleName() + ": POST BODY\n==================\n" + post
                        + "\n==================\n\n");
            }

            connection.setDoOutput(true);

            byte[] outBytes;
            try {
                outBytes = post.getBytes("UTF-8");
            } catch (UnsupportedEncodingException uee) {
                // This should never happen
                Log.wtf(Const.LOG_TAG, getClass().getSimpleName() + " // this should not happen", uee);
                throw new RuntimeException(uee);
            }
            connection.setFixedLengthStreamingMode(outBytes.length);

            // Check for cancel
            if (isCancelled()) {
                res = RESULT_CANCEL;
            }

            if (res == RESULT_OK) {
                // Connect and send the post
                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(connection.getOutputStream());
                    out.write(outBytes);
                    out.flush();
                } catch (IOException e) {
                    Log.e(Const.LOG_TAG, getClass().getSimpleName() + " // error opening and writing output stream // "
                            + getURL(), e);
                    res = RESULT_ERROR;
                }
            }
        } // post

        return res;
    }

    /**
     * Parse the returned stream using the desired parser. Be sure to close the stream when done parsing.
     * 
     * @param is
     *            An {@link InputStream} of the data returned by the server.
     * 
     * @return The int result code of the operation.
     */
    protected abstract int parseStream(InputStream is);

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
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        return parseStream(is);
    }

    /**
     * Called on the background thread after parsing is complete. This is the ideal place to write data to a local store.
     * 
     * @return The int result code of the operation.
     */
    protected int onPostParse() {
        return RESULT_OK;
    }

    @Override
    protected void onCancelled() {
        resultCode = RESULT_CANCEL;

        BaseAsyncResultReceiver receiver = getReceiver();
        if (receiver != null) {
            receiver.send(resultCode, resultData);
        } else {
            Log.w(Const.LOG_TAG, getClass().getSimpleName() + " // no receiver, dropping result");
        }

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (connectivityListener != null) {
            DataConnectivityManager.removeListener(getContext(), connectivityListener);
        }

        resultCode = result;

        BaseAsyncResultReceiver receiver = getReceiver();
        if (receiver != null) {
            receiver.send(resultCode, resultData);
        } else {
            Log.w(Const.LOG_TAG, getClass().getSimpleName() + " // no receiver, dropping result");
        }
    }

    @SuppressWarnings("resource")
    private InputStream logResponse(InputStream inputStream) {
        if (LOG_TRAFFIC) {
            java.util.Scanner scanner = null;
            try {
                ByteArrayOutputStream baos = IOUtils.reusableOutputStream(inputStream);
                InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
                // Copy back the original input stream so it can be re-used
                inputStream = new ByteArrayInputStream(baos.toByteArray());

                scanner = new java.util.Scanner(is1, "UTF-8").useDelimiter("\\A");
                String response = (scanner.hasNext() ? scanner.next() : "");
                Log.d(Const.LOG_TAG, getClass().getSimpleName() + "  HTTP RESPONSE:\n==================\n" + response
                        + "\n==================\n\n");

            } catch (Exception e) {
                Log.d(Const.LOG_TAG, getClass().getSimpleName() + " Error parsing response.", e);
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }

        return inputStream;

    } // logResponse()

    /**
     * Will retain a copy of <code>inputStream</code> if <code>RETAIN_RESPONSE</code> is enabled.
     * 
     * @param inputStream
     *            contains a reference to an input stream.
     * @return
     * 
     */
    private InputStream retainResponse(InputStream inputStream) {
        if (isRetainResponseEnabled()) {
            try {
                ByteArrayOutputStream baos = IOUtils.reusableOutputStream(inputStream);
                response = new ByteArrayInputStream(baos.toByteArray());
                inputStream = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".retainResponse: " + exc);
            }
        }
        return inputStream;
    }

    /**
     * Gets whether or not the request response should be retained.
     * 
     * @return returns whether the request response should be retained; defaults to <code>false</code>.
     */
    public boolean isRetainResponseEnabled() {
        return retainServerResponse;
    }

    /**
     * Will return the response from the server as an input stream.
     * 
     * @return returns any captured response as an input stream.
     */
    public InputStream getResponse() {
        return response;
    }

}
