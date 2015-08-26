package com.concur.mobile.platform.request;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.dto.RequestSegmentDTO;
import com.concur.mobile.platform.request.groupConfiguration.SegmentType;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.request.util.RequestStatus;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by OlivierB on 25/08/2015.
 * <p/>
 * Note: this requires an existing active Request with at least one entry on your VM if you disable
 * mocks.
 */
public class RequestDetailTaskTest extends AsyncRequestTest {

    private static final String MOCK_REQUEST_ID = "3334";
    protected static final String MOCK_REQUEST_NAME = "ščřžýáíĐëŸč뜱뛯ｸウあ";
    protected static final String MOCK_REQUEST_POLICY_ID = "gWohOcl7WcxM34o3LnfEe$s2lWjryBP$s5zWQ";
    protected static final String MOCK_REQUEST_PURPOSE = "あああ";
    protected static final String MOCK_ENTRY_ID = "gWlb7X$sbxthzYAq7t8OqPeqb3nUM$pObtyMg";
    protected static final String MOCK_TO_LOC_ID = "gWoH$sYdLcIWJ8htEYF$sOCLa1d4oqWAx35lw";
    protected static final String MOCK_FROM_LOC_ID = "gWkf$s2FZT$pVA8fw6oAc76Ij4lNWYPwIT9Cg";

    private RequestDTO initMockedTR() {
        final RequestDTO tr = new RequestDTO();
        tr.setId(MOCK_REQUEST_ID);
        tr.setName(MOCK_REQUEST_NAME);
        tr.setCurrencyCode(Currency.getInstance(Locale.US).getCurrencyCode());
        tr.setPolicyId(MOCK_REQUEST_POLICY_ID);
        tr.setPurpose(MOCK_REQUEST_PURPOSE);
        tr.setEntriesMap(new HashMap<String, RequestEntryDTO>());

        final RequestEntryDTO entryDTO = new RequestEntryDTO();
        entryDTO.setId(MOCK_ENTRY_ID);
        entryDTO.setListSegment(new ArrayList<RequestSegmentDTO>());
        entryDTO.setSegmentTypeCode(SegmentType.RequestSegmentType.AIR.getCode());
        entryDTO.setTripType(RequestEntryDTO.TripType.ONE_WAY);

        final RequestSegmentDTO segmentDTO = new RequestSegmentDTO();
        final Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        segmentDTO.setDepartureDate(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 7);
        segmentDTO.setArrivalDate(cal.getTime());
        segmentDTO.setFromLocationId(MOCK_FROM_LOC_ID);
        segmentDTO.setToLocationId(MOCK_TO_LOC_ID);
        entryDTO.getListSegment().add(segmentDTO);

        tr.getEntriesMap().put(entryDTO.getId(), entryDTO);

        return tr;
    }

    public void doTest() throws Exception {

        final RequestDTO requestDTO;

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "request/DetailResponse.json");
            requestDTO = initMockedTR();
        } else {
            // --- creates the request on the VM and retrieve the ID
            requestDTO = getTR();
            // --- if no TR was retrieved, then exit the test (=> no existing active request with an entry on VM !!)
            if (requestDTO == null) {
                return;
            }
        }

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(), 1,
                resListener, ConnectHelper.Action.DETAIL, requestDTO.getId());

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            try {
                RequestParser
                        .parseTRDetailResponse(requestDTO, result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("EntriesMap is empty", requestDTO.getEntriesMap());
                Assert.assertTrue(requestDTO.getEntriesMap().size() > 0);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }
        }
    }

    private RequestDTO getTR() {
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
                listRequests = RequestParser
                        .parseTRListResponse(result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                if (listRequests.size() > 0) {
                    for (RequestDTO tr : listRequests) {
                        if (tr.getEntriesMap() != null && tr.getEntriesMap().size() > 0) {
                            return tr;
                        }
                    }
                }
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }
        }
        return null;
    }
}
