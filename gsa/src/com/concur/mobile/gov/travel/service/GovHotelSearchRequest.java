/**
 * 
 */
package com.concur.mobile.gov.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>PostServiceRequest</code> for performing
 * a hotel search.
 * 
 * @author AndrewK
 */
public class GovHotelSearchRequest extends PostServiceRequest {

    private static final String CLS_TAG = GovHotelSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Hotel/Search2";

    public Calendar dateEnd;
    public Calendar dateStart;

    public String hotelChain;
    public String lat;
    public String lon;
    public String radius;
    public String scale;

    public Integer startIndex;
    public Integer count;

    public Double perDiemRate;

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
        String retVal = SERVICE_END_POINT;
        if (startIndex != null && count != null) {
            StringBuilder strBldr = new StringBuilder(retVal);
            strBldr.append('/');
            strBldr.append(startIndex);
            strBldr.append('/');
            strBldr.append(count);
            retVal = strBldr.toString();
        }
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
        HotelSearchReply reply = new HotelSearchReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = HotelSearchReply.parseXMLReply(responseXml);
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

    @Override
    protected String buildRequestBody() {
        String postBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<HotelCriteria>");
        addElement(strBldr, "DateEnd",
            Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, dateEnd));
        addElement(strBldr, "DateStart",
            Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, dateStart));
        if (hotelChain == null || hotelChain.length() == 0) {
            hotelChain = "";
        }
        addElement(strBldr, "Hotel", FormatUtil.escapeForXML(hotelChain));
        addElement(strBldr, "IncludeDepositRequired", "true");
        addElement(strBldr, "Lat", lat);
        addElement(strBldr, "Lon", lon);
        addElement(strBldr, "PerdiemRate", Double.toString(perDiemRate));
        addElement(strBldr, "Radius", radius);
        addElement(strBldr, "Scale", scale);
        addElement(strBldr, "Smoking", "0");
        strBldr.append("</HotelCriteria>");
        postBody = strBldr.toString();
        return postBody;
    }

}
