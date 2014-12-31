/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;

/**
 * A service request for posting a report approval.
 * 
 * @author AndrewK
 */
public class ApproveReportRequest extends PostServiceRequest {

    private static final String CLS_TAG = ApproveReportRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Expense/ApproveExpenseReport";

    public String reportKey;

    public ExpenseReport expRep;

    public String comment;

    public String statKey;

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
        strBldr.append(reportKey);
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
        ReportApproveRejectServiceReply reply = new ReportApproveRejectServiceReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = ReportApproveRejectServiceReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
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

    @Override
    public String buildRequestBody() {
        String requestBody = null;

        if (expRep != null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("<AdvanceWorkflow>");
            strBldr.append("<Comment>");
            if (comment != null && comment.length() > 0) {
                comment = FormatUtil.escapeForXML(comment);
                strBldr.append(comment);
            }
            strBldr.append("</Comment>");
            strBldr.append("<CurrentSequence>");
            strBldr.append(expRep.currentSequence);
            strBldr.append("</CurrentSequence>");
            strBldr.append("<ProcessInstanceKey>");
            strBldr.append(expRep.processInstanceKey);
            strBldr.append("</ProcessInstanceKey>");
            strBldr.append("<RoleCode>");
            strBldr.append("MANAGER");
            strBldr.append("</RoleCode>");
            ViewUtil.addXmlElement(strBldr, "StatKey", statKey);
            strBldr.append("</AdvanceWorkflow>");
            requestBody = strBldr.toString();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildApproveRejectReportXML: can't find expense report!");
        }
        return requestBody;
    }

}
