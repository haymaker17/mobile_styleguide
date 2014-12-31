package com.concur.mobile.core.travel.air.service;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.air.data.AirDictionaries;
import com.concur.mobile.core.travel.air.data.AirDictionaries.Pair;
import com.concur.mobile.core.travel.air.data.AirlineEntry;
import com.concur.mobile.core.travel.data.Benchmark;
import com.concur.mobile.core.travel.data.TravelPointsBank;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class AirSearchReply extends ServiceReply {

    private static final String CLS_TAG = AirSearchReply.class.getSimpleName();

    public Calendar searchStart;
    public Calendar searchEnd;

    public Map<Integer, List<AirlineEntry>> stopGroups;

    public Benchmark benchmark;

    public TravelPointsBank travelPointsBank;

    public AirSearchReply() {
        stopGroups = new HashMap<Integer, List<AirlineEntry>>();
    }

    public int getResultCount() {
        int count = 0;

        Collection<List<AirlineEntry>> groups = stopGroups.values();
        for (List<AirlineEntry> ael : groups) {
            for (AirlineEntry ae : ael) {
                count += ae.numChoices;
            }
        }

        return count;
    }

    // //////////////////////////////////////////////////////////////////////
    // At the signpost ahead: XML parsing
    // //////////////////////////////////////////////////////////////////////

    public static AirSearchReply parseXMLReply(String responseXml) {

        AirSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AirSearchReplySAXHandler handler = new AirSearchReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * Will parse from <code>reader</code> an air search response object.
     * 
     * @param reader
     *            the reader providing the content.
     * @return an instance of <code>AirSearchReply</code>.
     */
    public static AirSearchReply parseXmlReply(Reader reader) {
        AirSearchReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AirSearchReplySAXHandler handler = new AirSearchReplySAXHandler();
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

    protected static class AirSearchReplySAXHandler extends DefaultHandler {

        private static final String PAIR = "AirPair";
        private static final String KEY = "Key";
        private static final String VALUE = "Value";
        private static final String AIRPORT_CITY_CODES = "AirportCityCodes";
        private static final String AIRPORT_CODES = "AirportCodes";
        private static final String EQUIPMENT_CODES = "EquipmentCodes";
        private static final String PREFERENCE_RANKINGS = "PreferenceRankings";
        private static final String VENDOR_CODES = "VendorCodes";
        private static final String STOPS_GROUP = "StopsGroup";
        private static final String AIRLINE_ENTRY = "AirlineEntry";
        private static final String BENCHMARK = "Benchmark";
        private static final String TRAVEL_POINTS_BANK = "TravelPointsBank";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inPair;
        private boolean inAirportCityCodes;
        private boolean inAirportCodes;
        private boolean inEquipmentCodes;
        private boolean inPreferenceRankings;
        private boolean inVendorCodes;
        private boolean inStopsGroup;
        private boolean inAirlineEntry;
        private boolean inBenchmark;
        private boolean inTravelPointsBank;

        // Holders for our parsed data
        private AirSearchReply reply;
        private Pair pair;
        private AirlineEntry airlineEntry;
        private int stopGroupNum;
        private List<AirlineEntry> airlineEntryList;

        private Map<String, String> currentDict;

        public Benchmark benchmark;

        protected AirSearchReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new AirSearchReply();

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

            if (localName.equalsIgnoreCase(AIRPORT_CITY_CODES)) {
                inAirportCityCodes = true;
                currentDict = AirDictionaries.airportCityCodeMap;
            } else if (localName.equalsIgnoreCase(AIRPORT_CODES)) {
                inAirportCodes = true;
                currentDict = AirDictionaries.airportCodeMap;
            } else if (localName.equalsIgnoreCase(EQUIPMENT_CODES)) {
                inEquipmentCodes = true;
                currentDict = AirDictionaries.equipmentCodeMap;
            } else if (localName.equalsIgnoreCase(PREFERENCE_RANKINGS)) {
                inPreferenceRankings = true;
                currentDict = AirDictionaries.preferenceRankMap;
            } else if (localName.equalsIgnoreCase(VENDOR_CODES)) {
                inVendorCodes = true;
                currentDict = AirDictionaries.vendorCodeMap;
            } else if (localName.equalsIgnoreCase(PAIR)) {
                pair = new Pair();
                inPair = true;
            } else if (localName.equalsIgnoreCase(STOPS_GROUP)) {
                airlineEntryList = new ArrayList<AirlineEntry>();
                inStopsGroup = true;
            } else if (localName.equalsIgnoreCase(AIRLINE_ENTRY)) {
                airlineEntry = new AirlineEntry();
                inAirlineEntry = true;
            } else if (localName.equalsIgnoreCase(BENCHMARK)) {
                benchmark = new Benchmark();
                inBenchmark = true;
            } else if (localName.equalsIgnoreCase(TRAVEL_POINTS_BANK)) {
                reply.travelPointsBank = new TravelPointsBank();
                inTravelPointsBank = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inPair) {
                if (localName.equalsIgnoreCase(PAIR)) {
                    AirDictionaries.addPairToMap(currentDict, pair);
                    inPair = false;
                } else if (localName.equalsIgnoreCase(KEY)) {
                    pair.key = cleanChars;
                } else if (localName.equalsIgnoreCase(VALUE)) {
                    pair.value = cleanChars;
                }
            } else if (inAirportCityCodes) {
                if (localName.equalsIgnoreCase(AIRPORT_CITY_CODES)) {
                    inAirportCityCodes = false;
                    currentDict = null;
                }
            } else if (inAirportCodes) {
                if (localName.equalsIgnoreCase(AIRPORT_CODES)) {
                    inAirportCodes = false;
                    currentDict = null;
                }
            } else if (inEquipmentCodes) {
                if (localName.equalsIgnoreCase(EQUIPMENT_CODES)) {
                    inEquipmentCodes = false;
                    currentDict = null;
                }
            } else if (inPreferenceRankings) {
                if (localName.equalsIgnoreCase(PREFERENCE_RANKINGS)) {
                    inPreferenceRankings = false;
                    currentDict = null;
                }
            } else if (inVendorCodes) {
                if (localName.equalsIgnoreCase(VENDOR_CODES)) {
                    inVendorCodes = false;
                    currentDict = null;
                }
            } else if (inAirlineEntry) {
                if (localName.equalsIgnoreCase(AIRLINE_ENTRY)) {
                    airlineEntryList.add(airlineEntry);
                    inAirlineEntry = false;
                } else {
                    airlineEntry.handleElement(localName, cleanChars);
                }
            } else if (inStopsGroup) {
                if (localName.equalsIgnoreCase(STOPS_GROUP)) {
                    reply.stopGroups.put(stopGroupNum, airlineEntryList);
                    inStopsGroup = false;
                } else {
                    handleElement(localName, cleanChars);
                }
            } else if (inBenchmark) {
                if (localName.equalsIgnoreCase(BENCHMARK)) {
                    reply.benchmark = benchmark;
                    inBenchmark = false;
                } else {
                    benchmark.handleElement(localName, cleanChars);
                }
            } else if (inTravelPointsBank) {
                if (localName.equalsIgnoreCase(TRAVEL_POINTS_BANK)) {
                    inTravelPointsBank = false;
                } else {
                    reply.travelPointsBank.handleElement(localName, cleanChars);
                }
            } else {
                // Top-level elements
                handleElement(localName, cleanChars);
            }

            chars.setLength(0);
        }

        /**
         * Handle the response level elements
         * 
         * @param localName
         */
        private void handleElement(String localName, String cleanChars) {

            if (localName.equalsIgnoreCase("Start")) {
                reply.searchStart = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("End")) {
                reply.searchEnd = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("NumStops")) {
                // This in StopsGroup, not the root
                stopGroupNum = Parse.safeParseInteger(cleanChars);
            } else if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = cleanChars;
            }

        }

    }
}
