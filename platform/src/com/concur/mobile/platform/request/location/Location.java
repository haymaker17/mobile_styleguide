package com.concur.mobile.platform.request.location;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 17/02/2015.
 */
public class Location {

    public enum LocationType {
        AIRPORT,
        CITY
    }

    @SerializedName("ID")
    private String id;
    @SerializedName("Name")
    private String name;

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
}
