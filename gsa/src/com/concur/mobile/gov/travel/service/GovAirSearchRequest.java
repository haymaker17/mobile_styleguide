/**
 * Copyright (c) 2013 Concur Technologies, Inc.
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
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * @author Chris N. Diaz
 * 
 */
public class GovAirSearchRequest extends PostServiceRequest {

    private static final String CLS_TAG = GovAirSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Air/Search";

    public String cabinClass;
    public String departIATA;
    public String arriveIATA;
    public Calendar departDateTime;
    public Calendar returnDateTime;
    public boolean refundableOnly;
    public boolean showGovRateTypes;

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

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GovAirSearchReply reply = new GovAirSearchReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = GovAirSearchReply.parseXMLReply(responseXml);
        }
        return reply;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.service.AirSearchRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();

        body.append("<AirCriteria>");
        addElement(body, "Cabin", cabinClass);
        addElement(body, "NumTravelers", "1");
        addElement(body, "RefundableOnly", Boolean.toString(refundableOnly));
        body.append("<Segments>");
        body.append("<AirSegmentCriteria>");
        addElement(body, "Date", Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, departDateTime));
        addElement(body, "EndIata", arriveIATA);
        addElement(body, "SearchTime", Integer.toString(departDateTime.get(Calendar.HOUR_OF_DAY)));
        addElement(body, "StartIata", departIATA);
        addElement(body, "TimeIsDeparture", "true");
        addElement(body, "TimeWindow", "3");
        body.append("</AirSegmentCriteria>");
        if (returnDateTime != null) {
            body.append("<AirSegmentCriteria>");
            addElement(body, "Date", Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, returnDateTime));
            addElement(body, "EndIata", departIATA);
            addElement(body, "SearchTime", Integer.toString(returnDateTime.get(Calendar.HOUR_OF_DAY)));
            addElement(body, "StartIata", arriveIATA);
            addElement(body, "TimeIsDeparture", "true");
            addElement(body, "TimeWindow", "3");
            body.append("</AirSegmentCriteria>");
        }
        body.append("</Segments>");
        addElement(body, "ShowGovRateTypes", Boolean.toString(showGovRateTypes));
        body.append("</AirCriteria>");

        return body.toString();
    }
}
