package com.concur.mobile.platform.request.groupConfiguration;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class RequestGroupConfiguration {

    @SerializedName("ID") private String id;
    @SerializedName("Name") private String name;
    @SerializedName("DefaultPolicyID") private String defaultPolicyId;
    @SerializedName("AgencyOffices") private List<Agency> agencies;
    @SerializedName("Policies") private List<Policy> policies;
    @SerializedName("RequestTypes") private List<RequestType> requestTypes;
    @SerializedName("URI") private String uri;

    private String defaultFormId = null;

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

    public String getDefaultPolicyId() {
        return defaultPolicyId;
    }

    public void setDefaultPolicyId(String defaultPolicyId) {
        this.defaultPolicyId = defaultPolicyId;
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

    public String getFormId() {
        if (defaultFormId == null && defaultPolicyId != null) {
            for (Policy p : policies) {
                // we can't use isDefault because this 'default' property doesn't have the same meaning as the other one...
                if (p.getId().equals(defaultPolicyId)) {
                    defaultFormId = p.getHeaderFormId();
                    break;
                }
            }
        }
        return defaultFormId;
    }

    public String extractSegmentFormId(final String policyId, final SegmentType.RequestSegmentType segmentType) {
        for (Policy p : getPolicies()) {
            if (p.getId().equals(policyId)) {
                for (SegmentType st : p.getSegmentTypes()) {
                    if (st.getIconCode().equals(segmentType.getCode())) {
                        return st.getSegmentFormID();
                    }
                }
            }
        }
        return null;
    }

    public String extractSegmentTypeId(final String policyId, final SegmentType.RequestSegmentType segmentType) {
        for (Policy p : getPolicies()) {
            if (p.getId().equals(policyId)) {
                for (SegmentType st : p.getSegmentTypes()) {
                    if (st.getIconCode().equals(segmentType.getCode())) {
                        return st.getId();
                    }
                }
            }
        }
        return null;
    }
}