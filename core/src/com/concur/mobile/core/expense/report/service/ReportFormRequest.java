/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> for getting the list of form fields for a report.
 * 
 * @author Chris N. Diaz
 * 
 */
public class ReportFormRequest extends GetServiceRequest {

    private static final String CLS_TAG = ReportFormRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/ReportForm";

    public final String reportKey = "-1"; // Always -1 to indicate a new report.
    public String policyKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        String retVal = SERVICE_END_POINT;
        if (policyKey != null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(SERVICE_END_POINT);
            strBldr.append('/');
            strBldr.append(policyKey);
            retVal = strBldr.toString();
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        ReportFormReply reply = new ReportFormReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            // Manually setting the status because the XML returned is just
            // a ReportDetail with its FormFields. Gotta love inconsistent responses...
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
