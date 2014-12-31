package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.service.KeyedServiceReply;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class AddToReportReply extends ServiceReply {

    private static final String CLS_TAG = AddToReportReply.class.getSimpleName();

    public ArrayList<KeyedServiceReply> smartExpenseIds;

    public ExpenseReportDetail reportDetail;

    public String reportKey;

    /**
     * Contains the original body of the reply.
     */
    String xmlReply;

    public static AddToReportReply parseXMLReply(String responseXml) {

        AddToReportReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AddToReportReplySAXHandler handler = new AddToReportReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            if (reply.reportDetail != null) {
                StringBuilder strBldr = new StringBuilder();
                ExpenseReportDetail.ReportDetailSAXHandler.serializeAllToXML(strBldr, reply.reportDetail);
                reply.xmlReply = strBldr.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class AddToReportReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = AddToReportReply.CLS_TAG + "."
                + AddToReportReplySAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String REPORT = "Report";
        private static final String REPORT_KEY = "RptKey";
        private static final String SMART_EXPID = "SmartExpenseId";

        // The main reply
        private AddToReportReply reply;

        // The list of sub-replies being parsed
        private ArrayList<KeyedServiceReply> subReplies;

        // The sub-reply being parsed.
        private KeyedServiceReply keyReply;

        /**
         * Contains a reference to the handler for parsing the report detail object.
         */
        private ExpenseReportDetail.ReportDetailSAXHandler reportDetailHandler;

        private StringBuilder chars;

        AddToReportReply getReply() {
            return reply;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (reportDetailHandler != null) {
                reportDetailHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            chars = new StringBuilder();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (reportDetailHandler != null) {
                reportDetailHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                if (reply == null) {
                    // Should be the very first status (global)
                    reply = new AddToReportReply();
                } else {
                    // A sub-element status
                    keyReply = new KeyedServiceReply();
                }
            } else if (localName.equalsIgnoreCase(SMART_EXPID)) {
                if (subReplies == null) {
                    subReplies = new ArrayList<KeyedServiceReply>();
                }
            } else if (localName.equalsIgnoreCase(REPORT)) {
                reportDetailHandler = new ExpenseReportDetail.ReportDetailSAXHandler();
                reportDetailHandler.initForParsing();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (reply != null) {
                if (reportDetailHandler != null) {
                    if (localName.equalsIgnoreCase(REPORT)) {
                        // Finished parsing the report.
                        reportDetailHandler.finishForParsing();
                        reply.reportDetail = reportDetailHandler.getReport();
                        reportDetailHandler = null;
                    } else {
                        reportDetailHandler.endElement(uri, localName, qName);
                    }
                } else if (localName.equalsIgnoreCase(ERROR_MESSAGE)) {
                    reply.mwsErrorMessage = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(REPORT_KEY)) {
                    reply.reportKey = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(STATUS)) {
                    String c = chars.toString().trim();
                    if (keyReply != null) {
                        // Sub-reply
                        keyReply.mwsStatus = c;
                    } else {
                        reply.mwsStatus = c;
                    }
                } else if (localName.equalsIgnoreCase(SMART_EXPID)) {
                    keyReply.key = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    if (keyReply != null) {
                        // Ending a sub-reply
                        subReplies.add(keyReply);
                        keyReply = null;
                    } else {
                        reply.smartExpenseIds = subReplies;
                    }
                } else {
                    // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML node -- '" + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply reference is null!");
            }

            chars.setLength(0);
        }
    }

}
