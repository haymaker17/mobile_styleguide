package com.concur.mobile.core.travel.data;

import java.util.List;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;

public class SellOptionField extends TravelCustomField {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = 5220688398389284233L;

    private String instructions;

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    // generate XML with pre-sell options for the segment sell i.e flight options for air sell etc.
    public static void serializePreSellOptionsToXMLForWire(StringBuilder strBldr, List<SellOptionField> flds,
            String rootElementName, String elementName) {
        if (strBldr != null) {
            if (flds != null && flds.size() > 0) {
                StringBuilder optionStrBldr = new StringBuilder();
                String valueStr = null;
                for (SellOptionField sellOptField : flds) {
                    // get the value for this field
                    valueStr = getValueOfSellOptionField(sellOptField);

                    // add only when a value is available
                    if (valueStr != null && valueStr.trim().length() > 0) {
                        optionStrBldr.append("<" + elementName + ">");
                        ViewUtil.addXmlElement(optionStrBldr, "Id", sellOptField.getId());
                        optionStrBldr.append("<Value>");
                        optionStrBldr.append(FormatUtil.escapeForXML(valueStr));
                        optionStrBldr.append("</Value>");
                        optionStrBldr.append("</" + elementName + ">");
                    }
                }

                if (optionStrBldr.length() > 0) {
                    strBldr.append("<" + rootElementName + ">");
                    strBldr.append(optionStrBldr);
                    strBldr.append("</" + rootElementName + ">");
                }
            }
        }
    }

    public static String getValueOfSellOptionField(SellOptionField sellOptField) {
        String valueStr = null;
        switch (sellOptField.getControlType()) {
        case TEXT_AREA:
        case EDIT: {
            valueStr = sellOptField.getValue();
            break;
        }
        case CHECKBOX: {
            // Value will be either "yes"/"no".
            String sellOptFieldValue = sellOptField.getValue();
            if (sellOptFieldValue != null) {
                if (sellOptFieldValue.equalsIgnoreCase("yes")) {
                    sellOptFieldValue = "true";
                } else {
                    sellOptFieldValue = "false";
                }
            }
            break;
        }
        case PICK_LIST: {
            valueStr = sellOptField.getLiKey();
            break;
        }
        }
        return valueStr;
    }

}
