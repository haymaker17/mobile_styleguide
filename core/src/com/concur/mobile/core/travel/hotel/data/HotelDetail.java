/**
 * 
 */
package com.concur.mobile.core.travel.hotel.data;

/**
 * Models a detail about a hotel.
 * 
 * @author AndrewK
 */
public class HotelDetail {

    // private static final String CLS_TAG = HotelDetail.class.getSimpleName();

    public String name;
    public String text;

    private static final String NAME = "Name";
    private static final String TEXT = "Text";

    public boolean handleElement(String localName, StringBuilder chars) {
        boolean elementHandled = false;

        if (localName.equalsIgnoreCase(NAME)) {
            name = chars.toString().trim();
        } else if (localName.equalsIgnoreCase(TEXT)) {
            text = chars.toString().trim();
        } else {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML tag '" + localName +
            // "' with value '" + chars.toString() + "'.");
        }
        return elementHandled;
    }

}
