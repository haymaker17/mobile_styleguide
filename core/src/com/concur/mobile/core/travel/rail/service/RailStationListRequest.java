package com.concur.mobile.core.travel.rail.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class RailStationListRequest extends GetServiceRequest {

    public static final String CLS_TAG = RailStationListRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Rail/GetRailStationList";

    // The vendor code of the rail company
    public String vendorCode;

    @Override
    protected String getServiceEndpointURI() {
        String endPoint = SERVICE_END_POINT;
        if (vendorCode != null && vendorCode.length() > 0) {
            StringBuilder strBldr = new StringBuilder(endPoint);
            strBldr.append('/');
            strBldr.append(vendorCode);
            endPoint = strBldr.toString();
        }
        return endPoint;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        RailStationListReply reply = new RailStationListReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);
            try {
                reply = RailStationListReply.parseXMLReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.xmlReply = xmlReply;
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
