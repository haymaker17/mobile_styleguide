package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class RequestGroupConfiguration {

    @SerializedName("ID")
    private String id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Agencies")
    private List<Agency> agencies;
    @SerializedName("Policies")
    private List<Policy> policies;
    @SerializedName("RequestTypes")
    private List<RequestType> requestTypes;
    @SerializedName("URI")
    private String uri;

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

    public List<Agency> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Agency> agencies) {
        this.agencies = agencies;
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

    public List<RequestType> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<RequestType> requestTypes) {
        this.requestTypes = requestTypes;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
