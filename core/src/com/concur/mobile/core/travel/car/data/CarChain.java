package com.concur.mobile.core.travel.car.data;

import java.net.URI;
import java.util.ArrayList;

import android.util.Log;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Format;

public class CarChain {

    private static final String CLS_TAG = CarChain.class.getSimpleName();

    public String chainCode;
    public String chainName;
    public URI imageUri;

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("ChainCode")) {
            chainCode = cleanChars;
        } else if (localName.equalsIgnoreCase("ChainName")) {
            chainName = cleanChars;
        } else if (localName.equalsIgnoreCase("ImageUri")) {
            imageUri = Format.formatServerURI(false, Preferences.getServerAddress(), cleanChars);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML node '" + localName + "' with value '"
                    + cleanChars + "'.");

        }
    }

    public static CarChain findChainByCode(ArrayList<CarChain> chains, String code) {
        if (code == null)
            return null;

        CarChain chain = null;

        int size = chains.size();
        for (int i = 0; i < size; i++) {
            CarChain cc = chains.get(i);
            if (code.equalsIgnoreCase(cc.chainCode)) {
                chain = cc;
                break;
            }
        }

        return chain;
    }
}
