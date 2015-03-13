package com.concur.mobile.platform.common.formfield;

import com.concur.mobile.platform.common.IListFieldItem;
import com.concur.mobile.platform.common.SpinnerItem;

import java.util.ArrayList;

/**
 * Common interface extracted from {@link com.concur.mobile.core.expense.report.data.ExpenseReportFormField}
 *
 * @author yiwenw
 */
public interface IFormField {

    public interface EnumField<T> {

        String getName();
    }

    /**
     * Models the form field data type.
     */
    public static enum DataType implements EnumField<DataType> {
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
         * @param name the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.DataType</code>.
         * @throws IllegalArgumentException if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException     if <code>name</code> is <code>null</code>.
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
    }

    /**
     * Models the form field access type.
     */
    public static enum AccessType implements EnumField<DataType> {
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
         * @param name the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.AccessType</code>.
         * @throws IllegalArgumentException if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException     if <code>name</code> is <code>null</code>.
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
     * Models the form field control type.
     */
    public static enum ControlType implements EnumField<DataType> {
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
        TEXT_AREA("textarea"),
        /**
         * Specific to time fields
         */
        TIME("time");

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
         * @param name the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.ControlType</code>.
         * @throws IllegalArgumentException if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException     if <code>name</code> is <code>null</code>.
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
    public static enum InputType implements EnumField<DataType> {
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
         * @param name the enumeration value name.
         * @return an instance of <code>ExpenseReportFormField.InputType</code>.
         * @throws IllegalArgumentException if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException     if <code>name</code> is <code>null</code>.
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

    /**
     * Gets the form field id.
     *
     * @return the form field id.
     */
    public String getId();

    /**
     * Sets the form field id.
     *
     * @param id the form field id.
     */
    public void setId(String id);

    /**
     * Gets the form field label.
     *
     * @return the form field label.
     */
    public String getLabel();

    /**
     * Sets the form field label.
     */
    public void setLabel(String label);

    /**
     * Gets the form field value.
     *
     * @return the form field value.
     */
    public String getValue();

    /**
     * Sets the form field value.
     *
     * @param value the form field value.
     */
    public void setValue(String value);

    /**
     * Gets the form field access type, i.e., RW, R, W.
     *
     * @return the form field access type.
     */
    public AccessType getAccessType();

    /**
     * Sets the form field access type, i.e., RW, R, W.
     *
     * @param at the form field access type
     */
    public void setAccessType(AccessType at);

    /**
     * Gets the form field control type.
     *
     * @return the form field control type.
     */
    public ControlType getControlType();

    /**
     * Sets the form field control type.
     *
     * @param controlType the form field control type.
     */
    public void setControlType(ControlType controlType);

    /**
     * Gets the form field data type.
     *
     * @return the form field data type.
     */
    public DataType getDataType();

    /**
     * Sets the form field data type.
     *
     * @param dataType the form field data type.
     */
    public void setDataType(DataType dataType);

    /**
     * Gets whether the form field is required.
     *
     * @return whether the form field is required.
     */
    public boolean isRequired();

    /**
     * Sets whether the form field is required.
     *
     * @param required whether the form field is required.
     */
    public void setRequired(Boolean required);

    /**
     * Gets the minimum length attribute.
     *
     * @return the minimum length.
     */
    public int getMinLength();

    /**
     * Sets the minimum length attribute.
     *
     * @param minLength the minimum length attribute.
     */
    public void setMinLength(int minLength);

    /**
     * Gets the maximum length attribute.
     *
     * @return the maximum length.
     */
    public int getMaxLength();

    /**
     * Sets the maximum length attribute.
     *
     * @param maxLength the maximum length attribute.
     */
    public void setMaxLength(int maxLength);

    /**
     * Gets the form field input type.
     *
     * @return the form field input type.
     */
    public InputType getInputType();

    /**
     * Sets the form field input type.
     *
     * @param it the form field input type.
     */
    public void setInputType(InputType it);

    /**
     * Gets the form field li key.
     *
     * @return the form field li key.
     */
    public String getLiKey();

    /**
     * Sets the form field li key.
     *
     * @param liKey the form field li key.
     */
    public void setLiKey(String liKey);

    /**
     * Get the form field li code.
     *
     * @return the form field li code.
     */
    public String getLiCode();

    /**
     * Sets the form field li code.
     *
     * @param liCode the form field li code.
     */
    public void setLiCode(String liCode);

    /**
     * Gets the form field list key.
     *
     * @return the form field list key.
     */
    public String getListKey();

    /**
     * Sets the form field list key.
     *
     * @param listKey the form field list key.
     */
    public void setListKey(String listKey);

    /**
     * Gets the form field hiearchy key (connected list).
     *
     * @return the form field hiearchy key (connected list).
     */
    public int getHierKey();

    /**
     * Gets the form field hiearchy level.
     *
     * @return the form field hiearchy level.
     */
    public int getHierLevel();

    /**
     * Gets the parent field id (connected list).
     *
     * @return the parent field id (connected list).
     */
    public String getParFieldId();

    /**
     * Gets the parent form type code (connected list).
     *
     * @return the parent form type code (connected list).
     */
    public String getParFtCode();

    /**
     * Gets the form field parent hiearchy level (connected list).
     *
     * @return the form field parent hiearchy level (connected list).
     */
    public int getParHierLevel();

    /**
     * Gets the form field parent list item key (connected list).
     *
     * @return the form field parent list item key.
     */
    public String getParLiKey();

    /**
     * Sets the form field parent list item key.
     *
     * @param parLiKey the form field parent list item key.
     */
    public void setParLiKey(String parLiKey);

    /**
     * Gets the form field copy down form type.
     *
     * @return the form field copy down form type.
     */
    public String getCopyDownFormType();

    /**
     * Gets the form field copy down source.
     *
     * @return the form field copy down source.
     */
    public String getCopyDownSource();

    /**
     * Gets whether or not to verify this field's value when saving.
     *
     * @return <code>true</code> to verify the value when saving, otherwise <code>false</code> to skip verification.
     */
    public boolean isVerifyValue();

    /**
     * Sets whether or not to verify if this value is valid when saving the form.
     *
     * @param verifyValue <code>true</code> to verify the value when saving, otherwise set to <code>false</code> to skip verification.
     */
    public void setVerifyValue(Boolean verifyValue);

    /**
     * Gets the form field ft code.
     *
     * @return the form field ft code.
     */
    public String getFtCode();

    /**
     * Sets the static list of items to use for this field.
     *
     * @param items an array of SpinnerItem that holds the possible values
     */
    public void setStaticList(SpinnerItem[] items);

    /**
     * Return the list of static SpinnerItems used for this field. This will only be called for a list field that isn't a
     * connected list or boolean. See ViewUtil.buildFormFieldView().
     *
     * @return The array if set, otherwise null.
     */
    public SpinnerItem[] getStaticList();

    public ArrayList<IListFieldItem> getSearchableStaticList();

    /**
     * get failure message
     */
    public String getFailureMsg();

    /**
     * set failure msg
     */
    public void setFailureMsg(String failureMsg);

    /**
     * get valid regular expression
     */
    public String getValidExp();

    /**
     * set valid regular expression
     */
    public void setValidExp(String validExp);

    /**
     * Specify whether this list field has a large set of values
     *
     * @param hasLargeValueCount
     */
    public void setLargeValueCount(boolean hasLargeValueCount);

    /**
     * whether this list field has a large set of values
     *
     * @return
     */
    public boolean hasLargeValueCount();

    /**
     * Need to expose the Object.clone() method
     *
     * @return
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;

}
