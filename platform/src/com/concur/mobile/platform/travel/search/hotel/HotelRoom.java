/**
 * 
 */
package com.concur.mobile.platform.travel.search.hotel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.platform.util.Parse;

/**
 * @author AndrewK
 */
public class HotelRoom {

    // private static final String CLS_TAG = HotelRoom.class.getSimpleName();

    public String crnCode;
    public String rate;
    public Float rateF;
    public String summary;
    public String bicCode;
    public String sellSource;
    public boolean depositRequired;
    public Integer maxEnforcementLevel;
    public String choiceId;

    public static class RoomSAXHandler extends DefaultHandler {

        // private static final String CLS_TAG = HotelRoom.CLS_TAG + "." + RoomSAXHandler.class.getSimpleName();

        private static final String CRN_CODE = "CrnCode";
        private static final String RATE = "Rate";
        private static final String SUMMARY = "Summary";
        private static final String BIC_CODE = "BicCode";
        private static final String SELL_SOURCE = "SellSource";
        private static final String DEPOSIT_REQUIRED = "DepositRequired";
        private static final String MAX_ENFORCEMENT_LEVEL = "MaxEnforcementLevel";
        private static final String CHOICE_ID = "ChoiceId";

        protected HotelRoom room;

        protected StringBuilder chars;

        protected boolean elementHandled;

        /**
         * Constructs an instance of <code>RoomSAXHandler</code> to parse hotel room information.
         */
        RoomSAXHandler() {
            chars = new StringBuilder();
            room = createRoom();
        }

        /**
         * Creates the appropriate instance of <code>Room</code> based on the parser.
         * 
         * @return an instance of <code>Room</code> based on the parser.
         */
        protected HotelRoom createRoom() {
            return new HotelRoom();
        }

        /**
         * Gets the parsed room.
         * 
         * @return the parsed room.
         */
        HotelRoom getRoom() {
            return room;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            elementHandled = false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            elementHandled = false;
            if (localName.equalsIgnoreCase(CRN_CODE)) {
                room.crnCode = chars.toString().trim();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(RATE)) {
                room.rate = chars.toString().trim();
                room.rateF = Parse.safeParseFloat(room.rate);
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(SUMMARY)) {
                room.summary = chars.toString().trim();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(BIC_CODE)) {
                room.bicCode = chars.toString().trim();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(SELL_SOURCE)) {
                room.sellSource = chars.toString().trim();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(DEPOSIT_REQUIRED)) {
                room.depositRequired = Parse.safeParseBoolean(chars.toString().trim());
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(MAX_ENFORCEMENT_LEVEL)) {
                room.maxEnforcementLevel = Parse.safeParseInteger(chars.toString().trim());
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(CHOICE_ID)) {
                room.choiceId = chars.toString().trim();
            } else if (this.getClass().equals(RoomSAXHandler.class)) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" +
                // localName + "' with value '" + chars.toString() + "'.");
            }
            // Clear out the parsed characters if the parser instance is of this class.
            if (this.getClass().equals(RoomSAXHandler.class)) {
                chars.setLength(0);
            }
        }

    }

}
