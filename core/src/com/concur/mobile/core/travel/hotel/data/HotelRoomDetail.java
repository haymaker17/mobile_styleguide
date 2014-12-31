/**
 * 
 */
package com.concur.mobile.core.travel.hotel.data;

import org.xml.sax.SAXException;

/**
 * An extension of <code>HotelRoomWithViolation</code> modeling detailed hotel room information.
 * 
 * @author AndrewK
 */
public class HotelRoomDetail extends HotelRoomWithViolation {

    // private static final String CLS_TAG = HotelRoomDetail.class.getSimpleName();

    /**
     * An extension of <code>RoomWithViolationSAXHandler</code> for parsing detailed hotel room information.
     * 
     * @author AndrewK
     */
    public static class HotelRoomDetailSAXHandler extends RoomWithViolationSAXHandler {

        // private static final String CLS_TAG = HotelRoomDetail.CLS_TAG + "."
        // + HotelRoomDetailSAXHandler.class.getSimpleName();

        private static final String ROOM = "Room";

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.travel.HotelRoomWithViolation.RoomWithViolationSAXHandler#createRoom()
         */
        @Override
        protected HotelRoom createRoom() {
            return new HotelRoomDetail();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.travel.HotelRoomWithViolation.RoomWithViolationSAXHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(ROOM)) {
                    // No-op.
                    elementHandled = true;
                } else if (this.getClass().equals(HotelRoomDetailSAXHandler.class)) {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '" +
                    // chars.toString() + "'.");
                }
            }
            // Clear out the parsed characters if the parser instance is of 'this' class.
            if (this.getClass().equals(HotelRoomDetailSAXHandler.class)) {
                chars.setLength(0);
            }
        }

    }

}
