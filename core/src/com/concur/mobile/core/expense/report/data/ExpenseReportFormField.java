/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ExpenseTypeFormFieldView;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * Models an expense report form field.
 * 
 * @author AndrewK
 */
public class ExpenseReportFormField implements Serializable, Cloneable {

    private static final long serialVersionUID = -2393463139816587908L;

    public static final String TRANSACTION_DATE_FIELD_ID = "TransactionDate";

    public static final String LOCATION_NAME = "LocName";

    public static final String INSTANCE_COUNT_FIELD_ID = "InstanceCount";
    public static final String ATTENDEE_TYPE_KEY_FIELD_ID = "AtnTypeKey";
    public static final String CURRENCY_KEY_FIELD_ID = "CrnKey";

    public static final String EXPENSE_TYPE_LI_KEY_ID = "ExpTypeLiKey";

    public static final String TRANSACTION_AMOUNT_FIELD_ID = "TransactionAmount";

    public static final String REPORT_ID_FIELD_ID = "ReportId";

    /**
     * Models the form field data type.
     */
    public static enum DataType {
        /**
         * An unspecified data type.
         */
        UNSPECIFED(""),
        /**
         * Variable character data type.
         */
        VARCHAR("VARCHAR"),
        /**
         * A money data type.
         */
        MONEY("MONEY"),
        /**
         * A numeric data type.
         */
        NUMERIC("NUMERIC"),
        /**
         * A character data type.
         */
        CHAR("CHAR"),
        /**
         * An integer data type.
         */
        INTEGER("INTEGER"),
        /**
         * A timestamp data type.
         */
        TIMESTAMP("TIMESTAMP"),
        /**
         * An "expense type" data type.
         */
        EXPENSE_TYPE("EXPTYPE"),
        /**
         * A boolean character data type.
         */
        BOOLEAN("BOOLEANCHAR"),
        /**
         * A connected list data type.
         */
        CONNECTED_LIST("MLIST"),
        /**
         * A list data type (typically paired with ControlType of 'list_edit').
         */
        LIST("LIST"),
        /**
         * A currency data type.
         */
        CURRENCY("CURRENCY"),
        /**
         * A location data type.
         */
        LOCATION("LOCATION");

        private String name;

        DataType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>DataType</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.DataType</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static DataType fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (DataType dt : DataType.values()) {
                    if (dt.name.equalsIgnoreCase(name)) {
                        return dt;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    };

    /**
     * Models the form field access type.
     */
    public static enum AccessType {
        /**
         * An unspecified access type.
         */
        UNSPECIFED(""),
        /**
         * Read/Write access.
         */
        RW("RW"),
        /**
         * Read-Only access.
         */
        RO("RO"),
        /**
         * Hidden.
         */
        HD("HD");

        private String name;

        AccessType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>AccessType</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.AccessType</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static AccessType fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (AccessType at : AccessType.values()) {
                    if (at.name.equalsIgnoreCase(name)) {
                        return at;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    }

    /**
     * Models the form field access type.
     */
    public static enum FormFieldType {
        /**
         * A VAT type form field
         */
        VAT("VAT"),
        /**
         * An Expense report form field
         */
        EXPRPT("EXPRPT");

        private String name;

        FormFieldType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>FormFieldType</code> for <code>name</code>.
         * 
         * @param name
         *            the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.FormFieldType</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static FormFieldType fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (FormFieldType at : FormFieldType.values()) {
                    if (at.name.equalsIgnoreCase(name)) {
                        return at;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    }

    /**
     * Models the form field control type.
     */
    public static enum ControlType {
        /**
         * An unspecified control type.
         */
        UNSPECIFED(""),
        /**
         * A hidden control
         */
        HIDDEN("hidden"),
        /**
         * An inline text field control type.
         */
        EDIT("edit"),
        /**
         * A checkbox control type.
         */
        CHECKBOX("checkbox"),
        /**
         * A searchable list control type.
         */
        PICK_LIST("picklist"),
        /**
         * Another incarnation of a list control type.
         */
        LIST_EDIT("list_edit"),
        /**
         * A control used for date editing.
         */
        DATE_EDIT("date_edit"),
        /**
         * A static field.
         */
        STATIC("static"),
        /**
         * A multi-line text field.
         */
        TEXT_AREA("textarea");

        private String name;

        ControlType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>ControlType</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.ControlType</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static ControlType fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (ControlType ct : ControlType.values()) {
                    if (ct.name.equalsIgnoreCase(name)) {
                        return ct;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }

    }

    /**
     * Models how input is provided for this field.
     */
    public static enum InputType {
        /**
         * End-user supplied value.
         */
        USER("USER"),
        /**
         * Calculated value.
         */
        CALC("CALC");

        private String name;

        InputType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>InputType</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.InputType</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static InputType fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (InputType ct : InputType.values()) {
                    if (ct.name.equalsIgnoreCase(name)) {
                        return ct;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    }

    private static final String CLS_TAG = ExpenseReportFormField.class.getSimpleName();

    protected String id;

    protected String label;

    protected String value;

    protected AccessType accessType;

    protected ControlType controlType;

    protected DataType dataType;

    protected InputType inputType;

    protected FormFieldType formFieldType;

    protected String liKey;

    protected String liCode;

    protected Boolean required;

    protected String ftCode;

    protected String width;

    protected int maxLength = -1;

    protected int minLength = -1;

    protected String copyDownFormType;

    protected String copyDownSource;

    protected String custom;

    protected String listKey;

    protected Boolean isCopyDownSourceForOtherForms;

    protected int hierKey = -1;

    protected int hierLevel = -1;

    protected String parFieldId;

    protected String parFtCode;

    protected int parHierLevel = -1;

    protected String parLiKey;

    protected String itemCopyDownAction;

    protected SpinnerItem[] staticList;

    // Contains the static list of searchable items. Provided to the ListSearch activity as needed.
    protected ArrayList<ListItem> searchableStaticList;

    // Always default to verify the value is valid.
    protected Boolean verifyValue = true;

    protected String failureMsg;

    protected String validExp;

    /**
     * Contains whether or not this field has a signicantly large number of possible values.
     */
    protected Boolean largeValueCount;

    /**
     * Indicates whether this field is a dynamic field that drives other fields visibility
    */
    protected Boolean isConditionalField;

    /**
     * holds the form field key value
    */
    protected String formFieldKey;

    /**
     * holds the form field original ctrl type if current ctrl is hidden
     */
    protected ControlType originalCtrlType;

    public ControlType getOriginalCtrlType() {
        return originalCtrlType;
    }

    public String getFormFieldKey() {
        return formFieldKey;
    }

    public Boolean getIsConditionalField() {
        return isConditionalField;
    }

    public void setLargeValueCount(boolean hasLargeValueCount) {
        largeValueCount = hasLargeValueCount;
    }

    public boolean hasLargeValueCount() {
        return (largeValueCount == null ? false : largeValueCount);
    }

    public ExpenseReportFormField() {
        // There are cases where the access type is not specified, most likely any synthetic field.
        // BusinessDistance on the mileage form is one of these. Default access to RW unless
        // otherwise specified.
        accessType = AccessType.RW;
        // Initialize to the unspecified value as some form field definitions are missing them.
        controlType = ControlType.UNSPECIFED;
        dataType = DataType.UNSPECIFED;
        // Initialize to normal input type.
        inputType = InputType.USER;
    }

    public ExpenseReportFormField(String id, String label, String value, AccessType accessType,
            ControlType controlType, DataType dataType, boolean required) {
        this();
        this.id = id;
        this.label = label;
        this.value = value;
        this.accessType = accessType;
        this.controlType = controlType;
        this.dataType = dataType;
        this.required = required;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Will copy attributes from <code>this</code> into <code>dst</code>.
     * 
     * @param dst
     *            contains the destination form field.
     */
    @SuppressWarnings("unchecked")
    public void copy(ExpenseReportFormField dst) {
        dst.id = id;
        dst.label = label;
        dst.value = value;
        dst.accessType = accessType;
        dst.controlType = controlType;
        dst.dataType = dataType;
        dst.inputType = inputType;
        dst.liKey = liKey;
        dst.liCode = liCode;
        dst.required = required;
        dst.verifyValue = verifyValue;
        dst.ftCode = ftCode;
        dst.width = width;
        dst.maxLength = maxLength;
        dst.minLength = minLength;
        dst.copyDownFormType = copyDownFormType;
        dst.copyDownSource = copyDownSource;
        dst.custom = custom;
        dst.listKey = listKey;
        dst.isCopyDownSourceForOtherForms = isCopyDownSourceForOtherForms;
        dst.hierKey = hierKey;
        dst.hierLevel = hierLevel;
        dst.parFieldId = parFieldId;
        dst.parFtCode = parFtCode;
        dst.parHierLevel = parHierLevel;
        dst.parLiKey = parLiKey;
        dst.itemCopyDownAction = itemCopyDownAction;
        dst.staticList = staticList;
        dst.searchableStaticList = (ArrayList<ListItem>) searchableStaticList.clone();
        dst.failureMsg = failureMsg;
        dst.validExp = validExp;
    }

    /**
     * Gets the form field id.
     * 
     * @return the form field id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the form field id.
     * 
     * @param id
     *            the form field id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the form field label.
     * 
     * @return the form field label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the form field label.
     * 
     * @return the form field label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the form field value.
     * 
     * @return the form field value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the form field value.
     * 
     * @param value
     *            the form field value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the form field access type, i.e., RW, R, W.
     * 
     * @return the form field access type.
     */
    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType at) {
        accessType = at;
    }

    /**
     * Gets the form field control type.
     * 
     * @return the form field control type.
     */
    public ControlType getControlType() {
        return controlType;
    }

    /**
     * Sets the form field control type.
     * 
     * @param controlType
     *            the form field control type.
     */
    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    /**
     * Gets the form field data type.
     * 
     * @return the form field data type.
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Gets the form field type.
     * 
     * @return the form field data type.
     */
    public FormFieldType getFormFieldType() {
        return formFieldType;
    }

    /**
     * Sets the form field type.
     * 
     * @param FormFieldType
     *            the form field type.
     */
    public void setFormFieldType(FormFieldType formFieldType) {
        this.formFieldType = formFieldType;
    }

    /**
     * Sets the form field data type.
     * 
     * @param dataType
     *            the form field data type.
     */
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the form field input type.
     * 
     * @return the form field input type.
     */
    public InputType getInputType() {
        return inputType;
    }

    /**
     * Sets the form field input type.
     * 
     * @param it
     *            the form field input type.
     */
    public void setInputType(InputType it) {
        inputType = it;
    }

    /**
     * Gets the form field li key.
     * 
     * @return the form field li key.
     */
    public String getLiKey() {
        return liKey;
    }

    /**
     * Sets the form field li key.
     * 
     * @param liKey
     *            the form field li key.
     */
    public void setLiKey(String liKey) {
        this.liKey = liKey;
    }

    /**
     * Get the form field li code.
     * 
     * @return the form field li code.
     */
    public String getLiCode() {
        return liCode;
    }

    /**
     * Sets the form field li code.
     * 
     * @param liCode
     *            the form field li code.
     */
    public void setLiCode(String liCode) {
        this.liCode = liCode;
    }

    /**
     * Gets the form field custom.
     * 
     * @return the form field custom.
     */
    public String getCustom() {
        return custom;
    }

    /**
     * Gets whether the form field is required.
     * 
     * @return whether the form field is required.
     */
    public boolean isRequired() {
        return ((required != null) ? required : Boolean.FALSE);
    }

    /**
     * Sets whether the form field is required.
     * 
     * @param required
     *            whether the form field is required.
     */
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * Gets whether or not to verify this field's value when saving.
     * 
     * @return <code>true</code> to verify the value when saving, otherwise <code>false</code> to skip verification.
     */
    public boolean isVerifyValue() {
        return verifyValue;
    }

    /**
     * Sets whether or not to verify if this value is valid when saving the Expense.
     * 
     * @param verifyValue
     *            <code>true</code> to verify the value when saving, otherwise set to <code>false</code> to skip verification.
     */
    public void setVerifyValue(Boolean verifyValue) {
        this.verifyValue = verifyValue;
    }

    /**
     * Gets the form field ft code.
     * 
     * @return the form field ft code.
     */
    public String getFtCode() {
        return ftCode;
    }

    /**
     * Gets the form field width.
     * 
     * @return the form field width.
     */
    public String getWidth() {
        return width;
    }

    /**
     * Gets the maximum length attribute.
     * 
     * @return the maximum length.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length attribute.
     * 
     * @param maxLength
     *            the maximum length attribute.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Gets the minimum length attribute.
     * 
     * @return the minimum length.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length attribute.
     * 
     * @param minLength
     *            the minimum length attribute.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Gets the form field copy down form type.
     * 
     * @return the form field copy down form type.
     */
    public String getCopyDownFormType() {
        return copyDownFormType;
    }

    /**
     * Gets the form field copy down source.
     * 
     * @return the form field copy down source.
     */
    public String getCopyDownSource() {
        return copyDownSource;
    }

    /**
     * Gets the form field list key.
     * 
     * @return the form field list key.
     */
    public String getListKey() {
        return listKey;
    }

    /**
     * Sets the form field list key.
     * 
     * @param listKey
     *            the form field list key.
     */
    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    /**
     * Gets the form field "is copy down source for other forms" value.
     * 
     * @return the "is copy down source for other forms" value.
     */
    public boolean isCopyDownSourceForOtherForms() {
        return ((isCopyDownSourceForOtherForms != null) ? isCopyDownSourceForOtherForms : Boolean.FALSE);
    }

    /**
     * Gets the form field hiearchy key (connected list).
     * 
     * @return the form field hiearchy key (connected list).
     */
    public int getHierKey() {
        return hierKey;
    }

    /**
     * Gets the form field hiearchy level.
     * 
     * @return the form field hiearchy level.
     */
    public int getHierLevel() {
        return hierLevel;
    }

    /**
     * Gets the parent field id (connected list).
     * 
     * @return the parent field id (connected list).
     */
    public String getParFieldId() {
        return parFieldId;
    }

    /**
     * Gets the parent form type code (connected list).
     * 
     * @return the parent form type code (connected list).
     */
    public String getParFtCode() {
        return parFtCode;
    }

    /**
     * Gets the form field parent hiearchy level (connected list).
     * 
     * @return the form field parent hiearchy level (connected list).
     */
    public int getParHierLevel() {
        return parHierLevel;
    }

    /**
     * Gets the form field parent list item key (connected list).
     * 
     * @return the form field parent list item key.
     */
    public String getParLiKey() {
        return parLiKey;
    }

    /**
     * Sets the form field parent list item key.
     * 
     * @param parLiKey
     *            the form field parent list item key.
     */
    public void setParLiKey(String parLiKey) {
        this.parLiKey = parLiKey;
    }

    /**
     * Gets the item copy down action value.
     * 
     * @return the item copy down action.
     */
    public String getItemCopyDownAction() {
        return itemCopyDownAction;
    }

    /**
     * Sets the item copy down action value.
     * 
     * @param itemCopyDownAction
     *            the item copy down action.
     */
    public void setItemCopyDownAction(String itemCopyDownAction) {
        this.itemCopyDownAction = itemCopyDownAction;
    }

    /**
     * Sets the static list of items to use for this field.
     * 
     * @param items
     *            an array of SpinnerItem that holds the possible values
     */
    public void setStaticList(SpinnerItem[] items) {
        staticList = items;
    }

    /**
     * Return the list of static SpinnerItems used for this field. This will only be called for a list field that isn't a
     * connected list or boolean. See ViewUtil.buildFormFieldView().
     * 
     * @return The array if set, otherwise null.
     */
    public SpinnerItem[] getStaticList() {
        return staticList;
    }

    public ArrayList<ListItem> getSearchableStaticList() {
        return searchableStaticList;
    }

    /**
     * Gets whether or not this form field contains a timestamp value.
     * 
     * @return whether or not this form field contains a timestamp value.
     */
    public boolean isTimestampField() {
        return (getDataType() != null && getDataType() == DataType.TIMESTAMP);
    }

    /**
     * get failure message
     * */
    public String getFailureMsg() {
        return failureMsg;
    }

    /**
     * set failure msg
     * */
    public void setFailureMsg(String failureMsg) {
        this.failureMsg = failureMsg;
    }

    /**
     * get valid regular expression
     * */
    public String getValidExp() {
        return validExp;
    }

    /**
     * set valid regular expression
     * */
    public void setValidExp(String validExp) {
        this.validExp = validExp;
    }

    /**
     * Will format the value stored in this form field with the passed in date formatter.
     * 
     * @param sdf
     *            the data format used to format the timestamp value.
     * @return the formatted timestamp value.
     */
    public String formatTimestampValue(DateFormat sdf) {
        String retVal = value;
        if (retVal != null && sdf != null) {
            Calendar cal = Parse.parseXMLTimestamp(retVal);
            if (cal != null) {
                retVal = Format.safeFormatCalendar(sdf, cal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".formatTimestampValue: null calendar object.");
            }
        }

        return retVal;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense report form field information.
     * 
     * @author AndrewK
     */
    public static class ExpenseReportFormFieldSAXHandler extends DefaultHandler implements Serializable {

        private static final long serialVersionUID = 649181669926835695L;

        private static final String CLS_TAG = ExpenseReportFormField.CLS_TAG + "."
                + ExpenseReportFormFieldSAXHandler.class.getSimpleName();

        // Contains a default timestamp value passed down from the server.
        private static final String DEFAULT_TIMESTAMP_VALUE = "0001-01-01T00:00:00";

        private static final String FORM_FIELD = "FormField";

        private static final String ID = "Id";

        private static final String LABEL = "Label";

        private static final String VALUE = "Value";

        private static final String DEFAULT_VALUE = "DefaultValue";

        private static final String ACCESS = "Access";

        private static final String CONTROL_TYPE = "CtrlType";

        private static final String DATA_TYPE = "DataType";

        private static final String FT_CODE = "FtCode";

        private static final String REQUIRED = "Required";

        private static final String WIDTH = "Width";

        private static final String MAX_LENGTH = "MaxLength";

        private static final String MIN_LENGTH = "MinLength";

        private static final String LI_KEY = "LiKey";

        private static final String COPY_DOWN_FORM_TYPE = "CopyDownFormType";

        private static final String COPY_DOWN_SOURCE = "CopyDownSource";

        private static final String ITEM_COPY_DOWN_ACTION = "ItemCopyDownAction";

        private static final String CUSTOM = "Custom";

        private static final String LI_CODE = "LiCode";

        private static final String LIST_KEY = "ListKey";

        private static final String IS_COPY_DOWN_SOURCE_FOR_OTHER_FORMS = "IsCopyDownSourceForOtherForms";

        private static final String HIER_KEY = "HierKey";

        private static final String HIER_LEVEL = "HierLevel";

        private static final String PARENT_FIELD_ID = "ParFieldId";

        private static final String PARENT_FT_CODE = "ParFtCode";

        private static final String PARENT_HIER_LEVEL = "ParHierLevel";

        private static final String PARENT_LI_KEY = "ParLiKey";

        private static final String FAIL_MSG = "FailureMsg";

        private static final String VALID_EXP = "ValidationExpression";

        private static final String IS_DYNAMIC_FIELD = "IsDynamicField";

        private static final String FORM_FIELD_KEY = "FfKey";

        private static final String ORIGINAL_CTRL_TYPE = "OriginalCtrlType";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>ExpenseReportFormField</code> objects that have been parsed.
         */
        private List<ExpenseReportFormField> reportFormFields = new ArrayList<ExpenseReportFormField>();

        /**
         * Contains a reference to the report form field currently being built.
         */
        private ExpenseReportFormField reportFormField;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets the list of <code>ExpenseReportFormField</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ExpenseReportFormField</code> objects.
         */
        public List<ExpenseReportFormField> getReportFormFields() {
            return reportFormFields;
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

            if (localName.equalsIgnoreCase(FORM_FIELD)) {
                reportFormField = new ExpenseReportFormField();
                chars.setLength(0);
                elementHandled = true;
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

            if (reportFormField != null) {
                if (localName.equalsIgnoreCase(ID)) {
                    reportFormField.id = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HIER_KEY)) {
                    try {
                        reportFormField.hierKey = Integer.parseInt(chars.toString().trim());
                    } catch (NumberFormatException numFrmExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: failed to parse '" + HIER_KEY + "' for field '"
                                + reportFormField.label + "'", numFrmExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HIER_LEVEL)) {
                    try {
                        reportFormField.hierLevel = Integer.parseInt(chars.toString().trim());
                    } catch (NumberFormatException numFrmExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: failed to parse '" + HIER_LEVEL + "' for field '"
                                + reportFormField.label + "'", numFrmExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_FIELD_ID)) {
                    reportFormField.parFieldId = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_FT_CODE)) {
                    reportFormField.parFtCode = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_LI_KEY)) {
                    reportFormField.parLiKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_HIER_LEVEL)) {
                    try {
                        reportFormField.parHierLevel = Integer.parseInt(chars.toString().trim());
                    } catch (NumberFormatException numFrmExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: failed to parse '" + PARENT_HIER_LEVEL
                                + "' for field '" + reportFormField.label + "'", numFrmExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(LABEL)) {
                    reportFormField.label = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VALUE)) {
                    reportFormField.value = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(DEFAULT_VALUE)) {
                    String value = reportFormField.value;
                    if (value == null || value.length() == 0) {
                        reportFormField.value = chars.toString().trim();
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ACCESS)) {
                    String accessTypeValue = chars.toString().trim();
                    try {
                        reportFormField.accessType = AccessType.fromString(accessTypeValue);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unexpected value for AccessType -- '"
                                + accessTypeValue + "'.");
                    } catch (NullPointerException npeExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null pointer exception while parsing access type",
                                npeExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CONTROL_TYPE)) {
                    String controlTypeValue = chars.toString().trim();
                    try {
                        reportFormField.controlType = ControlType.fromString(controlTypeValue);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unexpected value for ControlType -- '"
                                + controlTypeValue + "'.");
                    } catch (NullPointerException npeExc) {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".endElement: null pointer exception while parsing control type", npeExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(DATA_TYPE)) {
                    String dataTypeValue = chars.toString().trim();
                    try {
                        reportFormField.dataType = DataType.fromString(dataTypeValue);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unexpected value for DataType -- '"
                                + dataTypeValue + "'.");
                    } catch (NullPointerException npeExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null pointer exception while parsing data type",
                                npeExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FT_CODE)) {
                    reportFormField.ftCode = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(REQUIRED)) {
                    // This is for a special case where if Report ID is shown, but not filled yet (IE before report is saved), we
                    // don't set "required" flag because it's read only
                    if (reportFormField.id.equalsIgnoreCase(REPORT_ID_FIELD_ID)) {
                        reportFormField.required = false;
                    } else {
                        reportFormField.required = Parse.safeParseBoolean(chars.toString().trim());
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(WIDTH)) {
                    reportFormField.width = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(MAX_LENGTH)) {
                    try {
                        reportFormField.maxLength = Integer.parseInt(chars.toString().trim());
                    } catch (NumberFormatException numFrmExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: failed to parse '" + MAX_LENGTH + "' for field '"
                                + reportFormField.label + "'", numFrmExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(MIN_LENGTH)) {
                    try {
                        reportFormField.minLength = Integer.parseInt(chars.toString().trim());
                    } catch (NumberFormatException numFrmExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: failed to parse '" + MIN_LENGTH + "' for field '"
                                + reportFormField.label + "'", numFrmExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(LI_KEY)) {
                    reportFormField.liKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(COPY_DOWN_FORM_TYPE)) {
                    reportFormField.copyDownFormType = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(COPY_DOWN_SOURCE)) {
                    reportFormField.copyDownSource = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ITEM_COPY_DOWN_ACTION)) {
                    reportFormField.itemCopyDownAction = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CUSTOM)) {
                    reportFormField.custom = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(LI_CODE)) {
                    reportFormField.liCode = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(LIST_KEY)) {
                    reportFormField.listKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_COPY_DOWN_SOURCE_FOR_OTHER_FORMS)) {
                    reportFormField.isCopyDownSourceForOtherForms = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FAIL_MSG)) {
                    reportFormField.failureMsg = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VALID_EXP)) {
                    reportFormField.validExp = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(IS_DYNAMIC_FIELD)) {
                    reportFormField.isConditionalField = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FORM_FIELD_KEY)) {
                    reportFormField.formFieldKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ORIGINAL_CTRL_TYPE)) {
                    String controlTypeValue = chars.toString().trim();
                    try {
                        reportFormField.originalCtrlType = ControlType.fromString(controlTypeValue);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".endElement: unexpected value for originalCtrlType -- '"
                                + controlTypeValue + "'.");
                    } catch (NullPointerException npeExc) {
                        Log.d(Const.LOG_TAG,
                                CLS_TAG + ".endElement: null pointer exception while parsing control type", npeExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FORM_FIELD)) {
                    // Hack Alert:
                    // Currently the MWS is returning the Policy field as an editable field
                    // when the web does not! So, we'll check for this field id and ensure the
                    // access type is set to read-only.
                	// MOB-17496: The hack mentioned above is no longer needed now that MWS
                	//	correctly returns the FormField Access Type for Policy Key.
                    /* 
                     * if (reportFormField.getId() != null && reportFormField.getId().equalsIgnoreCase("PolKey")) {
                     *   reportFormField.accessType = ExpenseReportFormField.AccessType.RO;
                     *}
                     */
                    // Ensure that for a timestamp field, if the default value of 'DEFAULT_TIMESTAMP_VALUE' has
                    // been parsed, then set it to 'null'.
                    if (reportFormField.isTimestampField() && reportFormField.value != null
                            && reportFormField.value.equalsIgnoreCase(DEFAULT_TIMESTAMP_VALUE)) {
                        reportFormField.value = null;
                    }
                    reportFormFields.add(reportFormField);
                    elementHandled = true;
                } else if (!elementHandled && this.getClass().equals(ExpenseReportFormFieldSAXHandler.class)) {
                    // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element name '" + localName
                    // + "' and value '" + chars.toString().trim() + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current report form field!");
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

        public static void serializeToXML(StringBuilder strBldr, ExpenseReportFormField frmFld) {
            serializeToXML(strBldr, frmFld, false);
        }

        /**
         * Will serialize to XML this <code>ExpenseReportFormField</code> object.
         * 
         * @param strBldr
         *            the <code>StringBuilder</code> to hold the serialization.
         * @param frmFld
         *            the <code>ExpenseReportFormField</code> object to be serialized.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseReportFormField frmFld,
                boolean includeNameSpaceIdentifier) {
            if (strBldr != null) {
                if (frmFld != null) {
                    strBldr.append('<');
                    if (includeNameSpaceIdentifier) {
                        strBldr.append("f:");
                    }
                    strBldr.append(FORM_FIELD);
                    strBldr.append('>');

                    // Access (accessType)
                    if (frmFld.getAccessType() != null) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, ACCESS), frmFld
                                .getAccessType().getName());
                    }
                    // CopyDownFormType (copyDownFormType)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, COPY_DOWN_FORM_TYPE),
                            frmFld.copyDownFormType);
                    // CopyDownSource (copyDownSource)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, COPY_DOWN_SOURCE),
                            frmFld.copyDownSource);
                    // CtrlType(controlType)
                    if (frmFld.getControlType() != null) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, CONTROL_TYPE), frmFld
                                .getControlType().getName());
                    }
                    // Custom (custom)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, CUSTOM), frmFld.custom);
                    // DataType(dataType)
                    if (frmFld.getDataType() != null) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, DATA_TYPE), frmFld
                                .getDataType().getName());
                    }
                    // FtCode (FtCode)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, FT_CODE), frmFld.ftCode);
                    // HierKey (hierKey)
                    if (frmFld.getHierKey() != -1) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, HIER_KEY),
                                Integer.toString(frmFld.hierKey));
                    }
                    // HierLevel (hierLevel)
                    if (frmFld.getHierLevel() != -1) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, HIER_LEVEL),
                                Integer.toString(frmFld.hierLevel));
                    }
                    // Id (id)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, ID), frmFld.id);
                    // IsCopyDownSourceForOtherForms (isCopyDownSourceForOtherForms)
                    ViewUtil.addXmlElementYN(strBldr,
                            xmlNodeName(includeNameSpaceIdentifier, IS_COPY_DOWN_SOURCE_FOR_OTHER_FORMS),
                            frmFld.isCopyDownSourceForOtherForms);
                    // Label (label)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, LABEL), frmFld.label);
                    // LiCode (liCode)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, LI_CODE), frmFld.liCode);
                    // LiKey (liKey)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, LI_KEY), frmFld.liKey);
                    // ListKey (listKey)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, LIST_KEY), frmFld.listKey);
                    // MaxLength (maxLength)
                    if (frmFld.getMaxLength() != -1) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, MAX_LENGTH),
                                Integer.toString(frmFld.maxLength));
                    }
                    // MaxLength (maxLength)
                    if (frmFld.getMinLength() != -1) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, MIN_LENGTH),
                                Integer.toString(frmFld.minLength));
                    }
                    // ParFieldId (parFieldId)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, PARENT_FIELD_ID),
                            frmFld.parFieldId);
                    // ParFtCode (parFtCode)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, PARENT_FT_CODE),
                            frmFld.parFtCode);
                    // ParHierLevel (parHierLevel)
                    if (frmFld.getParHierLevel() != -1) {
                        ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, PARENT_HIER_LEVEL),
                                Integer.toString(frmFld.parHierLevel));
                    }
                    // ParLiKey (parLiKey)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, PARENT_LI_KEY),
                            frmFld.parLiKey);
                    // Required (Required)
                    ViewUtil.addXmlElementYN(strBldr, xmlNodeName(includeNameSpaceIdentifier, REQUIRED),
                            frmFld.required);
                    // Value (value)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, VALUE), frmFld.value);
                    // Width (width)
                    ViewUtil.addXmlElement(strBldr, xmlNodeName(includeNameSpaceIdentifier, WIDTH), frmFld.width);

                    strBldr.append("</");
                    if (includeNameSpaceIdentifier) {
                        strBldr.append("f:");
                    }
                    strBldr.append(FORM_FIELD);
                    strBldr.append('>');
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: frmFld is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }

        private static String xmlNodeName(boolean includeNameSpaceIdentifier, String elementName) {
            return (includeNameSpaceIdentifier) ? ("f:" + elementName) : elementName;
        }

    }

    public FormFieldView getExpensePickListFormFieldView(IFormFieldViewListener listener) {
        return new ExpenseTypeFormFieldView(this, listener);
    }

    public FormFieldView getSearchListFormFieldView(IFormFieldViewListener listener) {
        return new SearchListFormFieldView(this, listener);
    }

}
