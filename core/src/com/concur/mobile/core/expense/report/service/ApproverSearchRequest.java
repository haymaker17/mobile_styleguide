/**
 * Copyright (c) 2011 Concur Technologies, Inc.
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
import android.util.Xml;
import android.util.Xml.Encoding;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>PostServiceRequest</code> to search for approvers.
 * 
 * @author Chris N. Diaz
 */
public class ApproverSearchRequest extends PostServiceRequest {

    public static final String CLS_TAG = ApproverSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/SearchApproversV2";

    // The following are the valid search field names.
    public static final String FIELD_LAST_NAME = "LAST_NAME";
    public static final String FIELD_FIRST_NAME = "FIRST_NAME";
    public static final String FIELD_EMAIL_ADDRESS = "EMAIL_ADDRESS";
    public static final String FIELD_LOGIN_ID = "LOGIN_ID";

    /**
     * The search field name - i.e. one of LAST_NAME, FIRST_NAME, EMAIL_ADDRESS, or LOGIN_ID
     */
    public String fieldName;

    /**
     * Contains the text query.
     */
    public String query;

    /**
     * Contains the report key.
     */
    public String rptKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        String body = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<ApproverSearchCriteria>");

        // Add the search field name.
        strBldr.append("<FieldName>").append(fieldName).append("</FieldName>");

        // Add the search query.
        strBldr.append("<Query>");
        if (query != null && query.trim().length() > 0) {
            strBldr.append(query.trim());
        } else {
            // Search for all approvers.
            strBldr.append("*");
        }
        strBldr.append("</Query>");

        // Add the report key.
        strBldr.append("<RptKey>").append(rptKey).append("</RptKey>");

        strBldr.append("</ApproverSearchCriteria>");
        body = strBldr.toString();
        return body;
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

        ApproverSearchReply reply = new ApproverSearchReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {

            InputStream is = new BufferedInputStream(response.getInputStream());
            Encoding encoding = null;
            try {
                String encodingHeader = response.getContentEncoding();
                encoding = (encodingHeader != null) ? Encoding.valueOf(encodingHeader) : Xml.Encoding.UTF_8;
            } catch (Exception e) {
                // Couldn't parse the encoding in the header, so just default to UTF-8;
                encoding = Xml.Encoding.UTF_8;
            }
            try {
                reply = ApproverSearchReply.parseXMLReply(is, encoding);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.query = query;

        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
