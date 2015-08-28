package com.concur.mobile.platform.request;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.location.Location;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by OlivierB on 25/08/2015.
 */
public class RequestLocationTaskTest extends AsyncRequestTest {

    private static final String SEARCHED_TEXT = "ter";

    public void doTest() throws Exception {
        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "request/RequestLocationResponse.json");
        }

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1,
                resListener, ConnectHelper.ConnectVersion.VERSION_3_1,
                ConnectHelper.Module.REQUEST_LOCATION, ConnectHelper.Action.LIST, null)
                .addUrlParameter(RequestTask.P_LOCATION_SEARCH_TEXT,
                        URLEncoder.encode(SEARCHED_TEXT))
                .addUrlParameter(RequestTask.P_LOCATION_TYPE, Location.LocationType.AIRPORT.toString())
                .addUrlParameter(ConnectHelper.PARAM_LIMIT, "15");

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            // --- parse the configurations received
            List<Location> listLocation;
            try {
                listLocation = RequestParser
                        .parseLocations(result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", listLocation);
                Assert.assertTrue(listLocation.size() > 0);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }
        }
    }
}
