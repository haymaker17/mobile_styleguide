package com.concur.mobile.gov.travel.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.hotel.service.HotelConfirmRequest;
import com.concur.mobile.core.util.FormatUtil;

public class GovHotelConfirmRequest extends HotelConfirmRequest {

    public String existingTANumber;
    public String perdiemLocationID;
    public String country;
    public String zipCode;
    public String state;
    public String name;

    public GovHotelConfirmRequest() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.PostServiceRequest#buildRequestBody()
     */
    @Override
    protected String buildRequestBody() {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<SellCriteria>");
        addElement(strBldr, "BicCode", bicCode);
        addElement(strBldr, "CcId", ccId);
        addElement(strBldr, "ChainCode", chainCode);
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(strBldr, fields, false);
        }
        if (existingTANumber != null) {
            addElement(strBldr, "ExistingTANumber", existingTANumber);
        }
        if (perdiemLocationID != null) {
            addElement(strBldr, "PerdiemLocationID", perdiemLocationID);
        }
        addElement(strBldr, "PropertyId", propertyId);
        addElement(strBldr, "PropertyName", FormatUtil.escapeForXML(propertyName));
        if (sellSource != null) {
            addElement(strBldr, "SellSource", FormatUtil.escapeForXML(sellSource));
        } else {
            strBldr.append("<SellSource/>");
        }
        if (tripId != null) {
            addElement(strBldr, "TripId", tripId);
        }
        strBldr.append("<USGovtPerDiemLocation>");
        if (country != null) {
            addElement(strBldr, "Country", country);
        }
        if (name != null) {
            addElement(strBldr, "Name", name);
        }
        if (state != null) {
            addElement(strBldr, "State", state);
        }
        if (zipCode != null) {
            addElement(strBldr, "ZipCode", zipCode);
        }
        strBldr.append("</USGovtPerDiemLocation>");
        if (hotelReason != null) {
            addElement(strBldr, "ViolationCode", ((hotelReasonCode != null) ? FormatUtil.escapeForXML(hotelReasonCode)
                : ""));
            addElement(strBldr, "ViolationJustification", FormatUtil.escapeForXML(hotelReason));
        }
        strBldr.append("</SellCriteria>");

        return strBldr.toString();
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GovHotelConfirmReply reply = new GovHotelConfirmReply();

        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String responseXml = readStream(is, encoding);
            reply = GovHotelConfirmReply.parseXMLReply(responseXml);
        }
        return reply;

    }

    @Override
    protected int getSoTimeout() {
        return 300000;
    }
}
