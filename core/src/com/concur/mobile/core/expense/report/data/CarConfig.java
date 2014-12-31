package com.concur.mobile.core.expense.report.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.util.Parse;

public class CarConfig {

    private static final String CLS_TAG = CarConfig.class.getSimpleName();

    public final static String PERSONAL = "PER";
    public final static String COMPANY = "COM";

    public final static String TYPE_PER_ONE = "PER_ONE";
    public final static String TYPE_PER_VAR = "PER_VARIABLE";
    public final static String TYPE_COM_FIX = "COM_FIXED";
    public final static String TYPE_COM_FULL = "COM_FULLY";

    public final static String DISTANCE_MILE = "MILE";
    public final static String DISTANCE_KM = "KM";

    public boolean canCreateExpense;
    public Integer key;
    public String companyPersonal;
    public String configType;
    public String crnCode;
    public Integer crnKey;
    public String countryCode;
    public String distanceUnit;

    public ArrayList<CarDetail> details = new ArrayList<CarDetail>();
    public ArrayList<CarRate> rates = new ArrayList<CarRate>();

    /**
     * Iterate the list of car details and build a list of vehicles to use as spinner items
     */
    public SpinnerItem[] getCarItems() {
        SpinnerItem[] items = null;

        if (details != null && details.size() > 0) {
            int size = details.size();
            items = new SpinnerItem[size];
            for (int i = 0; i < size; i++) {
                CarDetail detail = details.get(i);
                items[i] = new SpinnerItem(Integer.toString(detail.key), detail.vehicleId);
            }
        }

        if (items == null) {
            items = new SpinnerItem[0];
        }

        return items;
    }

    /**
     * Return the first vehicle
     */
    public CarDetail getFirstCar() {
        CarDetail first = null;

        if (details != null && details.size() > 0) {
            first = details.get(0);
        }

        return first;
    }

    /**
     * Return the preferred vehicle
     */
    public CarDetail getPreferredCar() {
        CarDetail pref = null;

        if (details != null && details.size() > 0) {
            int size = details.size();
            for (int i = 0; i < size; i++) {
                CarDetail detail = details.get(i);
                if (detail.isPreferred) {
                    pref = detail;
                    break;
                }
            }
        }

        return pref;
    }

    /**
     * Gets a <code>CarDetail</code> object within this config based on its key value.
     * 
     * @param carKey
     *            contains the car key value.
     * @return returns an instance of <code>CarDetail</code> upon success; <code>null</code> otherwise.
     */
    public CarDetail getCarDetail(Integer carKey) {
        CarDetail retVal = null;
        if (carKey != null) {
            if (details != null) {
                for (CarDetail detail : details) {
                    if (carKey.equals(detail.key)) {
                        retVal = detail;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Return the distance to date for a given vehicle. NOTE: DTD appears to be global across all vehicles but the server response
     * attaches it to each vehicle. We'll search for the vehicle because we can and because if the server ever sends different
     * distances for a vehicle then the client will just work.
     * 
     * @return The distance to date for the given vehicle or 0.
     */
    public long getDistanceToDate(Integer carKey) {
        long dtd = 0L;

        if (details != null && details.size() > 0 && carKey != null) {
            int size = details.size();
            for (int i = 0; i < size; i++) {
                CarDetail detail = details.get(i);
                if (carKey.equals(detail.key)) {
                    dtd = detail.distanceToDate;
                    break;
                }
            }
        }

        return dtd;
    }

    /**
     * Update the distance to date. NOTE: DTD appears to be global across all vehicles but the server response attaches it to each
     * vehicle. We'll just update all of them.
     */
    public void updateDistanceToDate(long newDtd) {
        if (details != null && details.size() > 0 && newDtd > 0) {
            int size = details.size();
            for (int i = 0; i < size; i++) {
                CarDetail detail = details.get(i);
                detail.distanceToDate = newDtd;
            }
        }
    }

    public void updateDistanceToDate(Integer carKey, long newDtd) {
        if (details != null) {
            for (CarDetail carDetail : details) {
                if (carDetail.key == carKey) {
                    carDetail.distanceToDate = newDtd;
                }
            }
        }
    }

    public static CarRate findFixedRate(CarConfig config, Calendar cal) {

        if (config != null && TYPE_PER_ONE.equals(config.configType)) {
            CarRate rate;
            int rateSize = config.rates.size();
            // Rates should be in date order
            for (int j = rateSize - 1; j >= 0; j--) {
                rate = config.rates.get(j);
                if (rate.startDate.compareTo(cal) < 0) {
                    // Rate start is before transaction date, use it.
                    return rate;
                }
            }
        }
        return null;
    }

    public static CarRate findVariableRate(CarConfig config, Calendar cal, Integer vehicleId, String rateType,
            long newDistance) {

        if (config != null && vehicleId != null && TYPE_PER_VAR.equals(config.configType)) {
            if (config.details != null) {
                int size = config.details.size();

                // Find the car detail
                for (int i = 0; i < size; i++) {
                    CarDetail detail = config.details.get(i);
                    if (vehicleId.equals(detail.key)) {
                        long totalDistance = detail.distanceToDate + newDistance;

                        // Find the rate type based on type and distance
                        CarRateType crt = detail.getRateType(rateType, totalDistance);
                        if (crt != null) {
                            // Find the exact rate based on date
                            return crt.findRate(cal);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static CarRate findCompanyVariableRate(CarConfig config, Calendar cal, Integer vehicleId, String rateType,
            long newDistance) {

        if (config != null && vehicleId != null
                && (TYPE_COM_FIX.equals(config.configType) || TYPE_COM_FULL.equals(config.configType))) {

            if (config.details != null) {
                int size = config.details.size();

                // Find the car detail
                for (int i = 0; i < size; i++) {
                    CarDetail detail = config.details.get(i);
                    if (vehicleId.equals(detail.key)) {
                        long totalDistance = detail.distanceToDate + newDistance;

                        // Find the rate type based on type and distance
                        CarRateType crt = detail.getRateType(rateType, totalDistance);
                        if (crt != null) {
                            // Find the exact rate based on date
                            return crt.findRate(cal);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasVariableRate(CarConfig config, Calendar cal, Integer vehicleId, String rateType) {

        if (config != null
                && vehicleId != null
                && (TYPE_COM_FIX.equals(config.configType) || TYPE_COM_FULL.equals(config.configType) || TYPE_PER_VAR
                        .equals(config.configType))) {

            if (config.details != null) {
                int size = config.details.size();

                // Find the car detail
                for (int i = 0; i < size; i++) {
                    CarDetail detail = config.details.get(i);
                    if (vehicleId.equals(detail.key)) {
                        // Return whether or not this CarDetail has any rates.
                        return (detail.rateTypes != null && !detail.rateTypes.isEmpty());
                    }
                }
            }
        }
        return false;
    }

    public static double calculateVariableAmount(CarConfig config, Calendar date, Integer vehicleId, String rateType,
            long distance) {

        if (config != null
                && vehicleId != null
                && (TYPE_COM_FIX.equals(config.configType) || TYPE_COM_FULL.equals(config.configType) || TYPE_PER_VAR
                        .equals(config.configType))) {

            if (config.details != null) {
                int size = config.details.size();

                // Find the car detail
                for (int i = 0; i < size; i++) {
                    CarDetail detail = config.details.get(i);
                    if (vehicleId.equals(detail.key)) {

                        long distanceCalculated = 0L;
                        long totalDistance = detail.distanceToDate + distance;
                        double amount = 0.0;

                        // We got the correct CarDetail, now go through each CarRateType
                        // and calculate the amount based on the upper/lower distance limits
                        // and the with the rate for the specified date.
                        for (CarRateType crt : detail.rateTypes) {
                            if (rateType.equals(crt.rateType)) {

                                // Get the correct rate based on the date.
                                CarRate rate = crt.findRate(date);
                                if (rate != null) {

                                    long lower = (crt.lowerLimit == null ? 0 : crt.lowerLimit);
                                    long upper = (crt.upperLimit == null ? Integer.MAX_VALUE : crt.upperLimit);

                                    if (totalDistance > upper) {

                                        if (detail.distanceToDate >= lower && detail.distanceToDate <= upper) {
                                            // Indicates a rate band where the total distance is greater than upper, but the
                                            // DtD falls within this band, so distance from Dtd to upper should count towards
                                            // the calculated distance and amount.
                                            amount += (upper - detail.distanceToDate) * rate.rate;
                                            distanceCalculated += (upper - detail.distanceToDate);

                                        } else if (detail.distanceToDate < lower) {

                                            // Indicates a rate band where the total distance is greater than upper, and
                                            // the DtD is less than the lower. All of this band should be included in the
                                            // calculated distance and amount.

                                            // Adding 1 cause distance limits are inclusive.
                                            amount += ((upper - lower) + 1) * rate.rate;
                                            distanceCalculated += (upper - lower) + 1;
                                        } else {

                                            // No-op, indicates a rate band where the totalDistance is greater than
                                            // upper, but current DtD is greater than upper-limit.
                                        }
                                    } else {

                                        // Indicates a rate band where the total distance falls within it, but only
                                        // the remaining uncalculated distance should be applied towards the amount.

                                        amount += (distance - distanceCalculated) * rate.rate;
                                        break;
                                    }

                                }
                            }
                        } // end for-loop

                        return amount;
                    }
                } // end for-loop
            }
        } // end if

        return 0.0;

    } // end calculateVariableAmount()

    public static CarConfig findPersonalConfig(ArrayList<CarConfig> configs) {
        return findConfig(PERSONAL, configs);
    }

    public static CarConfig findCompanyConfig(ArrayList<CarConfig> configs) {
        return findConfig(COMPANY, configs);
    }

    /**
     * 
     * @param type
     *            either <code>PERSONAL</code> or <code>COMPANY</code>
     * 
     * @param configs
     * @return
     */
    private static CarConfig findConfig(String type, ArrayList<CarConfig> configs) {

        if (type != null && configs != null) {
            CarConfig config;
            int size = configs.size();
            for (int i = 0; i < size; i++) {
                config = configs.get(i);

                if (type.equals(config.companyPersonal)) {
                    // Admin config only allows one personal config.
                    return config;
                }
            }
        }
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW HERE BE SAX DRAGONS
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////

    public static ArrayList<CarConfig> parseCarConfigXml(String responseXml) throws IOException {

        ArrayList<CarConfig> configs = null;

        if (responseXml != null && responseXml.length() > 0) {

            configs = new ArrayList<CarConfig>();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();
                CarConfigSAXHandler handler = new CarConfigSAXHandler();
                parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
                configs = handler.getConfigs();
            } catch (ParserConfigurationException parsConfExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseCarConfigXml: parser exception.", parsConfExc);
                throw new IOException(parsConfExc.getMessage());
            } catch (SAXException saxExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseCarConfigXml: sax parsing exception.", saxExc);
                throw new IOException(saxExc.getMessage());
            }
        }

        return configs;
    }

    /**
     * Handle the config level elements
     * 
     * @param localName
     */
    protected void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("CanCreateExp")) {
            canCreateExpense = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("CarcfgKey")) {
            key = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("CompanyOrPersonal")) {
            companyPersonal = cleanChars;
        } else if (localName.equalsIgnoreCase("ConfigType")) {
            configType = cleanChars;
        } else if (localName.equalsIgnoreCase("CrnCode")) {
            crnCode = cleanChars;
        } else if (localName.equalsIgnoreCase("CrnKey")) {
            crnKey = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("CtryCode")) {
            countryCode = cleanChars;
        } else if (localName.equalsIgnoreCase("CtryDistanceUnitCode")) {
            distanceUnit = cleanChars;
        }
    }

    /**
     * Helper class to handle parsing of config XML.
     */
    protected static class CarConfigSAXHandler extends DefaultHandler {

        private static final String CONFIG = "CarConfig";
        private static final String DETAILS = "CarDetails";
        private static final String RATE = "CarRate";

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inConfig;
        private boolean inDetails;
        private boolean inRate;

        // Sub-element SAX parsers
        private CarDetail.CarDetailSAXHandler detailSAXHandler;

        // Holders for our parsed data
        private ArrayList<CarConfig> configs;
        private CarConfig config;
        private CarRate rate;

        /**
         * Retrieve our list of parsed configs
         * 
         * @return A List of {@link CarConfig} objects parsed from the XML
         */
        protected ArrayList<CarConfig> getConfigs() {
            return configs;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            configs = new ArrayList<CarConfig>();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (inDetails) {
                detailSAXHandler.characters(ch, start, length);
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

            if (inDetails) {
                detailSAXHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(CONFIG)) {
                config = new CarConfig();
                inConfig = true;
            } else if (inConfig) {
                if (localName.equalsIgnoreCase(DETAILS)) {
                    detailSAXHandler = new CarDetail.CarDetailSAXHandler();
                    inDetails = true;
                } else if (localName.equalsIgnoreCase(RATE)) {
                    // This situation only arises when the CarRate elements are direct children of
                    // the CarConfig (e.g. Personal Fixed mileage). When the CarRate elements are
                    // children of lower elements (e.g. CarRateType in Personal Variable mileage)
                    // they are handled in that parent's SAX parser class
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

            if (config != null) { // paranoia

                if (inDetails) {
                    if (localName.equalsIgnoreCase(DETAILS)) {
                        inDetails = false;
                        config.details = detailSAXHandler.getDetails();
                    } else {
                        detailSAXHandler.endElement(uri, localName, qName);
                    }
                } else {
                    final String cleanChars = chars.toString().trim();

                    if (inConfig) {
                        if (inRate) {
                            if (localName.equalsIgnoreCase(RATE)) {
                                inRate = false;
                                config.rates.add(rate);
                            } else {
                                rate.handleElement(localName, cleanChars);
                            }
                        } else if (localName.equalsIgnoreCase(CONFIG)) {
                            inConfig = false;
                            // Sort the dates
                            Collections.sort(config.rates);
                            configs.add(config);
                        } else {
                            config.handleElement(localName, cleanChars);
                        }
                    }
                }
                chars.setLength(0);
            }
        }

    }

}
