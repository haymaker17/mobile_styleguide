package com.concur.mobile.platform.request;

/**
 * Created by OlivierB on 07/08/2015.
 */

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestExceptionDTO;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.Currency;
import java.util.Locale;

public class SaveAndSubmitTaskTest extends AsyncRequestTest {

    private RequestDTO tr;

    protected static final String REQUEST_NAME_STRING = "Trip in March";
    protected static final String REQUEST_POLICY_ID_STRING = "gWohOcl7WcxM34o3LnfEe$s2lWjryBP$s5zWQ";
    protected static final String REQUEST_PURPOSE_STRING = "March trip test";

    public SaveAndSubmitTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    private void initCreate(boolean applyPurpose) {
        tr = null;
        // --- TR initialization
        tr = new RequestDTO();
        tr.setName(REQUEST_NAME_STRING);
        tr.setCurrencyCode(Currency.getInstance(Locale.US).getCurrencyCode());
        tr.setPolicyId(REQUEST_POLICY_ID_STRING);
        if (applyPurpose) {
            // --- no purpose = Blocking error
            // --- note: you should create a rule related to purpose content on your VM to test
            // Non-blocking E when mocks are disabled.
            tr.setPurpose(REQUEST_PURPOSE_STRING);
        }
    }

    /**
     * Execute a Save test without any submit action chained
     *
     * @throws Exception
     */
    private void doSaveTest() throws Exception {
        // Set the mock response if the mock server is being used.
        if (useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "request/SaveNoSubmitResponse.json");
        }

        // --- Creates the tr object to use in this test
        initCreate(true);

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1, resListener, tr.getId() != null ?
                ConnectHelper.Action.UPDATE_AND_SUBMIT :
                ConnectHelper.Action.CREATE_AND_SUBMIT, tr.getId()).setPostBody(RequestParser.toJson(tr))
                .addUrlParameter(RequestTask.P_REQUEST_DO_SUBMIT, Boolean.FALSE.toString())
                .addUrlParameter(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.FALSE.toString())
                .addResultData(RequestTask.P_REQUEST_FORCE_SUBMIT, Boolean.FALSE.toString());

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            // --- parse the configurations received
            RequestDTO request = null;
            try {
                request = RequestParser.parseSaveAndSubmitResponse(
                        result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", request);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }

            Assert.assertEquals(RequestDTO.ApprovalStatus.CREATION, request.getApprovalStatus());
            Assert.assertEquals(REQUEST_NAME_STRING, request.getName());
            Assert.assertEquals(REQUEST_POLICY_ID_STRING, request.getPolicyId());
            Assert.assertEquals(REQUEST_PURPOSE_STRING, request.getPurpose());
        }
    }


    /**
     * Execute a Save And Submit request which must return a blocking exception.
     * Notes while mocks are disabled:
     * - Make sure that Purpose is required in your form on VM side if you're not using mocks.
     *
     * @param blockingException whether the exception thrown should be blocking or not
     * @param forceIfRequired
     * @throws Exception
     */
    private void doSaveAndSubmitWithExceptionTest(boolean blockingException, Boolean forceIfRequired) throws Exception {
        if (forceIfRequired == null) {
            forceIfRequired = false;
        }
        // Set the mock response if the mock server is being used.
        if (useMockServer()) {
            // Set the mock response for the test.
            if (blockingException) {
                setMockResponse(mockServer, HttpStatus.SC_OK, "request/SaveAndSubmitBExceptionResponse.json");
            } else if (!forceIfRequired) {
                setMockResponse(mockServer, HttpStatus.SC_OK, "request/SaveAndSubmitNBExceptionResponse.json");
            } else {
                setMockResponse(mockServer, HttpStatus.SC_OK, "request/SaveAndSubmitResponse.json");
            }
        }

        // --- Creates the tr object to use in this test
        initCreate(!blockingException);

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1, resListener, tr.getId() != null ?
                ConnectHelper.Action.UPDATE_AND_SUBMIT :
                ConnectHelper.Action.CREATE_AND_SUBMIT, tr.getId()).setPostBody(RequestParser.toJson(tr))
                .addUrlParameter(RequestTask.P_REQUEST_DO_SUBMIT, Boolean.TRUE.toString())
                .addUrlParameter(RequestTask.P_REQUEST_FORCE_SUBMIT, forceIfRequired.toString())
                .addResultData(RequestTask.P_REQUEST_FORCE_SUBMIT, forceIfRequired.toString());

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            // --- parse the configurations received
            RequestDTO request = null;
            try {
                request = RequestParser.parseSaveAndSubmitResponse(
                        result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", request);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }

            Assert.assertEquals(REQUEST_NAME_STRING, request.getName());
            Assert.assertEquals(REQUEST_POLICY_ID_STRING, request.getPolicyId());
            // --- Check exception related properties
            if (blockingException) {
                // --- Enabling forceSubmit should NOT change the result: you can't force a submit
                // if there's a BLOCKING error.
                Assert.assertEquals(RequestDTO.ApprovalStatus.CREATION, request.getApprovalStatus());
                Assert.assertEquals("", request.getPurpose());
                Assert.assertEquals(RequestExceptionDTO.ExceptionLevel.BLOCKING, request.getHighestExceptionLevel());
                Assert.assertTrue(request.getExceptions().size() > 0);
            } else {
                Assert.assertEquals(REQUEST_PURPOSE_STRING, request.getPurpose());
                Assert.assertEquals(RequestExceptionDTO.ExceptionLevel.NON_BLOCKING, request.getHighestExceptionLevel());
                Assert.assertTrue(request.getExceptions().size() > 0);
                if (forceIfRequired) {
                    Assert.assertEquals(RequestDTO.ApprovalStatus.PENDING_VALIDATION, request.getApprovalStatus());
                } else {
                    Assert.assertEquals(RequestDTO.ApprovalStatus.CREATION, request.getApprovalStatus());
                }
            }
        }
    }

    public void doTest() throws Exception {
        doSaveTest();
        // blocking - not forced
        doSaveAndSubmitWithExceptionTest(true, false);
        // blocking - forced
        doSaveAndSubmitWithExceptionTest(true, true);
        // non-blocking - not forced
        doSaveAndSubmitWithExceptionTest(false, false);
        // non-blocking - forced
        // NOTE: This works the same as if we had no exception
        doSaveAndSubmitWithExceptionTest(false, true);
    }

}
