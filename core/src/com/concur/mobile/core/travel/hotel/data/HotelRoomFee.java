/**
 * 
 */
package com.concur.mobile.core.travel.hotel.data;

/**
 * Models hotel detail fee information.
 * 
 * @author AndrewK
 */
public class HotelRoomFee {

    // private static final String CLS_TAG = HotelRoomFee.class.getSimpleName();

    public String feeDetail;
    public String feeType;

    private static final String FEE_DETAIL = "FeeDetails";
    private static final String FEE_TYPE = "FeeType";

    boolean handleElement(String localName, StringBuilder chars) {

        boolean elementHandled = false;
        if (localName.equalsIgnoreCase(FEE_DETAIL)) {
            feeDetail = chars.toString().trim();
            elementHandled = true;
        } else if (localName.equalsIgnoreCase(FEE_TYPE)) {
            feeType = chars.toString().trim();
            elementHandled = true;
        } else {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML tag '" +
            // localName + "' with value '" + chars.toString() + "'.");
        }
        return elementHandled;
    }

}
