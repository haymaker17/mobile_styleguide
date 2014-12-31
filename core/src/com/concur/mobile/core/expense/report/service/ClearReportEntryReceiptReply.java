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
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail.ExpenseReportEntryDetailSAXHandler;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;

public class ClearReportEntryReceiptReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = ClearReportEntryReceiptReply.class.getSimpleName();

    public ExpenseReportEntryDetail expRepEntDet;

    public static ClearReportEntryReceiptReply parseReply(String responseXml) {

        ClearReportEntryReceiptReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new ClearReportEntryReceiptSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (ClearReportEntryReceiptReply) handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    protected static class ClearReportEntryReceiptSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = ClearReportEntryReceiptReply.CLS_TAG
                + ClearReportEntryReceiptSAXHandler.class.getSimpleName();

        private static final String REPORT_ENTRY_DETAIL = "ReportEntryDetail";
        private static final String REPORT_ENTRY = "ReportEntry";

        // Contains a reference to the handler parsing the report detail object.
        private ExpenseReportEntryDetailSAXHandler reportEntryDetailHandler;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new ClearReportEntryReceiptReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (reportEntryDetailHandler == null) {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(REPORT_ENTRY_DETAIL) || localName.equalsIgnoreCase(REPORT_ENTRY)) {
                        reportEntryDetailHandler = new ExpenseReportEntryDetailSAXHandler();
                        reportEntryDetailHandler.startElement(uri, localName, qName, attributes);
                    } else {
                    }
                }
            } else {
                elementHandled = true;
                reportEntryDetailHandler.startElement(uri, localName, qName, attributes);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (reportEntryDetailHandler != null) {
                reportEntryDetailHandler.characters(ch, start, length);
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
                if (reportEntryDetailHandler != null) {
                    reportEntryDetailHandler.endElement(uri, localName, qName);
                    if (localName.equalsIgnoreCase(REPORT_ENTRY_DETAIL) || localName.equalsIgnoreCase(REPORT_ENTRY)) {
                        ArrayList<ExpenseReportEntry> entries = reportEntryDetailHandler.getReportEntries();
                        if (entries != null && entries.size() == 1) {
                            ((ClearReportEntryReceiptReply) reply).expRepEntDet = (ExpenseReportEntryDetail) entries
                                    .get(0);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: parsed report entries is null or zero length!");
                        }
                        reportEntryDetailHandler = null;
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled && this.getClass().equals(ClearReportEntryReceiptSAXHandler.class)) {
                        // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '"
                        // + localName + "'.");
                        // Set the collected chars length to '0' since no sub-class of this is performing
                        // parsing and we don't recognize the tag.
                        chars.setLength(0);
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
