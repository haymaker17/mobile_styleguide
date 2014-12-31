/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>GetServiceRequest</code> to record that an approver has viewed a report entry receipt.
 */
public class MarkEntryReceiptViewedRequest extends GetServiceRequest {

    public static final String CLS_TAG = MarkEntryReceiptViewedRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/MarkEntryReceiptAsViewed";

    /**
     * Contains the report entry key.
     */
    public String rpeKey;

    @Override
    protected String getServiceEndpointURI() {
        String endPoint = SERVICE_END_POINT;
        if (rpeKey != null && rpeKey.length() > 0) {
            StringBuilder strBldr = new StringBuilder(endPoint);
            strBldr.append('/');
            strBldr.append(rpeKey);
            endPoint = strBldr.toString();
        }
        return endPoint;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        ActionStatusServiceReply reply = new ActionStatusServiceReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = ActionStatusServiceReply.parseReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
