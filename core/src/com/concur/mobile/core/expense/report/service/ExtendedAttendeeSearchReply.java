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

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ActionStatusServiceReply.ActionStatusSAXHandler;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> to handle the results of an attendee search request.
 * 
 * @author andy
 */
public class ExtendedAttendeeSearchReply extends ActionStatusServiceReply {

    private static final String CLS_TAG = ExtendedAttendeeSearchReply.class.getSimpleName();

    /**
     * Contains the attendee search results.
     */
    public List<ExpenseReportAttendee> results;

    /**
     * Contains the list of column definitions to be applied over <code>results</code>.
     */
    public List<ExpenseReportFormField> columnDefinitions;

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
    public static ExtendedAttendeeSearchReply parseXMLReply(String responseXml) {

        ExtendedAttendeeSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExtendedSearchAttendeesReplySAXHandler handler = new ExtendedSearchAttendeesReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = (ExtendedAttendeeSearchReply) handler.getReply();
            // If no status has been set on the reply, then there was no 'ActionStatus' object returned indicating
            // that the response was successful. An response that resulted in an error returns an 'ActionStatus' object.
            // AVK - 6/27/2012.
            if (reply.mwsStatus == null) {
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            }
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
    protected static class ExtendedSearchAttendeesReplySAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = ExtendedAttendeeSearchReply.CLS_TAG + "."
                + ExtendedSearchAttendeesReplySAXHandler.class.getSimpleName();

        private static final String ATTENDEES_WITH_COLUMN_INFO = "AttendeesWithColumnInfo";

        // The current attendee handler.
        private ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler attendeeHandler;

        @Override
        protected ActionStatusServiceReply createReply() {
            return new ExtendedAttendeeSearchReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (attendeeHandler != null) {
                attendeeHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
            }
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
                    elementHandled = true;
                } else {
                    elementHandled = false;
                    super.startElement(uri, localName, qName, attributes);
                    if (!elementHandled) {
                        if (localName.equalsIgnoreCase(ATTENDEES_WITH_COLUMN_INFO)) {
                            attendeeHandler = new ExpenseReportAttendeeSAXHandler();
                            elementHandled = true;
                        } else if (this.getClass().equals(ExtendedSearchAttendeesReplySAXHandler.class)) {
                            // Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: unhandled tag '" + localName + "'.");
                        }
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
                    if (localName.equalsIgnoreCase(ATTENDEES_WITH_COLUMN_INFO)) {
                        ((ExtendedAttendeeSearchReply) reply).results = attendeeHandler.getReportAttendees();
                        ((ExtendedAttendeeSearchReply) reply).columnDefinitions = attendeeHandler
                                .getColumnDefinitions();
                        attendeeHandler = null;
                    } else {
                        attendeeHandler.endElement(uri, localName, qName);
                    }
                    elementHandled = true;
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled) {
                        if (this.getClass().equals(ExtendedSearchAttendeesReplySAXHandler.class)) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled tag '" + localName + "'.");
                            elementHandled = true;
                        }
                    }
                }
                // If this class did handle the tag, then clear the characters.
                if (elementHandled) {
                    chars.setLength(0);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: reply is null!");
            }
        }

    }

}
