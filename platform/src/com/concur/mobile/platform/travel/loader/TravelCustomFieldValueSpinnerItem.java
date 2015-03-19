package com.concur.mobile.platform.travel.loader;

import android.util.Log;

import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>SpinnerItem</code> for modelling travel custom field values.
 * 
 * @author RatanK
 * 
 */

public class TravelCustomFieldValueSpinnerItem extends SpinnerItem {

    /**
     * generated serial version ID
     */
    private static final long serialVersionUID = 1596914130332930620L;

    private static final String CLS_TAG = TravelCustomFieldValueSpinnerItem.class.getSimpleName();

    private static final String ATTRIBUTE_ID = "AttributeId";
    private static final String OPTION_TEXT = "OptionText";
    private static final String SEQUENCE = "Sequence";
    private static final String VALUE = "Value";
    private static final String VALUE_ID = "ValueId";

    public String attributeId;

    public String optionText;

    public String sequence;

    public String value;

    public String valueId;

    /**
     * Constructs an instance of <code>TravelCustomFieldValueSpinnerItem</code> with an id and name.
     * 
     * @param id
     *            the spinner item id.
     * @param name
     *            the spinner item text.
     */
    public TravelCustomFieldValueSpinnerItem(String id, CharSequence name) {
        super(id, name);
    }

    public void handleValue(String attrName, String attrValue) {
        if (attrName.equalsIgnoreCase(ATTRIBUTE_ID)) {
            attributeId = attrValue;
        } else if (attrName.equalsIgnoreCase(OPTION_TEXT)) {
            optionText = attrValue;
            name = attrValue;
        } else if (attrName.equalsIgnoreCase(SEQUENCE)) {
            sequence = attrValue;
        } else if (attrName.equalsIgnoreCase(VALUE)) {
            value = attrValue;
        } else if (attrName.equalsIgnoreCase(VALUE_ID)) {
            valueId = attrValue;
            id = attrValue;
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleValue: unknown attribute '" + attrName + "'.");
        }
    }
}
