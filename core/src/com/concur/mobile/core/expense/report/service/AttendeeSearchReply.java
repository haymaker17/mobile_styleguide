/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> to handle the results of an attendee search request.
 * 
 * @author andy
 */
public class AttendeeSearchReply extends ServiceReply {

    private static final String CLS_TAG = AttendeeSearchReply.class.getSimpleName();

    /**
     * Contains the attendee search results.
     */
    public List<ExpenseReportAttendee> results;

    /**
     * The source query.
     */
    public String query;

    /**
     * Will parse the XML response body to an attendee search request.
     * 
     * @param responseXml
     *            the response XML body.
     * @return an instance of <code>SearchAttendeesReply</code> containing the list of attendees.
     */
    public static AttendeeSearchReply parseXMLReply(String responseXml) {

        AttendeeSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SearchAttendeesReplySAXHandler handler = new SearchAttendeesReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * An extension of <code>DefaultHandler</code> for parsing the attendee search response.
     * 
     * @author andy
     */
    protected static class SearchAttendeesReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = AttendeeSearchReply.CLS_TAG + "."
                + SearchAttendeesReplySAXHandler.class.getSimpleName();

        private static final String ATTENDEE_ARRAY = "ArrayOfAttendee";

        // The current attendee handler.
        private ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler attendeeHandler;

        private StringBuilder chars;

        // Contains the parsed reply.
        private AttendeeSearchReply reply;

        /**
         * Gets the parsed reply.
         * 
         * @return the parsed reply.
         */
        AttendeeSearchReply getReply() {
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
            if (attendeeHandler != null) {
                attendeeHandler.characters(ch, start, length);
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
            reply = new AttendeeSearchReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (reply != null) {
                if (attendeeHandler != null) {
                    attendeeHandler.startElement(uri, localName, qName, attributes);
                } else {
                    super.startElement(uri, localName, qName, attributes);
                    if (localName.equalsIgnoreCase(ATTENDEE_ARRAY)) {
                        attendeeHandler = new ExpenseReportAttendeeSAXHandler();
                    } else if (this.getClass().equals(SearchAttendeesReplySAXHandler.class)) {
                        // Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: unhandled tag '" + localName + "'.");
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: reply is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (reply != null) {
                if (attendeeHandler != null) {
                    if (localName.equalsIgnoreCase(ATTENDEE_ARRAY)) {
                        reply.results = attendeeHandler.getReportAttendees();
                        attendeeHandler = null;
                    } else {
                        attendeeHandler.endElement(uri, localName, qName);
                    }
                } else {
                    if (this.getClass().equals(SearchAttendeesReplySAXHandler.class)) {
                        // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled tag '" + localName + "'.");
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
            }
        }

    }

}
