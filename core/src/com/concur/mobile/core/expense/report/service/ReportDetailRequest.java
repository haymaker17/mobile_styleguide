/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>MessageParams</code> for specifying a report key.
 * 
 * @author AndrewK
 */
public class ReportDetailRequest extends GetServiceRequest {

    /**
     * Models the version of the request.
     */
    public static enum Version {
        V2, // Version 2 retrieve a full detailed report object.
        V3
        // Version 3 retrieves a detailed header, but summary objects.
        // This version is used with larger reports.
    };

    public static final String CLS_TAG = ReportDetailRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/GetReportDetail";

    // The report key.
    public String reportKey;

    // The report source.
    public int reportSourceKey;

    // Contains the version of the request. {default: V2}.
    public Version version = Version.V2;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append(version.name());
        strBldr.append('/');
        strBldr.append(reportKey);
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
        ReportDetailReply reply = new ReportDetailReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }

            try {
                reply.report = ExpenseReportDetail.parseReportDetailXml(getReader(is, encoding),
                        (version == Version.V3));
            } catch (RuntimeException re) {
                // Log it. In this case we'll be using the new'd RDR which will have a blank
                // report member which will cause the service handler code to fall through without
                // doing anything.
                Log.e(Const.LOG_TAG, CLS_TAG + ".processResponse: runtime exception during parse", re);
            } finally {
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            }
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
