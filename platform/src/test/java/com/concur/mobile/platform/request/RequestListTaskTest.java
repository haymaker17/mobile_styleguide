package com.concur.mobile.platform.request;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.request.util.RequestStatus;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by OlivierB on 25/08/2015.
 */
public class RequestListTaskTest
        extends AsyncRequestTest {

    private RequestParser requestParser;
    private RequestListCache requestListCache;

    public RequestListTaskTest(RequestParser requestParser) {
        this.requestParser = requestParser;
    }

    /**
     * Execute a Request List retrieving test
     *
     * @throws Exception
     */
    public void doTest() throws Exception {

        // setup
        if (requestListCache == null) {
            requestListCache = new RequestListCache();
        }

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "request/ListResponse.json");
        }

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1,
                resListener, ConnectHelper.Action.LIST, null)
                .addUrlParameter(RequestTask.P_REQUESTS_STATUS, RequestStatus.PENDING_EBOOKING.toString())
                .addUrlParameter(RequestTask.P_REQUESTS_WITH_SEG_TYPES, Boolean.TRUE.toString())
                .addUrlParameter(RequestTask.P_REQUESTS_WITH_USER_PERMISSIONS, Boolean.TRUE.toString())
                .addUrlParameter(ConnectHelper.PARAM_LIMIT, "100");

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            // --- parse the configurations received
            List<RequestDTO> listRequests;
            try {
                listRequests = requestParser
                        .parseTRListResponse(result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", listRequests);
                Assert.assertTrue(listRequests.size() > 0);
                fillCacheTest(listRequests);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }
        }
    }

    /**
     * Same behavior as what's done on Application class
     * TODO: create a dedicated method somewhere suited and call it instead.
     *
     * @param listRequests
     */
    private void fillCacheTest(List<RequestDTO> listRequests) {
        final Set<String> headerFormIds = new HashSet<>();
        if (requestListCache.hasCachedValues()) {
            requestListCache.clear();
        }
        for (RequestDTO trDTO : listRequests) {
            // --- cache refresh
            requestListCache.addValue(trDTO);
            // --- hashset will handle duplicates
            headerFormIds.add(trDTO.getHeaderFormId());
        }
    }
}
