/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

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
import com.concur.mobile.core.util.FormatUtil;

/**
 * An extension of <code>PostServiceRequest</code> for the purposes of cancelling a hotel booking.
 * 
 * @author AndrewK
 */
public class CancelHotelRequest extends PostServiceRequest {

    private static final String CLS_TAG = CancelHotelRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Hotel/Cancel";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    public static String buildRequestBody(String bookingSource, String reason, String recordLocator, String segmentKey,
            String tripId) {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<CancelCriteria>");
        addElement(strBldr, "BookingSource", ((bookingSource != null) ? FormatUtil.escapeForXML(bookingSource) : ""));
        addElement(strBldr, "Reason", ((reason != null) ? FormatUtil.escapeForXML(reason) : ""));
        addElement(strBldr, "RecordLocator", ((recordLocator != null) ? FormatUtil.escapeForXML(recordLocator) : ""));
        addElement(strBldr, "SegmentKey", ((segmentKey != null) ? FormatUtil.escapeForXML(segmentKey) : ""));
        addElement(strBldr, "TripId", ((tripId != null) ? FormatUtil.escapeForXML(tripId) : ""));
        strBldr.append("</CancelCriteria>");
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        // No-op.
        return null;
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
            entity = new StringEntity(requestBody, Const.HTTP_BODY_CHARACTER_ENCODING);
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
        ActionStatusServiceReply reply = new ActionStatusServiceReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = ActionStatusServiceReply.parseReply(responseXml);
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
