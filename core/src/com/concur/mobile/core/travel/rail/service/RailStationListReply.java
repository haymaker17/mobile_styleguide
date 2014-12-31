package com.concur.mobile.core.travel.rail.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.rail.data.RailStation;

public class RailStationListReply extends ServiceReply {

    public ArrayList<RailStation> railStations;
    public String xmlReply;
    // Contains a map from railstation code to rail station object.
    public HashMap<String, RailStation> codeStationMap;

    public RailStationListReply() {
        railStations = new ArrayList<RailStation>();
        codeStationMap = new HashMap<String, RailStation>();
    }

    public static RailStationListReply parseXMLReply(String responseXml) {

        RailStationListReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RailStationListReplySAXHandler handler = new RailStationListReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class RailStationListReplySAXHandler extends DefaultHandler {

        private static final String RAIL_STATION = "RailStation";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        private boolean inStation;

        // Holders for our parsed data
        private RailStationListReply reply;
        private RailStation station;

        protected RailStationListReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new RailStationListReply();
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

            if (localName.equalsIgnoreCase(RAIL_STATION)) {
                station = new RailStation();
                inStation = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inStation) {
                if (localName.equalsIgnoreCase(RAIL_STATION)) {
                    // Sanity
                    if (station != null) {
                        reply.railStations.add(station);
                        reply.codeStationMap.put(station.stationCode, station);
                        station = null;
                    }
                    inStation = false;
                } else {
                    // Sanity
                    if (station != null) {
                        // hand off to object
                        station.handleElement(localName, cleanChars);
                    }
                }
            }

            chars.setLength(0);
        }

    }
}
