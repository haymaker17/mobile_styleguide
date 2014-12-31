/**
 * 
 */
package com.concur.mobile.core.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.ReasonCode;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> for the purpose of retrieving both standard + custom reason codes.
 */
public class ReasonCodeRequest extends GetServiceRequest {

    private static final String CLS_TAG = ReasonCodeRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Config/GetReasonCodes";

    /**
     * Contains a one-letter filter code used
     */
    public String filterCode;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        String retVal = SERVICE_END_POINT;
        if (filterCode != null && filterCode.length() > 0) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(SERVICE_END_POINT);
            strBldr.append('/');
            strBldr.append(filterCode);
            retVal = strBldr.toString();
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.core.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        ReasonCodeReply reply = new ReasonCodeReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            try {
                reply.reasonCodes = ReasonCode.parseReasonCodes(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;

    }

}
