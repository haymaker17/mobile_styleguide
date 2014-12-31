package com.concur.mobile.core.travel.air.service;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.air.data.AirBookingSegment;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;

public class AirFilterReply extends ServiceReply {

    private static final String CLS_TAG = AirFilterReply.class.getSimpleName();

    public String airlineCode;
    public String numStops;

    public ArrayList<AirChoice> choices;

    public AirFilterReply() {
        choices = new ArrayList<AirChoice>();
    }

    // //////////////////////////////////////////////////////////////////////
    // At the signpost ahead: XML parsing
    // //////////////////////////////////////////////////////////////////////

    public static AirFilterReply parseXMLReply(String responseXml) {

        AirFilterReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AirFilterReplySAXHandler handler = new AirFilterReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * Will parse from <code>reader</code> an air filter response object.
     * 
     * @param reader
     *            the reader providing the content.
     * @return an instance of <code>AirFilterReply</code>.
     */
    public static AirFilterReply parseXmlReply(Reader reader) {
        AirFilterReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AirFilterReplySAXHandler handler = new AirFilterReplySAXHandler();
            parser.parse(new InputSource(reader), handler);
            reply = handler.getReply();
            // If no status has been parsed, default to success.
            if (reply.mwsStatus == null || reply.mwsStatus.length() == 0) {
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
                Log.w(Const.LOG_TAG, CLS_TAG + ".parseXmlReply: defaulting status to success!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class AirFilterReplySAXHandler extends DefaultHandler {

        private static final String AIR_CHOICE = "AirChoice";
        private static final String AIR_SEGMENT = "AirSegment";
        private static final String FLIGHT = "Flight";
        private static final String VIOLATION = "Violation";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inAirChoice;
        private boolean inAirSegment;
        private boolean inFlight;
        private boolean inViolation;

        // Holders for our parsed data
        private AirFilterReply reply;
        private AirChoice airChoice;
        private AirBookingSegment airSegment;
        private Flight flight;
        private Violation violation;

        protected AirFilterReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new AirFilterReply();

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            chars.append(ch, start, length);
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(AIR_CHOICE)) {
                inAirChoice = true;
                airChoice = new AirChoice();
            } else if (localName.equalsIgnoreCase(AIR_SEGMENT)) {
                inAirSegment = true;
                airSegment = new AirBookingSegment();
            } else if (localName.equalsIgnoreCase(FLIGHT)) {
                inFlight = true;
                flight = new Flight();
            } else if (localName.equalsIgnoreCase(VIOLATION)) {
                inViolation = true;
                violation = new Violation();
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inFlight) {
                if (localName.equalsIgnoreCase(FLIGHT)) {
                    airSegment.flights.add(flight);
                    inFlight = false;
                } else {
                    flight.handleElement(localName, cleanChars);
                }
            } else if (inViolation) {
                if (localName.equalsIgnoreCase(VIOLATION)) {
                    airChoice.violations.add(violation);
                    inViolation = false;
                } else {
                    violation.handleElement(localName, cleanChars);
                }
            } else if (inAirSegment) {
                if (localName.equalsIgnoreCase(AIR_SEGMENT)) {
                    airChoice.segments.add(airSegment);
                    inAirSegment = false;
                } else {
                    airSegment.handleElement(localName, cleanChars);
                }
            } else if (inAirChoice) {
                if (localName.equalsIgnoreCase(AIR_CHOICE)) {
                    reply.choices.add(airChoice);
                    inAirChoice = false;
                } else {
                    airChoice.handleElement(localName, cleanChars);
                }
            } else if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = cleanChars;
            }

            chars.setLength(0);
        }

    }
}
