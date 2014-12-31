package com.concur.mobile.platform.test.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Ignore;

import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * A mock server for handling HTTP requests. Clients can set mock response data.
 * 
 * The design of this class is based on
 * http://olafsblog.sysbsb.de/lightweight-testing-of-webservice-http-clients-with-junit-and-jetty.
 */
@Ignore
public class MockMWSServer {

    private static final String CLS_TAG = "MockMWSServer";

    /**
     * Contains the address name upon which clients should connect.
     */
    public static final String ADDRESS = "localhost";

    /**
     * Contains the local port upon which clients should connect.
     */
    public static final int PORT = 50036;

    // Contains the reference to the Jetty server object.
    private Server server;

    // Contains the request body.
    private String requestBody;

    // Contains the response body.
    private String responseBody;

    // Contains the response stream.
    private InputStream responseStream;

    // Contains the response HTTP status code.
    private int responseHttpStatusCode = HttpStatus.SC_OK;

    // Contains the response headers.
    private Map<String, String> responseHeaders;

    /**
     * Constructs an instance of <code>MockMWSServer</code>.
     */
    public MockMWSServer() {
    }

    /**
     * Constructs an instance of <code>MockMWSServer</code> with a mock response.
     * 
     * @param mockData
     *            contains the mock response data.
     */
    public MockMWSServer(String mockData) {
        setResponseBody(mockData);
    }

    /**
     * Will start this instance of <code>MockMWSServer</code>.
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        configureServer();
        startServer();
    }

    // Starts the server.
    private void startServer() throws Exception {
        server.start();
    }

    /**
     * Configures this instance of <code>MWSMockServer</code>.
     */
    protected void configureServer() {
        server = new Server(new InetSocketAddress("localhost", PORT));
        server.setHandler(getMockHandler());
    }

    /**
     * Creates an {@link AbstractHandler handler} returning an arbitrary String as a response.
     * 
     * @return never <code>null</code>.
     */
    public Handler getMockHandler() {
        Handler handler = new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest servletRequest,
                    HttpServletResponse servletResponse) throws IOException, ServletException {
                setRequestBody(IOUtils.toString(baseRequest.getInputStream()));
                servletResponse.setStatus(getMockResponseHttpStatusCode());
                if (responseHeaders != null) {
                    for (String key : responseHeaders.keySet()) {
                        servletResponse.addHeader(key, responseHeaders.get(key));
                    }
                } else {
                    servletResponse.setContentType("text/xml");
                }
                if (servletResponse.getContentType().startsWith("image")) {
                    if (responseStream != null) {
                        ServletOutputStream servOutStr = servletResponse.getOutputStream();
                        BufferedInputStream bufIn = null;
                        try {
                            bufIn = new BufferedInputStream(responseStream);
                            byte[] buf = new byte[(16 * 1024)];
                            int bytesRead = -1;

                            while ((bytesRead = bufIn.read(buf)) != -1) {
                                servOutStr.write(buf, 0, bytesRead);
                            }
                            // Flush the servlet output stream.
                            servOutStr.flush();
                        } finally {
                            if (bufIn != null) {
                                try {
                                    bufIn.close();
                                } catch (IOException ioExc) {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".handle: I/O exception closing response stream",
                                            ioExc);
                                }
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".handle: content type starts with 'image' but no response stream!");
                    }
                } else {
                    if (getResponseBody() != null) {
                        servletResponse.getWriter().println(getResponseBody());
                    }
                }

                baseRequest.setHandled(true);
            }
        };
        return handler;
    }

    /**
     * Will stop the mock server from running.
     * 
     * @throws Exception
     *             throws an exception upon error.
     */
    public void stop() throws Exception {
        server.stop();
    }

    /**
     * Sets the current request body.
     * 
     * @param requestBody
     *            contains the request body.
     */
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * Gets the current request body.
     * 
     * @return returns the current request body.
     */
    public String getRequestBody() {
        return requestBody;
    }

    /**
     * Sets the response body.
     * 
     * @param responseBody
     *            contains the response body.
     */
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * Gets the response body.
     * 
     * @return returns the response body.
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Sets the response stream.
     * 
     * @param responseStream
     *            contains the response stream.
     */
    public void setResponseStream(InputStream responseStream) {
        this.responseStream = responseStream;
    }

    /**
     * Gets the response stream.
     * 
     * @return returns the response stream.
     */
    public InputStream getResponseStream() {
        return responseStream;
    }

    /**
     * Sets the response HTTP status code.
     * 
     * @param responseHttpStatusCode
     *            contains the response HTTP status code.
     */
    public void setResponseHttpStatusCode(int responseHttpStatusCode) {
        this.responseHttpStatusCode = responseHttpStatusCode;
    }

    /**
     * Gets the mock response HTTP status code.
     * 
     * @return returns the mock response HTTP status code.
     */
    public int getMockResponseHttpStatusCode() {
        return this.responseHttpStatusCode;
    }

    /**
     * Sets the response headers.
     * 
     * @param responseHeaders
     *            contains the response headers.
     */
    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Gets the response headers.
     * 
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Gets the instance of <code>Server</code> being used by this mock MWS server.
     * 
     * @return returns the instance of <code>Server</code> being used by this mock MWS server.
     * @see org.eclipse.jetty.server.Server
     */
    protected Server getServer() {
        return server;
    }
}
