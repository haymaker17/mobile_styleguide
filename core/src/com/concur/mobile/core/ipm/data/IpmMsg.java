package com.concur.mobile.core.ipm.data;

/**
 * Class to represent IPM data types
 * 
 * @author tejoa
 * 
 */
public class IpmMsg {

    public String adUnitId;
    public String adType;
    public String target;
    public String adKey;
    public IpmParams params;

    /**
     * @return the adUnitId
     */
    public String getAdUnitId() {
        return adUnitId;
    }

    /**
     * @param adUnitId
     *            the adUnitId to set
     */
    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    /**
     * @return the adType
     */
    public String getAdType() {
        return adType;
    }

    /**
     * @param adType
     *            the adType to set
     */
    public void setAdType(String adType) {
        this.adType = adType;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the adKey
     */
    public String getAdKey() {
        return adKey;
    }

    /**
     * @param adKey
     *            the adKey to set
     */
    public void setAdKey(String adKey) {
        this.adKey = adKey;
    }

    /**
     * @return the params
     */
    public IpmParams getParams() {
        return params;
    }

    /**
     * @param params
     *            the params to set
     */
    public void setParams(IpmParams params) {
        this.params = params;
    }

}
