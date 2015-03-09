package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class Agency {

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
