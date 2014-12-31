/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import android.content.Intent;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.CountSummary;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

/**
 * A get request used to retrieve count summary information.
 * 
 * @author AndrewK
 */
public class CountSummaryRequest extends GetServiceRequest {

    private static final String CLS_TAG = CountSummaryRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Home/GetCountSummary";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected void handleRequest(ConcurService concurService, int networkActivityType) {
        try {
            // Broadcast the start network activity message.
            concurService.handler.broadcastStartNetworkActivity(networkActivityType,
                    concurService.getText(R.string.retrieve_summary_count).toString());

            Calendar lastRetrievedTS = null;
            Intent intent = new Intent(Const.ACTION_SUMMARY_UPDATED);
            try {

                CountSummaryReply reply = (CountSummaryReply) process(concurService);
                intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_OKAY);
                intent.putExtra(Const.REPLY_HTTP_STATUS_CODE, reply.httpStatusCode);
                intent.putExtra(Const.REPLY_HTTP_STATUS_TEXT, reply.httpStatusText);
                if (reply.httpStatusCode == HttpStatus.SC_OK) {
                    intent.putExtra(Const.REPLY_STATUS, reply.mwsStatus);
                    if (reply.mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                        lastRetrievedTS = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        // Save the response.
                        concurService.db.saveResponse(networkActivityType, lastRetrievedTS, reply.xmlReply, userId);
                        // Set the trip information on the app object.
                        ConcurCore app = (ConcurCore) concurService.getApplication();
                        app.setSummary(reply.countSummary);
                        app.setSummaryLastRetrieved(lastRetrievedTS);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleRequest(CountSummaryRequest): MWS status("
                                + reply.mwsStatus + ") - " + reply.mwsErrorMessage + ".");
                        intent.putExtra(Const.REPLY_ERROR_MESSAGE, reply.mwsErrorMessage);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleRequest(CountSummaryRequest): HTTP status("
                            + reply.httpStatusCode + ") - " + reply.httpStatusText + ".");
                }
            } catch (ServiceRequestException srvReqExc) {
                intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST);
                intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, srvReqExc.getMessage());
            } catch (IOException ioExc) {
                intent.putExtra(Const.SERVICE_REQUEST_STATUS, Const.SERVICE_REQUEST_STATUS_IO_ERROR);
                intent.putExtra(Const.SERVICE_REQUEST_STATUS_TEXT, ioExc.getMessage());
            }

            // Send broadcast
            concurService.sendBroadcast(intent);
        } finally {
            // Broadcast the stop network activity message.
            concurService.handler.broadcastStopNetworkActivity(networkActivityType);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        CountSummaryReply reply = new CountSummaryReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            try {
                reply.countSummary = CountSummary.parseSummaryXml(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;

    }

}
