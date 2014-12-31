/**
 * 
 */
package com.concur.mobile.platform.authentication;

/**
 * Provides an interface for describing site setting information returned via a login result.
 * 
 * @author andrewk
 */
public interface SiteSettingInfo {

    /**
     * Gets the name.
     * 
     * @return the name.
     */
    public String getName();

    /**
     * Gets the type.
     * 
     * @return the type.
     */
    public String getType();

    /**
     * Gets the value.
     * 
     * @return the value.
     */
    public String getValue();

    /**
     * Gets the user id.
     * 
     * @return the user id.
     */
    public String getUserId();

}
