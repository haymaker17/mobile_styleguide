package com.concur.mobile.platform.ui.common.view;

import android.util.Log;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>SpinnerItem</code> for modeling list form field values.
 * 
 * Copied from TravelCustomFieldValueSpinnerItem in concur.mobile.core
 */
public class FieldValueSpinnerItem extends SpinnerItem {

    private static final long serialVersionUID = -7943366792562691269L;

    private static final String CLS_TAG = FieldValueSpinnerItem.class.getSimpleName();

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
     * Constructs an instance of <code>FieldValueSpinnerItem</code> with an id and name.
     * 
     * @param id
     *            the spinner item id.
     * @param name
     *            the spinner item text.
     */
    public FieldValueSpinnerItem(String id, CharSequence name) {
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
