package com.concur.mobile.platform.authentication.test;

import java.util.Calendar;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Provides a model of login response information. <br>
 * <br>
 * <b>NOTE:</b>&nbsp;&nbsp; Some of the fields below have the "required = false" flag set as this same class is used by SimpleXML
 * to hold both PPLoginResponse and PPLoginLightResponse data.
 */
@Root(name = "LoginResult")
public class LoginResult {

    public static final String TAG_ANALYTICS_ID = "AnalyticsId";
    /**
     * Contains the access token.
     */
    @Element(name = "AccessToken")
    public AccessToken accessToken;

    /**
     * Contains the authentication type.
     */
    @Element(name = "AuthenticationType", required = false)
    public String authenticationType;

    /**
     * Contains the entity type.
     */
    @Element(name = "EntityType", required = false)
    public String entityType;

    /**
     * Contains the expense country code.
     */
    @Element(name = "ExpenseCtryCode", required = false)
    public String expenseCountryCode;

    /**
     * Contains whether there are required custom fields.
     */
    @Element(name = "HasRequiredCustomFields", required = false)
    public Boolean hasRequiredCustomFields;

    /**
     * Contains the pin expiration date.
     */
    @Element(name = "PinExpirationDate")
    public String pinExpirationDateStr;
    public Calendar pinExpirationDate;

    /**
     * Contains the product offering.
     */
    @Element(name = "ProductOffering", required = false)
    public String productOffering;

    /**
     * Contains the profile status.
     */
    @Element(name = "ProfileStatus", required = false)
    public Integer profileStatus;

    /**
     * Contains the remote wipe flag.
     */
    @Element(name = "RemoteWipe", required = false)
    public Boolean remoteWipe;

    /**
     * Contains the mobile roles.
     */
    @Element(name = "RolesMobile", required = false)
    public String rolesMobile;

    /**
     * Contains the session.
     */
    @Element(name = "Session")
    public Session session;

    /**
     * Contains the site settings.
     */
    @ElementList(name = "SiteSettings", required = false)
    public List<SiteSetting> siteSettings;

    /**
     * Contains the user contact.
     */
    @Element(name = "UserContact", required = false)
    public UserContact userContact;

    /**
     * Contains the currency code.
     */
    @Element(name = "UserCrnCode", required = false)
    public String userCurrencyCode;

    /**
     * Contains the user id.
     */
    @Element(name = "UserId")
    public String userId;

    /**
     * Contains the user id.
     */
    @Element(name = "AnalyticsId")
    public String analyticsId;

}
