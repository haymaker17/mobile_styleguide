package com.concur.mobile.core.travel.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.platform.util.Parse;

/**
 * Travel points benchmark
 * 
 * @author RatanK
 * 
 */
public class Benchmark implements Serializable {

    private String crnCode;
    private Calendar departureDate;
    private String originAirportCode;
    private String destinationAirportCode;
    private Double price;
    private boolean roundTrip;

    public String getCrnCode() {
        return crnCode;
    }

    public Calendar getDepartureDate() {
        return departureDate;
    }

    public String getOriginAirportCode() {
        return originAirportCode;
    }

    public String getDestinationAirportCode() {
        return destinationAirportCode;
    }

    public Double getPrice() {
        return price;
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Currency")) {
            crnCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Price")) {
            price = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Date")) {
            departureDate = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("RoundTrip")) {
            roundTrip = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("Destination")) {
            destinationAirportCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Origin")) {
            originAirportCode = cleanChars;
        }

    }
}
