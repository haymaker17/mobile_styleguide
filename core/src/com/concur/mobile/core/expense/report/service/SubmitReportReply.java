/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

import com.concur.mobile.core.expense.report.data.ExpenseReportApprover;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for the purposes of handling a reply to submitting a report.
 * 
 * @author AndrewK
 */
public class SubmitReportReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = SubmitReportReply.class.getSimpleName();

    private static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/Snowbird";

    private static final String EMPTY_NAMESPACE = "";

    /**
     * Contains a parsed expense report detail object.
     */
    public ExpenseReportDetail reportDetail;

    /**
     * Contains the report key of <code>reportDetail</code>.
     */
    public String reportKey;

    /**
     * The default approver, can be null.
     */
    public ExpenseReportApprover defaultApprover;

    /**
     * Will parse a response to a <code>SubmitReport</code> request.
     * 
     * @param responseXml
     *            the XML response body.
     * @return an instance of <code>SubmitReportReply</code> containing the parsed reply.
     */
    public static SubmitReportReply parseXMLReply(String responseXml) {

        SubmitReportReply srvReply = null;
        // First parse the <Report> element and other default elements.
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ActionStatusSAXHandler handler = new SubmitReportSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            srvReply = (SubmitReportReply) handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Parse the default attendee (if specified).
        if (srvReply != null) {
            srvReply.defaultApprover = parseDefaultApprover(responseXml);
        }

        return srvReply;
    }

    /**
     * Using Android XML SAX wrapper parser instead of old-school SAX - it's simpler, faster, more efficient, and less
     * error-prone.
     * 
     * @param responseXml
     *            the xml to parse.
     * @return an <code>Approver</code> with its fields initialized to the parsed data, or <code>null</code> if the
     *         <code>respnseXml</code> is <code>null</code>.
     */
    protected static ExpenseReportApprover parseDefaultApprover(String responseXml) {

        if (responseXml == null) {
            return null;
        }

        final ExpenseReportApprover expenseReportApprover = new ExpenseReportApprover();
        // Create a wrapper where we can reference the boolean value of whether or not
        // the XML response contains a default approver.
        final Map<String, Boolean> wrapper = new HashMap<String, Boolean>();
        wrapper.put("HAS_DEFAULT_APPROVER", Boolean.FALSE);

        RootElement root = new RootElement(NAMESPACE, "ActionStatus");
        Element approverNode = root.getChild(NAMESPACE, "Approver");
        // Add a listener to see if a default approver was specified.
        approverNode.setStartElementListener(new StartElementListener() {

            public void start(Attributes attributes) {
                wrapper.put("HAS_DEFAULT_APPROVER", Boolean.TRUE);
            }
        });

        Element approverRptKey = approverNode.getChild(EMPTY_NAMESPACE, "ApproverRptKey");
        approverRptKey.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.approverRptKey = body;
            }
        });

        Element email = approverNode.getChild(EMPTY_NAMESPACE, "Email");
        email.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.email = body;
            }
        });

        Element empKey = approverNode.getChild(EMPTY_NAMESPACE, "EmpKey");
        empKey.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.empKey = body;
            }
        });

        Element externalUserName = approverNode.getChild(EMPTY_NAMESPACE, "ExternalUserName");
        externalUserName.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.externalUserName = body;
            }
        });

        Element firstName = approverNode.getChild(EMPTY_NAMESPACE, "FirstName");
        firstName.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.firstName = body;
            }
        });

        Element lastName = approverNode.getChild(EMPTY_NAMESPACE, "LastName");
        lastName.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.lastName = body;
            }
        });

        Element loginId = approverNode.getChild(EMPTY_NAMESPACE, "LoginId");
        loginId.setEndTextElementListener(new EndTextElementListener() {

            public void end(String body) {
                expenseReportApprover.loginId = body;
            }
        });

        try {
            Xml.parse(new ByteArrayInputStream(responseXml.getBytes()), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return (wrapper.get("HAS_DEFAULT_APPROVER") ? expenseReportApprover : null);
    }

    /**
     * An extension of <code>ActionStatusSAXHandler</code> to handle parsing an 'ActionStatus' service reply with additional
     * information.
     * 
     * @author AndrewK
     */
    protected static class SubmitReportSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = SubmitReportReply.CLS_TAG + "."
                + SubmitReportSAXHandler.class.getSimpleName();

        private static final String REPORT = "Report";
        private static final String REPORT_KEY = "RptKey";

        /**
         * Contains a reference to the handler for parsing the report detail object.
         */
        private ExpenseReportDetail.ReportDetailSAXHandler reportDetailHandler;

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
                        ((SubmitReportReply) reply).reportDetail = reportDetailHandler.getReport();
                        reportDetailHandler = null;
                    } else {
                        reportDetailHandler.endElement(uri, localName, qName);
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (localName.equalsIgnoreCase(REPORT_KEY)) {
                        ((SubmitReportReply) reply).reportKey = chars.toString().trim();
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(SubmitReportSAXHandler.class)) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName + "'.");
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

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new SubmitReportReply();
        }

    }

}
