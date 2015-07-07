/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of an expense report entry.
 * 
 * @author AndrewK
 */
public class ExpenseReportEntry implements Serializable {

    private static final long serialVersionUID = -3209345822782852247L;

    private static final String CLS_TAG = ExpenseReportEntry.class.getSimpleName();

    public Double approvedAmount;

    public String expenseName;

    public Boolean hasAllocation;

    public Boolean hasAttendees;

    public Boolean hasComments;

    public Boolean hasExceptions;

    public Boolean isCreditCardCharge;

    public Boolean isItemized;

    public Boolean isPersonal;

    // TODO: This value isn't actually being parsed at the moment!
    public Boolean rentalCar;

    // TODO: This value isn't actually being parsed at the moment!
    public Boolean flight;

    public String locationName;

    public String parentReportEntryKey;

    public String reportEntryKey;

    public Double transactionAmount;

    public String transactionCrnCode;

    public String transactionDate;

    public Calendar transactionDateCalendar;

    public String vendorDescription;

    public Boolean personalCardCharge;

    public String meKey;

    public Boolean receiptRequired;

    public Boolean imageRequired;

    public String severityLevel;

    public Boolean hasMobileReceipt;

    public String eReceiptId;

    public String receiptImageId;

    public String expKey;

    public String formKey;

    public String rptKey;

    public int noShowCount;

    public String eReceiptImageId;

    public String taDayKey;

    // A flag indicating whether or not the attendee no show count has changed.
    public boolean noShowCountChanged;

    /**
     * Contructs an empty instance of <code>ExpenseReportEntry</code>.
     */
    public ExpenseReportEntry() {
    }

    /**
     * Constructs an instance of <code>ExpenseReportEntry</code> from a detailed version.
     * 
     * @param expRepEntDet
     *            the detailed version.
     */
    public ExpenseReportEntry(ExpenseReportEntryDetail expRepEntDet) {
        approvedAmount = expRepEntDet.approvedAmount;
        expenseName = expRepEntDet.expenseName;
        hasAllocation = expRepEntDet.hasAllocation;
        hasAttendees = expRepEntDet.hasAttendees;
        hasComments = expRepEntDet.hasComments;
        hasExceptions = expRepEntDet.hasExceptions;
        isCreditCardCharge = expRepEntDet.isCreditCardCharge;
        isItemized = expRepEntDet.isItemized;
        isPersonal = expRepEntDet.isPersonal;
        rentalCar = expRepEntDet.rentalCar;
        flight = expRepEntDet.flight;
        locationName = expRepEntDet.locationName;
        parentReportEntryKey = expRepEntDet.parentReportEntryKey;
        reportEntryKey = expRepEntDet.reportEntryKey;
        transactionAmount = expRepEntDet.transactionAmount;
        transactionCrnCode = expRepEntDet.transactionCrnCode;
        transactionDate = expRepEntDet.transactionDate;
        transactionDateCalendar = expRepEntDet.transactionDateCalendar;
        vendorDescription = expRepEntDet.vendorDescription;
        personalCardCharge = expRepEntDet.personalCardCharge;
        meKey = expRepEntDet.meKey;
        receiptRequired = expRepEntDet.receiptRequired;
        imageRequired = expRepEntDet.imageRequired;
        severityLevel = expRepEntDet.severityLevel;
        hasMobileReceipt = expRepEntDet.hasMobileReceipt;
        eReceiptId = expRepEntDet.eReceiptId;
        receiptImageId = expRepEntDet.receiptImageId;
        expKey = expRepEntDet.expKey;
        formKey = expRepEntDet.formKey;
        rptKey = expRepEntDet.rptKey;
        noShowCount = expRepEntDet.noShowCount;
        eReceiptImageId = expRepEntDet.eReceiptImageId;
    }

    /**
     * Gets whether this is a detail report entry.
     * 
     * @return whether this is a detailed report entry.
     */
    public boolean isDetail() {
        return false;
    }

    /**
     * Gets whether this report entry is an itemization.
     * 
     * @return whether this report entry is an itemization.
     */
    public boolean isItemization() {
        return (parentReportEntryKey != null && parentReportEntryKey.length() > 0);
    }

    /**
     * Whether or not this report entry has allocations.
     * 
     * @return whether this report entry has allocations.
     */
    public boolean hasAllocation() {
        return ((hasAllocation != null) ? hasAllocation : Boolean.FALSE);
    }

    /**
     * Whether the report entry has attendees.
     * 
     * @return whether the report entry has attendees.
     */
    public boolean hasAttendees() {
        return ((hasAttendees != null) ? hasAttendees : Boolean.FALSE);
    }

    /**
     * Whether the report entry has comments.
     * 
     * @return whether the report entry has comments.
     */
    public boolean hasComments() {
        return ((hasComments != null) ? hasComments : Boolean.FALSE);
    }

    /**
     * Whether the report entry has exceptions.
     * 
     * @return whether the report entry has exceptions.
     */
    public boolean hasExceptions() {
        return ((hasExceptions != null) ? hasExceptions : Boolean.FALSE);
    }

    /**
     * Whether the report entry represents a credit card charge.
     * 
     * @return whether the report entry represents a credit card charge.
     */
    public boolean isCreditCardCharge() {
        return ((isCreditCardCharge != null) ? isCreditCardCharge : Boolean.FALSE);
    }

    /**
     * Whether the report entry is itemized.
     * 
     * @return whether the report entry is itemized.
     */
    public boolean isItemized() {
        return ((isItemized != null) ? isItemized : Boolean.FALSE);
    }

    /**
     * Whether the entry can be itemized.
     * 
     * This needs to be based on expense type (the object, not just the key) but for now we hack it to look at the EXP_KEY. The
     * MWS needs to return additional info about the expense type and we need to get a reference to the ExpenseType object to
     * check if it can be itemized.
     * 
     * @return
     */
    public boolean canBeItemized(List<ExpenseType> expenseTypes) {
        // TODO: The real check. For now, just say that MILEG/CARMI and personal and children cannot be itemized.

        // Check the value of 'itemizeType' in the list of passed in expense types. Assume 'true' unless the client
        // is explicitly told 'no'.
        boolean expTypeCanBeItemized = true;
        if (expenseTypes != null) {
            for (ExpenseType expType : expenseTypes) {
                if (expType.key != null && expKey != null && expType.key.equalsIgnoreCase(expKey)) {
                    expTypeCanBeItemized = (expType.itemizeType != null && (expType.itemizeType
                            .equalsIgnoreCase(Const.ITEMIZATION_TYPE_OPTIONAL) || expType.itemizeType
                            .equalsIgnoreCase(Const.ITEMIZATION_TYPE_REQUIRED)));
                    break;
                }
            }
        }
        // Return conjunction of whether expense type, is personal and non-itemization.
        return expTypeCanBeItemized && !isPersonal() && parentReportEntryKey == null;
    }

    /**
     * Whether the report entry uses the hotel wizard for itemization.
     * 
     * @param expTypes
     *            references a list of expense types to check.
     * @return whether this report entry uses the hotel wizard for itemization.
     */
    public boolean usesHotelWizard(List<ExpenseType> expTypes) {
        boolean retVal = false;
        if (expTypes != null) {
            for (ExpenseType expType : expTypes) {
                if (expType.key != null && expType.key.equalsIgnoreCase(expKey)) {
                    retVal = ((expType.itemizeStyle != null && expType.itemizeStyle.equalsIgnoreCase("lodging")) ? true
                            : false);
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Whether the report entry is a personal expense.
     * 
     * @return whether the report entry is a personal expense.
     */
    public boolean isPersonal() {
        return ((isPersonal != null) ? isPersonal : Boolean.FALSE);
    }

    /**
     * Whether the report entry is a rental car.
     * 
     * @return whether the report entry is a rental car.
     */
    public boolean isRentalCar() {
        return ((rentalCar != null) ? rentalCar : Boolean.FALSE);
    }

    /**
     * Whether the report entry is a flight.
     * 
     * @return whether the report entry is a flight.
     */
    public boolean isFlight() {
        return ((flight != null) ? flight : Boolean.FALSE);
    }

    /**
     * Gets the report creation date as a string formatted to <code>FormatUtil.SHORT_DAY_MONTH_YEAR_DISPLAY</code> format.
     * 
     * @return report creation date formatted to <code>FormatUtil.SHORT_DAY_MONTH_YEAR_DISPLAY</code>
     */
    public String getFormattedTransactionDate() {
        return FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(transactionDateCalendar.getTime());
    }

    /**
     * Gets whether this is a personal card charge entry.
     * 
     * @return whether this is a personal card charge entry.
     */
    public boolean isPersonalCardCharge() {
        return ((personalCardCharge != null) ? personalCardCharge : Boolean.FALSE);
    }

    /**
     * Gets whether a receipt is required for this report entry.
     * 
     * @return whether a receipt is required for this report entry.
     */
    public boolean isReceiptRequired() {
        return ((receiptRequired != null) ? receiptRequired : Boolean.FALSE);
    }

    /**
     * Gets whether an image is required for this report entry.
     * 
     * @return whether an image is required for this report entry.
     */
    public boolean isImageRequired() {
        return ((imageRequired != null) ? imageRequired : Boolean.FALSE);
    }

    /**
     * Gets whether this expense entry has a mobile receipt.
     * 
     * @return whether this expense entry has a mobile receipt.
     */
    public boolean hasMobileReceipt() {
        return ((hasMobileReceipt != null) ? hasMobileReceipt : Boolean.FALSE);
    }

    /**
     * Gets whether this expense entry has a e-receipt image id.
     * 
     * @return whether this expense entry has a e-receipt image id.
     */
    public boolean hasEReceiptImageId() {
        return ((eReceiptImageId != null) ? true : Boolean.FALSE);
    }

    /**
     * Will parse the XML description of a report entry and return an <code>ExpenseReportEntry</code> object.
     * 
     * @param reportEntryXml
     *            the report entry xml.
     * @return an instance of <code>ExpenseReportEntry</code>.
     */
    public static ExpenseReportEntry parseReportEntry(String reportEntryXml) {
        return parseReportEntry(new ByteArrayInputStream(reportEntryXml.getBytes()));
    }

    /**
     * Will parse the XML description of a report entry and return an <code>ExpenseReportEntry</code> object.
     * 
     * @param stream
     *            a reference to a stream providing report entry XML content.
     * @return an instance of <code>ExpenseReportEntry</code>.
     */
    public static ExpenseReportEntry parseReportEntry(InputStream stream) {
        ExpenseReportEntry entry = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseReportEntrySAXHandler handler = new ExpenseReportEntrySAXHandler();
            parser.parse(stream, handler);
            ArrayList<ExpenseReportEntry> entries = handler.getReportEntries();
            if (entries.size() > 0) {
                entry = (ExpenseReportEntry) entries.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entry;
    }

    /**
     * Will parse the XML description of a report entry and return an <code>ExpenseReportEntry</code> object.
     * 
     * @param reader
     *            a reference to a reader providing report entry XML content.
     * @return an instance of <code>ExpenseReportEntry</code>.
     */
    public static ExpenseReportEntry parseReportEntry(Reader reader) {
        ExpenseReportEntry entry = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseReportEntrySAXHandler handler = new ExpenseReportEntrySAXHandler();
            parser.parse(new InputSource(reader), handler);
            ArrayList<ExpenseReportEntry> entries = handler.getReportEntries();
            if (entries.size() > 0) {
                entry = (ExpenseReportEntry) entries.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entry;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense report entry information.
     * 
     * @author AndrewK
     */
    public static class ExpenseReportEntrySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ExpenseReportEntry.CLS_TAG + "."
                + ExpenseReportEntrySAXHandler.class.getSimpleName();

        protected static final String REPORT_ENTRY = "ReportEntry";

        protected static final String REPORT_ENTRY_DETAIL = "ReportEntryDetail";

        protected static final String APPROVED_AMOUNT = "ApprovedAmount";

        protected static final String EXPENSE_NAME = "ExpName";

        protected static final String HAS_ALLOCATION = "HasAllocation";

        protected static final String HAS_ATTENDEES = "HasAttendees";

        protected static final String HAS_COMMENTS = "HasComments";

        protected static final String HAS_EXCEPTIONS = "HasExceptions";

        protected static final String IS_CREDIT_CARD_CHARGE = "IsCreditCardCharge";

        protected static final String IS_ITEMIZED = "IsItemized";

        protected static final String IS_PERSONAL = "IsPersonal";

        protected static final String LOCATION_NAME = "LocationName";

        protected static final String PARENT_REPORT_ENTRY_KEY = "ParentRpeKey";

        protected static final String REPORT_ENTRY_KEY = "RpeKey";

        protected static final String TRANSACTION_AMOUNT = "TransactionAmount";

        protected static final String TRANSACTION_CRN_CODE = "TransactionCrnCode";

        protected static final String TRANSACTION_DATE = "TransactionDate";

        protected static final String VENDOR_DESCRIPTION = "VendorDescription";

        protected static final String IS_PERSONAL_CARD_CHARGE = "IsPersonalCardCharge";

        protected static final String ME_KEY = "MeKey";

        protected static final String RECEIPT_REQUIRED = "ReceiptRequired";

        protected static final String IMAGE_REQUIRED = "ImageRequired";

        protected static final String SEVERITY_LEVEL = "SeverityLevel";

        protected static final String HAS_MOBILE_RECEIPT = "HasMobileReceipt";

        protected static final String E_RECEIPT_ID = "EreceiptId";

        protected static final String RECEIPT_IMAGE_ID = "ReceiptImageId";

        protected static final String EXPENSE_KEY = "ExpKey";

        protected static final String FORM_KEY = "FormKey";

        protected static final String REPORT_KEY = "RptKey";

        protected static final String NO_SHOW_COUNT = "NoShowCount";

        protected static final String E_RECEIPT_IMGID = "EreceiptImageId";

        protected static final String TA_DAY_KEY = "TaDayKey";

        // Fields to help parsing
        protected StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ExpenseReportEntry</code> objects that have been parsed.
         */
        protected ArrayList<ExpenseReportEntry> reportEntries = new ArrayList<ExpenseReportEntry>();

        /**
         * Contains a reference to the report entry currently being built.
         */
        protected ExpenseReportEntry reportEntry;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets the list of <code>ExpenseReportEntry</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ExpenseReportEntry</code> objects.
         */
        public ArrayList<ExpenseReportEntry> getReportEntries() {
            return reportEntries;
        }

        /**
         * Constructs an instance of <code>ExpenseReportEntry</code>.
         * 
         * @return an instance of <code>ExpenseReportEntry</code>.
         */
        protected ExpenseReportEntry createReportEntry() {
            return new ExpenseReportEntry();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(REPORT_ENTRY) || localName.equalsIgnoreCase(REPORT_ENTRY_DETAIL)) {
                reportEntry = createReportEntry();
                chars.setLength(0);
                elementHandled = true;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;

            super.endElement(uri, localName, qName);

            if (reportEntry != null) {
                final String cleanChars = chars.toString().trim();
                if (localName.equalsIgnoreCase(EXPENSE_KEY)) {
                    reportEntry.expKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FORM_KEY)) {
                    reportEntry.formKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REPORT_KEY)) {
                    reportEntry.rptKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(APPROVED_AMOUNT)) {
                    reportEntry.approvedAmount = Parse.safeParseDouble(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXPENSE_NAME)) {
                    reportEntry.expenseName = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(SEVERITY_LEVEL)) {
                    reportEntry.severityLevel = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ME_KEY)) {
                    reportEntry.meKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(E_RECEIPT_ID)) {
                    reportEntry.eReceiptId = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_IMAGE_ID)) {
                    reportEntry.receiptImageId = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(E_RECEIPT_IMGID)) {
                    reportEntry.eReceiptImageId = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_PERSONAL_CARD_CHARGE)) {
                    reportEntry.personalCardCharge = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_MOBILE_RECEIPT)) {
                    reportEntry.hasMobileReceipt = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_REQUIRED)) {
                    reportEntry.receiptRequired = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IMAGE_REQUIRED)) {
                    reportEntry.imageRequired = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_ALLOCATION)) {
                    reportEntry.hasAllocation = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_ATTENDEES)) {
                    reportEntry.hasAttendees = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_COMMENTS)) {
                    reportEntry.hasComments = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_EXCEPTIONS)) {
                    reportEntry.hasExceptions = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_CREDIT_CARD_CHARGE)) {
                    reportEntry.isCreditCardCharge = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_ITEMIZED)) {
                    reportEntry.isItemized = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_PERSONAL)) {
                    reportEntry.isPersonal = Parse.safeParseBoolean(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(LOCATION_NAME)) {
                    reportEntry.locationName = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(NO_SHOW_COUNT)) {
                    reportEntry.noShowCount = Parse.safeParseInteger(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_REPORT_ENTRY_KEY)) {
                    reportEntry.parentReportEntryKey = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REPORT_ENTRY_KEY)) {
                    reportEntry.reportEntryKey = cleanChars;
                    // Add this key to the 'ExpenseReport.reportEntryMap'.
                    if (ExpenseReport.reportEntryMap != null) {
                        ExpenseReport.reportEntryMap.put(reportEntry.reportEntryKey, reportEntry);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_AMOUNT)) {
                    reportEntry.transactionAmount = Parse.safeParseDouble(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_CRN_CODE)) {
                    reportEntry.transactionCrnCode = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_DATE)) {
                    reportEntry.transactionDate = cleanChars;
                    reportEntry.transactionDateCalendar = Parse.parseXMLTimestamp(reportEntry.transactionDate);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VENDOR_DESCRIPTION)) {
                    reportEntry.vendorDescription = cleanChars;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TA_DAY_KEY)) {
                    // only relevant for travel allowance
                    reportEntry.taDayKey = cleanChars;
                } else if (localName.equalsIgnoreCase(REPORT_ENTRY) || localName.equalsIgnoreCase(REPORT_ENTRY_DETAIL)) {
                    reportEntries.add(reportEntry);
                    reportEntry = null;
                    elementHandled = true;
                } else if (!elementHandled && this.getClass().equals(ExpenseReportEntrySAXHandler.class)) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element '" + localName + "'.");
                    // Clear out any collected characters if this class is the last stop.
                    chars.setLength(0);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current report entry!");
                chars.setLength(0);
            }

            // Only clear out if handled.
            if (elementHandled) {
                chars.setLength(0);
            }

        }

    }

}
