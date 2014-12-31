/**
 * 
 */
package com.concur.mobile.core.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

/**
 * Models a collection of request parameters.
 * 
 * @author AndrewK
 */
public abstract class ServiceRequest {

    private static final String CLS_TAG = ServiceRequest.class.getSimpleName();

    /**
     * Contains the session ID.
     */
    public String sessionId;

    /**
     * Contains the access token.
     */
    public String accessToken;
    
    /**
     * Contains the content type
     */
    public String contentType = null;

    /**
     * Contains the user ID.
     */
    public String userId;

    /**
     * Contains a unique message ID associated with this request.
     */
    public String messageId;

    /**
     * Contains the underlying http request object.
     */
    public HttpRequestBase httpRequest;

    /**
     * Contains whether or not this request has been canceled.
     */
    public boolean canceled;

    /**
     * Whether or not this request is currently being processed. i.e. The request has not yet returned or not completed.
     */
    public volatile boolean isProcessing;

    /**
     * Contains whether or not this request should be handled asynchronously with respect to other requests.
     */
    public boolean background;

    /**
     * Contains whether or not this request required message id
     */

    public boolean msgIdReq = false;

    /**
     * flag to enable Spdy protocol
     */
    private boolean enableSpdy;

    /**
     * Gets the URI for this request.
     * 
     * @return the request URI.
     */
    protected String getURI() throws URISyntaxException {
        // Grab the server address
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(serverAdd);
        strBldr.append(getServiceEndpointURI());
        String serverURI = strBldr.toString();
        enableSpdy = Preferences.shouldEnableSpdy();
        return serverURI;
    }

    protected int getSoTimeout() {
        if (Format.isDevServer(Preferences.getServerAddress())) {
            return 300000;
        } else {
            return 60000;
        }
    }

    /**
     * Determines whether or not this request requires a valid session in order to complete.
     * 
     * @return whether this request requires a valid session. Defaults to <code>true</code>.
     */
    protected boolean isSessionRequired() {
        return true;
    }

    protected void addHeaders(HttpRequestBase request) {
        if (sessionId != null) {
            request.addHeader(Const.HTTP_HEADER_XSESSION_ID, URLEncoder.encode(sessionId));
        }

        request.addHeader(Const.HTTP_HEADER_USER_AGENT, Const.HTTP_HEADER_USER_AGENT_VALUE);

        // if (isMsgIdReq() && messageId != null) {
        if (messageId != null) {
            request.addHeader(Const.HTTP_HEADER_MESSAGE_ID, URLEncoder.encode(messageId));
        }

        request.addHeader(Const.HTTP_HEADER_CHARACTER_ENCODING, URLEncoder.encode(Const.HTTP_BODY_CHARACTER_ENCODING));

        if (accessToken != null) {
            request.addHeader(Const.HTTP_HEADER_AUTHORIZATION, "OAuth " + URLEncoder.encode(accessToken));
        }
        
        if (contentType != null) {
        	request.addHeader(Const.HTTP_HEADER_CONTENT_TYPE, contentType);
        }

    }

    /**
     * Will process the service request and return a <code>ServiceReply</code> object.
     * 
     * @param concurService
     *            the instance of the service associated with this request.
     * 
     * @return an instance of <code>ServiceReply</code>.
     * 
     * @throws IOException
     *             if any I/O error results during the processing of the request or reply.
     * @throws URISyntaxException
     */

    public ServiceReply process(ConcurService concurService) throws IOException {

        ServiceReply reply = null;

        if (sessionId != null || !isSessionRequired()) {
            try {

                // Check whether the queued request has been cancelled.
                if (canceled) {
                    // Throw a service request exception indicating session id
                    // is null.
                    throw new ServiceRequestException(CLS_TAG + ".process: request cancelled!");
                }

                // Set flag so we know that this service is currently
                // processing.
                isProcessing = true;

                // Build our URI
                String urlStr = getURI();

                URL url = null;

                Log.d(Const.LOG_TAG, getClass().getSimpleName() + ": MWS call to - " + urlStr);

                url = new URL(urlStr);

                long startTimeMillis = System.currentTimeMillis();

                HttpURLConnection connection = null;

                try {
                    // Start the connection

                    if (enableSpdy && Build.VERSION.SDK_INT < 19) {
                        OkHttpClient client = new OkHttpClient();
                        // client.setCache(new Cache(cacheFolder.getRoot(), 10 * 1024 * 1024));
                        OkUrlFactory factory = new OkUrlFactory(client);
                        connection = factory.open(url);
                        Log.d(Const.LOG_TAG, getClass().getSimpleName() + " // SPDY is enabled // ");

                    } else {
                        connection = (HttpURLConnection) url.openConnection();
                    }

                    // Check whether the queued request has been cancelled.
                    if (canceled) {
                        // Throw a service request exception indicating session id
                        // is null.
                        throw new ServiceRequestException(CLS_TAG + ".process: request cancelled!");
                    }

                    // Set timeout values
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(getSoTimeout());

                    HttpRequestBase request = getRequestBase(concurService);
                    setRequest(request);

                    addHeaders(request);
                    connection.setRequestMethod(request.getMethod());
                    // adding header properties to HttpURLConnection from HttpRequestBase.
                    // TODO replace HttpRequestBase request to HttpURLConnection
                    for (Header header : request.getAllHeaders()) {
                        connection.setRequestProperty(header.getName(), header.getValue());
                    }
                    if (request.getMethod() == "POST") {
                        HttpEntity entity = ((HttpPost) request).getEntity();
                        if (entity != null) {
                            connection.setDoOutput(true);
                            int length = (int) entity.getContentLength();
                            connection.setFixedLengthStreamingMode(length);

                            // Connect and send the post
                            OutputStream out = null;
                            out = new BufferedOutputStream(connection.getOutputStream());
                            entity.writeTo(out);
                            out.flush();

                        }
                    }

                    long stopTimeMillis = System.currentTimeMillis();

                    Log.d(Const.LOG_TAG, CLS_TAG + ".process: request(" + getRequestName() + ") took "
                            + (stopTimeMillis - startTimeMillis) + " ms.");

                    reply = processResponse(connection, concurService);

                    if (reply != null) {
                        reply.httpStatusCode = connection.getResponseCode();
                        reply.httpStatusText = connection.getResponseMessage();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".process: reply is null!");
                    }

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            } catch (MalformedURLException e) {
                throw new ServiceRequestException(e.getMessage());
            } catch (URISyntaxException uriSynExc) {
                throw new ServiceRequestException(uriSynExc.getMessage());

            } finally {
                isProcessing = false;
                setRequest(null);
            }
        } else {

            isProcessing = false;
            // Throw a service request exception indicating session id is null.
            throw new ServiceRequestException(CLS_TAG + ".process: null session id!");
        }

        return reply;
    }

    /**
     * Gets a simple request name.
     * 
     * @return returns a simple request name.
     */
    protected String getRequestName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Will attempt to cancel this request.
     * 
     * @return returns <code>true</code> if the request was successfully canceled; <code>false</code> otherwise.
     */
    public synchronized void cancel() {
        canceled = true;
        isProcessing = false;
        Log.d(Const.LOG_TAG, CLS_TAG + ".cancel: " + this.getClass().getSimpleName() + " cancelled.");

        // The actual call cannot occur on the main thread. Push it off into an
        // AsyncTask.
        // DO NOT replicate this functionality to the new Async service request
        // pattern. That doesn't need this since cancels will already happen on
        // the background thread.
        new AsyncTask<HttpRequestBase, Void, Void>() {

            @Override
            protected Void doInBackground(HttpRequestBase... params) {
                HttpRequestBase request = params[0];

                if (request != null) {
                    request.abort();
                }
                return null;
            }

        }.execute(httpRequest);

    }

    /**
     * Gets whether or not this request was canceled.
     * 
     * @return whether or not this request was canceled.
     */
    public synchronized boolean isCanceled() {
        return canceled;
    }

    /**
     * Sets the underlying http request object.
     * 
     * @param httpRequest
     *            the underlying http request object.
     */
    public synchronized void setRequest(HttpRequestBase httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * Gets the service end-point URI for the request, i.e., "/mobile/Expense/GetExpenseTypes"
     * 
     * @return the service end-point URI.
     */
    protected abstract String getServiceEndpointURI();

    /**
     * Gets the instance of <code>HttpRequestBase</code> used to issue the request. <b>NOTE:</b> Should return one of the
     * extensions of the <code>HttpRequestBase</code> class.
     * 
     * @param concurService
     *            a reference to the concur service.
     * 
     * @return an instance of <code>HttpRequestBase</code>.
     */
    protected abstract HttpRequestBase getRequestBase(ConcurService concurService) throws ServiceRequestException;

    /**
     * Will log a non-200 HTTP response.
     * 
     * @param response
     *            the response object.
     * @param clsMthTag
     *            class tag.
     */
    public static void logError(HttpURLConnection response, String clsMthTag) {
        String replyBody = null;
        int statusCode = 0;
        String statusLine = null;
        try {
            statusCode = response.getResponseCode();
            statusLine = response.getResponseMessage();
            InputStream is = new BufferedInputStream(response.getInputStream());
            if (is != null) {
                String encodingHeader = response.getContentEncoding();
                String encoding = "UTF-8";
                if (encodingHeader != null) {
                    encoding = encodingHeader;
                }
                replyBody = readStream(is, encoding);
            }

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".logError: I/O exception reading response body", ioExc);
        } finally {
            Log.e(Const.LOG_TAG, clsMthTag + ": StatusCode: " + statusCode + ", StatusLine: " + statusLine
                    + ", response: " + ((replyBody != null) ? replyBody : "null") + ".");
        }
    }

    /**
     * A simple helper to convert an InputStream into a String.
     * 
     * @param is
     *            The input stream
     * @return A String containing the full contents of the InputStream
     * @throws IOException
     */
    protected static String readStream(InputStream is, String encoding) throws IOException {
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

    /**
     * Gets an instance of <code>Reader</code> used to read content from <code>is</code>.
     * 
     * @param is
     *            the underlying input stream.
     * @param encoding
     *            the input stream character encoding.
     * @return returns an instance of <code>Reader</code> used to read content.
     * @throws IOException
     *             throws an <code>IOException</code> if the reader can't be opened.
     */
    protected static Reader getReader(InputStream is, String encoding) throws IOException {
        Reader in;
        try {
            in = new InputStreamReader(new BufferedInputStream(is, (256 * 1024)), encoding);
        } catch (UnsupportedEncodingException e) {
            in = new InputStreamReader(is);
        }
        return in;
    }

    /**
     * Will process the response and return an instance of <code>ServiceReply</code>.
     * 
     * @param response
     *            the response from the server.
     * @param concurService
     *            the concur service.
     * 
     * @return an instance of <code>ServiceReply</code> containing response.
     */
    protected abstract ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
            throws IOException;

    /**
     * Will run this request synchronously if <code>background</code> is <code>false</code>; asynchronously otherwise.
     * 
     * @param concurService
     *            contains a reference to a <code>ConcurService</code) object.
     * @param networkActivityType
     *            contains a constant identifying the network activity type.
     */
    public void run(final ConcurService concurService, final int networkActivityType) {
        if (background) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    handleRequest(concurService, networkActivityType);
                }
            });
            t.start();
        } else {
            handleRequest(concurService, networkActivityType);
        }
    }

    /**
     * Will handle the request.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * This method is a no-op, sub-classes should override this method to provide handling of the request.
     * 
     * @param concurService
     *            contains a reference to a <code>ConcurService</code> object.
     * @param networkActivityType
     *            contains a constant value indicating the network activity type.
     */
    protected void handleRequest(ConcurService concurService, int networkActivityType) {
    }

    public boolean isMsgIdReq() {
        return msgIdReq;
    }

    public void setMsgIdReq(boolean msgIdReq) {
        this.msgIdReq = msgIdReq;
    }

    // /////////////////////////////////////////////////////////////////
    // Keep the code below around because it comes in handy.
    // DO NOT EVER USER IT FOR A RELEASE CONNECTION.
    // /////////////////////////////////////////////////////////////////

    // private HttpClient getTrustingHttpClient(HttpParams params) {
    // try {
    // KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    // trustStore.load(null, null);
    //
    // SSLSocketFactory sf = new AllTrustingSSLSocketFactory(trustStore);
    // sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    //
    // // HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    // // HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
    //
    // SchemeRegistry registry = new SchemeRegistry();
    // registry.register(new Scheme("http",
    // PlainSocketFactory.getSocketFactory(), 80));
    // registry.register(new Scheme("https", sf, 443));
    //
    // ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
    // registry);
    //
    // return new DefaultHttpClient(ccm, params);
    // } catch (Exception e) {
    // return new DefaultHttpClient(params);
    // }
    // }
    //
    // public static class AllTrustingSSLSocketFactory extends SSLSocketFactory
    // {
    //
    // SSLContext sslContext = SSLContext.getInstance("TLS");
    //
    // public AllTrustingSSLSocketFactory(KeyStore truststore) throws
    // NoSuchAlgorithmException,
    // KeyManagementException, KeyStoreException, UnrecoverableKeyException {
    // super(truststore);
    //
    // TrustManager tm = new X509TrustManager() {
    //
    // public void checkClientTrusted(X509Certificate[] chain, String authType)
    // throws CertificateException {
    // }
    //
    // public void checkServerTrusted(X509Certificate[] chain, String authType)
    // throws CertificateException {
    // }
    //
    // public X509Certificate[] getAcceptedIssuers() {
    // return null;
    // }
    // };
    //
    // sslContext.init(null, new TrustManager[] { tm }, null);
    // }
    //
    // @Override
    // public Socket createSocket(Socket socket, String host, int port, boolean
    // autoClose) throws IOException,
    // UnknownHostException {
    // return sslContext.getSocketFactory().createSocket(socket, host, port,
    // autoClose);
    // }
    //
    // @Override
    // public Socket createSocket() throws IOException {
    // return sslContext.getSocketFactory().createSocket();
    // }
    // }

}
