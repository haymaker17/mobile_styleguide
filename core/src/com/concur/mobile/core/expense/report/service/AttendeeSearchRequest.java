/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;

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
 * An extension of <code>PostServiceRequest</code> to search for attendees.
 * 
 * @author andy
 */
public class AttendeeSearchRequest extends PostServiceRequest {

    public static final String CLS_TAG = AttendeeSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/SearchAttendees";

    /**
     * Contains the list of strings containing attendee keys that should be excluded from the search results.
     */
    public List<String> excAtnKeys;

    /**
     * Contains the text query.
     */
    public String query;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        String body = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<AttendeeSearchCriteria>");
        strBldr.append("<ExcludeAtnKeys>");
        if (excAtnKeys != null) {
            boolean addedFirstAtnKey = false;
            for (String atnKey : excAtnKeys) {
                if (addedFirstAtnKey) {
                    strBldr.append(',');
                    strBldr.append(atnKey);
                } else {
                    strBldr.append(atnKey);
                    addedFirstAtnKey = true;
                }
            }
        }
        strBldr.append("</ExcludeAtnKeys>");
        strBldr.append("<Query>");
        if (query != null) {
            strBldr.append(query);
        }
        strBldr.append("</Query>");
        strBldr.append("</AttendeeSearchCriteria>");
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

        AttendeeSearchReply reply = new AttendeeSearchReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = AttendeeSearchReply.parseXMLReply(responseXml);
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
