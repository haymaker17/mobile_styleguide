/**
 * 
 */
package com.concur.mobile.core.travel.hotel.data;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.travel.data.HotelBenchmark;
import com.concur.mobile.core.travel.hotel.data.HotelRoomDetail.HotelRoomDetailSAXHandler;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models detailed information about a <code>HotelChoice</code> object.
 * 
 * @author AndrewK
 */
public class HotelChoiceDetail {

    private static final String CLS_TAG = HotelChoiceDetail.class.getSimpleName();

    /**
     * Contains the list of hotel room fees.
     */
    public ArrayList<HotelRoomFee> fees;

    /**
     * Contains the list of hotel room details.
     */
    public ArrayList<HotelRoomDetail> rooms;

    /**
     * Contains the list of hotel details.
     */
    public ArrayList<HotelDetail> details;

    public HotelBenchmark hotelBenchmark;

    /**
     * An extension of <code>DefaultHandler</code> for parsing hotel choice detail objects.
     * 
     * @author AndrewK
     */
    public static class HotelChoiceDetailSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = HotelChoiceDetail.CLS_TAG + "."
                + HotelChoiceDetailSAXHandler.class.getSimpleName();

        private static final String FEES = "Fees";
        private static final String FEE = "Fee";
        private static final String ROOMS = "Rooms";
        private static final String ROOM = "Room";
        private static final String DETAILS = "Details";
        private static final String DETAIL = "Detail";
        private static final String HOTELBENCHMARK = "HotelBenchmark";

        private StringBuilder chars;

        HotelChoiceDetail choiceDetail;

        // Contains the current hotel room fee object being parsed.
        private HotelRoomFee hotelRoomFee;
        // Contains the current hotel room detail being parsed.
        // private HotelRoomDetail hotelRoomDetail;
        // Contains the current hotel detail being parsed.
        private HotelDetail hotelDetail;
        // Contains the handler for parsing a hotel room detail object.
        private HotelRoomDetailSAXHandler roomDetailHandler;
        private boolean inHotelBenchmark;

        /**
         * Gets the parsed hotel choice detail object.
         * 
         * @return the hotel choice detail object.
         */
        public HotelChoiceDetail getHotelChoiceDetail() {
            return choiceDetail;
        }

        /**
         * Contructs a new instance of <code>HotelChoiceDetailSAXHandler</code> for parsing a hotel detail object.
         */
        public HotelChoiceDetailSAXHandler() {
            chars = new StringBuilder();
            choiceDetail = new HotelChoiceDetail();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (roomDetailHandler != null) {
                roomDetailHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
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
            if (roomDetailHandler != null) {
                roomDetailHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(FEES)) {
                choiceDetail.fees = new ArrayList<HotelRoomFee>();
            } else if (localName.equalsIgnoreCase(DETAILS)) {
                choiceDetail.details = new ArrayList<HotelDetail>();
            } else if (localName.equalsIgnoreCase(ROOMS)) {
                choiceDetail.rooms = new ArrayList<HotelRoomDetail>();
            } else if (localName.equalsIgnoreCase(ROOM)) {
                roomDetailHandler = new HotelRoomDetailSAXHandler();
            } else if (localName.equalsIgnoreCase(FEE)) {
                hotelRoomFee = new HotelRoomFee();
            } else if (localName.equalsIgnoreCase(DETAIL)) {
                hotelDetail = new HotelDetail();
            } else if (localName.equalsIgnoreCase(HOTELBENCHMARK)) {
                choiceDetail.hotelBenchmark = new HotelBenchmark();
                inHotelBenchmark = true;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (inHotelBenchmark) {
                if (localName.equalsIgnoreCase("Currency")) {
                    choiceDetail.hotelBenchmark.setCrnCode(chars.toString());
                } else if (localName.equalsIgnoreCase("Price")) {
                    choiceDetail.hotelBenchmark.setPrice(Parse.safeParseDouble(chars.toString()));
                } else if (localName.equalsIgnoreCase(HOTELBENCHMARK)) {
                    inHotelBenchmark = false;
                }
            } else if (localName.equalsIgnoreCase(FEE)) {
                if (hotelRoomFee != null) {
                    if (choiceDetail.fees != null) {
                        choiceDetail.fees.add(hotelRoomFee);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: fees list is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: hotel room fee is null!");
                }
                hotelRoomFee = null;
            } else if (localName.equalsIgnoreCase(DETAIL)) {
                if (hotelDetail != null) {
                    if (choiceDetail.details != null) {
                        choiceDetail.details.add(hotelDetail);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: details list is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: hotel detail is null!");
                }
                hotelDetail = null;
            } else if (localName.equalsIgnoreCase(ROOM)) {
                if (roomDetailHandler != null) {
                    if (choiceDetail.rooms != null) {
                        HotelRoom room = roomDetailHandler.getRoom();
                        if (room != null) {
                            if (room instanceof HotelRoomDetail) {
                                choiceDetail.rooms.add((HotelRoomDetail) roomDetailHandler.getRoom());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".endElement: room detail parser returned non room detail object type!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: room detail parser returned null room object!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: rooms list is null!");
                    }
                    roomDetailHandler = null;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: root detail handler is null!");
                }
            } else if (localName.equalsIgnoreCase(FEES)) {
                // No-op.
            } else if (localName.equalsIgnoreCase(DETAILS)) {
                // No-op.
            } else if (roomDetailHandler != null) {
                roomDetailHandler.endElement(uri, localName, qName);
            } else if (hotelDetail != null) {
                hotelDetail.handleElement(localName, chars);
            } else if (hotelRoomFee != null) {
                hotelRoomFee.handleElement(localName, chars);
            } else if (this.getClass().equals(HotelChoiceDetailSAXHandler.class)) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '" +
                // chars.toString() + "'.");
            }
            // Clear out the parsed characters if the parser instance is of 'this' class.
            if (this.getClass().equals(HotelChoiceDetailSAXHandler.class)) {
                chars.setLength(0);
            }
        }

    }

}
