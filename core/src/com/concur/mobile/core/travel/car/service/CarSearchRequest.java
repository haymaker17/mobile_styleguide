package com.concur.mobile.core.travel.car.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

public class CarSearchRequest extends PostServiceRequest {

    private static final String CLS_TAG = CarSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Car/GetCarList";

    public String pickupLat;
    public String pickupLong;
    public Calendar pickupDateTime;
    public String dropoffLat;
    public String dropoffLong;
    public Calendar dropoffDateTime;
    public String carType;
    public boolean offAirport;
    public String pickupIata;
    public String dropoffIata;

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

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        CarSearchReply reply = new CarSearchReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            try {
                reply = CarSearchReply.parseXmlReply(getReader(is, encoding));
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

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();

        Calendar pickupDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        pickupDate.clear();
        pickupDate.set(pickupDateTime.get(Calendar.YEAR), pickupDateTime.get(Calendar.MONTH),
                pickupDateTime.get(Calendar.DAY_OF_MONTH));

        int pickupHour = pickupDateTime.get(Calendar.HOUR_OF_DAY);

        Calendar dropoffDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        dropoffDate.clear();
        dropoffDate.set(dropoffDateTime.get(Calendar.YEAR), dropoffDateTime.get(Calendar.MONTH),
                dropoffDateTime.get(Calendar.DAY_OF_MONTH));

        int dropoffHour = dropoffDateTime.get(Calendar.HOUR_OF_DAY);

        body.append("<CarShop>");
        addElement(body, "CarType", carType);
        addElement(body, "DropOffDate", Format.safeFormatCalendar(FormatUtil.XML_DF, dropoffDate));
        addElement(body, "DropOffHour", Integer.toString(dropoffHour));
        if (dropoffIata != null && dropoffIata.trim().length() > 0) {
            addElement(body, "DropOffIATA", dropoffIata);
        }
        addElement(body, "DropOffLatitude", dropoffLat);
        addElement(body, "DropOffLongitude", dropoffLong);
        addElement(body, "IsOffAirport", Boolean.toString(offAirport));
        addElement(body, "PickUpDate", Format.safeFormatCalendar(FormatUtil.XML_DF, pickupDate));
        addElement(body, "PickUpHour", Integer.toString(pickupHour));
        if (pickupIata != null && pickupIata.trim().length() > 0) {
            addElement(body, "PickUpIATA", pickupIata);
        }
        addElement(body, "PickUpLatitude", pickupLat);
        addElement(body, "PickUpLongitude", pickupLong);
        addElement(body, "Smoking", "0");
        body.append("</CarShop>");

        return body.toString();
    }

}
