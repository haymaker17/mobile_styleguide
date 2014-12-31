/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.air.service.AirFilterRequest;

/**
 * 
 * @author Chris N. Diaz
 * 
 */
public class GovAirFilterRequest extends AirFilterRequest {

    public String rateType;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.service.AirFilterRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.core.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GovAirFilterReply reply = new GovAirFilterReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = GovAirFilterReply.parseXMLReply(responseXml);
            if (reply != null) {
                reply.airlineCode = airlineCode;
                reply.rateType = rateType;
            }
        }
        return reply;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.service.AirFilterRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();

        body.append("<FilterCriteria>");
        addElement(body, "Airline", airlineCode);
        addElement(body, "RateType", rateType);
        body.append("</FilterCriteria>");

        return body.toString();
    }
}
