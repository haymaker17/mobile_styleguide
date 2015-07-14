package com.concur.mobile.platform.test.server;

/**
 * A mock server for handling HTTP requests. Clients can set mock response data.
 * 
 * The design of this class is based on
 * http://olafsblog.sysbsb.de/lightweight-testing-of-webservice-http-clients-with-junit-and-jetty.
 */
public class MockMWSServer extends MockServer {
    /**
     * Contains the local port upon which clients should connect.
     */
    public static final int PORT = 50036;

    @Override
    public int getPort() {
        return PORT;
    }
}