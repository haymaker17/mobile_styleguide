package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class RequestGroupConfigurationsContainer {

    @SerializedName("Items")
    private List<RequestGroupConfiguration> configurationlist;

    public RequestGroupConfigurationsContainer() {
        // No-args constructor.
    }

    public List<RequestGroupConfiguration> getGroupConfigurations(){
        return configurationlist;
    }

    public void add(RequestGroupConfiguration groupConfiguration) {
        if (configurationlist == null)
            configurationlist = new ArrayList<RequestGroupConfiguration>();
        configurationlist.add(groupConfiguration);
    }
}
