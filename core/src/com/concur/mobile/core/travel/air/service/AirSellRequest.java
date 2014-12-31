/**
 * 
 */
package com.concur.mobile.core.travel.air.service;

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
import com.concur.mobile.core.travel.data.SellOptionField;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>PostServiceRequest</code> for booking a flight.
 */
public class AirSellRequest extends PostServiceRequest {

    private static final String CLS_TAG = AirSellRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Air/SellV2";

    public int ccId;

    public String fareId;

    public String programId;

    public boolean refundableOnly;

    public String tripName;

    public String violationCode;

    public String violationJustification;

    public List<TravelCustomField> fields;

    public List<SellOptionField> preSellOptionFields;

    public String cvvNumber; // TODO - encrypt?

    public boolean hasSellOptions;

    public boolean redeemTravelPoints;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<AirSellCriteria>");
        ViewUtil.addXmlElement(strBldr, "CcId", ccId);
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(strBldr, fields, false);
        }
        ViewUtil.addXmlElement(strBldr, "CvvNumber", cvvNumber);
        ViewUtil.addXmlElement(strBldr, "FareId", fareId);
        if (preSellOptionFields != null) {
            SellOptionField.serializePreSellOptionsToXMLForWire(strBldr, preSellOptionFields, "FlightOptionsSelected",
                    "FlightOption");
        }
        ViewUtil.addXmlElement(strBldr, "ProgramId", programId);
        ViewUtil.addXmlElementTF(strBldr, "RedeemTravelPoints", redeemTravelPoints);
        ViewUtil.addXmlElementTF(strBldr, "RefundableOnly", refundableOnly);
        ViewUtil.addXmlElement(strBldr, "TripName", tripName);
        ViewUtil.addXmlElement(strBldr, "ViolationCode", violationCode);
        ViewUtil.addXmlElement(strBldr, "ViolationJustification", violationJustification);
        strBldr.append("</AirSellCriteria>");
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
        return SERVICE_END_POINT;
    }

    @Override
    protected int getSoTimeout() {
        // MOB-15483 - increase the socket timeout
        if (hasSellOptions) {
            return 300000;
        } else {
            return super.getSoTimeout();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        AirSellReply reply = new AirSellReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = AirSellReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        }
        return reply;
    }

}
