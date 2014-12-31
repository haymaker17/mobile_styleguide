package com.concur.mobile.platform.common.formfield;

import com.concur.mobile.platform.common.formfield.IFormField.AccessType;
import com.concur.mobile.platform.common.formfield.IFormField.ControlType;
import com.concur.mobile.platform.common.formfield.IFormField.DataType;
import com.google.gson.annotations.SerializedName;

/**
 * A Field Bean of a Form object (FormField) deserialized through Gson Based upon ConcurConnect V3.
 * 
 * @author OlivierB
 */
public class ConnectFormField implements Comparable<ConnectFormField>  {

    // --- IFormFields fields
    @SerializedName("Access")
    private AccessType accessType;
    @SerializedName("Cols")
    private String cols;
    @SerializedName("ControlType")
    private ControlType controlType;
    @SerializedName("CopyDownFormType")
    private String copyDownFormType;
    @SerializedName("CopyDownSource")
    private String copyDownSource;
    @SerializedName("DataType")
    private DataType dataType;
    @SerializedName("DefaultValue")
    private String defaultValue;
    @SerializedName("DefaultValueListItemCode")
    private String defaultValueListItemCode;
    @SerializedName("FailureMessage")
    private String failureMsg;
    @SerializedName("FormTypeCode")
    private String ftCode;
    @SerializedName("HierKey")
    private Integer hierKey;
    @SerializedName("HierLevel")
    private Integer hierLevel;
    @SerializedName("ID")
    private String id;
    @SerializedName("IsCopyDownSourceForOtherForms")
    private Boolean copyDownSourceForOtherForms;
    @SerializedName("IsCustom")
    private Boolean custom;
    @SerializedName("IsDynamic")
    private Boolean dynamic;
    @SerializedName("IsRequired")
    private Boolean required;
    @SerializedName("ItemCopyDownAction")
    private String itemCopyDownAction;
    @SerializedName("Label")
    private String label;
    @SerializedName("ListID")
    private String listId;
    @SerializedName("ListName")
    private String listName;
    @SerializedName("MaxLength")
    private Integer maxLength;
    @SerializedName("Name")
    private String Name;
    @SerializedName("ParentFieldName")
    private String parentFieldName;
    @SerializedName("ParentFormTypeCode")
    private String parFtCode;
    @SerializedName("ParentHierLevel")
    private String parentHierLevel;
    @SerializedName("Sequence")
    private Integer Sequence;
    @SerializedName("URI")
    private String URI;
    @SerializedName("ValidationExpression")
    private String validExp;
    @SerializedName("Width")
    private Integer width;
    @SerializedName("HasLineSeparator")
    private Boolean isLineSeparator;

    /*
     * --- enums -- retrieved from IFormFields => we can't reuse IFormFields because of inheritances and dependencies levels
     * regarding it's use in Parse class.
     */

    // private String value;

    public Boolean isLineSeparator() {
		return isLineSeparator;
	}

	public void setIsLineSeparator(Boolean isLineSeparator) {
		this.isLineSeparator = isLineSeparator;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /*
     * public String getValue() { return value; }
     * 
     * 
     * public void setValue(String value) { this.value = value; }
     */

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType at) {
        this.accessType = at;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getHierKey() {
        return hierKey;
    }

    public int getHierLevel() {
        return hierLevel;
    }

    public String getParFieldId() {
        return parentFieldName;
    }

    public String getParFtCode() {
        return parFtCode;
    }

    public String getCopyDownFormType() {
        return copyDownFormType;
    }

    public String getCopyDownSource() {
        return copyDownSource;
    }

    public String getFtCode() {
        return ftCode;
    }

    public String getFailureMsg() {
        return failureMsg;
    }

    public void setFailureMsg(String failureMsg) {
        this.failureMsg = failureMsg;
    }

    public String getValidExp() {
        return validExp;
    }

    public void setValidExp(String validExp) {
        this.validExp = validExp;
    }

    public String getCols() {
        return cols;
    }

    public void setCols(String cols) {
        this.cols = cols;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValueListItemCode() {
        return defaultValueListItemCode;
    }

    public void setDefaultValueListItemCode(String defaultValueListItemCode) {
        this.defaultValueListItemCode = defaultValueListItemCode;
    }

    public boolean isCopyDownSourceForOtherForms() {
        return copyDownSourceForOtherForms;
    }

    public void setCopyDownSourceForOtherForms(boolean copyDownSourceForOtherForms) {
        this.copyDownSourceForOtherForms = copyDownSourceForOtherForms;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getItemCopyDownAction() {
        return itemCopyDownAction;
    }

    public void setItemCopyDownAction(String itemCopyDownAction) {
        this.itemCopyDownAction = itemCopyDownAction;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getParentHierLevel() {
        return parentHierLevel;
    }

    public void setParentHierLevel(String parentHierLevel) {
        this.parentHierLevel = parentHierLevel;
    }

    public Integer getSequence() {
        return Sequence;
    }

    public void setSequence(Integer sequence) {
        Sequence = sequence;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String uRI) {
        URI = uRI;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public void setCopyDownFormType(String copyDownFormType) {
        this.copyDownFormType = copyDownFormType;
    }

    public void setCopyDownSource(String copyDownSource) {
        this.copyDownSource = copyDownSource;
    }

    public void setHierKey(Integer hierKey) {
        this.hierKey = hierKey;
    }

    public void setHierLevel(Integer hierLevel) {
        this.hierLevel = hierLevel;
    }

    public void setParFieldId(String parFieldId) {
        this.parentFieldName = parFieldId;
    }

    public void setParFtCode(String parFtCode) {
        this.parFtCode = parFtCode;
    }

    public void setFtCode(String ftCode) {
        this.ftCode = ftCode;
    }

	@Override
	public int compareTo(ConnectFormField another) {
		return compare(this, another);
	}	
	
	public int compare(ConnectFormField ff1, ConnectFormField ff2) {
		return ff1.getSequence() < ff2.getSequence() ? -1 : ff1.getSequence() == ff2.getSequence() ? 0 : 1;
	}
}
