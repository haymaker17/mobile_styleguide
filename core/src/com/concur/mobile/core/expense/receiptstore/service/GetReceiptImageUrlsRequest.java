/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> for the purposes of retrieving the list of URL for available receipts in the
 * receipt store.
 * 
 * @author AndrewK
 */
public class GetReceiptImageUrlsRequest extends GetServiceRequest {

    private static final String CLS_TAG = GetReceiptImageUrlsRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/Expense/GetReceiptImageUrlsV2";

    /**
     * Contains whether or not the MWS should filter out receipts already associated with mobile expenses, i.e., receipts
     * associated with mobile expenses that have not been added to a report.
     */
    public boolean filterMobileExpenses;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        if (filterMobileExpenses) {
            strBldr.append('Y');
        }
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GetReceiptImageUrlsReply reply = new GetReceiptImageUrlsReply();

        // Parse the response or log an error.
        int statusCode = response.getResponseCode();
        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = GetReceiptImageUrlsReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }

            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Log.e(Const.LOG_TAG, "GetReceiptImageUrlsRequest: StatusCode: " + statusCode + ", StatusLine: "
                        + response.getResponseMessage() + ", response: "
                        + ((reply.mwsErrorMessage != null) ? reply.mwsErrorMessage : "null") + ".");
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
