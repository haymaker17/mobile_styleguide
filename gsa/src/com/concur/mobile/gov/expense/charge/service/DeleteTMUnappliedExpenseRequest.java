/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.expense.charge.service;

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
import com.concur.mobile.core.util.ViewUtil;

/**
 * Extension of <code>PostServiceRequest</code> for invoking
 * the DeleteTMUnappliedExpense MWS endpoint.
 * 
 * @author Chris N. Diaz
 * 
 */
public class DeleteTMUnappliedExpenseRequest extends PostServiceRequest {

    private static final String CLS_TAG = DeleteTMUnappliedExpenseRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/DeleteTMUnappliedExpense";

    public String ccExpId;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.service.PostServiceRequest#getPostEntity(com.concur.core.service.ConcurService)
     */
    @Override
    protected HttpEntity getPostEntity(ConcurService concurService) throws ServiceRequestException {
        HttpEntity entity = null;
        try {
            final String buildRequestBody = buildRequestBody();
            entity = new StringEntity(buildRequestBody, Const.HTTP_BODY_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", uee);
            throw new ServiceRequestException(uee.getMessage());
        }
        return entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {

        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<DeleteTMUnappliedExpenseRequest>");

        ViewUtil.addXmlElement(strBldr, "ccExpId", ccExpId);

        strBldr.append("</DeleteTMUnappliedExpenseRequest>");
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
            reply = ActionStatusServiceReply.parseReply(xmlReply);
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

                Log.e(Const.LOG_TAG, "DeleteReceiptImageRequest: StatusCode: "
                    + statusCode
                    + ", StatusLine: " + response.getResponseMessage() + ", response: "
                    + ((reply.mwsErrorMessage != null) ? reply.mwsErrorMessage : "null") + ".");

            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
