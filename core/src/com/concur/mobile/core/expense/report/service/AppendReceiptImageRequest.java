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

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>PostServiceRequest</code> for the purposes of appending a receipt to an existing report entry line item
 * receipt.
 * 
 * @author andy
 */
public class AppendReceiptImageRequest extends PostServiceRequest {

    public static final String CLS_TAG = AppendReceiptImageRequest.class.getSimpleName();

    static final String SERVICE_END_POINT = "/mobile/Expense/AppendReceipt";

    /**
     * Contains the receipt image ID of the image to be appended.
     */
    public String fromReceiptImageId;

    /**
     * Contains the receipt image ID of the image being appended to.
     */
    public String toReceiptImageId;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.PostServiceRequest#getPostEntity(com.concur.mobile.core.service.ConcurService)
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
     * @see com.concur.mobile.core.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<AppendReceiptAction>");
        addElement(strBldr, "FromReceiptImageId", fromReceiptImageId);
        addElement(strBldr, "ToReceiptImageId", toReceiptImageId);
        strBldr.append("</AppendReceiptAction>");
        String body = strBldr.toString();
        return body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.core.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ActionStatusServiceReply reply = new ActionStatusServiceReply();

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
                reply = ActionStatusServiceReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Log.e(Const.LOG_TAG, "AppendReceiptImageRequest: StatusCode: " + statusCode + ", StatusLine: "
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
