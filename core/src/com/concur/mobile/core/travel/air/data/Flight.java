package com.concur.mobile.core.travel.air.data;

import java.util.Calendar;

import com.concur.mobile.platform.util.Parse;

public class Flight {

    public int airMiles;
    public String aircraftCode;
    public Calendar arrivalDateTime;
    public String bic;
    public String carrier;
    public Calendar departureDateTime;
    public String endIATA;
    public int flightNum;
    public int flightTime;
    public int numStops;
    public String operatingCarrier;
    public String seatClass;
    public String startIATA;
    public String title;
    public String departureAirport;
    public String arrivalAirport;
    public AlternativeCOS cos;

    public String getCarrierName() {
        return AirDictionaries.vendorCodeMap.get(carrier);
    }

    public String getOperatingCarrierName() {
        return AirDictionaries.vendorCodeMap.get(operatingCarrier);
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("AirMiles")) {
            airMiles = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("AircraftCode")) {
            aircraftCode = cleanChars;
        } else if (localName.equalsIgnoreCase("ArrivalTime")) {
            arrivalDateTime = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("Bic")) {
            bic = cleanChars;
        } else if (localName.equalsIgnoreCase("Carrier")) {
            carrier = cleanChars;
        } else if (localName.equalsIgnoreCase("DepartureTime")) {
            departureDateTime = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("EndIata")) {
            endIATA = cleanChars;
        } else if (localName.equalsIgnoreCase("FlightNum") || localName.equalsIgnoreCase("FltNum")) {
            flightNum = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("FlightTime")) {
            flightTime = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("NumStops")) {
            numStops = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("OperatingCarrier")) {
            operatingCarrier = cleanChars;
        } else if (localName.equalsIgnoreCase("FltClass")) {
            seatClass = cleanChars;
        } else if (localName.equalsIgnoreCase("StartIata")) {
            startIATA = cleanChars;
        } else if (localName.equalsIgnoreCase("Title")) {
            title = cleanChars;
        } else if (localName.equalsIgnoreCase("DepAirp")) {
            departureAirport = cleanChars;
        } else if (localName.equalsIgnoreCase("DepDateTime")) {
            departureDateTime = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("ArrAirp")) {
            arrivalAirport = cleanChars;
        } else if (localName.equalsIgnoreCase("ArrDateTime")) {
            arrivalDateTime = Parse.parseXMLTimestamp(cleanChars);
        }
    }

    public void setCOS(AlternativeCOS cos) {
        this.cos = cos;
    }

    public AlternativeCOS getCOS() {
        return cos;
    }
}
