package com.concur.mobile.core.travel.rail.service;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.rail.data.RailChoice;
import com.concur.mobile.core.travel.rail.data.RailChoiceLeg;
import com.concur.mobile.core.travel.rail.data.RailChoiceSegment;
import com.concur.mobile.core.util.Const;

public class RailSearchReply extends ServiceReply {

    private static final String CLS_TAG = RailSearchReply.class.getSimpleName();

    public LinkedHashMap<String, ArrayList<RailChoice>> choiceMap;

    public RailSearchReply() {
        choiceMap = new LinkedHashMap<String, ArrayList<RailChoice>>();
    }

    protected void insertChoice(RailChoice choice) {

        String key = choice.groupId;

        ArrayList<RailChoice> choiceGroup = choiceMap.get(key);
        if (choiceGroup == null) {
            choiceGroup = new ArrayList<RailChoice>();
            choiceMap.put(key, choiceGroup);
        }

        choiceGroup.add(choice);
    }

    protected void populateLegClass() {
        // Iterate all the choices and push their class descriptions down to the legs
        // Also use this to set the Acela flag
        for (String groupId : choiceMap.keySet()) {
            ArrayList<RailChoice> choices = choiceMap.get(groupId);
            for (RailChoice choice : choices) {
                choice.populateLegClass();
            }
        }
    }

    public static RailSearchReply parseXMLReply(String responseXml) {

        RailSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RailSearchReplySAXHandler handler = new RailSearchReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * Will parse from <code>reader</code> a rail search response object.
     * 
     * @param reader
     *            the reader providing the content.
     * @return an instance of <code>RailSearchReply</code>.
     */
    public static RailSearchReply parseXmlReply(Reader reader) {
        RailSearchReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RailSearchReplySAXHandler handler = new RailSearchReplySAXHandler();
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

    protected static class RailSearchReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = RailSearchReply.CLS_TAG + "."
                + RailSearchReplySAXHandler.class.getSimpleName();

        private static final String RAIL_CHOICE = "RailChoice";
        private static final String SEGMENT_OPTION = "SegmentOption";
        private static final String TRAIN = "Flight";
        private static final String VIOLATIONS = "Violations";
        private static final String VIOLATION = "Violation";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inRailChoice;
        private boolean inSegment;
        private boolean inTrain;
        private boolean inViolation;

        // Holders for our parsed data
        private RailSearchReply reply;
        private RailChoice choice;
        private RailChoiceSegment segment;
        private RailChoiceLeg leg;
        private Violation violation;

        protected RailSearchReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new RailSearchReply();
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            reply.populateLegClass();
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

            if (localName.equalsIgnoreCase(RAIL_CHOICE)) {
                choice = new RailChoice();
                inRailChoice = true;
            } else if (localName.equalsIgnoreCase(SEGMENT_OPTION)) {
                segment = new RailChoiceSegment();
                inSegment = true;
            } else if (localName.equalsIgnoreCase(TRAIN)) {
                leg = new RailChoiceLeg();
                inTrain = true;
            } else if (localName.equalsIgnoreCase(VIOLATIONS)) {
                if (inRailChoice) {
                    if (choice != null) {
                        choice.violations = new ArrayList<Violation>();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: choice is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: violations found outside of rail choice!");
                }
            } else if (localName.equalsIgnoreCase(VIOLATION)) {
                if (inRailChoice) {
                    if (choice != null) {
                        inViolation = true;
                        violation = new Violation();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: choice is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: violations found outside of rail choice!");
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

            if (inTrain) {
                if (localName.equalsIgnoreCase(TRAIN)) {
                    segment.legs.add(leg);
                    inTrain = false;
                } else {
                    // hand off to object
                    leg.handleElement(localName, cleanChars);
                }
            } else if (inSegment) {
                if (localName.equalsIgnoreCase(SEGMENT_OPTION)) {
                    choice.segments.add(segment);
                    inSegment = false;
                } else {
                    // hand off to object
                    segment.handleElement(localName, cleanChars);
                }
            } else if (inViolation) {
                if (violation != null) {
                    if (localName.equalsIgnoreCase(VIOLATION)) {
                        choice.violations.add(violation);
                        violation = null;
                        inViolation = false;
                    } else {
                        // hand off to object.
                        violation.handleElement(localName, cleanChars);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: violation is null!");
                }
            } else if (inRailChoice) {
                if (localName.equalsIgnoreCase(RAIL_CHOICE)) {
                    reply.insertChoice(choice);
                    inRailChoice = false;
                } else {
                    // hand off to object
                    choice.handleElement(localName, cleanChars);
                }
            } else {
                // Top-level elements
                handleResponseElement(localName, cleanChars);
            }

            chars.setLength(0);
        }

        /**
         * Handle the response level elements
         * 
         * @param localName
         */
        private void handleResponseElement(String localName, String cleanChars) {

            if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = cleanChars;
            }
        }

    }
}
