package com.concur.mobile.platform.request.permission;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by OlivierB on 21/05/2015.
 */
public class Link {

    @SerializedName("Links")
    private List<UserPermission> permissions;

    public List<UserPermission> getPermissions() {
        return permissions;
    }
}
