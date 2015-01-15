package com.concur.mobile.core.travel.air.service;

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

public class AirSearchRequest extends PostServiceRequest {

    private static final String CLS_TAG = AirSearchRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Air/Search";

    public String cabinClass;
    public String departIATA;
    public String arriveIATA;
    public Calendar departDateTime;
    public Calendar returnDateTime;
    public boolean refundableOnly;

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
        AirSearchReply reply = new AirSearchReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            try {
                reply = AirSearchReply.parseXmlReply(getReader(is, encoding));
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

        body.append("<AirCriteria>");
        addElement(body, "Cabin", cabinClass);
        addElement(body, "GetBenchmark", "true");
        addElement(body, "IncludeDirectConnect", "Travelfusion");
        addElement(body, "NumTravelers", "1");
        addElement(body, "RefundableOnly", Boolean.toString(refundableOnly));
        body.append("<Segments>");
        body.append("<AirSegmentCriteria>");

        // MOB-22200
        // java.text.DateFormat df = new SimpleDateFormat("yyyy-MM-dd");// FormatUtil.LONG_YEAR_MONTH_DAY;
        // df.setTimeZone(TimeZone.getDefault());

        addElement(body, "Date", Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_LOCAL, departDateTime));
        addElement(body, "EndIata", arriveIATA);
        addElement(body, "SearchTime", Integer.toString(departDateTime.get(Calendar.HOUR_OF_DAY)));
        addElement(body, "StartIata", departIATA);
        addElement(body, "TimeIsDeparture", "true");
        addElement(body, "TimeWindow", "3");
        body.append("</AirSegmentCriteria>");
        if (returnDateTime != null) {
            body.append("<AirSegmentCriteria>");
            addElement(body, "Date", Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_LOCAL, returnDateTime));
            addElement(body, "EndIata", departIATA);
            addElement(body, "SearchTime", Integer.toString(returnDateTime.get(Calendar.HOUR_OF_DAY)));
            addElement(body, "StartIata", arriveIATA);
            addElement(body, "TimeIsDeparture", "true");
            addElement(body, "TimeWindow", "3");
            body.append("</AirSegmentCriteria>");
        }
        body.append("</Segments>");
        body.append("</AirCriteria>");

        return body.toString();
    }
}
