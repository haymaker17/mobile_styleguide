/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class HotelImagesRequest extends GetServiceRequest {

    private static final String CLS_TAG = HotelImagesRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Hotel/Images";

    public int gdsType;
    public String propertyId;

    @Override
    protected String getServiceEndpointURI() {
        StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
        strBldr.append('/');
        strBldr.append(gdsType);
        strBldr.append('/');
        strBldr.append(URLEncoder.encode(propertyId));
        return strBldr.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        HotelImagesReply reply = new HotelImagesReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = HotelImagesReply.parseXMLReply(responseXml);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
