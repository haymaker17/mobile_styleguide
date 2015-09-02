package com.concur.mobile.platform.common.formfield;

import com.google.gson.annotations.SerializedName;

/**
 * A Field Bean of a Form object (FormField) deserialized through Gson Based upon ConcurConnect V3.1
 *
 * @author OlivierB
 */
public class ConnectFormField /*implements Comparable<ConnectFormField>*/ {

    /**
     * Models the form field access type.
     */
    public enum AccessType {
        RW,
        RO,
        HD;
    }

    /**
     * Models the form field data type.
     */
    public enum DataType {
        BOOLEAN,
        STRING,
        NUMBER,
        TIMESTAMP;
    }

    /**
     * Field NAME
     */
    public enum NameType {
        STARTDATE("StartDate"),
        ENDDATE("EndDate"),
        PURPOSE("Purpose"),
        COMMENT("Comment"),
        STARDATE("StartDate"),
        ENDATE("EndDate"),
        STARTLOCATION("StartLocation");

        private String value;

        NameType(String value_) {
            value = value_;
        }

        public String getValue(){
            return value;
        }
    }

    /**
     * Models the form field display type.
     */
    public enum DisplayType {
        SEARCHABLE_LIST,
        PICKLIST,
        MULTIPLE_PICKLIST,
        LOCATION,
        DATETIME,
        DATE,
        TIME,
        CHECKBOX,
        TEXT,
        TEXTAREA,
        INTEGER,
        DOUBLE,
        AMOUNT;
    }

    @SerializedName("Access") private AccessType accessType;
    @SerializedName("DataType") private DataType dataType;
    @SerializedName("DisplayType") private DisplayType displayType;
    @SerializedName("IsRequired") private Boolean required;
    @SerializedName("Label") private String label;
    @SerializedName("MaxLength") private Integer maxLength;
    @SerializedName("Name") private String Name;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType at) {
        this.accessType = at;
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /*@Override public int compareTo(ConnectFormField another) {
        return compare(this, another);
    }

    public int compare(ConnectFormField ff1, ConnectFormField ff2) {
        return ff1.getSequence() < ff2.getSequence() ? -1 : ff1.getSequence() == ff2.getSequence() ? 0 : 1;
    }*/
}
