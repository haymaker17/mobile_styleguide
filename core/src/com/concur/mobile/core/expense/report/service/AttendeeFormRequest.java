/**
 * 
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

/**
 * An extension of <code>GetServiceRequest</code> for requesting an attendee editing form.
 * 
 * @author andy
 */
public class AttendeeFormRequest extends GetServiceRequest {

    private static final String CLS_TAG = AttendeeFormRequest.class.getSimpleName();

    private static final String SERVICE_END_POINT = "/mobile/Expense/AttendeeForm";

    /**
     * Contains the attendee type key.
     */
    public String atnTypeKey;

    /**
     * Contains the attendee key.
     */
    public String atnKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        String retVal = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(atnTypeKey);
        if (atnKey != null) {
            strBldr.append('/');
            strBldr.append(atnKey);
        }
        retVal = strBldr.toString();
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
        AttendeeFormReply reply = new AttendeeFormReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = AttendeeFormReply.parseXMLReply(responseXml);
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
