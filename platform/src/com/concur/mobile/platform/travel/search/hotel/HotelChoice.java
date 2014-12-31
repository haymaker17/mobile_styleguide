/**
 * 
 */
package com.concur.mobile.platform.travel.search.hotel;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.platform.travel.search.hotel.HotelRecommendation.RecommendationSourceEnum;
import com.concur.mobile.platform.travel.search.hotel.HotelRoomWithViolation.RoomWithViolationSAXHandler;
import com.concur.mobile.platform.travel.search.hotel.ImagePair.ImagePairSAXHandler;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * @author AndrewK
 */
public class HotelChoice {

    private static final String CLS_TAG = HotelChoice.class.getSimpleName();

    public String addr1;
    public String addr2;
    public String chainCode;
    public String chainName;
    public HotelRoom cheapestRoom;
    public HotelRoomWithViolation cheapestRoomWithViolation;
    public String city;
    public String country;
    public String countryCode;
    public String distance;
    public Float distanceF;
    public String distanceUnit;
    public String lat;
    public String lon;
    public String hotel;
    public String phone;
    public String propertyId;
    public ArrayList<ImagePair> propertyImages;
    public String propertyURI;
    public String starRating;
    public Integer starRatingI;
    public String prefRank;
    public Integer prefRankI;
    public String state;
    public String stateAbbrev;
    public String tollFree;
    public String zipCode;

    public Boolean isAdditional;
    public String isCompanyPreferredChain;
    public String isContract;
    public String preferenceType;
    public String companyPriority;
    public String contractRate;
    public Float contractRateF;

    public RecommendationSourceEnum recommendationSource;
    public double recommendationScore;
    public long recommendationSourceNumber;

    // flags for sold out and no rates hotels
    public boolean isSoldOut;
    public boolean isNoRates;

    public HotelChoice() {
        propertyImages = new ArrayList<ImagePair>();
    }

    public static class HotelChoiceSAXHandler extends DefaultHandler {

        private final String CLS_TAG = HotelChoice.CLS_TAG + "." + HotelChoiceSAXHandler.class.getSimpleName();

        private static final String ADDR_1 = "Addr1";
        private static final String ADDR_2 = "Addr2";
        private static final String CHAIN_CODE = "ChainCode";
        private static final String CHAIN_NAME = "ChainName";
        private static final String CHEAPEST_ROOM = "CheapestRoom";
        private static final String CHEAPEST_ROOM_WITH_VIOLATION = "CheapestRoomWithViolation";
        private static final String CITY = "City";
        private static final String COUNTRY = "Country";
        private static final String COUNTRY_CODE = "CountryCode";
        private static final String DISTANCE = "Distance";
        private static final String DISTANCE_UNIT = "DistanceUnit";
        private static final String LAT = "Lat";
        private static final String LON = "Lng";
        private static final String HOTEL = "Hotel";
        private static final String PHONE = "Phone";
        private static final String PROPERTY_ID = "PropertyId";
        private static final String PROPERTY_IMAGES = "PropertyImages";
        private static final String PROPERTY_URI = "PropertyUri";
        private static final String STAR_RATING = "StarRating";
        private static final String PREF_RANK = "HotelPrefRank";
        private static final String STATE = "State";
        private static final String STATE_ABBREV = "StateAbbrev";
        private static final String TOLL_FREE = "TollFree";
        private static final String ZIP = "Zip";

        private static final String IS_ADDITIONAL = "IsAdditional";
        private static final String IS_COMPANY_PREFERRED_CHAIN = "IsCompanyPreferredChain";
        private static final String IS_CONTRACT = "IsContract";
        private static final String PREFERENCE_TYPE = "PreferenceType";
        private static final String COMPANY_PRIORITY = "CompanyPriority";
        private static final String CONTRACT_RATE = "ContractRate";

        private static final String RECOMMENDATION = "Recommendation";
        private static final String GDS_RATE_ERROR_CODE = "GdsRateErrorCode";
        private static final String PROPERTY_NOT_AVAILABLE = "PropertyNotAvailable";

        // Fields to help parsing
        private StringBuilder chars;

        // The hotel choice.
        private HotelChoice choice;

        // Handles parsing "cheapest room" attributes.
        private HotelRoom.RoomSAXHandler cheapestRoomHandler;

        // Handles parsing "cheapest room with violations" attributes.
        private HotelRoomWithViolation.RoomWithViolationSAXHandler cheapestRoomViolationHandler;

        // Handles parsing property images.
        private ImagePairSAXHandler imagePairHandler;

        // Handles parsing recommendation
        private HotelRecommendation.RecommendationSAXHandler recommendationHandler;

        public boolean hasRecommendation;

        /**
         * Constructs an instance of <code>HotelChoiceSAXHandler</code> for parsing a <code>HotelChoice</code> object.
         */
        public HotelChoiceSAXHandler() {
            choice = new HotelChoice();
            chars = new StringBuilder();
        }

        /**
         * Gets the built hotel choice.
         * 
         * @return the parsed hotel choice.
         */
        public HotelChoice getHotelChoice() {
            return choice;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (cheapestRoomHandler != null) {
                cheapestRoomHandler.characters(ch, start, length);
            } else if (cheapestRoomViolationHandler != null) {
                cheapestRoomViolationHandler.characters(ch, start, length);
            } else if (imagePairHandler != null) {
                imagePairHandler.characters(ch, start, length);
            } else if (recommendationHandler != null) {
                recommendationHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        /**
         * Handle the opening of all elements.
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (cheapestRoomHandler != null) {
                cheapestRoomHandler.startElement(uri, localName, qName, attributes);
            } else if (cheapestRoomViolationHandler != null) {
                cheapestRoomViolationHandler.startElement(uri, localName, qName, attributes);
            } else if (imagePairHandler != null) {
                imagePairHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(CHEAPEST_ROOM)) {
                cheapestRoomHandler = new HotelRoom.RoomSAXHandler();
            } else if (localName.equalsIgnoreCase(CHEAPEST_ROOM_WITH_VIOLATION)) {
                cheapestRoomViolationHandler = new RoomWithViolationSAXHandler();
            } else if (localName.equalsIgnoreCase(PROPERTY_IMAGES)) {
                imagePairHandler = new ImagePairSAXHandler();
            } else if (localName.equalsIgnoreCase(RECOMMENDATION)) {
                recommendationHandler = new HotelRecommendation.RecommendationSAXHandler();
            } else if (recommendationHandler != null) {
                recommendationHandler.startElement(uri, localName, qName, attributes);
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (recommendationHandler != null) {
                if (localName.equalsIgnoreCase(RECOMMENDATION)) {
                    choice.recommendationSource = recommendationHandler.getSourceEnum();
                    choice.recommendationScore = recommendationHandler.getScore();
                    choice.recommendationSourceNumber = recommendationHandler.getSourceNumber();
                    hasRecommendation = true;
                    recommendationHandler = null;
                } else {
                    recommendationHandler.endElement(uri, localName, qName);
                }
            } else if (cheapestRoomHandler != null) {
                if (localName.equalsIgnoreCase(CHEAPEST_ROOM)) {
                    choice.cheapestRoom = cheapestRoomHandler.getRoom();
                    cheapestRoomHandler = null;
                } else {
                    cheapestRoomHandler.endElement(uri, localName, qName);
                }
            } else if (cheapestRoomViolationHandler != null) {
                if (localName.equalsIgnoreCase(CHEAPEST_ROOM_WITH_VIOLATION)) {
                    choice.cheapestRoomWithViolation = (HotelRoomWithViolation) cheapestRoomViolationHandler.getRoom();
                    cheapestRoomViolationHandler = null;
                } else {
                    cheapestRoomViolationHandler.endElement(uri, localName, qName);
                }
            } else if (imagePairHandler != null) {
                if (localName.equalsIgnoreCase(PROPERTY_IMAGES)) {
                    choice.propertyImages = imagePairHandler.getImagePairs();
                    imagePairHandler = null;
                } else {
                    imagePairHandler.endElement(uri, localName, qName);
                }
            } else if (localName.equalsIgnoreCase(ADDR_1)) {
                choice.addr1 = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(ADDR_2)) {
                choice.addr2 = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(CHAIN_CODE)) {
                choice.chainCode = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(CHAIN_NAME)) {
                choice.chainName = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(CITY)) {
                choice.city = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(COUNTRY)) {
                choice.country = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(COUNTRY_CODE)) {
                choice.countryCode = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(DISTANCE)) {
                choice.distance = chars.toString().trim();
                choice.distanceF = Parse.safeParseFloat(choice.distance);
            } else if (localName.equalsIgnoreCase(DISTANCE_UNIT)) {
                choice.distanceUnit = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(HOTEL)) {
                choice.hotel = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(LAT)) {
                choice.lat = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(LON)) {
                choice.lon = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(PHONE)) {
                choice.phone = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(PROPERTY_ID)) {
                choice.propertyId = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(PROPERTY_URI)) {
                choice.propertyURI = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(STAR_RATING)) {
                choice.starRating = chars.toString().trim();
                choice.starRatingI = Parse.safeParseInteger(choice.starRating);
            } else if (localName.equalsIgnoreCase(PREF_RANK)) {
                choice.prefRank = chars.toString().trim();
                choice.prefRankI = Parse.safeParseInteger(choice.prefRank);
            } else if (localName.equalsIgnoreCase(STATE)) {
                choice.state = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(STATE_ABBREV)) {
                choice.stateAbbrev = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(TOLL_FREE)) {
                choice.tollFree = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(ZIP)) {
                choice.zipCode = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(IS_ADDITIONAL)) {
                choice.isAdditional = Parse.safeParseBoolean(chars.toString().trim());
            } else if (localName.equalsIgnoreCase(IS_COMPANY_PREFERRED_CHAIN)) {
                choice.isCompanyPreferredChain = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(IS_CONTRACT)) {
                choice.isContract = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(PREFERENCE_TYPE)) {
                choice.preferenceType = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(COMPANY_PRIORITY)) {
                choice.companyPriority = chars.toString().trim();
            } else if (localName.equalsIgnoreCase(CONTRACT_RATE)) {
                choice.contractRate = chars.toString().trim();
                choice.contractRateF = Parse.safeParseFloat(choice.contractRate);
            } else if (localName.equalsIgnoreCase(GDS_RATE_ERROR_CODE)) {
                String res = chars.toString().trim();
                if (res.equalsIgnoreCase(PROPERTY_NOT_AVAILABLE)) {
                    choice.isSoldOut = true;
                } else if (res != null) {
                    choice.isNoRates = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML node '" + localName + "' with value '"
                        + chars.toString() + "'.");
            }
            chars.setLength(0);
        }
    }
}
