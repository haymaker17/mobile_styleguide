/**
 * @author sunill
 */
package com.concur.mobile.gov.travel.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class PerDiemRateListRow implements Serializable {

    private static final long serialVersionUID = 4770322480926322894L;

    public String locate;
    public String locst;
    public String comment;
    public String currency;
    public String customrtorg;
    public String snlname;
    public String exchar1, exchar2;
    public String tabRow;

    public Calendar effdate;
    public Calendar expdate;
    public Calendar snlstart;
    public Calendar snlend;
    // <extra-date1 xsi:nil="true"/>
    // no need to parse right now..but in future it may required
    public Calendar exdate;

    public Double ldgrate;
    public Double mierate;
    public Double ftnoterate;
    public Double incidamt;
    public Double exdec1;
    public Double exdec2;

    public PerDiemRateListRow() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("locate")) {
            locate = cleanChars;
        } else if (localName.equalsIgnoreCase("locst")) {
            locst = cleanChars;
        } else if (localName.equalsIgnoreCase("comment")) {
            comment = cleanChars;
        } else if (localName.equalsIgnoreCase("currency")) {
            currency = cleanChars;
        } else if (localName.equalsIgnoreCase("custom-rt-org")) {
            customrtorg = cleanChars;
        } else if (localName.equalsIgnoreCase("effdate")) {
            effdate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("snl-start")) {
            snlstart = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("snl-end")) {
            snlend = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("snl-end")) {
            expdate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("ldgrate")) {
            ldgrate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("mierate")) {
            mierate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("ftnote-rate")) {
            ftnoterate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("incid-amt")) {
            incidamt = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("snl-name")) {
            snlname = cleanChars;
        } else if (localName.equalsIgnoreCase("extra-char1")) {
            exchar1 = cleanChars;
        } else if (localName.equalsIgnoreCase("extra-char2")) {
            exchar2 = cleanChars;
        } else if (localName.equalsIgnoreCase("extra-dec1")) {
            exdec1 = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("extra-dec2")) {
            exdec2 = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("TabRow")) {
            tabRow = cleanChars;
        }
    }
}
