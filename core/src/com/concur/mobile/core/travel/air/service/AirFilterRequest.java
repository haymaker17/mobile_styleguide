package com.concur.mobile.core.travel.air.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.util.Const;

public class AirFilterRequest extends PostServiceRequest {

    private static final String CLS_TAG = AirFilterRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/Air/Filter";

    public static final String WILDCARD = "*";

    public String airlineCode;
    public String numStops;

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
        AirFilterReply reply = new AirFilterReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            try {
                reply = AirFilterReply.parseXmlReply(getReader(is, encoding));
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            if (reply != null) {
                reply.airlineCode = airlineCode;
                reply.numStops = numStops;
            }
        }
        return reply;
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();

        body.append("<FilterCriteria>");
        addElement(body, "Airline", airlineCode);
        addElement(body, "NumStops", numStops);
        body.append("</FilterCriteria>");

        return body.toString();
    }
}
