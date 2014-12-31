package com.concur.mobile.core.travel.car.data;

import java.util.ArrayList;

import android.util.Log;

import com.concur.mobile.core.util.Const;

public class CarLocation {

    private static final String CLS_TAG = CarLocation.class.getSimpleName();

    public String chainCode;
    public String iataCode;
    public String locationCategory;
    public String locationName;
    public String address1;
    public String address2;
    public String state;
    public String countryCode;
    public String latitude;
    public String longitude;
    public String phoneNumber;

    public String dropOffCloses;
    public String dropOffOpens;
    public String pickUpCloses;
    public String pickUpOpens;
    public String zipCode;

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Address1")) {
            address1 = cleanChars;
        } else if (localName.equalsIgnoreCase("Address2")) {
            address2 = cleanChars;
        } else if (localName.equalsIgnoreCase("ChainCode")) {
            chainCode = cleanChars;
        } else if (localName.equalsIgnoreCase("CountryCode")) {
            countryCode = cleanChars;
        } else if (localName.equalsIgnoreCase("IataCode")) {
            iataCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Latitude")) {
            latitude = cleanChars;
        } else if (localName.equalsIgnoreCase("LocationCategory")) {
            locationCategory = cleanChars;
        } else if (localName.equalsIgnoreCase("LocationName")) {
            locationName = cleanChars;
        } else if (localName.equalsIgnoreCase("Longitude")) {
            longitude = cleanChars;
        } else if (localName.equalsIgnoreCase("PhoneNumber")) {
            phoneNumber = cleanChars;
        } else if (localName.equalsIgnoreCase("State")) {
            state = cleanChars;
        } else if (localName.equalsIgnoreCase("DropOffCloses")) {
            dropOffCloses = cleanChars;
        } else if (localName.equalsIgnoreCase("DropOffOpens")) {
            dropOffOpens = cleanChars;
        } else if (localName.equalsIgnoreCase("PickUpCloses")) {
            pickUpCloses = cleanChars;
        } else if (localName.equalsIgnoreCase("PickUpOpens")) {
            pickUpOpens = cleanChars;
        } else if (localName.equalsIgnoreCase("ZipCode")) {
            zipCode = cleanChars;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML node '" + localName + "' with value '"
                    + cleanChars + "'.");
        }

    }

    public static CarLocation findLocationByChain(ArrayList<CarLocation> locations, String chainCode) {
        if (chainCode == null)
            return null;

        CarLocation loc = null;

        for (int i = 0, size = locations.size(); i < size; i++) {
            CarLocation cl = locations.get(i);
            if (chainCode.equals(cl.chainCode)) {
                loc = cl;
                break;
            }
        }

        return loc;
    }
}
