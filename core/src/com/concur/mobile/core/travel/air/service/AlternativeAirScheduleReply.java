/**
 * Alternative flight schedule reply will parse and handle in this class
 * 
 * @author sunill
 * */
package com.concur.mobile.core.travel.air.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.air.data.AlternativeCOS;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.travel.data.SegmentOption;
import com.concur.mobile.core.util.Const;

public class AlternativeAirScheduleReply extends ServiceReply {

    public List<SegmentOption> listofSegmentOptions;

    public AlternativeAirScheduleReply() {
        listofSegmentOptions = new ArrayList<SegmentOption>();
    }

    // //////////////////////////////////////////////////////////////////////
    // At the signpost ahead: XML parsing
    // //////////////////////////////////////////////////////////////////////

    public static AlternativeAirScheduleReply parseXMLReply(String responseXml) {

        AlternativeAirScheduleReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AlternativeAirScheduleReplySAXHandler handler = new AlternativeAirScheduleReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class AlternativeAirScheduleReplySAXHandler extends DefaultHandler {

        private static final String SEG_OPT = "SegmentOption";
        private static final String FLIGHT = "Flight";
        private static final String COS = "CoS";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        private boolean inSegOpt;
        private boolean inFlight;

        // Holders for our parsed data
        private AlternativeAirScheduleReply reply;
        private SegmentOption segmentOption;
        private Flight flight;
        private AlternativeCOS cos;

        protected AlternativeAirScheduleReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new AlternativeAirScheduleReply();

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

            if (localName.equalsIgnoreCase(SEG_OPT)) {
                inSegOpt = true;
                segmentOption = new SegmentOption();
            } else if (localName.equalsIgnoreCase(FLIGHT)) {
                inFlight = true;
                flight = new Flight();
                cos = new AlternativeCOS();
            } else if (localName.equalsIgnoreCase(COS)) {
                if (cos != null && flight != null && inFlight) {
                    cos.setCabin(attributes.getValue("cabin"));
                    cos.setSeats(attributes.getValue("seats"));
                    flight.setCOS(cos);
                }
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
                    segmentOption.flights.add(flight);
                    inFlight = false;
                } else {
                    flight.handleElement(localName, cleanChars);
                }
            } else if (inSegOpt) {
                if (localName.equalsIgnoreCase(SEG_OPT)) {
                    reply.listofSegmentOptions.add(segmentOption);
                    inSegOpt = false;
                } else {
                    segmentOption.handleElement(localName, cleanChars);
                }
            }
            chars.setLength(0);
        }

    }
}
