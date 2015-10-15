/**
 *
 */
package com.concur.mobile.core.travel.data;

import android.os.Bundle;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.Const;

/**
 * Provides a model of a company office location.
 *
 * @author AndrewK
 */
public class CompanyLocation extends LocationChoice {

    private static final String CLS_TAG = CompanyLocation.class.getSimpleName();

    protected static final String KEY_COUNTRY = "Country";
    protected static final String KEY_ADDRESS = "Address";

    public String country;
    public String address;

    public CompanyLocation() {
    }

    public CompanyLocation(Bundle b) {
        super(b);
        if (b != null) {
            city = b.getString(KEY_CITY);
            state = b.getString(KEY_STATE);
            country = b.getString(KEY_COUNTRY);
            address = b.getString(KEY_ADDRESS);
        }
    }

    public Bundle getBundle() {
        Bundle b = super.getBundle();
        b.putString(KEY_CITY, city);
        b.putString(KEY_STATE, state);
        b.putString(KEY_COUNTRY, country);
        b.putString(KEY_ADDRESS, address);

        return b;
    }

    @Override
    public String getName() {
        String formattedName = null;
        if (state == null || state.length() == 0) {
            formattedName = Format.localizeText(ConcurCore.getContext(), R.string.general_citycountry, new Object[]{
                    city, country});
        } else {
            formattedName = Format.localizeText(ConcurCore.getContext(), R.string.general_citystatecountry,
                    new Object[]{city, state, country});
        }
        if (address != null && address.length() > 0) {
            StringBuilder strBdlr = new StringBuilder();
            strBdlr.append(address);
            strBdlr.append(',');
            strBdlr.append(formattedName);
            formattedName = strBdlr.toString();
        }
        return formattedName;
    }

    /**
     * Will examine the attribute <code>localName</code> and assign the value in <code>cleanChars</code>.
     *
     * @param localName the attribute name.
     * @param value     the attribute value trimmed of whitespace.
     */
    public boolean handleElement(String localName, String value) {
        boolean attrSet = super.handleElement(localName, value);

        if (!attrSet) {
            if (localName.equalsIgnoreCase(KEY_CITY)) {
                city = value;
            } else if (localName.equalsIgnoreCase(KEY_COUNTRY)) {
                country = value;
            } else if (localName.equalsIgnoreCase(KEY_STATE)) {
                state = value;
            } else if (localName.equalsIgnoreCase(KEY_ADDRESS)) {
                address = value;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: XML tag '" + localName + "' not handled!");
                attrSet = false;
            }
        }

        return attrSet;
    }

    public String getAddress() {
        return address;
    }

    public String getProvince() {
        String formattedName = null;
        if (state == null || state.length() == 0) {
            formattedName = Format.localizeText(ConcurCore.getContext(), R.string.general_citycountry, new Object[]{
                    city, country});

        } else {
            formattedName = Format.localizeText(ConcurCore.getContext(), R.string.general_citystatecountry,
                    new Object[]{city, state, country});
        }

        return formattedName;
    }
}
