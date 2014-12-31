/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

/**
 * @author AndrewK
 */
public class RemoveReportExpenseRequest extends PostServiceRequest {

    private static final String CLS_TAG = RemoveReportExpenseRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/DeleteReportEntriesV2";

    public String reportKey;

    public ArrayList<String> expenseEntryKeys;

    @Override
    public String buildRequestBody() {
        String requestBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<a:ArrayOfstring xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">");
        Iterator<String> keyIter = expenseEntryKeys.iterator();
        while (keyIter.hasNext()) {
            strBldr.append("<a:string>");
            strBldr.append(keyIter.next());
            strBldr.append("</a:string>");
        }
        strBldr.append("</a:ArrayOfstring>");
        requestBody = strBldr.toString();
        return requestBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#getPostEntity(com.concur.mobile.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            entity = new StringEntity(buildRequestBody(), Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException unSupEncExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", unSupEncExc);
            throw new ServiceRequestException(unSupEncExc.getMessage());
        }
        return entity;
    }

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
        RemoveReportExpenseReply reply = new RemoveReportExpenseReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = RemoveReportExpenseReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            // 6/17/2010 4:52PM - Temporary logic due to a bug in MWS 'DeleteExpenseEntries' request as upon
            // success, the 'Status' XML element is not being set. So, we'll override the value
            // of 'Status' based on whether the 'ErrorMessage' contains a value.
            if (reply.mwsErrorMessage != null && reply.mwsErrorMessage.length() > 0) {
                reply.mwsStatus = Const.REPLY_STATUS_FAILURE;
            } else {
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            }
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
