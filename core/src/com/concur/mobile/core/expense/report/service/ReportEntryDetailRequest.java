/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> to retrieve report entry details.
 * 
 * @author andy
 */
public class ReportEntryDetailRequest extends GetServiceRequest {

    private static final String CLS_TAG = ReportEntryDetailRequest.class.getSimpleName();

    private static final String SERVICE_END_POINT = "/mobile/Expense/GetReportEntryDetailV4";

    /**
     * Contains the report key.
     */
    public String reportKey;

    /**
     * Contains the report entry key.
     */
    public String reportEntryKey;

    /**
     * Contains the report source key, i.e., active/approvals.
     */
    public int reportSourceKey;

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
        strBldr.append(reportEntryKey);
        switch (reportSourceKey) {
        case Const.EXPENSE_REPORT_SOURCE_ACTIVE:
            strBldr.append("/MOBILE_EXPENSE_TRAVELER");
            break;
        case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL:
            strBldr.append("/MANAGER");
            break;
        }
        String serverURI = strBldr.toString();
        return serverURI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ReportEntryDetailReply reply = new ReportEntryDetailReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            try {
                reply.expRepEntDet = ExpenseReportEntryDetail.parseReportEntryDetailXml(getReader(is, encoding),
                        response.getContentLength());
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
