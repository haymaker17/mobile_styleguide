/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.service.ActionStatusServiceReply.ActionStatusSAXHandler;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ActionStatusServiceReply</code> for the purposes of handling the result of performing an attendee save.
 */
public class AttendeeSaveReply extends ActionStatusServiceReply {

    private static String CLS_TAG = AttendeeSaveReply.class.getSimpleName();

    /**
     * Contains the parsed attendee.
     */
    public ExpenseReportAttendee attendee;

    // A list of any duplicate attendees that were in the save response
    public List<ExpenseReportAttendee> duplicateAttendees;

    /**
     * Will parse the XML response body to an attendee search request.
     * 
     * @param responseXml
     *            the response XML body.
     * @return an instance of <code>SearchAttendeesReply</code> containing the list of attendees.
     */
    public static AttendeeSaveReply parseXMLReply(String responseXml) {

        AttendeeSaveReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SaveAttendeeSAXHandler handler = new SaveAttendeeSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = (AttendeeSaveReply) handler.getReply();
            // Currently, the MWS returns a status of error if there is in fact an error; otherwise,
            // it returns a parsed attendee object! So, if the client did parse an attendee object, then
            // set to success.
            if (reply.attendee != null) {
                reply.mwsStatus = Const.STATUS_SUCCESS;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * An extension of <code>ActionStatusSAXHandler</code> for parsing a response to a <code>SaveAttendee</code> request.
     * 
     * @author AndrewK
     */
    protected static class SaveAttendeeSAXHandler extends ActionStatusSAXHandler {

        private static final String CLS_TAG = AttendeeSaveReply.CLS_TAG + "."
                + SaveAttendeeSAXHandler.class.getSimpleName();

        private static final String ATTENDEE = "Attendee";
        private static final String DUPLICATES = "DuplicateAttendees";

        /**
         * Contains a reference to the handler for parsing the attendee object.
         */
        private ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler attendeeHandler;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#createReply()
         */
        @Override
        protected ActionStatusServiceReply createReply() {
            return new AttendeeSaveReply();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#characters(char[], int, int)
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
         * @see com.concur.mobile.service.ActionStatusServiceReply.ActionStatusSAXHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (attendeeHandler == null) {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(ATTENDEE)) {
                        attendeeHandler = new ExpenseReportAttendee.ExpenseReportAttendeeSAXHandler();
                        attendeeHandler.startElement(uri, localName, qName, attributes);
                        elementHandled = true;
                    }
                }
            } else {
                if (localName.equalsIgnoreCase(DUPLICATES)) {
                    // Into the duplicate list
                    ((AttendeeSaveReply) reply).duplicateAttendees = new ArrayList<ExpenseReportAttendee>();
                    attendeeHandler.inDuplicates = true;
                    attendeeHandler.startElement(uri, localName, qName, attributes);
                } else {
                    attendeeHandler.startElement(uri, localName, qName, attributes);
                }
                elementHandled = true;
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
                if (attendeeHandler != null) {
                    if (localName.equalsIgnoreCase(ATTENDEE)) {
                        attendeeHandler.endElement(uri, localName, qName);
                        if (((AttendeeSaveReply) reply).duplicateAttendees != null) {
                            // Grab the last parsed attendee for our duplicate list
                            ((AttendeeSaveReply) reply).duplicateAttendees.add(attendeeHandler.reportAttendee);
                        } else {
                            List<ExpenseReportAttendee> attendees = attendeeHandler.getReportAttendees();
                            if (attendees != null && attendees.size() > 0) {
                                ((AttendeeSaveReply) reply).attendee = attendees.get(0);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: attendee list is empty/null!");
                            }
                        }
                    } else {
                        attendeeHandler.endElement(uri, localName, qName);
                    }
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled && this.getClass().equals(SaveAttendeeSAXHandler.class)) {
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

        @Override
        public void endDocument() throws SAXException {
            attendeeHandler = null;
            super.endDocument();
        }

    }

}
