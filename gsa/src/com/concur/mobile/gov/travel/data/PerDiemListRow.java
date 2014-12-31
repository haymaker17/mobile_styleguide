/**
 * @author sunill
 */
package com.concur.mobile.gov.travel.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class PerDiemListRow implements Serializable {

    private static final long serialVersionUID = -776339491383018198L;

    public String locate;
    public String locst;
    public String passwd;
    public String linkloc;
    public String linkst;
    public String comment;
    public String comploc;
    public String countyid;
    public String county;
    public String currency;
    public String doscode;
    public String fips;
    public String customrtorg;
    public String conus;

    public Calendar effdate;
    public Calendar expdate;

    public Integer timezone;
    public Integer zipcode;

    public String lattitude;
    public String longitude;

    public Boolean dodind;

    public Boolean isDefault;

    public PerDiemListRow() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("locate")) {
            locate = cleanChars;
        } else if (localName.equalsIgnoreCase("locst")) {
            locst = cleanChars;
        } else if (localName.equalsIgnoreCase("passwd")) {
            passwd = cleanChars;
        } else if (localName.equalsIgnoreCase("linkloc")) {
            linkloc = cleanChars;
        } else if (localName.equalsIgnoreCase("linkst")) {
            linkst = cleanChars;
        } else if (localName.equalsIgnoreCase("comment")) {
            comment = cleanChars;
        } else if (localName.equalsIgnoreCase("comploc")) {
            comploc = cleanChars;
        } else if (localName.equalsIgnoreCase("countyid")) {
            countyid = cleanChars;
        } else if (localName.equalsIgnoreCase("county")) {
            county = cleanChars;
        } else if (localName.equalsIgnoreCase("currency")) {
            currency = cleanChars;
        } else if (localName.equalsIgnoreCase("dos-code")) {
            doscode = cleanChars;
        } else if (localName.equalsIgnoreCase("fips")) {
            fips = cleanChars;
        } else if (localName.equalsIgnoreCase("custom-rt-org")) {
            customrtorg = cleanChars;
        } else if (localName.equalsIgnoreCase("conus")) {
            conus = cleanChars;
        } else if (localName.equalsIgnoreCase("effdate")) {
            effdate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("expdate")) {
            expdate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("time-zone")) {
            timezone = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("zipcode")) {
            zipcode = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("lattitude")) {
            lattitude = cleanChars;
        } else if (localName.equalsIgnoreCase("longitude")) {
            longitude = cleanChars;
        } else if (localName.equalsIgnoreCase("dod-ind")) {
            dodind = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("isDefault")) {
            isDefault = Parse.safeParseBoolean(cleanChars);
        }
    }
}
