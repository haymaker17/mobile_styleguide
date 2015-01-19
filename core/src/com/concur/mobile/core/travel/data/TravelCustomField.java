/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>ExpenseReportFormField</code> for model travel custom fields.
 */
public class TravelCustomField extends ExpenseReportFormField {

    private static final long serialVersionUID = 3867408793350160478L;

    private static final String CLS_TAG = TravelCustomField.class.getSimpleName();

    /**
     * Contains the dependency attribute ID for this travel custom field.
     */
    protected String dependencyAttributeId;

    /**
     * Contains whether or not this field has dependent fields.
     */
    protected Boolean hasDependency;

    /**
     * Contains whether or not this field should be displayed at the start of booking. If <code>true</code> then display at start;
     * otherwise, display at end.
     */
    protected Boolean displayAtStart;

    /**
     * Contains the list of possible values for this field.
     */
    protected List<TravelCustomFieldValueSpinnerItem> fieldValues;

    /**
     * Models saved field information.
     * 
     * @author andy
     */
    public static class SavedFieldInfo {

        public String fieldId;

        public String value;

        public String valueId;

    };

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Gets the list of field values associated with this field.
     * 
     * @return return the list of associated field values.
     */
    public List<TravelCustomFieldValueSpinnerItem> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<TravelCustomFieldValueSpinnerItem> fieldValuesList) {
        fieldValues = fieldValuesList;
    }

    /**
     * Gets whether other form field values are dependent upon the value of this field.
     * 
     * @return returns whether other form field values are dependent upon the value of this field.
     */
    public boolean hasDependency() {
        return ((hasDependency != null) ? hasDependency : false);
    }

    /**
     * Gets whether or not this form field should be displayed at the start of a booking search.
     * 
     * @return return <code>true</code> if the field should be displayed at the start of the booking process; <code>false</code>
     *         if the field should be display at the end of booking process.
     */
    public boolean displayAtStart() {
        return ((displayAtStart != null) ? displayAtStart : false);
    }

    /**
     * Will deserialize saved travel custom field information that was serialized with a call to
     * <code>serializeToXMLForWire</code>.
     * 
     * @param savedFieldInfo
     *            the XML serialized string.
     * @return returns a list of <code>TravelCustomField.SaveFieldInfo</code> objects that can be used to fill in field values.
     */
    public static List<SavedFieldInfo> deserializeSavedFieldInfo(String savedFieldInfo) {
        List<SavedFieldInfo> retVal = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            TravelCustomFieldSaveSAXHandler handler = new TravelCustomFieldSaveSAXHandler();
            parser.parse(new ByteArrayInputStream(savedFieldInfo.getBytes()), handler);
            retVal = handler.getFields();
        } catch (ParserConfigurationException parsConfExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".deserializeSavedFieldInfo: parser exception.", parsConfExc);
        } catch (SAXException saxExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".deserializeSavedFieldInfo: sax parsing exception.", saxExc);
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".deserializeSavedFieldInfo: sax parsing exception.", ioExc);
        }
        return retVal;
    }

    /**
     * Will serialize to XML a list of form fields suitable to be sent in an booking request.
     * 
     * @param strBldr
     *            an instance of <code>StringBuilder</code> into which the XML serialization should be written.
     * @param flds
     *            the list of travel custom fields to be serialized.
     * @param includeValueIds
     *            whether to include the value id's for pick-list controls.
     */
    public static void serializeToXMLForWire(StringBuilder strBldr, List<TravelCustomField> flds,
            boolean includeValueIds) {
        if (strBldr != null) {
            if (flds != null && flds.size() > 0) {
                strBldr.append("<CustomFields>");
                for (TravelCustomField tcf : flds) {
                    strBldr.append("<Field>");
                    ViewUtil.addXmlElement(strBldr, "Id", tcf.getId());
                    switch (tcf.getControlType()) {
                    case TEXT_AREA:
                    case EDIT: {
                        String tcfValue = tcf.getValue();
                        strBldr.append("<Value>");
                        strBldr.append((tcfValue != null) ? FormatUtil.escapeForXML(tcfValue) : "");
                        strBldr.append("</Value>");
                        break;
                    }
                    case CHECKBOX: {
                        // Value will be either "yes"/"no".
                        String tcfValue = tcf.getValue();
                        if (tcfValue != null) {
                            if (tcfValue.equalsIgnoreCase("yes")) {
                                tcfValue = "true";
                            } else {
                                tcfValue = "false";
                            }
                        } else {
                            tcfValue = "";
                        }
                        strBldr.append("<Value>");
                        strBldr.append(tcfValue);
                        strBldr.append("</Value>");
                        break;
                    }
                    case PICK_LIST: {
                        strBldr.append("<Value>");
                        // The StaticPickListFormField will store the ValueId in 'LiKey', but the OptionText in
                        // 'Value'. So, we need to look-up the 'Value' based on a match of 'ValueId'.
                        String tcfValue = null;
                        for (TravelCustomFieldValueSpinnerItem tcfsi : tcf.fieldValues) {
                            if (tcf.getLiKey() != null && tcfsi.id != null && tcf.getLiKey().equalsIgnoreCase(tcfsi.id)) {
                                tcfValue = FormatUtil.escapeForXML(tcfsi.value);
                                break;
                            }
                        }
                        strBldr.append((tcfValue != null) ? tcfValue : "");
                        strBldr.append("</Value>");
                        if (includeValueIds) {
                            strBldr.append("<ValueId>");
                            if (tcf.getLiKey() != null) {
                                strBldr.append(FormatUtil.escapeForXML(tcf.getLiKey()));
                            }
                            strBldr.append("</ValueId>");
                        }
                        break;
                    }
                    }
                    strBldr.append("</Field>");
                }
                strBldr.append("</CustomFields>");
            }
        }
    }

    /**
     * An extension of <code>DefaultHandler</code> for parsing persisted travel custom fields.
     */
    public static class TravelCustomFieldSaveSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = TravelCustomField.CLS_TAG + "."
                + TravelCustomFieldSaveSAXHandler.class.getSimpleName();

        private static final String CUSTOM_FIELDS = "CustomFields";
        private static final String FIELD = "Field";
        private static final String VALUE = "Value";
        private static final String VALUE_ID = "ValueId";
        private static final String ID = "Id";

        /**
         * Contains the list of parsed saved fields.
         */
        protected List<SavedFieldInfo> savedFields;

        /**
         * Contains the currently saved field.
         */
        protected SavedFieldInfo savedField;

        /**
         * Contained parsed field data.
         */
        protected StringBuilder chars = new StringBuilder();

        /**
         * Gets the list of parsed form fields.
         * 
         * @return returns the list of parsed form fields.
         */
        public List<SavedFieldInfo> getFields() {
            return savedFields;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(CUSTOM_FIELDS)) {
                savedFields = new ArrayList<SavedFieldInfo>();
            } else if (localName.equalsIgnoreCase(FIELD)) {
                savedField = new SavedFieldInfo();
            }
            chars.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (localName.equalsIgnoreCase(FIELD)) {
                if (savedFields != null) {
                    if (savedField != null) {
                        savedFields.add(savedField);
                        savedField = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: savedField is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: savedFields is null!");
                }
            } else if (localName.equalsIgnoreCase(VALUE)) {
                if (savedField != null) {
                    savedField.value = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: savedField is null!");
                }
            } else if (localName.equalsIgnoreCase(VALUE_ID)) {
                if (savedField != null) {
                    savedField.valueId = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: savedField is null!");
                }
            } else if (localName.equalsIgnoreCase(ID)) {
                if (savedField != null) {
                    savedField.fieldId = chars.toString().trim();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: savedField is null!");
                }
            } else if (localName.equalsIgnoreCase(CUSTOM_FIELDS)) {
                // No-op.
            }

            chars.setLength(0);
        }

    }

    /**
     * An extension of <code>DefaultHandler</code> for parsing travel custom field information.
     */
    public static class TravelCustomFieldSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = TravelCustomField.CLS_TAG + "."
                + TravelCustomFieldSAXHandler.class.getSimpleName();

        private static final String FIELD = "Field";
        private static final String VALUES = "Values";
        private static final String ATTRIBUTE_VALUE = "AttributeValue";

        private static final String ATTRIBUTE_ID = "AttributeId";
        private static final String ATTRIBUTE_TITLE = "AttributeTitle";
        private static final String CURRENT_VALUE = "CurrentValue";
        private static final String DATA_TYPE = "DataType";
        private static final String HAS_DEPENDENCY = "HasDependency";
        private static final String DEPENDENCY_ATTRIBUTE_ID = "DependencyAttributeId";
        private static final String MAX_LENGTH = "MaxLength";
        private static final String MIN_LENGTH = "MinLength";
        private static final String REQUIRED = "Required";
        private static final String DISPLAY_AT_START = "DisplayAtStart";
        private static final String LARGE_VALUE_COUNT = "LargeValueCount";

        private static final String DATA_TYPE_STRING = "string";
        private static final String DATA_TYPE_TEXT = "text";
        private static final String DATA_TYPE_NUMBER = "number";
        private static final String DATA_TYPE_BOOLEAN = "boolean";

        /**
         * Contains a reference to the parsed list of form fields.
         */
        protected List<TravelCustomField> fields;

        /**
         * Contains the current form field being parsed.
         */
        protected TravelCustomField curFld;

        /**
         * Contains the current form field value being parsed.
         */
        protected TravelCustomFieldValueSpinnerItem curFldValue;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Contained parsed field data.
         */
        protected StringBuilder chars = new StringBuilder();

        /**
         * Gets the list of parsed form fields.
         * 
         * @return returns the list of parsed form fields.
         */
        public List<TravelCustomField> getFields() {
            return fields;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(FIELD)) {
                curFld = new TravelCustomField();
                chars.setLength(0);
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(VALUES)) {
                if (curFld != null) {
                    curFld.fieldValues = new ArrayList<TravelCustomFieldValueSpinnerItem>();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: curFld is null!");
                }
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(ATTRIBUTE_VALUE)) {
                if (curFld != null) {
                    curFldValue = new TravelCustomFieldValueSpinnerItem("", "");
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: curFld is null!");
                }
                elementHandled = true;
            }
            if (elementHandled) {
                chars.setLength(0);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;
            super.endElement(uri, localName, qName);
            if (!elementHandled) {
                String cleanChars = chars.toString().trim();
                if (curFldValue != null) {
                    if (localName.equalsIgnoreCase(ATTRIBUTE_VALUE)) {
                        if (curFld.fieldValues != null) {
                            curFld.fieldValues.add(curFldValue);
                            curFldValue = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: curFld.fieldValues is null!");
                        }
                    } else {
                        curFldValue.handleValue(localName, cleanChars);
                    }
                    elementHandled = true;
                } else if (curFld != null) {
                    if (localName.equalsIgnoreCase(VALUES)) {
                        // Ensure 'OptionText' is set.
                        if (curFld.fieldValues != null && curFld.fieldValues.size() > 0) {
                            for (TravelCustomFieldValueSpinnerItem tcfsi : curFld.fieldValues) {
                                if (tcfsi.optionText == null || tcfsi.optionText.length() == 0) {
                                    if (tcfsi.value != null && tcfsi.value.length() > 0) {
                                        tcfsi.optionText = tcfsi.value;
                                        tcfsi.name = tcfsi.value;
                                    }
                                }
                            }
                        }
                        curFld.setStaticList((SpinnerItem[]) curFld.fieldValues
                                .toArray(new TravelCustomFieldValueSpinnerItem[0]));
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FIELD)) {
                        if (fields == null) {
                            fields = new ArrayList<TravelCustomField>();
                        }
                        fields.add(curFld);
                        // If a list of field values have been set on a field, then set the control type
                        // to a pick list ensuring a static pick list control will be created.
                        if (curFld.fieldValues != null && curFld.fieldValues.size() > 0) {
                            // Set the control type.
                            curFld.setControlType(ControlType.PICK_LIST);
                            // Set the 'LiKey' on 'curFld' if it has a value.
                            if (curFld.getValue() != null) {
                                for (TravelCustomFieldValueSpinnerItem tcfsi : curFld.fieldValues) {
                                    if (tcfsi.value != null && tcfsi.value.equalsIgnoreCase(curFld.getValue())) {
                                        curFld.setLiKey(tcfsi.id);
                                    }
                                }
                            }
                        }
                        // Disable the field length values if:
                        // 1. min > max
                        // 2. min and max == 0.
                        if (curFld.getMinLength() != -1
                                && curFld.getMaxLength() != -1
                                && ((curFld.getMinLength() > curFld.getMaxLength()) || ((curFld.getMinLength() == 0 && curFld
                                        .getMaxLength() == 0)))) {
                            curFld.setMinLength(-1);
                            curFld.setMaxLength(-1);
                        }
                        curFld = null;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ATTRIBUTE_ID)) {
                        curFld.setId(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(ATTRIBUTE_TITLE)) {
                        curFld.setLabel(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(CURRENT_VALUE)) {
                        curFld.setValue(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(DATA_TYPE)) {
                        if (cleanChars.equalsIgnoreCase(DATA_TYPE_STRING)) {
                            // Define an inline text field.
                            curFld.setDataType(DataType.CHAR);
                            curFld.setControlType(ControlType.EDIT);
                            curFld.setInputType(InputType.USER);
                            curFld.setAccessType(AccessType.RW);
                        } else if (cleanChars.equalsIgnoreCase(DATA_TYPE_BOOLEAN)) {
                            // Define a boolean pick list control.
                            curFld.setDataType(DataType.BOOLEAN);
                            curFld.setControlType(ControlType.CHECKBOX);
                            curFld.setInputType(InputType.USER);
                            curFld.setAccessType(AccessType.RW);
                        } else if (cleanChars.equalsIgnoreCase(DATA_TYPE_NUMBER)) {
                            // Define a one-line edit control for an number.
                            curFld.setDataType(DataType.INTEGER);
                            curFld.setControlType(ControlType.EDIT);
                            curFld.setInputType(InputType.USER);
                            curFld.setAccessType(AccessType.RW);
                        } else if (cleanChars.equalsIgnoreCase(DATA_TYPE_TEXT)) {
                            // Define a multi-line text area control.
                            curFld.setDataType(DataType.CHAR);
                            curFld.setControlType(ControlType.TEXT_AREA);
                            curFld.setInputType(InputType.USER);
                            curFld.setAccessType(AccessType.RW);
                        } else {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unknown data type '" + cleanChars + "'.");
                        }
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(DEPENDENCY_ATTRIBUTE_ID)) {
                        curFld.dependencyAttributeId = cleanChars;
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(HAS_DEPENDENCY)) {
                        curFld.hasDependency = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(MAX_LENGTH)) {
                        Integer maxLength = Parse.safeParseInteger(cleanChars);
                        if (maxLength != null) {
                            curFld.setMaxLength(maxLength);
                        }
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(MIN_LENGTH)) {
                        Integer minLength = Parse.safeParseInteger(cleanChars);
                        if (minLength != null) {
                            curFld.setMinLength(minLength);
                        }
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(REQUIRED)) {
                        curFld.setRequired(Parse.safeParseBoolean(cleanChars));
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(DISPLAY_AT_START)) {
                        curFld.displayAtStart = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(LARGE_VALUE_COUNT)) {
                        curFld.largeValueCount = Parse.safeParseBoolean(cleanChars);
                        elementHandled = true;
                    } else if (!elementHandled && this.getClass().equals(TravelCustomFieldSAXHandler.class)) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName
                                + "' and value '" + cleanChars + "'.");
                        // Ensure the element is marked as handled if 'this' isn't a sub-class to ensure
                        // 'chars' is cleared out.
                        elementHandled = true;
                    }
                }
            }

            if (elementHandled) {
                // Clear out the stored element values.
                chars.setLength(0);
            }
        }

    }

}