/**
 * 
 */
package com.concur.mobile.core.travel.service;

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
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> to retrieve an itinerary.
 * 
 * @author andy
 */
public class ItineraryRequest extends PostServiceRequest {

    private static final String CLS_TAG = ItineraryRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/SingleItinerary";
    public static final String SERVICE_END_POINT_V2 = "/Mobile/SingleItineraryV2";

    // optional, defaults to logged in user's company
    public String companyId;

    public String itinLocator;

    public boolean isForApprover;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<TripSpecifier>");

        ViewUtil.addXmlElement(strBldr, "CompanyId", companyId);

        if (isForApprover) {
            ViewUtil.addXmlElementTF(strBldr, "ForApprover", isForApprover);
            ViewUtil.addXmlElement(strBldr, "ItinLocator", itinLocator);
        } else {
            ViewUtil.addXmlElement(strBldr, "TripId", itinLocator);
        }

        if (isForApprover) {
            ViewUtil.addXmlElement(strBldr, "UserId", userId);
        }

        strBldr.append("</TripSpecifier>");

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
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        if (isForApprover) {
            return SERVICE_END_POINT_V2;
        }
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

        ItineraryReply reply = new ItineraryReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            List<Trip> trips = null;
            try {
                trips = Trip.parseItineraryXml(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            if (trips != null && trips.size() == 1) {
                reply.trip = trips.get(0);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".processResponse: no detailed itinerary in response!");
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;

    }

}
