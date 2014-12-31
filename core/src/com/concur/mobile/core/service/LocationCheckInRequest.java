/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

/**
 * An extension of <code>GetServiceRequest</code> for handling "location check in".
 * 
 * @author Chris N. Diaz
 * 
 */
public class LocationCheckInRequest extends GetServiceRequest {

    private static final String CLS_TAG = LocationCheckInRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/mobile/SafetyCheckIn.ashx";

    public String longitude;
    public String latitude;
    public String city;
    public String state;
    public String countryCode;
    public String assistanceRequired;
    public String daysRemaining;
    public String comment;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        return buildURLRequest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        LocationCheckInReply reply = new LocationCheckInReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = LocationCheckInReply.parseXMLReply(responseXml);
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

    // ######################## HELPER METHODS ###################### //

    private String buildURLRequest() {

        StringBuilder request = new StringBuilder();
        request.append("?long=").append(longitude);
        request.append("&lat=").append(latitude);
        request.append("&city=").append(getEscapedStr(city));
        request.append("&state=").append(getEscapedStr(state));
        request.append("&ctry=").append(countryCode);
        request.append("&assist=").append(assistanceRequired);
        request.append("&days=").append(daysRemaining);
        request.append("&comment=").append(getEscapedStr(FormatUtil.escapeForXML(comment)));

        Log.d(Const.LOG_TAG, CLS_TAG + ".buildURLRequest: request parameters = " + request);

        return SERVICE_END_POINT + request.toString();
    }

    private String getEscapedStr(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getEscapedStr", e);
            return str;
        }
    }

}
