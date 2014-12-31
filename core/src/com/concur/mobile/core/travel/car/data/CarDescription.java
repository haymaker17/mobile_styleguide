package com.concur.mobile.core.travel.car.data;

import java.util.ArrayList;

import android.util.Log;

import com.concur.mobile.core.util.Const;

public class CarDescription {

    private static String CLS_TAG = CarDescription.class.getSimpleName();

    public String carCode;
    public String carClass;
    public String carBody;
    public String carTrans;
    public String carFuel;
    public String carAC;

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("CarAC")) {
            carAC = cleanChars;
        } else if (localName.equalsIgnoreCase("CarBody")) {
            carBody = cleanChars;
        } else if (localName.equalsIgnoreCase("CarClass")) {
            carClass = cleanChars;
        } else if (localName.equalsIgnoreCase("CarCode")) {
            carCode = cleanChars;
        } else if (localName.equalsIgnoreCase("CarFuel")) {
            carFuel = cleanChars;
        } else if (localName.equalsIgnoreCase("CarTrans")) {
            carTrans = cleanChars;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML node '" + localName + "' with value '"
                    + cleanChars + "'.");
        }

    }

    public static CarDescription findDescByCode(ArrayList<CarDescription> descs, String code) {
        if (code == null)
            return null;

        CarDescription desc = null;

        int size = descs.size();
        for (int i = 0; i < size; i++) {
            CarDescription cd = descs.get(i);
            if (code.equalsIgnoreCase(cd.carCode)) {
                desc = cd;
                break;
            }
        }

        return desc;
    }
}
