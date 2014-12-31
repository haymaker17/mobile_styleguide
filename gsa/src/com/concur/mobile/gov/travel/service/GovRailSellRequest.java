package com.concur.mobile.gov.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.rail.service.RailSellRequest;

public class GovRailSellRequest extends RailSellRequest {

    public String existingTANumber;
    public String perdiemLocationID;
    public String country;
    public String zipCode;
    public String state;
    public String name;

    public GovRailSellRequest() {
        // TODO Auto-generated constructor stub
    }

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
        addElement(body, "ExistingTANumber", existingTANumber);
        addElement(body, "GroupId", groupId);
        addElement(body, "PerdiemLocationID", perdiemLocationID);
        body.append("<USGovtPerDiemLocation>");
        if (country != null) {
            addElement(body, "Country", country);
        }
        if (name != null) {
            addElement(body, "Name", name);
        }
        if (state != null) {
            addElement(body, "State", state);
        }
        if (zipCode != null) {
            addElement(body, "ZipCode", zipCode);
        }
        body.append("</USGovtPerDiemLocation>");
        addElement(body, "ViolationCode", violationCode);
        addElement(body, "ViolationJustification", violationJustification);
        body.append("</RailSell>");

        return body.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GovRailSellReply reply = new GovRailSellReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = GovRailSellReply.parseXMLReply(responseXml);
        }
        return reply;
    }

    @Override
    protected int getSoTimeout() {
        return 300000;
    }
}
