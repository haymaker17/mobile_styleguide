package com.concur.mobile.core.expense.report.data;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.platform.util.Parse;

public class CarDetail {

    public Integer key;
    public String critName;
    public Long distanceToDate;
    public Long odometerStart;
    public boolean isPreferred;
    public String vehicleId;

    public ArrayList<CarRateType> rateTypes;

    public CarRateType getRateType(String rateType, long distance) {
        CarRateType crt = null;

        if (rateType != null && rateTypes != null) {
            int size = rateTypes.size();
            for (int i = 0; i < size; i++) {
                CarRateType rt = rateTypes.get(i);
                if (rateType.equals(rt.rateType)) {
                    if (CarRateType.TYPE_PER_VAR_CAR.equals(rt.rateType)
                            || CarRateType.TYPE_COM_FIX_BUS.equals(rt.rateType)
                            || CarRateType.TYPE_COM_FIX_PER.equals(rt.rateType)) {
                        long lower = (rt.lowerLimit == null ? 0 : rt.lowerLimit);
                        long upper = (rt.upperLimit == null ? Integer.MAX_VALUE : rt.upperLimit);
                        if (distance >= lower && distance <= upper) {
                            crt = rt;
                            break;
                        }
                    } else {
                        // Passenger rate, no distance brackets
                        crt = rt;
                        break;
                    }
                }
            }
        }
        return crt;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW HERE BE SAX DRAGONS
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the detail level elements
     * 
     * @param localName
     */
    protected void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("CarKey")) {
            key = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("CriteriaName")) {
            critName = cleanChars;
        } else if (localName.equalsIgnoreCase("DistanceToDate")) {
            distanceToDate = Parse.safeParseLong(cleanChars);
        } else if (localName.equalsIgnoreCase("OdometerStart")) {
            odometerStart = Parse.safeParseLong(cleanChars);
        } else if (localName.equalsIgnoreCase("IsPreferred")) {
            isPreferred = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("VehicleId")) {
            vehicleId = cleanChars;
        }
    }

    /**
     * Helper class to handle parsing of car detail XML.
     */
    protected static class CarDetailSAXHandler extends DefaultHandler {

        private static final String DETAIL = "CarDetail";
        private static final String RATETYPES = "CarRateTypes";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inDetail;
        private boolean inRateTypes;

        // Sub-element SAX parsers
        private CarRateType.CarRateTypeSAXHandler rateTypeSAXHandler;

        // Holders for our parsed data
        private ArrayList<CarDetail> details = new ArrayList<CarDetail>();
        private CarDetail detail;

        /**
         * Retrieve our list of parsed details
         * 
         * @return A List of {@link CarDetail} objects parsed from the XML
         */
        protected ArrayList<CarDetail> getDetails() {
            return details;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (inRateTypes) {
                rateTypeSAXHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (inRateTypes) {
                rateTypeSAXHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(DETAIL)) {
                detail = new CarDetail();
                inDetail = true;
            } else if (inDetail) {
                if (localName.equalsIgnoreCase(RATETYPES)) {
                    rateTypeSAXHandler = new CarRateType.CarRateTypeSAXHandler();
                    inRateTypes = true;
                }
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (detail != null) { // paranoia

                final String cleanChars = chars.toString().trim();

                if (inDetail) {
                    if (inRateTypes) {
                        if (localName.equalsIgnoreCase(RATETYPES)) {
                            inRateTypes = false;
                            detail.rateTypes = rateTypeSAXHandler.getRateTypes();
                        } else {
                            rateTypeSAXHandler.endElement(uri, localName, qName);
                        }
                    } else if (localName.equalsIgnoreCase(DETAIL)) {
                        inDetail = false;
                        details.add(detail);
                    } else {
                        detail.handleElement(localName, cleanChars);
                    }
                }
                chars.setLength(0);
            }
        }

    }
}
