package com.concur.mobile.core.travel.rail.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;

import android.util.Log;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.PostServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.util.Const;

public class RailSellRequest extends PostServiceRequest {

    private static final String CLS_TAG = RailSellRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Rail/AmtrakSell";

    public String bucket;
    public String creditCardId;
    public String deliveryOption;
    public String groupId;
    public List<TravelCustomField> fields;
    public String violationCode;
    public String violationJustification;

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();

        body.append("<RailSell>");
        addElement(body, "Bucket", bucket);
        addElement(body, "CreditCardId", creditCardId);
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(body, fields, false);
        }
        addElement(body, "DeliveryOption", deliveryOption);
        addElement(body, "GroupId", groupId);
        addElement(body, "ViolationCode", violationCode);
        addElement(body, "ViolationJustification", violationJustification);
        body.append("</RailSell>");

        return body.toString();
    }

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
        RailSellReply reply = new RailSellReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            try {
                reply = RailSellReply.parseXMLReply(responseXml);
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

}