/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

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
 * An extension of <code>PostServiceRequest</code> used to save a report entry receipt.
 * 
 * @author AndrewK
 */
public class SaveReportEntryReceiptRequest extends PostServiceRequest {

    public static String CLS_TAG = SaveReportEntryReceiptRequest.class.getSimpleName();

    /**
     * Contains the report key.
     */
    public String reportKey;

    /**
     * Contains the report entry key.
     */
    public String reportEntryKey;

    /**
     * Contains the receipt image id.
     */
    public String receiptImageId;

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/Mobile/Expense/SaveReportEntryV4";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        if (requestBody == null) {
            buildRequestBody(this);
        }
        return requestBody;
    }

    /**
     * Builds the request body for <code>request</code>.
     * 
     * @param request
     *            the request.
     */
    static void buildRequestBody(SaveReportEntryReceiptRequest request) {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<ReportEntry xmlns=\"http://schemas.datacontract.org/2004/07/Snowbird\" ");
        strBldr.append("xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">");
        addElement(strBldr, "ReceiptImageId", request.receiptImageId);
        addElement(strBldr, "RpeKey", request.reportEntryKey);
        addElement(strBldr, "RptKey", request.reportKey);
        strBldr.append("</ReportEntry>");
        request.requestBody = strBldr.toString();
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
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(Const.MOBILE_EXPENSE_USER);
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

        SaveReportEntryReceiptReply reply = new SaveReportEntryReceiptReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = SaveReportEntryReceiptReply.parseReply(xmlReply);
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
