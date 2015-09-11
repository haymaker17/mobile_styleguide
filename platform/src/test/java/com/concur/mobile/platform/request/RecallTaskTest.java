package com.concur.mobile.platform.request;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.Currency;
import java.util.Locale;

/**
 * Created by OlivierB on 14/08/2015.
 */
public class RecallTaskTest extends AsyncRequestTest {

    private RequestParser requestParser;
    private String requestIdForRecall;

    public RecallTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    public void doSampleSubmit() throws Exception {
        String requestId = null;
        final RequestDTO tr = new RequestDTO();
        tr.setName(SaveAndSubmitTaskTest.REQUEST_NAME_STRING);
        tr.setCurrencyCode(Currency.getInstance(Locale.US).getCurrencyCode());
        tr.setPolicyId(SaveAndSubmitTaskTest.REQUEST_POLICY_ID_STRING);
        tr.setPurpose(SaveAndSubmitTaskTest.REQUEST_PURPOSE_STRING);

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncRequestTest.AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1, resListener,
                ConnectHelper.Action.CREATE_AND_SUBMIT, null).setPostBody(RequestParser.toJson(tr))
                .addUrlParameter(RequestTask.P_REQUEST_DO_SUBMIT, Boolean.TRUE.toString())
                .addUrlParameter(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.TRUE.toString())
                .addResultData(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.TRUE.toString());

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            RequestDTO request = null;
            try {
                request = requestParser.parseSaveAndSubmitResponse(
                        result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", request);
                requestId = request.getId();
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }
        }
        requestIdForRecall = requestId;
    }

    /*@Test */
    public void doTest() throws Exception {

        // Set the mock response if the mock server is being used.
        if (useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, ""/*"request/SaveNoSubmitResponse.json"*/);
            requestIdForRecall = "ABCD";
        } else {
            doSampleSubmit();
        }

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1,
                resListener, ConnectHelper.Action.RECALL, requestIdForRecall)
                .addResultData(RequestTask.P_REQUEST_ID, requestIdForRecall);

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            Assert.assertTrue(result.resultCode == BaseAsyncRequestTask.RESULT_OK);
        }
    }
}
