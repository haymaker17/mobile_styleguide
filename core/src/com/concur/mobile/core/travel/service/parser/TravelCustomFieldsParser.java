package com.concur.mobile.core.travel.service.parser;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.TravelCustomFieldValueSpinnerItem;
import com.concur.mobile.platform.util.Parse;

/**
 * 
 * @author RatanK
 * 
 */
public class TravelCustomFieldsParser implements Parser {

    public TravelCustomField custField;
    private List<TravelCustomFieldValueSpinnerItem> fieldValues;
    private TravelCustomFieldValueSpinnerItem fieldValue;

    @Override
    public void startTag(String tag) {
        // can be enhanced to use in the existing end point /Mobile/Config/TravelCustomFields
        if (tag.equals("TravelCustomFieldSearch")) {
            custField = new TravelCustomField();
        } else if (tag.equals("Values")) {
            fieldValues = new ArrayList<TravelCustomFieldValueSpinnerItem>();
        } else if (tag.equals("AttributeValue")) {
            fieldValue = new TravelCustomFieldValueSpinnerItem("", "");
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
