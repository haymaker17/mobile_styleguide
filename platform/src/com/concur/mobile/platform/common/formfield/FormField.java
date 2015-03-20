package com.concur.mobile.platform.common.formfield;

import java.util.ArrayList;

import com.concur.mobile.platform.common.IListFieldItem;
import com.concur.mobile.platform.common.SpinnerItem;

/**
 * 
 * @author RatanK
 * 
 */
public class FormField implements IFormField {

    private static final String CLS_TAG = FormField.class.getSimpleName();

    protected String id;

    protected String label;

    protected String value;

    protected AccessType accessType;

    protected ControlType controlType;

    protected DataType dataType;

    protected InputType inputType;

    // protected IFormFieldType formFieldType;

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
    protected ArrayList<IListFieldItem> searchableStaticList;

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

    protected String itemCopyDownFormType;

    protected String itemCopyDownsource;

    public FormField() {
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

    public FormField(String id, String label, String value, AccessType accessType, ControlType controlType,
            DataType dataType, boolean required) {
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
    public void copy(FormField dst) {
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
        dst.searchableStaticList = (ArrayList<IListFieldItem>) searchableStaticList.clone();
        dst.failureMsg = failureMsg;
        dst.validExp = validExp;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the form field access type, i.e., RW, R, W.
     * 
     * @return the form field access type.
     */
    @Override
    public AccessType getAccessType() {
        return accessType;
    }

    @Override
    public void setAccessType(AccessType at) {
        accessType = at;
    }

    @Override
    public ControlType getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean isRequired() {
        return ((required != null) ? required : Boolean.FALSE);
    }

    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public int getMinLength() {
        return minLength;
    }

    @Override
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public InputType getInputType() {
        return inputType;
    }

    @Override
    public void setInputType(InputType it) {
        inputType = it;
    }

    @Override
    public String getLiKey() {
        return liKey;
    }

    @Override
    public void setLiKey(String liKey) {
        this.liKey = liKey;
    }

    @Override
    public String getLiCode() {
        return liCode;
    }

    @Override
    public void setLiCode(String liCode) {
        this.liCode = liCode;
    }

    @Override
    public String getListKey() {
        return listKey;
    }

    @Override
    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    @Override
    public int getHierKey() {
        return hierKey;
    }

    @Override
    public int getHierLevel() {
        return hierLevel;
    }

    @Override
    public String getParFieldId() {
        return parFieldId;
    }

    @Override
    public String getParFtCode() {
        return parFtCode;
    }

    @Override
    public int getParHierLevel() {
        return parHierLevel;
    }

    @Override
    public String getParLiKey() {
        return parLiKey;
    }

    @Override
    public void setParLiKey(String parLiKey) {
        this.parLiKey = parLiKey;
    }

    @Override
    public String getCopyDownFormType() {
        return itemCopyDownFormType;
    }

    @Override
    public String getCopyDownSource() {
        return itemCopyDownsource;
    }

    @Override
    public boolean isVerifyValue() {
        return verifyValue;
    }

    @Override
    public void setVerifyValue(Boolean verifyValue) {
        this.verifyValue = verifyValue;
    }

    @Override
    public String getFtCode() {
        return ftCode;
    }

    @Override
    public void setStaticList(SpinnerItem[] items) {
        staticList = items;
    }

    @Override
    public SpinnerItem[] getStaticList() {
        return staticList;
    }

    @Override
    public ArrayList<IListFieldItem> getSearchableStaticList() {
        return searchableStaticList;
    }

    @Override
    public String getFailureMsg() {
        return failureMsg;
    }

    @Override
    public void setFailureMsg(String failureMsg) {
        this.failureMsg = failureMsg;
    }

    @Override
    public String getValidExp() {
        return validExp;
    }

    @Override
    public void setValidExp(String validExp) {
        this.validExp = validExp;
    }

    @Override
    public void setLargeValueCount(boolean hasLargeValueCount) {
        largeValueCount = hasLargeValueCount;
    }

    @Override
    public boolean hasLargeValueCount() {
        return (largeValueCount == null ? false : largeValueCount);
    }

}
