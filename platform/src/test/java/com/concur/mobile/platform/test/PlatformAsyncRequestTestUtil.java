/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import com.concur.mobile.platform.test.server.MockServer;
import com.concur.platform.PlatformProperties;

public abstract class PlatformAsyncRequestTestUtil extends PlatformTestSuite {

    // Contains whether or not the mock server has been initialized.
    private boolean mockServerInitialized = Boolean.FALSE;

    // Contains a reference to the mock MWS server.
    protected MockServer server;

    protected abstract boolean useMockServer();

    /**
     * Will initialize the mock server.
     */
    protected void initMockServer(MockServer mockServer) throws Exception {
        // Short-circuit of the platform has already been inited.
        if (mockServerInitialized) {
            return;
        } else {
            mockServerInitialized = true;
        }

        // Initialize the mock MWS server.
        server = mockServer;
        server.start();
    }

    /**
     * Will initialize the config and travel and expense content provider authority.
     */
    protected static void initContentProvidersAuthority() {
        //set up authority for content providers
        PlatformProperties.setConfigProviderAuthority("com.concur.platform.provider.config");
        PlatformProperties.setTravelProviderAuthority("com.concur.platform.provider.travel");
        PlatformProperties.setExpenseProviderAuthority("com.concur.platform.provider.expense");
    }
}
