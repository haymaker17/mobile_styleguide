/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * Models a service reply for report approval/rejection.
 * 
 * @author AndrewK
 */
public class ReportApproveRejectServiceReply extends ServiceReply {

    private static final String CLS_TAG = ReportApproveRejectServiceReply.class.getSimpleName();

    public ArrayList<ExpenseReport> reportsToApprove;

    public String xmlReply;

    public static ReportApproveRejectServiceReply parseReply(String responseXml) {

        ReportApproveRejectServiceReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ReportApproveRejectSAXHandler handler = new ReportApproveRejectSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = handler.getReply();
            srvReply.xmlReply = responseXml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    /**
     * An extension of <code>DefaultHandler</code> to handle parsing a service reply.
     * 
     * @author AndrewK
     */
    protected static class ReportApproveRejectSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ReportApproveRejectServiceReply.CLS_TAG + "."
                + ReportApproveRejectSAXHandler.class.getSimpleName();

        private static final String ACTION_STATUS = "ActionStatus";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String STATUS = "Status";
        private static final String REPORT_TO_APPROVE_LIST = "ReportToApproveList";

        // The reply being parsed.
        private ReportApproveRejectServiceReply reply;

        private StringBuilder chars;

        /**
         * A reference to a handler for parsing reports.
         */
        private ExpenseReport.ReportListSAXHandler reportsHandler = null;

        ReportApproveRejectServiceReply getReply() {
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
            if (reportsHandler != null) {
                reportsHandler.characters(ch, start, length);
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

            if (reportsHandler != null) {
                reportsHandler.startElement(uri, localName, qName, attributes);
            } else {
                if (localName.equalsIgnoreCase(REPORT_TO_APPROVE_LIST)) {
                    reportsHandler = new ExpenseReport.ReportListSAXHandler();
                } else if (localName.equalsIgnoreCase(ACTION_STATUS)) {
                    reply = new ReportApproveRejectServiceReply();
                }
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

            if (localName.equalsIgnoreCase(REPORT_TO_APPROVE_LIST)) {
                if (reportsHandler != null) {
                    reply.reportsToApprove = reportsHandler.getReports();
                    reportsHandler = null;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null reports handler!");
                }
            } else {
                if (reportsHandler != null) {
                    reportsHandler.endElement(uri, localName, qName);
                } else {
                    if (localName.equalsIgnoreCase(ERROR_MESSAGE)) {
                        reply.mwsErrorMessage = chars.toString().trim();
                    } else if (localName.equalsIgnoreCase(STATUS)) {
                        reply.mwsStatus = chars.toString().trim();
                    }
                }
            }
            chars.setLength(0);
        }
    }
}
