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

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> to support form-field driven extended search request.
 * 
 * @author andy
 */
public class ExtendedAttendeeSearchRequest extends PostServiceRequest {

    private static final String CLS_TAG = ExtendedAttendeeSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Expense/SearchAttendeesExtendedV2";

    /**
     * Contains the attendee type key.
     */
    public String atnTypeKey;

    /**
     * Contains the list of form fields to search on.
     */
    public List<ExpenseReportFormField> formFields;

    /**
     * Contains the list of strings containing attendee keys that should be excluded from the search results.
     */
    public List<String> excAtnKeys;

    /**
     * Contains the expense key.
     */
    public String expKey;

    /**
     * Contains the report policy key.
     */
    public String rptPolKey;

    /**
     * Contains the report entry key.
     */
    public String rptEntKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        String requestBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<AttendeeExtendedSearchCriteria xmlns=\"\">");
        ViewUtil.addXmlElement(strBldr, "AtnTypeKey", atnTypeKey);
        ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler.serializeFieldsToXML(strBldr, formFields, true);
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
        ViewUtil.addXmlElement(strBldr, "ExpKey", expKey);
        ViewUtil.addXmlElement(strBldr, "PolKey", rptPolKey);
        ViewUtil.addXmlElement(strBldr, "RpeKey", rptEntKey);
        strBldr.append("</AttendeeExtendedSearchCriteria>");
        requestBody = strBldr.toString();
        return requestBody;
    }

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
        } catch (UnsupportedEncodingException uee) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getPostEntity: unsupported encoding exception!", uee);
            throw new ServiceRequestException(uee.getMessage());
        }
        return entity;
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
        ExtendedAttendeeSearchReply reply = new ExtendedAttendeeSearchReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = ExtendedAttendeeSearchReply.parseXMLReply(responseXml);
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

}
