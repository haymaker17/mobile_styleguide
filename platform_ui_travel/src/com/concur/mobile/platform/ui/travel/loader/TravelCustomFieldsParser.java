package com.concur.mobile.platform.ui.travel.loader;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.platform.common.FieldValueSpinnerItem;
import com.concur.mobile.platform.util.Parse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RatanK
 */

// This class was initially created in the Platform but did not have the visibility of FieldValueSpinnerItem.
// Hence moved to Platform Travel UI.
public class TravelCustomFieldsParser implements Parser {

    public TravelCustomField custField;
    private List<FieldValueSpinnerItem> fieldValues;
    private FieldValueSpinnerItem fieldValue;

    @Override
    public void startTag(String tag) {
        // can be enhanced to use in the existing end point /Mobile/Config/TravelCustomFields
        if (tag.equals("TravelCustomFieldSearch")) {
            custField = new TravelCustomField();
        } else if (tag.equals("Values")) {
            fieldValues = new ArrayList<FieldValueSpinnerItem>();
        } else if (tag.equals("AttributeValue")) {
            fieldValue = new FieldValueSpinnerItem("", "");
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("AttributeId")) {
            custField.setId(text);
        } else if (tag.equals("LargeValueCount")) {
            custField.setLargeValueCount(Parse.safeParseBoolean(text));
        } else if (tag.equals("Required")) {
            custField.setRequired(Parse.safeParseBoolean(text));
        } else if (tag.equals("AttributeId")) {
            fieldValue.attributeId = text;
        } else if (tag.equals("OptionText")) {
            fieldValue.optionText = text;
        } else if (tag.equals("Sequence")) {
            fieldValue.sequence = text;
        } else if (tag.equals("Value")) {
            fieldValue.value = text;
        } else if (tag.equals("ValueId")) {
            fieldValue.valueId = text;
        }
    }

    @Override
    public void endTag(String tag) {
        if (tag.equals("AttributeValue")) {
            fieldValues.add(fieldValue);
        } else if (tag.equals("Values")) {
            custField.setFieldValues(fieldValues);
        }
    }

}