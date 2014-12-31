/**
 * 
 */
package com.concur.mobile.platform.authentication;

import java.util.Calendar;

/**
 * Provides an interface for describing User information returned via a login result.
 * 
 * @author andrewk
 */
public interface UserInfo {

    /**
     * Gets the entity type.
     * 
     * @return the entity type.
     */
    public String getEntityType();

    /**
     * Gets the expense country code.
     * 
     * @return the expense country code.
     */
    public String getExpenseCountryCode();

    /**
     * Gets whether custom fields are required.
     * 
     * @return whether custom fields are required.
     */
    public Boolean hasRequiredCustomFields();

    /**
     * Gets the pin expiration date.
     * 
     * @return the pin expiration date.
     */
    public Calendar getPinExpirationDate();

    /**
     * Gets the product offering.
     * 
     * @return the product offering.
     */
    public String getProductOffering();

    /**
     * Gets the profile status.
     * 
     * @return the profile status.
     */
    public Integer getProfileStatus();

    /**
     * Gets the roles mobile.
     * 
     * @return the roles mobile.
     */
    public String getRolesMobile();

    /**
     * Gets the contact company name.
     * 
     * @return the contact company name.
     */
    public String getContactCompanyName();

    /**
     * Gets the contact email.
     * 
     * @return the contact email.
     */
    public String getContactEmail();

    /**
     * Gets the contact first name.
     * 
     * @return the contact first name.
     */
    public String getContactFirstName();

    /**
     * Gets the contact last name.
     * 
     * @return the contact last name.
     */
    public String getContactLastName();

    /**
     * Gets the contact middle initial.
     * 
     * @return the contact middle initial.
     */
    public String getContactMiddleInitial();

    /**
     * Gets the user currency code.
     * 
     * @return the user currency code.
     */
    public String getUserCurrencyCode();

    /**
     * Gets the user id.
     * 
     * @return the user id.
     */
    public String getUserId();

}
