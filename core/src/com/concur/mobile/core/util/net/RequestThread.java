package com.concur.mobile.core.util.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.util.Const;

// This class handles synchronous requests to the MWS.
abstract public class RequestThread extends Thread {

    protected String requestUri;
    protected String requestBody;
    protected String sessionId;

    protected Product product;

    protected HttpResponse response;

    protected String statusMessage;

    public RequestThread(String threadName, Product product) {
        super(threadName);
        this.product = product;
    }

    protected void setUri(String uri) {
        requestUri = uri;
    }

    protected void setBody(String body) {
        requestBody = body;
    }

    protected void setSession(String id) {
        sessionId = id;
    }

    protected HttpResponse getResponse() {
        return response;
    }

    protected Document getResponseAsDoc() {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document doc = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            doc = builder.parse(AndroidHttpClient.getUngzippedContent(response.getEntity()));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // Something went south on the request. Log it.
            Log.e(Const.LOG_TAG, "Request generated an error", e);
        } catch (IllegalStateException e) {
            // Unable to parse the response XML. You know what to do.
            Log.e(Const.LOG_TAG, "XML response was not parseable", e);
        } catch (SAXException e) {
            // Unable to parse the response XML. You know what to do.
            Log.e(Const.LOG_TAG, "XML response was not parseable", e);
        }

        return doc;
    }

    protected JSONObject getResponseAsJSON() {
        JSONObject jsonObj = null;
        if (response != null) {
            final HttpEntity entity = response.getEntity();
            try {
                Header encodingHeader = entity.getContentEncoding();
                String encoding = HTTP.UTF_8;
                if (encodingHeader != null) {
                    encoding = encodingHeader.getValue();
                }
                String respString = readStream(AndroidHttpClient.getUngzippedContent(entity), encoding);
                jsonObj = new JSONObject(respString);
            } catch (IOException e) {
                Log.e(Const.LOG_TAG, "Unable to read JSON response");
            } catch (JSONException e) {
                Log.e(Const.LOG_TAG, "Unable to parse JSON response");
            }
        }

        return jsonObj;
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
     * Execute the request and return the response
     */
    public void run() {
        int status = execute();
        handleResponse(status);
    }

    abstract protected void handleResponse(int status);

    protected void addPostHeaders(HttpRequestBase request) {
        request.setHeader(Const.HTTP_HEADER_CONTENT_TYPE, "text/xml");
        request.addHeader(Const.HTTP_HEADER_CHARACTER_ENCODING, URLEncoder.encode(Const.HTTP_BODY_CHARACTER_ENCODING));
    }

    protected void addHeaders(HttpRequestBase request) {
        if (sessionId != null) {
            request.setHeader(Const.HTTP_HEADER_XSESSION_ID, URLEncoder.encode(sessionId));
        }
        request.setHeader(Const.HTTP_HEADER_USER_AGENT, Const.HTTP_HEADER_USER_AGENT_VALUE);
    }

    /**
     * 
     */
    protected int execute() {

        int status = -1;

        try {
            // Create and open the connection
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);
            HttpConnectionParams.setSoTimeout(params, 30000);

            // /////////////////////////////////////////////////////////////////
            // Keep the code below around because it comes in handy.
            // DO NOT EVER USER IT FOR A RELEASE CONNECTION.
            // /////////////////////////////////////////////////////////////////
            HttpClient client;
            if (requestUri.contains("rqa3") || requestUri.contains("RQA3")) {
                client = getTrustingHttpClient(params);
            } else {
                client = new DefaultHttpClient(params);
            }

            // HttpClient client = new DefaultHttpClient(params);
            HttpRequestBase request;

            // Some assumptions here but they fit for everything we care about.
            if (requestBody == null) {
                request = new HttpGet();
            } else {
                request = new HttpPost();
                addPostHeaders(request);
                ((HttpPost) request).setEntity(new StringEntity(requestBody, Const.HTTP_BODY_CHARACTER_ENCODING));
            }

            addHeaders(request);

            request.setURI(new URI(requestUri));

            AndroidHttpClient.modifyRequestToAcceptGzipResponse(request);

            response = client.execute(request);
            status = response.getStatusLine().getStatusCode();

        } catch (MalformedURLException e) {
            // Something is really messed up. Log it.
            Log.e(Const.LOG_TAG, "Unable to create URI: " + requestUri, e);
            statusMessage = e.getMessage();
        } catch (URISyntaxException e) {
            // Something is really messed up. Log it.
            Log.e(Const.LOG_TAG, "Unable to create URI: " + requestUri, e);
            statusMessage = e.getMessage();
        } catch (UnsupportedEncodingException e) {
            // Something is really messed up. Log it.
            // Do not include the post text here: it may contain secure data
            Log.e(Const.LOG_TAG, "Unable to set the entity for the request", e);
            statusMessage = e.getMessage();
        } catch (ClientProtocolException e) {
            // Something went south on the request. Log it.
            Log.e(Const.LOG_TAG, "Request generated an error", e);
            statusMessage = e.getMessage();
        } catch (IllegalArgumentException e) {
            // Something is really messed up. Log it.
            // Do not include the post text here: it may contain secure data
            Log.e(Const.LOG_TAG, "Unable to execute the request", e);
            statusMessage = e.getMessage();
        } catch (IOException e) {
            // Something went south on the request. Log it.
            Log.e(Const.LOG_TAG, "Request generated an error", e);
            statusMessage = e.getMessage();
        } catch (Exception e) {
            // And something is just totally whacked (like SSL blowing up down in the bowels of Android
            Log.e(Const.LOG_TAG, "Unexpected exception on request", e);
            statusMessage = e.getMessage();
        }

        return status;
    }

    // /////////////////////////////////////////////////////////////////
    // Keep the code below around because it comes in handy.
    // DO NOT EVER USER IT FOR A RELEASE CONNECTION.
    // /////////////////////////////////////////////////////////////////

    private HttpClient getTrustingHttpClient(HttpParams params) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new AllTrustingSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            // HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            // HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient(params);
        }
    }

    public static class AllTrustingSSLSocketFactory extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public AllTrustingSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

}
