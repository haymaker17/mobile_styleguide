package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class Policy {

    @SerializedName("ID")
    private String id;
    @SerializedName("Name")
    private String name;
    @SerializedName("IsDefault")
    private Boolean isDefault;
    @SerializedName("NoCreation")
    private Boolean noCreation;
    @SerializedName("SegmentTypes")
    private List<SegmentType> segmentTypes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getNoCreation() {
        return noCreation;
    }

    public void setNoCreation(Boolean noCreation) {
        this.noCreation = noCreation;
    }

    public List<SegmentType> getSegmentTypes() {
        return segmentTypes;
    }

    public void setSegmentTypes(List<SegmentType> segmentTypes) {
        this.segmentTypes = segmentTypes;
    }
}
