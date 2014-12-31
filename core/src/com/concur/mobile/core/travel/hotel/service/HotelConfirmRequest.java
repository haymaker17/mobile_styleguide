/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

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

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

/**
 * @author AndrewK
 * 
 */
public class HotelConfirmRequest extends PostServiceRequest {

    private static final String CLS_TAG = HotelConfirmRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Hotel/Reserve";

    public String bicCode;

    public String ccId;

    public String chainCode;

    public String propertyId;

    public String propertyName;

    public String sellSource;

    public String tripId;

    public String hotelReason;

    public String hotelReasonCode;

    public List<TravelCustomField> fields;

    public boolean redeemTravelPoints;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<SellCriteria>");
        addElement(strBldr, "BicCode", bicCode);
        addElement(strBldr, "CcId", ccId);
        addElement(strBldr, "ChainCode", chainCode);
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(strBldr, fields, false);
        }
        addElement(strBldr, "PropertyId", propertyId);
        addElement(strBldr, "PropertyName", FormatUtil.escapeForXML(propertyName));
        ViewUtil.addXmlElementTF(strBldr, "RedeemTravelPoints", redeemTravelPoints);
        if (sellSource != null) {
            addElement(strBldr, "SellSource", FormatUtil.escapeForXML(sellSource));
        } else {
            strBldr.append("<SellSource/>");
        }
        if (tripId != null) {
            addElement(strBldr, "TripId", tripId);
        }
        if (hotelReason != null) {
            addElement(strBldr, "ViolationCode", ((hotelReasonCode != null) ? FormatUtil.escapeForXML(hotelReasonCode)
                    : ""));
            addElement(strBldr, "ViolationJustification", FormatUtil.escapeForXML(hotelReason));
        }
        strBldr.append("</SellCriteria>");

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

    @Override
    protected int getSoTimeout() {
        if (Format.isDevServer(Preferences.getServerAddress())) {
            return 300000;
        } else {
            return 90000;
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
        HotelConfirmReply reply = new HotelConfirmReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = HotelConfirmReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            logError(response, HotelConfirmRequest.CLS_TAG + ".processResponse");
        }
        return reply;

    }

}
