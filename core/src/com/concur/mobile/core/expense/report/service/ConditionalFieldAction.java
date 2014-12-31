package com.concur.mobile.core.expense.report.service;

import android.util.Log;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class ConditionalFieldAction
{
    public enum AccessVisibility {

        HIDE("HIDE"), SHOW("SHOW");

        private final String value;

        AccessVisibility(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AccessVisibility findByValue(String text) {
            if (text != null) {
                for (AccessVisibility access : AccessVisibility.values()) {
                    if (text.equalsIgnoreCase(access.value)) {
                        return access;
                    }
                }
            }
            return null;
        }
    }

    private static final String CLS_TAG = ConditionalFieldAction.class.getSimpleName();

    protected String formField;

    protected AccessVisibility visibility;

    protected ExpenseReportFormField.AccessType accessType;

    public String getFormField() {
        return formField;
    }

    public void setFormField(String formField) {
        this.formField = formField;
    }

    public AccessVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(AccessVisibility visibility) {
        this.visibility = visibility;
    }

    public ExpenseReportFormField.AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(ExpenseReportFormField.AccessType accessType) {
        this.accessType = accessType;
    }

    public static class ConditionalFieldActionSAXHandler extends DefaultHandler {

        private static final String DYNAMIC_ACTION_LIST = "ArrayOfDynamicFieldAction";

        private static final String DYNAMIC_FIELD_ACTION = "DynamicFieldAction";

        private static final String FORM_FIELD = "Field";

        private static final String FIELD_ACCESS = "Access";

        private static final String ACTION = "Action";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ConditionalFieldAction</code> objects that have been parsed.
         */
        private ArrayList<ConditionalFieldAction> conditionalFieldActions = new ArrayList<ConditionalFieldAction>();

        /**
         * Contains a reference to the action currently being built.
         */
        private ConditionalFieldAction conditionalFieldAction;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;


        /**
         * Gets the list of <code>ConditionalFieldAction</code> objects that have been parsed.
         *
         * @return the list of parsed <code>ConditionalFieldAction</code> objects.
         */
        public ArrayList<ConditionalFieldAction> getConditionalFieldActions() {
            return conditionalFieldActions;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(DYNAMIC_ACTION_LIST)) {
                conditionalFieldActions = new ArrayList<ConditionalFieldAction>();
                elementHandled = true;
            }
            else if (localName.equalsIgnoreCase(DYNAMIC_FIELD_ACTION)) {
                conditionalFieldAction = new ConditionalFieldAction();
                chars.setLength(0);
                elementHandled = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            elementHandled = false;
            super.endElement(uri, localName, qName);
            if (conditionalFieldAction != null) {
                if (localName.equalsIgnoreCase(FORM_FIELD)) {
                    conditionalFieldAction.formField = chars.toString().trim();
                    elementHandled = true;
                }
                else if (localName.equalsIgnoreCase(FIELD_ACCESS)) {
                    String access = chars.toString().trim();
                    if (access.equalsIgnoreCase("HD")) {
                        conditionalFieldAction.accessType = ExpenseReportFormField.AccessType.HD;
                    }
                    else if (access.equalsIgnoreCase("RO")) {
                        conditionalFieldAction.accessType = ExpenseReportFormField.AccessType.RO;
                    }
                    else if (access.equalsIgnoreCase("RW")) {
                        conditionalFieldAction.accessType = ExpenseReportFormField.AccessType.RW;
                    }
                    else {
                        conditionalFieldAction.accessType = ExpenseReportFormField.AccessType.UNSPECIFED;
                    }
                    elementHandled = true;
                }
                else if (localName.equalsIgnoreCase(ACTION)) {
                    String action = chars.toString().trim();
                    if (action.equalsIgnoreCase("HIDE")) {
                        conditionalFieldAction.visibility = AccessVisibility.HIDE;
                    }
                    else if (action.equalsIgnoreCase("SHOW")) {
                        conditionalFieldAction.visibility = AccessVisibility.SHOW;
                    }
                    elementHandled = true;
                }
                else if (localName.equalsIgnoreCase(DYNAMIC_FIELD_ACTION)) {
                    conditionalFieldActions.add(conditionalFieldAction);
                    conditionalFieldAction = null;
                    elementHandled = true;
                }
                else if (localName.equalsIgnoreCase(DYNAMIC_ACTION_LIST)) {
                    postProcessList();
                    elementHandled = true;
                }
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

        public void postProcessList() {

        }

    }

    public static ArrayList<ConditionalFieldAction> parseConditionalFieldActionXml(String responseXml) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".parseConditionalFieldActionXml: ");

        ArrayList<ConditionalFieldAction> conditionalFieldActions;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ConditionalFieldActionSAXHandler handler = new ConditionalFieldActionSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            conditionalFieldActions = handler.getConditionalFieldActions();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conditionalFieldActions;
    }
}
