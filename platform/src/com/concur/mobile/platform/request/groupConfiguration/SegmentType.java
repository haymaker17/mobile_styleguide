package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class SegmentType {

    @SerializedName("ID")
    private String id;
    @SerializedName("Name")
    private String name;
    @SerializedName("DisplayOrder")
    private Integer displayOrder;
    @SerializedName("IconCode")
    private String iconCode;
    @SerializedName("SegmentFormID")
    private String segmentFormID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public String getSegmentFormID() {
        return segmentFormID;
    }

    public void setSegmentFormID(String segmentFormID) {
        this.segmentFormID = segmentFormID;
    }
}
