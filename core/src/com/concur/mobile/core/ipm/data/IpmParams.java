package com.concur.mobile.core.ipm.data;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * class to represent IPM extra params in json
 * 
 * @author tejoa
 * 
 */
public class IpmParams implements Serializable {

    /**
     * auto generated
     */
    private static final long serialVersionUID = -4541534894459595339L;
    // includes CTE product type to whom ads should be displayed
    @SerializedName("CteProduct")
    public String[] cteProduct;

    @SerializedName("CteUnused")
    public String[] cteUnused;

    @SerializedName("IpmRole")
    public String[] ipmRole;

    @SerializedName("CteCfg")
    public String[] cteCfg;

    @SerializedName("Lang")
    public String lang;

    /**
     * @return the cteProduct
     */
    public String[] getCteProduct() {
        return cteProduct;
    }

    /**
     * @param cteProduct
     *            the cteProduct to set
     */
    public void setCteProduct(String[] cteProduct) {
        this.cteProduct = cteProduct;
    }

    /**
     * @return the cteUnused
     */
    public String[] getCteUnused() {
        return cteUnused;
    }

    /**
     * @param cteUnused
     *            the cteUnused to set
     */
    public void setCteUnused(String[] cteUnused) {
        this.cteUnused = cteUnused;
    }

    /**
     * @return the ipmRole
     */
    public String[] getIpmRole() {
        return ipmRole;
    }

    /**
     * @param ipmRole
     *            the ipmRole to set
     */
    public void setIpmRole(String[] ipmRole) {
        this.ipmRole = ipmRole;
    }

    /**
     * @return the cteCfg
     */
    public String[] getCteCfg() {
        return cteCfg;
    }

    /**
     * @param cteCfg
     *            the cteCfg to set
     */
    public void setCteCfg(String[] cteCfg) {
        this.cteCfg = cteCfg;
    }

    /**
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param lang
     *            the lang to set
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    public String toString(String[] params) {
        String value = null;

        for (String param : params) {
            if (value == null)
                value = param;
            else {
                value = value + "," + param;
            }
        }
        return value;

    }
}
