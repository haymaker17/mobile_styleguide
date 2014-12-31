package com.concur.mobile.core.travel.car.service;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.car.data.CarChain;
import com.concur.mobile.core.travel.car.data.CarChoice;
import com.concur.mobile.core.travel.car.data.CarDescription;
import com.concur.mobile.core.travel.car.data.CarLocation;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class CarSearchReply extends ServiceReply {

    private static final String CLS_TAG = CarSearchReply.class.getSimpleName();

    public String pickupIATA;
    public Calendar pickupDateTime;
    public String dropoffIATA;
    public Calendar dropoffDateTime;

    public ArrayList<CarDescription> carDescriptions;
    public ArrayList<CarChain> carChains;
    public ArrayList<CarLocation> carLocations;
    public ArrayList<CarChoice> carChoices;

    public CarSearchReply() {
        carDescriptions = new ArrayList<CarDescription>();
        carChains = new ArrayList<CarChain>();
        carLocations = new ArrayList<CarLocation>();
        carChoices = new ArrayList<CarChoice>();
    }

    public CarChoice findCarById(String id) {
        CarChoice choice = null;

        if (carChoices != null) {
            for (int i = 0, size = carChoices.size(); i < size; i++) {
                CarChoice cc = carChoices.get(i);
                if (cc.carId.equals(id)) {
                    choice = cc;
                    break;
                }
            }
        }

        return choice;
    }

    // //////////////////////////////////////////////////////////////////////
    // At the signpost ahead: XML parsing
    // //////////////////////////////////////////////////////////////////////

    public static CarSearchReply parseXMLReply(String responseXml) {

        CarSearchReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CarSearchReplySAXHandler handler = new CarSearchReplySAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = handler.getReply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

    /**
     * Will parse from <code>reader</code> a car search response object.
     * 
     * @param reader
     *            the reader providing the content.
     * @return an instance of <code>CarSearchReply</code>.
     */
    public static CarSearchReply parseXmlReply(Reader reader) {
        CarSearchReply reply = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CarSearchReplySAXHandler handler = new CarSearchReplySAXHandler();
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

    protected static class CarSearchReplySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = CarSearchReply.CLS_TAG + "."
                + CarSearchReplySAXHandler.class.getSimpleName();

        private static final String CAR_DESCRIPTIONS = "CarDescriptions";
        private static final String DESCRIPTION = "CarDescription";
        private static final String CAR_CHAINS = "CarChains";
        private static final String CHAIN = "CarChain";
        private static final String CAR_LOCATIONS = "CarLocations";
        private static final String LOCATION = "CarLocation";
        private static final String CAR_CHOICES = "CarChoices";
        private static final String CHOICE = "CarChoice";
        private static final String VIOLATION = "Violation";
        private static final String CAR_SHOP_RESPONSE = "CarShopResponse";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inDescription;
        private boolean inChain;
        private boolean inLocation;
        private boolean inChoice;
        private boolean inViolation;

        // Holders for our parsed data
        private CarSearchReply reply;
        private CarDescription carDesc;
        private CarChain carChain;
        private CarLocation carLoc;
        private CarChoice carChoice;
        private Violation carViolation;

        protected CarSearchReply getReply() {
            return reply;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            reply = new CarSearchReply();
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

            if (localName.equalsIgnoreCase(DESCRIPTION)) {
                carDesc = new CarDescription();
                inDescription = true;
            } else if (localName.equalsIgnoreCase(CHAIN)) {
                carChain = new CarChain();
                inChain = true;
            } else if (localName.equalsIgnoreCase(LOCATION)) {
                carLoc = new CarLocation();
                inLocation = true;
            } else if (localName.equalsIgnoreCase(CHOICE)) {
                carChoice = new CarChoice();
                inChoice = true;
            } else if (inChoice && localName.equalsIgnoreCase(VIOLATION)) {
                carViolation = new Violation();
                inViolation = true;
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            final String cleanChars = chars.toString().trim();

            if (inDescription) {
                if (localName.equalsIgnoreCase(DESCRIPTION)) {
                    reply.carDescriptions.add(carDesc);
                    inDescription = false;
                } else {
                    // hand off to object
                    carDesc.handleElement(localName, cleanChars);
                }
            } else if (inChain) {
                if (localName.equalsIgnoreCase(CHAIN)) {
                    reply.carChains.add(carChain);
                    inChain = false;
                } else {
                    // hand off to object
                    carChain.handleElement(localName, cleanChars);
                }
            } else if (inLocation) {
                if (localName.equalsIgnoreCase(LOCATION)) {
                    reply.carLocations.add(carLoc);
                    inLocation = false;
                } else {
                    // hand off to object
                    carLoc.handleElement(localName, cleanChars);
                }
            } else if (inChoice && !inViolation) {
                if (localName.equalsIgnoreCase(CHOICE)) {
                    reply.carChoices.add(carChoice);
                    inChoice = false;
                } else {
                    // hand off to object
                    carChoice.handleElement(localName, cleanChars);
                }
            } else if (inChoice && inViolation) {
                if (localName.equalsIgnoreCase(VIOLATION)) {
                    if (carChoice.violations == null) {
                        carChoice.violations = new ArrayList<Violation>();
                    }
                    carChoice.violations.add(carViolation);
                    inViolation = false;
                } else {
                    // hand off to object.
                    carViolation.handleElement(localName, cleanChars);
                }
            } else if (localName.equalsIgnoreCase(CAR_DESCRIPTIONS) || localName.equalsIgnoreCase(CAR_CHAINS)
                    || localName.equalsIgnoreCase(CAR_LOCATIONS) || localName.equalsIgnoreCase(CAR_CHOICES)
                    || localName.equalsIgnoreCase(CAR_SHOP_RESPONSE)) {
                // No-op.
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

            if (localName.equalsIgnoreCase("PickupIATA")) {
                reply.pickupIATA = cleanChars;
            } else if (localName.equalsIgnoreCase("PickupDateTime")) {
                reply.pickupDateTime = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("DropoffIATA")) {
                reply.dropoffIATA = cleanChars;
            } else if (localName.equalsIgnoreCase("DropoffDateTime")) {
                reply.dropoffDateTime = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("Status")) {
                reply.mwsStatus = cleanChars;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleResponseElement: unhandled XML node '" + localName
                        + "' with value '" + cleanChars + "'.");
            }
        }

    }
}
