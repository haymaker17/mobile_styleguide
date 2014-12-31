/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for handling the result of removing an entry from a report.
 * 
 * @author AndrewK
 */
public class RemoveReportExpenseReply extends ServiceReply {

    private static final String CLS_TAG = RemoveReportExpenseReply.class.getSimpleName();

    /**
     * Contains the parsed expense report detail.
     */
    public ExpenseReportDetail reportDetail;

    /**
     * Will parse an XML response body and return an instance of <code>RemoveReportExpenseReply</code> containing the result.
     * 
     * @param responseXml
     *            the XML response body.
     * @return and instance of <code>RemoveReportExpenseReply</code>.
     */
    public static RemoveReportExpenseReply parseXMLReply(String responseXml) {

        RemoveReportExpenseReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RemoveReportExpenseReplySAXHandler handler = new RemoveReportExpenseReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class RemoveReportExpenseReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = RemoveReportExpenseReply.CLS_TAG + "."
                + RemoveReportExpenseReplySAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String REPORT = "Report";

        private RemoveReportExpenseReply reply;

        /**
         * Contains a reference to the handler for parsing the report detail object.
         */
        private ExpenseReportDetail.ReportDetailSAXHandler reportDetailHandler;

        private StringBuilder chars;

        RemoveReportExpenseReply getReply() {
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
                    reply = new RemoveReportExpenseReply();
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
                } else if (localName.equalsIgnoreCase(STATUS)) {
                    reply.mwsStatus = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    // No-op.
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
