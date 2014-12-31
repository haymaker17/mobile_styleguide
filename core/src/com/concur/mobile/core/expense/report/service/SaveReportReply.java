/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for parsing the response to a <code>SaveReportRequest</code>.
 * 
 * @author AndrewK
 */
public class SaveReportReply extends ActionStatusServiceReply {

    public static final String CLS_TAG = SaveReportReply.class.getSimpleName();

    /**
     * Contains a parsed expense report detail object.
     */
    public ExpenseReportDetail reportDetail;

    /**
     * Contains the report key of <code>reportDetail</code>.
     */
    public String reportKey;

    /**
     * Will parse a response to a <code>SaveReport</code> request.
     * 
     * @param responseXml
     *            the XML response body.
     * @return an instance of <code>SaveReportReply</code> containing the parsed reply.
     */
    public static SaveReportReply parseXMLReply(String responseXml) {

        SaveReportReply srvReply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new SaveReportSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (SaveReportReply) handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return srvReply;
    }

    /**
     * An extension of <code>ActionStatusSAXHandler</code> for parsing a response to a <code>SaveReport</code> request.
     * 
     * @author AndrewK
     */
    protected static class SaveReportSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = SaveReportReply.CLS_TAG + "."
                + SaveReportSAXHandler.class.getSimpleName();

        private static final String REPORT = "Report";
        private static final String REPORT_KEY = "RptKey";

        /**
         * Contains a reference to the handler for parsing the report detail object.
         */
        private ExpenseReportDetail.ReportDetailSAXHandler reportDetailHandler;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new SaveReportReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (reportDetailHandler != null) {
                reportDetailHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (reportDetailHandler == null) {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(REPORT)) {
                        reportDetailHandler = new ExpenseReportDetail.ReportDetailSAXHandler();
                        reportDetailHandler.initForParsing();
                        elementHandled = true;
                    }
                }
            } else {
                elementHandled = true;
                reportDetailHandler.startElement(uri, localName, qName, attributes);
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
                if (reportDetailHandler != null) {
                    if (localName.equalsIgnoreCase(REPORT)) {
                        // Finished parsing the report.
                        reportDetailHandler.finishForParsing();
                        ((SaveReportReply) reply).reportDetail = reportDetailHandler.getReport();
                        reportDetailHandler = null;
                    } else {
                        reportDetailHandler.endElement(uri, localName, qName);
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (localName.equalsIgnoreCase(REPORT_KEY)) {
                        ((SaveReportReply) reply).reportKey = chars.toString().trim();
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(SaveReportSAXHandler.class)) {
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
