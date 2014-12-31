/**
 * 
 */
package com.concur.mobile.core.travel.data;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * An extension of <code>SpinnerItem</code> for modeling travel custom field values.
 */
public class TravelCustomFieldValueSpinnerItem extends SpinnerItem {

    private static final long serialVersionUID = -489685770470206513L;

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
