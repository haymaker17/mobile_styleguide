package com.concur.mobile.core.expense.report.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.platform.util.Parse;

public class CarRateType {

    public final static String TYPE_PER_VAR_CAR = "PER_VAR_CAR";
    public final static String TYPE_PER_VAR_PAS = "PER_VAR_PAS";
    public final static String TYPE_COM_FIX_BUS = "COM_FIXED_BUS";
    public final static String TYPE_COM_FIX_PER = "COM_FIXED_PER";
    public final static String TYPE_COM_FIX_PAS = "COM_FIXED_PAS";
    
    public final static String TYPE_PER_FIX = "PER_ONE";

    Long lowerLimit;
    Long upperLimit;
    String rateType;

    public ArrayList<CarRate> rates = new ArrayList<CarRate>();

    public CarRate findRate(Calendar cal) {
        CarRate rate = null;

        int size = rates.size();
        // Rates should be in date order
        for (int j = size - 1; j >= 0; j--) {
            CarRate r = rates.get(j);
            if (r.startDate.compareTo(cal) < 0) {
                // Rate start is before transaction date, use it.
                rate = r;
                break;
            }
        }

        return rate;
    }

    /**
     * Handle the rate type level elements
     * 
     * @param localName
     */
    protected void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("LowerLimit")) {
            lowerLimit = Parse.safeParseLong(cleanChars);
        } else if (localName.equalsIgnoreCase("RateType")) {
            rateType = cleanChars;
        } else if (localName.equalsIgnoreCase("UpperLimit")) {
            upperLimit = Parse.safeParseLong(cleanChars);
        }
    }

    /**
     * Helper class to handle parsing of rate type XML.
     */
    protected static class CarRateTypeSAXHandler extends DefaultHandler {

        private static final String RATETYPE = "CarRateType";
        private static final String RATE = "CarRate";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inRateType;
        private boolean inRate;

        // Holders for our parsed data
        private ArrayList<CarRateType> rateTypes = new ArrayList<CarRateType>();
        private CarRateType rateType;
        private CarRate rate;

        /**
         * Retrieve our list of parsed rate types
         * 
         * @return A List of {@link CarRateType} objects parsed from the XML
         */
        protected ArrayList<CarRateType> getRateTypes() {
            return rateTypes;
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

            if (localName.equalsIgnoreCase(RATETYPE)) {
                rateType = new CarRateType();
                inRateType = true;
            } else if (inRateType) {
                if (localName.equalsIgnoreCase(RATE)) {
                    rate = new CarRate();
                    inRate = true;
                }
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (rateType != null) { // paranoia

                final String cleanChars = chars.toString().trim();

                if (inRateType) {
                    if (inRate) {
                        if (localName.equalsIgnoreCase(RATE)) {
                            inRate = false;
                            rateType.rates.add(rate);
                        } else {
                            rate.handleElement(localName, cleanChars);
                        }
                    } else if (localName.equalsIgnoreCase(RATETYPE)) {
                        inRateType = false;
                        // Sort the dates
                        Collections.sort(rateType.rates);
                        rateTypes.add(rateType);
                    } else {
                        rateType.handleElement(localName, cleanChars);
                    }
                }
                chars.setLength(0);
            }
        }

    }
}
