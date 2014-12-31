package com.concur.mobile.core.travel.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class HotelBenchmark implements Serializable {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = 7593616084449861211L;

    private String crnCode;
    private String locationName;
    private Double price;
    private String subDivCode;

    public String getCrnCode() {
        return crnCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public Double getPrice() {
        return price;
    }

    public String getSubDivCode() {
        return subDivCode;
    }

    public void setCrnCode(String crnCode) {
        this.crnCode = crnCode;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setSubDivCode(String subDivCode) {
        this.subDivCode = subDivCode;
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Currency")) {
            crnCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Price")) {
            price = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Name")) {
            locationName = cleanChars;
        } else if (localName.equalsIgnoreCase("SubdivCode")) {
            subDivCode = cleanChars;
        }

    }
}
