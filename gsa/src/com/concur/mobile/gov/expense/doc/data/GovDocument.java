/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class GovDocument implements Serializable {

    private static final long serialVersionUID = 3023926108707293614L;

    public String travelerName;
    public String docTypeLabel;
    public String docType;
    public String gtmDocType;
    public String docName;
    public String purposeCode;
    public String approveLabel;
    public String travelerId;

    public Boolean authForVch;
    public Boolean needsStamping;

    public Double totalExpCost;

    public Calendar tripBeginDate;
    public Calendar tripEndDate;

    public GovDocument() {
        // TODO Auto-generated constructor stub
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("TravelerName")) {
            travelerName = cleanChars;
        } else if (localName.equalsIgnoreCase("DocTypeLabel")) {
            docTypeLabel = cleanChars;
        } else if (localName.equalsIgnoreCase("DocType")) {
            docType = cleanChars;
        } else if (localName.equalsIgnoreCase("GtmDocType")) {
            gtmDocType = cleanChars;
        } else if (localName.equalsIgnoreCase("DocName")) {
            docName = cleanChars;
        } else if (localName.equalsIgnoreCase("TravelerId")) {
            travelerId = cleanChars;
        } else if (localName.equalsIgnoreCase("PurposeCode")) {
            purposeCode = cleanChars;
        } else if (localName.equalsIgnoreCase("ApproveLabel")) {
            approveLabel = cleanChars;
        } else if (localName.equalsIgnoreCase("AuthForVch")) {
            authForVch = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("NeedsStamping")) {
            needsStamping = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("TripBeginDate")) {
            tripBeginDate = Parse.parseTimestamp(cleanChars,  FormatUtil.GOV_EXPENSE_DATE_LOCAL);
        } else if (localName.equalsIgnoreCase("TripEndDate")) {
            tripEndDate = Parse.parseTimestamp(cleanChars, FormatUtil.GOV_EXPENSE_DATE_LOCAL);
        } else if (localName.equalsIgnoreCase("TotalExpCost")) {
            totalExpCost = Parse.safeParseDouble(cleanChars);
        }
    }
}
