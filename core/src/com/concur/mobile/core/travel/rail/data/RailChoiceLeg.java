package com.concur.mobile.core.travel.rail.data;

import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class RailChoiceLeg {

    public String carrierCode;
    public String trainNum;
    public Calendar depDateTime;
    public Calendar arrDateTime;
    public int totalTime;
    public String depStationCode;
    public String arrStationCode;
    public String seatClassCode;
    public String seatClassName; // This is populated in the XML parser endDocument()
    public boolean isAcela;

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("BIC")) {
            seatClassCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Carrier")) {
            carrierCode = cleanChars;
        } else if (localName.equalsIgnoreCase("FltNum")) {
            trainNum = cleanChars;
        } else if (localName.equalsIgnoreCase("DepDateTime")) {
            depDateTime = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("ArrDateTime")) {
            arrDateTime = Parse.parseXMLTimestamp(cleanChars);
        } else if (localName.equalsIgnoreCase("FlightTime")) {
            totalTime = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("DepAirp")) {
            depStationCode = cleanChars;
        } else if (localName.equalsIgnoreCase("ArrAirp")) {
            arrStationCode = cleanChars;
        }
    }

    // We base this off the BIC right now. Eventually it will come in explicitly via the shop results
    // using a lookup on the server.
    public boolean isBus() {
        return seatClassCode.startsWith("T");
    }

    public String getElapsedTime(Context context) {
        return FormatUtil.formatElapsedTime(context, totalTime);
    }

}
