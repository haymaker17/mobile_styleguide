/**
 * 
 */
package com.concur.mobile.gov.expense.service;

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
import com.concur.mobile.core.util.FormatUtil;

/**
 * An extension of <code>PostServiceRequest</code> to send a search list request to the server.
 * 
 * @author AndrewK
 */
public class GovSearchListRequest extends PostServiceRequest {

    private static final String CLS_TAG = GovSearchListRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/GovTravelManager/SearchExpenseListField";

    public String query;

    /**
     * Contains the field id.
     */
    public String fieldId;

    /**
     * Contains the doc type.
     */
    public String docType;

    /**
     * Contains expense description.
     */
    public String expenseDescription;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<SearchExpenseListFieldRequest>");
        addElement(strBldr, "docType", ((docType != null) ? docType : ""));
        if (expenseDescription != null) {
            addElement(strBldr, "expenseDescription", expenseDescription);
        }
        addElement(strBldr, "fieldId", ((fieldId != null) ? fieldId : ""));

        addElement(strBldr, "query", ((query != null) ? FormatUtil.escapeForXML(query) : ""));
        strBldr.append("</SearchExpenseListFieldRequest>");
        return strBldr.toString();
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
        } catch (UnsupportedEncodingException uee) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", uee);
            throw new ServiceRequestException(uee.getMessage());
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

        GovSearchListResponse reply = new GovSearchListResponse();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            reply.parse(is, null);
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
