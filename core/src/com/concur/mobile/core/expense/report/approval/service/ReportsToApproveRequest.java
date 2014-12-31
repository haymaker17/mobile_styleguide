/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> for the purposes of retrieving a set of reports to approve.
 * 
 * @author AndrewK
 */
public class ReportsToApproveRequest extends GetServiceRequest {

    public static final String CLS_TAG = ReportsToApproveRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/GetReportsToApprove";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ReportsToApproveReply reply = new ReportsToApproveReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }

            reply.xmlReply = readStream(is, encoding);
            try {
                reply.reports = ExpenseReport.parseReportsXml(reply.xmlReply);
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
