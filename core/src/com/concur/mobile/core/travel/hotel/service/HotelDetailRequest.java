/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>GetServiceRequest</code> for the purposes of retrieving detailed hotel information.
 * 
 * @author AndrewK
 */
public class HotelDetailRequest extends GetServiceRequest {

    private static final String CLS_TAG = HotelDetailRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Hotel/Details";

    public String propertyId;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(URLEncoder.encode(propertyId));
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

        HotelDetailReply reply = new HotelDetailReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            try {
                reply = HotelDetailReply.parseXmlReply(getReader(is, encoding));
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
