/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryItemization.ExpenseReportEntryItemizationSAXHandler;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ActionStatusServiceReply</code> for parsing a response to a <code>SaveReportEntryRequest</code>.
 * 
 * @author AndrewK
 */
public class SaveReportEntryReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = SaveReportEntryReply.class.getSimpleName();

    /**
     * Contains the parsed report entry detail object.
     */
    public ExpenseReportEntryDetail expRepEntDet;

    /**
     * Contains the parsed "report total posted" amount.
     */
    public Double reportTotalPosted;

    /**
     * Contains the parsed "report total claimed" amount.
     */
    public Double reportTotalClaimed;

    /**
     * Contains the parsed "report total approved" amount.
     */
    public Double reportTotalApproved;

    public static SaveReportEntryReply parseReply(String responseXml) {

        SaveReportEntryReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new SaveReportEntrySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (SaveReportEntryReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class SaveReportEntrySAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = SaveReportEntryReply.CLS_TAG + "."
                + SaveReportEntrySAXHandler.class.getSimpleName();

        private static final String REPORT_ENTRY = "ReportEntry";
        private static final String REPORT_TOTAL_POSTED = "ReportTotalPosted";
        private static final String REPORT_TOTAL_CLAIMED = "ReportTotalClaimed";
        private static final String REPORT_TOTAL_APPROVED = "ReportTotalApproved";

        // Contains a reference to the handler parsing the report detail object.
        private ExpenseReportEntryItemizationSAXHandler reportEntryItemizationHandler;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new SaveReportEntryReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (reportEntryItemizationHandler == null) {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(REPORT_ENTRY)) {
                        reportEntryItemizationHandler = new ExpenseReportEntryItemizationSAXHandler();
                        reportEntryItemizationHandler.startElement(uri, localName, qName, attributes);
                    } else {
                    }
                }
            } else {
                elementHandled = true;
                reportEntryItemizationHandler.startElement(uri, localName, qName, attributes);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (reportEntryItemizationHandler != null) {
                reportEntryItemizationHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (reply != null) {
                if (reportEntryItemizationHandler != null) {
                    reportEntryItemizationHandler.endElement(uri, localName, qName);
                    if (localName.equalsIgnoreCase(REPORT_ENTRY)) {
                        ArrayList<ExpenseReportEntry> entries = reportEntryItemizationHandler.getReportEntries();
                        if (entries != null && entries.size() == 1) {
                            ((SaveReportEntryReply) reply).expRepEntDet = (ExpenseReportEntryDetail) entries.get(0);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: parsed report entries is null or zero length!");
                        }
                        reportEntryItemizationHandler = null;
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled) {
                        final String cleanChars = chars.toString().trim();
                        if (localName.equalsIgnoreCase(REPORT_TOTAL_APPROVED)) {
                            ((SaveReportEntryReply) reply).reportTotalApproved = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(REPORT_TOTAL_CLAIMED)) {
                            ((SaveReportEntryReply) reply).reportTotalClaimed = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(REPORT_TOTAL_POSTED)) {
                            ((SaveReportEntryReply) reply).reportTotalPosted = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (this.getClass().equals(SaveReportEntrySAXHandler.class)) {
                            // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName + "'.");
                            // Set the collected chars length to '0' since no sub-class of this is performing
                            // parsing and we don't recognize the tag.
                            chars.setLength(0);
                        }
                    }
                    // If this class did handle the tag, then clear the characters.
                    if (elementHandled) {
                        chars.setLength(0);
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
            }
        }
    }

}
