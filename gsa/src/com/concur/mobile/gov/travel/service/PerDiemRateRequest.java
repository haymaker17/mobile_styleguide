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
import com.concur.mobile.gov.expense.doc.service.DocumentDetailRequest;
import com.concur.mobile.platform.util.Format;

public class PerDiemRateRequest extends PostServiceRequest {

    private static final String CLS_TAG = DocumentDetailRequest.class.getSimpleName();
    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/GetPerDiemRate";

    public String stateOrCountryCode;
    public String currency;
    public String location;

    public Calendar checkInDate;
    public Calendar checkOutDate;

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
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService)
        throws IOException
    {
        PerDiemRateReply reply = new PerDiemRateReply();
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            reply = PerDiemRateReply.parseXml(reply.xmlReply);
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        }
        return reply;
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();
        body.append("<GetPerDiemRateRequest>");
        addElement(body, "currency", currency);
        addElement(body, "effDate", Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, checkInDate));
        addElement(body, "expDate", Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, checkOutDate));
        // String name = Uri.encodeUri.encodeUri.encode(location, Const.HTTP_BODY_CHARACTER_ENCODING);
        // addElement(body, "location", name);
        addElement(body, "location", location);
        addElement(body, "stateOrCountryCode", stateOrCountryCode);
        body.append("</GetPerDiemRateRequest>");
        return body.toString();
    }
}
