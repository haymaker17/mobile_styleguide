/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ExpenseReportFormFieldSAXHandler;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Models expense report attendee information.
 * 
 * @author AndrewK
 */
public class ExpenseReportAttendee implements Serializable {

    public static final String FIRST_NAME_FIELD_ID = "FirstName";
    public static final String LAST_NAME_FIELD_ID = "LastName";
    public static final String COMPANY_NAME_FIELD_ID = "CompanyName";
    public static final String EXTERNAL_FIELD_ID = "ExternalId";
    public static final String ATTENDEE_TYPE_KEY = "AtnTypeKey";
    public static final String ATTENDEE_TYPE_CODE = "AtnTypeCode";
    public static final String ATTENDEE_TYPE_NAME = "AtnTypeName";

    private static final String CLS_TAG = ExpenseReportAttendee.class.getSimpleName();

    public Double amount;

    public Boolean isAmountEdited;

    public String atnKey;

    public String company;

    protected List<ExpenseReportFormField> formFields;

    public Integer instanceCount;

    public String name;

    public String title;

    public String atnTypeKey;

    public String atnTypeName;

    public String atnTypeCode;

    public String firstName;

    public String lastName;

    public String versionNumber;

    public String currentVersionNumber;

    public String externalId;

    /**
     * Gets the attendee form fields.
     * 
     * @return the attendee form fields.
     */
    public List<ExpenseReportFormField> getFormFields() {
        return formFields;
    }

    /**
     * Sets the attendee form fields.
     * 
     * @param formFields
     *            the attendee form fields.
     */
    public void setFormFields(List<ExpenseReportFormField> formFields) {
        this.formFields = formFields;
    }

    /**
     * Will construct an instance of <code>ExpenseReportAttendee</code> based on a shallow copy of <code>attendee</code>.
     * 
     * @param attendee
     *            the copy constructor source attendee.
     */
    public ExpenseReportAttendee(ExpenseReportAttendee attendee) {
        this.copy(attendee);
    }

    /**
     * Performs a shallow copy of values from <code>this</code> to <code>attendee</code>.
     * 
     * @param attendee
     *            the attendee providing source values.
     */
    public void copy(ExpenseReportAttendee attendee) {
        this.amount = attendee.amount;
        this.isAmountEdited = attendee.isAmountEdited;
        this.atnKey = attendee.atnKey;
        this.company = attendee.company;
        this.formFields = attendee.formFields;
        this.instanceCount = attendee.instanceCount;
        this.name = attendee.name;
        this.title = attendee.title;
        this.atnTypeKey = attendee.atnTypeKey;
        this.atnTypeName = attendee.atnTypeName;
        this.atnTypeCode = attendee.atnTypeCode;
        this.firstName = attendee.firstName;
        this.lastName = attendee.lastName;
        this.versionNumber = attendee.versionNumber;
        this.currentVersionNumber = attendee.currentVersionNumber;
        this.externalId = attendee.externalId;
    }

    /**
     * Constructs an instance of <code>ExpenseReportAttendee</code> based on default values.
     */
    public ExpenseReportAttendee() {
    }

    public void dumpInfo() {
        if (formFields != null) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".dumpInfo: formFields != null.");
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".dumpInfo: formFields -> null.");
        }
    }

    /**
     * Finds an instance of <code>ExpenseReportFormField</code> based on a key look-up.
     * 
     * @param fieldId
     *            the form field ID.
     * 
     * @return an instance of <code>ExpenseReportFormField</code> if found; <code>null</code> otherwise.
     */
    public ExpenseReportFormField findFormFieldByFieldId(String fieldId) {

        ExpenseReportFormField expRepFrmFld = null;

        if (formFields != null) {
            Iterator<ExpenseReportFormField> frmFldIter = formFields.iterator();
            while (frmFldIter.hasNext()) {
                ExpenseReportFormField frmFld = frmFldIter.next();
                if (frmFld.getId().equalsIgnoreCase(fieldId)) {
                    expRepFrmFld = frmFld;
                    break;
                }
            }
        }
        return expRepFrmFld;
    }

    /**
     * Will determine whether or not this attendee is editable.
     * 
     * @param defaultAttendee
     *            the default attendee.
     * @return whether this attendee is editable.
     */
    public boolean isEditable(ExpenseReportAttendee defaultAttendee) {
        boolean isEditable = false;
        if (atnTypeCode != null) {
            // Check type-code for system employee name.
            isEditable = !atnTypeCode.equalsIgnoreCase(Const.SYSTEM_EMPLOYEE_ATTENDEE_TYPE_CODE);
        } else if (defaultAttendee != null) {
            if (atnKey != null && defaultAttendee.atnKey != null) {
                isEditable = !atnKey.equalsIgnoreCase(defaultAttendee.atnKey);
            }
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG
                    + ".isEditable: unable to determine whether attendee is editable, defaulting to 'false'.");
        }
        return isEditable;
    }

    /**
     * Determines whether an attendee having both a current version number and a version number have the same value.
     * 
     * @return whether this attendee has a current version number and a version number that match.
     */
    public boolean isVersionMismatch() {
        boolean retVal = false;
        retVal = (versionNumber != null && currentVersionNumber != null && !versionNumber
                .equalsIgnoreCase(currentVersionNumber));
        return retVal;
    }

    /**
     * Gets the attendee type key value. This is a convenience method that will first check the <code>atnTypeKey</code> attribute,
     * if <code>null</code> it will then look for the appropriate field.
     * 
     * @return the attendee type key.
     */
    public String getTypeKey() {
        String retVal = atnTypeKey;
        if (retVal == null) {
            ExpenseReportFormField ff = findFormFieldByFieldId(IExpenseReportFormField.ATTENDEE_TYPE_FIELD_ID);
            if (ff != null) {
                retVal = ff.getLiKey();
            }
        }
        return retVal;
    }

    /**
     * Gets the attendee type name. This is a convenience method that will first check the <code>atnTypeName</code> attribute, if
     * <code>null</code> it will then look for the appropriate field.
     * 
     * @return the attendee type key.
     */
    public String getTypeName() {
        String retVal = atnTypeName;
        if (retVal == null) {
            ExpenseReportFormField ff = findFormFieldByFieldId(IExpenseReportFormField.ATTENDEE_TYPE_FIELD_ID);
            if (ff != null) {
                retVal = ff.getValue();
            }
        }
        return retVal;
    }

    /**
     * Gets the first name for the attendee. This is a convenience method that will first check the <code>firstName</code>
     * attribute, if <code>null</code>, it will then look for the appropriate field.
     * 
     * @return the attendees first name.
     */
    public String getFirstName() {
        String retVal = firstName;
        if (retVal == null) {
            ExpenseReportFormField ff = findFormFieldByFieldId(IExpenseReportFormField.ATTENDEE_FIRST_NAME_FIELD_ID);
            if (ff != null) {
                retVal = ff.getValue();
            }
        }
        return retVal;
    }

    /**
     * Gets the last name for the attendee. This is a convenience method that will first check the <code>lastName</code>
     * attribute, if <code>null</code>, it will then look for the appropriate field.
     * 
     * @return the attendees last name.
     */
    public String getLastName() {
        String retVal = lastName;
        if (lastName == null) {
            ExpenseReportFormField ff = findFormFieldByFieldId(IExpenseReportFormField.ATTENDEE_LAST_NAME_FIELD_ID);
            if (ff != null) {
                retVal = ff.getValue();
            }
        }
        return retVal;
    }

    /**
     * Gets the company for the attendee. This is a convenience method that will first check the <code>company</code> attribute,
     * if <code>null</code>, it will then look for the appropriate field.
     * 
     * @return the attendees company.
     */
    public String getCompany() {
        String retVal = company;
        if (company == null) {
            ExpenseReportFormField ff = findFormFieldByFieldId(IExpenseReportFormField.ATTENDEE_COMPANY_FIELD_ID);
            if (ff != null) {
                retVal = ff.getValue();
            }
        }
        return retVal;
    }

    /**
     * Gets the title for the attendee. This is a convenience method that will first check the <code>title</code> attribute, if
     * <code>null</code>, it will then look for the appropriate field.
     * 
     * @return the attendees title.
     */
    public String getTitle() {
        String retVal = title;
        if (title == null) {
            ExpenseReportFormField ff = findFormFieldByFieldId(IExpenseReportFormField.ATTENDEE_TITLE_FIELD_ID);
            if (ff != null) {
                retVal = ff.getValue();
            }
        }
        return retVal;
    }

    /**
     * An extension of <code>DefaultHandler</code> for parsing attendee information.
     * 
     * @author AndrewK
     */
    public static class ExpenseReportAttendeeSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ExpenseReportAttendee.CLS_TAG + "."
                + ExpenseReportAttendeeSAXHandler.class.getSimpleName();

        private static final String ATTENDEE = "Attendee";
        private static final String ATTENDEES = "Attendees";
        private static final String AMOUNT = "Amount";
        private static final String ATN_KEY = "AtnKey";
        private static final String COMPANY = "Company";
        private static final String INSTANCE_COUNT = "InstanceCount";
        private static final String NAME = "Name";
        private static final String TITLE = "Title";
        private static final String FIELDS = "Fields";
        private static final String ATN_TYPE_KEY = "AtnTypeKey";
        private static final String ATN_TYPE_NAME = "AtnTypeName";
        private static final String ATN_TYPE_CODE = "AtnTypeCode";
        private static final String FIRST_NAME = "FirstName";
        private static final String LAST_NAME = "LastName";
        private static final String VERSION_NUMBER = "VersionNumber";
        private static final String IS_AMOUNT_EDITED = "IsAmountEdited";
        private static final String CURRENT_VERSION_NUMBER = "CurrentVersionNumber";
        private static final String EXTERNAL_ID = "ExternalId";
        private static final String COLUMN_DEFINITIONS = "ColumnDefinitions";

        /**
         * Contains a reference to the form field parser.
         */
        private ExpenseReportFormFieldSAXHandler formFieldHandler;

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ExpenseReportAttendee</code> objects that have been parsed.
         */
        private ArrayList<ExpenseReportAttendee> reportAttendees = new ArrayList<ExpenseReportAttendee>();

        /**
         * Contains a reference to a list of <code>ExpenseReportFormField</code> objects modeling attendee form field column
         * definitions.
         */
        private List<ExpenseReportFormField> columnDefinitions;

        /**
         * Contains a reference to the report attendee currently being built.
         */
        public ExpenseReportAttendee reportAttendee;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        // The handled is in the duplicate attendee sub-elements
        public boolean inDuplicates;

        // Indicates whether column definitions is being parsed.
        public boolean inColumnDefinitions;

        /**
         * Gets the list of <code>ExpenseReportAttendee</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ExpenseReportAttendee</code> objects.
         */
        public List<ExpenseReportAttendee> getReportAttendees() {
            return reportAttendees;
        }

        /**
         * Gets the list of <code>ExpenseReportFormField</code> objects representing column definitions.
         * 
         * @return returns a list of <code>ExpenseReportFormField</code> objects representing column definitions.
         */
        public List<ExpenseReportFormField> getColumnDefinitions() {
            return columnDefinitions;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (formFieldHandler != null) {
                formFieldHandler.characters(ch, start, length);
            } else {
                super.characters(ch, start, length);
                chars.append(ch, start, length);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (formFieldHandler != null) {
                formFieldHandler.startElement(uri, localName, qName, attributes);
            } else {
                elementHandled = false;
                super.startElement(uri, localName, qName, attributes);
                if (!elementHandled) {
                    if (localName.equalsIgnoreCase(ATTENDEE)) {
                        reportAttendee = new ExpenseReportAttendee();
                        chars.setLength(0);
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(FIELDS)) {
                        formFieldHandler = new ExpenseReportFormField.ExpenseReportFormFieldSAXHandler();
                        elementHandled = true;
                    } else if (localName.equalsIgnoreCase(COLUMN_DEFINITIONS)) {
                        formFieldHandler = new ExpenseReportFormField.ExpenseReportFormFieldSAXHandler();
                        inColumnDefinitions = true;
                        elementHandled = true;
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (reportAttendee != null) {
                if (formFieldHandler != null) {
                    if (localName.equalsIgnoreCase(FIELDS) || localName.equalsIgnoreCase(COLUMN_DEFINITIONS)) {
                        if (!inColumnDefinitions) {
                            reportAttendee.formFields = formFieldHandler.getReportFormFields();
                        } else {
                            columnDefinitions = formFieldHandler.getReportFormFields();
                            inColumnDefinitions = false;
                        }
                        formFieldHandler = null;
                    } else {
                        formFieldHandler.endElement(uri, localName, qName);
                    }
                    elementHandled = true;
                } else {
                    elementHandled = false;
                    super.endElement(uri, localName, qName);
                    if (!elementHandled) {
                        final String cleanChars = chars.toString().trim();
                        if (localName.equalsIgnoreCase(ATTENDEE)) {
                            if (!inDuplicates) {
                                // Only add the attendee to the list if not a
                                // duplicate.
                                // Duplicates stay in the reportAttendee object
                                // until the parent parser (in
                                // AttendeeSaveReply)
                                // grabs them for its own duplicate list.
                                reportAttendees.add(reportAttendee);
                            }
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(ATTENDEES)) {
                            // No-op.
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(ATN_TYPE_KEY)) {
                            reportAttendee.atnTypeKey = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(ATN_TYPE_NAME)) {
                            reportAttendee.atnTypeName = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(ATN_TYPE_CODE)) {
                            reportAttendee.atnTypeCode = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(EXTERNAL_ID)) {
                            reportAttendee.externalId = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(FIRST_NAME)) {
                            reportAttendee.firstName = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(LAST_NAME)) {
                            reportAttendee.lastName = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(AMOUNT)) {
                            reportAttendee.amount = Parse.safeParseDouble(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(IS_AMOUNT_EDITED)) {
                            reportAttendee.isAmountEdited = Parse.safeParseBoolean(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(ATN_KEY)) {
                            reportAttendee.atnKey = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(COMPANY)) {
                            reportAttendee.company = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(INSTANCE_COUNT)) {
                            reportAttendee.instanceCount = Parse.safeParseInteger(cleanChars);
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(NAME)) {
                            reportAttendee.name = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(TITLE)) {
                            reportAttendee.title = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(VERSION_NUMBER)) {
                            reportAttendee.versionNumber = cleanChars;
                            elementHandled = true;
                        } else if (localName.equalsIgnoreCase(CURRENT_VERSION_NUMBER)) {
                            reportAttendee.currentVersionNumber = cleanChars;
                            elementHandled = true;
                        } else if (this.getClass().equals(ExpenseReportAttendeeSAXHandler.class)) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled tag '" + localName + "'.");
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current report attendee!");
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

        /**
         * Will serialize to XML the attributes of an attendee object.
         * 
         * @param strBldr
         *            the string builder to append XML serialized data.
         * @param expRepEntDet
         *            the expense report detail object.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReportAttendee attendee) {
            serializeToXML(strBldr, attendee, false);
        }

        /**
         * Will serialize to XML the attributes of an attendee object.
         * 
         * @param strBldr
         *            the string builder to append XML serialized data.
         * @param expRepEntDet
         *            the expense report detail object.
         * @param includeNameSpace
         *            whether to include a namespace declaration for field XML nodes.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReportAttendee attendee,
                boolean includeNameSpace) {
            if (strBldr != null) {
                if (attendee != null) {
                    strBldr.append('<');
                    strBldr.append(ATTENDEE);
                    strBldr.append('>');

                    // Amount
                    ViewUtil.addXmlElement(strBldr, AMOUNT, attendee.amount);
                    // AtnKey
                    ViewUtil.addXmlElement(strBldr, ATN_KEY, attendee.atnKey);
                    // AtnTypeKey
                    ViewUtil.addXmlElement(strBldr, ATN_TYPE_KEY, attendee.atnTypeKey);
                    // AtnTypeName
                    ViewUtil.addXmlElement(strBldr, ATN_TYPE_NAME, attendee.atnTypeName);
                    // Company
                    ViewUtil.addXmlElement(strBldr, COMPANY, attendee.company);
                    // CurrentVersionNumber
                    ViewUtil.addXmlElement(strBldr, CURRENT_VERSION_NUMBER, attendee.currentVersionNumber);
                    // External Id
                    ViewUtil.addXmlElement(strBldr, EXTERNAL_ID, attendee.externalId);
                    // FirstName
                    ViewUtil.addXmlElement(strBldr, FIRST_NAME, attendee.firstName);
                    // InstanceCount
                    ViewUtil.addXmlElement(strBldr, INSTANCE_COUNT, attendee.instanceCount);
                    // IsAmountEdited
                    ViewUtil.addXmlElementYN(strBldr, IS_AMOUNT_EDITED, attendee.isAmountEdited);
                    // LastName
                    ViewUtil.addXmlElement(strBldr, LAST_NAME, attendee.lastName);
                    // Name
                    ViewUtil.addXmlElement(strBldr, NAME, attendee.name);
                    // Title
                    ViewUtil.addXmlElement(strBldr, TITLE, attendee.title);
                    // VersionNumber
                    ViewUtil.addXmlElement(strBldr, VERSION_NUMBER, attendee.versionNumber);
                    // Fields
                    serializeFieldsToXML(strBldr, attendee, includeNameSpace);
                    strBldr.append("</");
                    strBldr.append(ATTENDEE);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: attendee is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize only the fields associated with an attendee.
         * 
         * @param strBldr
         *            the string builder to append XML serialized data.
         * @param attendee
         *            the expense report detail object.
         * @param includeNameSpace
         *            whether to include a namespace declaration for field XML nodes.
         */
        public static void serializeFieldsToXML(StringBuilder strBldr, ExpenseReportAttendee attendee,
                boolean includeNameSpace) {

            serializeFieldsToXML(strBldr, attendee.formFields, includeNameSpace);
        }

        /**
         * Will serialize the passed in list of <code>ExpenseReportFormField</code> objects.
         * 
         * @param strBldr
         *            the instance of <code>StringBuilder</code> into which the serialization will be written.
         * @param formFields
         *            the list of form fields.
         * @param includeNameSpace
         *            whether or not to include a namespace declaration for the form field attributes.
         */
        public static void serializeFieldsToXML(StringBuilder strBldr, List<ExpenseReportFormField> formFields,
                boolean includeNameSpace) {
            // Fields
            if (formFields != null && formFields.size() > 0) {
                strBldr.append('<');
                strBldr.append(FIELDS);
                if (includeNameSpace) {
                    strBldr.append(" xmlns:f='http://schemas.datacontract.org/2004/07/Snowbird'");
                }
                strBldr.append('>');
                for (ExpenseReportFormField frmFld : formFields) {
                    ExpenseReportFormField.ExpenseReportFormFieldSAXHandler.serializeToXML(strBldr, frmFld,
                            includeNameSpace);
                }
                strBldr.append("</");
                strBldr.append(FIELDS);
                strBldr.append('>');
            }
        }
    }

}
