package com.concur.mobile.gov.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.car.service.CarSellRequest;
import com.concur.mobile.core.travel.data.TravelCustomField;

public class GovCarSellRequest extends CarSellRequest {

    public String existingTANumber;
    public String perdiemLocationID;
    public String country;
    public String zipCode;
    public String state;
    public String name;

    public GovCarSellRequest() {
        super();
    }

    @Override
    protected String buildRequestBody() {
        StringBuilder body = new StringBuilder();

        body.append("<CarSell>");
        addElement(body, "CarId", carId);
        if (creditCardId != null) {
            addElement(body, "CreditCardId", creditCardId);
        }
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(body, fields, false);
        }
        if (existingTANumber != null) {
            addElement(body, "ExistingTANumber", existingTANumber);
        }
        if (perdiemLocationID != null) {
            addElement(body, "PerdiemLocationID", perdiemLocationID);
        }
        addElement(body, "RecordLocator", recordLocator);
        addElement(body, "TripId", tripId);
        addElement(body, "TripLocator", tripLocator);
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
        body.append("</CarSell>");
        return body.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GovCarSellReply reply = new GovCarSellReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = GovCarSellReply.parseXMLReply(responseXml);
        }
        return reply;
    }

    @Override
    protected int getSoTimeout() {
        return 300000;
    }

}
