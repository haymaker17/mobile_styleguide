/**
 * 
 */
package com.concur.mobile.platform.travel.search.hotel;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>Room</code> supporting violations.
 * 
 * @author AndrewK
 */
public class HotelRoomWithViolation extends HotelRoom {

    private static final String CLS_TAG = HotelRoomWithViolation.class.getSimpleName();

    public List<Violation> violations;

    /**
     * Will return the maximum enforcment level over any violations associated with this hotel room.
     * 
     * @return the maximum enforcement level for any associated violations.
     */
    public int getMaxRuleEnforcementLevel() {
        int maxEnforcementLevel = 0;
        if (violations != null) {
            for (Violation violation : violations) {
                maxEnforcementLevel = Math.max(maxEnforcementLevel, violation.enforcementLevel);
            }
        }
        return maxEnforcementLevel;
    }

    /**
     * Will return an appropriate <code>RuleEnforcementLevel</code> value based on examining <code>level</code>.
     * 
     * @param level
     *            the rule enforcement level.
     * @return an instance of <code>RuleEnforcementLevel</code>.
     */
    public RuleEnforcementLevel getRuleEnforcementLevel(Integer level) {
        RuleEnforcementLevel retVal = RuleEnforcementLevel.NONE;
        if (level >= 30) {
            retVal = RuleEnforcementLevel.ERROR;
        } else if (level > 0 && level < 30) {
            retVal = RuleEnforcementLevel.WARNING;
        }
        return retVal;
    }

    /**
     * An extension of <code>RoomSAXHandler</code> for parsing hotel room with violation information.
     * 
     * @author AndrewK
     */
    public static class RoomWithViolationSAXHandler extends RoomSAXHandler {

        private static final String CLS_TAG = HotelRoomWithViolation.CLS_TAG + "."
                + RoomWithViolationSAXHandler.class.getSimpleName();

        private static final String VIOLATIONS = "Violations";
        private static final String VIOLATION = "Violation";

        // Contains the violation currently being parsed.
        private Violation violation;

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.travel.Room.RoomSAXHandler#createRoom()
         */
        @Override
        protected HotelRoom createRoom() {
            return new HotelRoomWithViolation();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.travel.Room.RoomSAXHandler#startElement(java.lang.String, java.lang.String,
         * java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(VIOLATIONS)) {
                    if (room != null) {
                        if (room instanceof HotelRoomWithViolation) {
                            HotelRoomWithViolation roomWithViolation = (HotelRoomWithViolation) room;
                            roomWithViolation.violations = new ArrayList<Violation>();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: expecting room of type RoomWithViolation!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: room is null!");
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VIOLATION)) {
                    violation = new Violation();
                    elementHandled = true;
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.data.travel.Room.RoomSAXHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                if (localName.equalsIgnoreCase(VIOLATION)) {
                    if (room != null) {
                        if (room instanceof HotelRoomWithViolation) {
                            HotelRoomWithViolation roomWithViolation = (HotelRoomWithViolation) room;
                            if (roomWithViolation.violations != null) {
                                roomWithViolation.violations.add(violation);
                                violation = null;
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: violations list is null!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: expecting room of type RoomWithViolation!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: room is null!");
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VIOLATIONS)) {
                    // No-op.
                    elementHandled = true;
                } else if (violation != null) {
                    violation.handleElement(localName, chars.toString().trim());
                    elementHandled = true;
                } else if (this.getClass().equals(RoomWithViolationSAXHandler.class)) {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '" +
                    // chars.toString() + "'.");
                }
            }
            // Clear out the parsed characters if the parser instance is of 'this' class.
            if (this.getClass().equals(RoomWithViolationSAXHandler.class)) {
                chars.setLength(0);
            }
        }
    }

}
