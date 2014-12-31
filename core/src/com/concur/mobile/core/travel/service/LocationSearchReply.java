package com.concur.mobile.core.travel.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.LocationChoice;

public class LocationSearchReply extends ServiceReply {

    // private static final String CLS_TAG = CarSearchReply.class.getSimpleName();

    public ArrayList<LocationChoice> locations;

    public LocationSearchReply() {
        locations = new ArrayList<LocationChoice>();
    }

    public static LocationSearchReply parseXMLReply(String responseXml) {

        LocationSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            LocationSearchReplySAXHandler handler = new LocationSearchReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    protected static class LocationSearchReplySAXHandler extends DefaultHandler {

        private static final String CHOICE = "LocationChoice";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        private boolean inChoice;

        // Holders for our parsed data
        private LocationSearchReply reply;
        private LocationChoice locChoice;

        protected LocationSearchReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new LocationSearchReply();
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

            if (localName.equalsIgnoreCase(CHOICE)) {
                locChoice = new LocationChoice();
                inChoice = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inChoice) {
                if (localName.equalsIgnoreCase(CHOICE)) {
                    reply.locations.add(locChoice);
                    inChoice = false;
                } else {
                    // hand off to object
                    locChoice.handleElement(localName, cleanChars);
                }
            }

            chars.setLength(0);
        }

    }

}
