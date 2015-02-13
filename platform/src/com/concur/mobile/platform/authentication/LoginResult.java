package com.concur.mobile.platform.authentication;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of login response information.
 */
public class LoginResult extends BaseParser {

    private static final String CLS_TAG = "LoginResult";

    public static final String TAG_LOGIN_RESULT = "LoginResult";

    // Tags.
    private static final String TAG_AUTHENTICATION_TYPE = "AuthenticationType";
    private static final String TAG_ENTITY_TYPE = "EntityType";
    private static final String TAG_EXPENSE_COUNTRY_CODE = "ExpenseCtryCode";
    private static final String TAG_HAS_REQUIRED_CUSTOM_FIELDS = "HasRequiredCustomFields";
    private static final String TAG_PIN_EXPIRATION_DATE = "PinExpirationDate";
    private static final String TAG_PRODUCT_OFFERING = "ProductOffering";
    private static final String TAG_PROFILE_STATUS = "ProfileStatus";
    private static final String TAG_REMOTE_WIPE = "RemoteWipe";
    private static final String TAG_ROLES_MOBILE = "RolesMobile";
    private static final String TAG_USER_CURRENCY_CODE = "UserCrnCode";
    private static final String TAG_USER_ID = "UserId";
    private static final String TAG_DISABLE_AUTOLOGIN = "DisableAutoLogin";
    public static final String TAG_ANALYTICS_ID = "AnalyticsId";

    // Tag codes.
    private static final int TAG_AUTHENTICATION_TYPE_CODE = 0;
    private static final int TAG_ENTITY_TYPE_CODE = 1;
    private static final int TAG_EXPENSE_COUNTRY_CODE_CODE = 2;
    private static final int TAG_HAS_REQUIRED_CUSTOM_FIELDS_CODE = 3;
    private static final int TAG_PIN_EXPIRATION_DATE_CODE = 4;
    private static final int TAG_PRODUCT_OFFERING_CODE = 5;
    private static final int TAG_PROFILE_STATUS_CODE = 6;
    private static final int TAG_REMOTE_WIPE_CODE = 7;
    private static final int TAG_ROLES_MOBILE_CODE = 8;
    private static final int TAG_USER_CURRENCY_CODE_CODE = 10;
    private static final int TAG_USER_ID_CODE = 11;
    private static final int TAG_DISABLE_AUTOLOGIN_CODE = 12;
    private static final int TAG_ANALYTICS_ID_CODE = 13;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_AUTHENTICATION_TYPE, TAG_AUTHENTICATION_TYPE_CODE);
        tagMap.put(TAG_ENTITY_TYPE, TAG_ENTITY_TYPE_CODE);
        tagMap.put(TAG_EXPENSE_COUNTRY_CODE, TAG_EXPENSE_COUNTRY_CODE_CODE);
        tagMap.put(TAG_HAS_REQUIRED_CUSTOM_FIELDS, TAG_HAS_REQUIRED_CUSTOM_FIELDS_CODE);
        tagMap.put(TAG_PIN_EXPIRATION_DATE, TAG_PIN_EXPIRATION_DATE_CODE);
        tagMap.put(TAG_PRODUCT_OFFERING, TAG_PRODUCT_OFFERING_CODE);
        tagMap.put(TAG_PROFILE_STATUS, TAG_PROFILE_STATUS_CODE);
        tagMap.put(TAG_REMOTE_WIPE, TAG_REMOTE_WIPE_CODE);
        tagMap.put(TAG_ROLES_MOBILE, TAG_ROLES_MOBILE_CODE);
        tagMap.put(TAG_USER_CURRENCY_CODE, TAG_USER_CURRENCY_CODE_CODE);
        tagMap.put(TAG_USER_ID, TAG_USER_ID_CODE);
        tagMap.put(TAG_DISABLE_AUTOLOGIN, TAG_DISABLE_AUTOLOGIN_CODE);
        tagMap.put(TAG_ANALYTICS_ID, TAG_ANALYTICS_ID_CODE);
    }

    /**
     * Contains the access token.
     */
    public AccessToken accessToken;

    /**
     * Contains the authentication type.
     */
    public String authenticationType;

    /**
     * Contains the entity type.
     */
    public String entityType;

    /**
     * Contains the expense country code.
     */
    public String expenseCountryCode;

    /**
     * Contains whether there are required custom fields.
     */
    public Boolean hasRequiredCustomFields;

    /**
     * Contains the pin expiration date.
     */
    public Calendar pinExpirationDate;

    /**
     * Contains the product offering.
     */
    public String productOffering;

    /**
     * Contains the profile status.
     */
    public Integer profileStatus;

    /**
     * Contains the remote wipe flag.
     */
    public Boolean remoteWipe;

    /**
     * Contains the mobile roles.
     */
    public String rolesMobile;

    /**
     * Contains the session.
     */
    public Session session;

    /**
     * Contains the site settings.
     */
    public List<SiteSetting> siteSettings;

    /**
     * Contains the user contact.
     */
    public UserContact userContact;

    /**
     * Contains the user permission.
     */
    public Permissions permissions;

    /**
     * Contains the currency code.
     */
    public String userCurrencyCode;

    /**
     * Contains the user id.
     */
    public String userId;

    /**
     * Contains a reference to a parser to parse an <code>AccessToken</code> object.
     */
    private ItemParser<AccessToken> accessTokenItemParser;

    /**
     * Contains a reference to a parser to parse an <code>Session</code> object.
     */
    private ItemParser<Session> sessionItemParser;

    /**
     * Contains a reference to a parser to parse a list of <code>SiteSetting</code> objects.
     */
    private ListParser<SiteSetting> siteSettingListParser;

    /**
     * Contains a reference to a parser to parse a <code>UserContact</code> object.
     */
    private ItemParser<UserContact> userContactItemParser;

    /**
     * Contains a reference to a parser to parse a list of <code>Permission</code> object.
     */
    private ItemParser<Permissions> permissionsItemParser;

    /**
     * Contains the start tag for this parser.
     */
    private String startTag;

    /**
     * Contains the disable auto-login flag.
     */
    public Boolean disableAutoLogin;

    /**
     * Contains the analytics id for log tracking, used in EventTracker class
     */
    public String analyticsId;

    /**
     * Constructs an instance of <code>LoginResult</code> with a parser and start tag.
     * 
     * @param parser
     *            contains the parser that will parse the login result.
     * @param startTag
     *            contains the start tag for this parser.
     */
    public LoginResult(CommonParser parser, String startTag) {

        // Set the start tag and register 'this' parser.
        this.startTag = startTag;
        parser.registerParser(this, startTag);

        // Create and register access token parser.
        String itemTag = "AccessToken";
        accessTokenItemParser = new ItemParser<AccessToken>(itemTag, AccessToken.class);
        parser.registerParser(accessTokenItemParser, itemTag);

        // Create and register the session parser.
        itemTag = "Session";
        sessionItemParser = new ItemParser<Session>(itemTag, Session.class);
        parser.registerParser(sessionItemParser, itemTag);

        // Create and register the site setting list parser.
        String listTag = "SiteSettings";
        siteSettingListParser = new ListParser<SiteSetting>(listTag, "SiteSetting", SiteSetting.class);
        parser.registerParser(siteSettingListParser, listTag);

        // Create and register the user contact parser.
        itemTag = "UserContact";
        userContactItemParser = new ItemParser<UserContact>(itemTag, UserContact.class);
        parser.registerParser(userContactItemParser, itemTag);

        // Create and register the user contact parser.
        itemTag = "Permissions";
        // parser parameter required to call the specific parsing constructor 
        permissionsItemParser = new ItemParser<Permissions>(parser, itemTag, Permissions.class);
        parser.registerParser(permissionsItemParser, itemTag);
    }

    public LoginResult() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_AUTHENTICATION_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    authenticationType = text.trim();
                }
                break;
            }
            case TAG_ENTITY_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    entityType = text.trim();
                }
                break;
            }
            case TAG_EXPENSE_COUNTRY_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    expenseCountryCode = text.trim();
                }
                break;
            }
            case TAG_HAS_REQUIRED_CUSTOM_FIELDS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    hasRequiredCustomFields = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_PIN_EXPIRATION_DATE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    pinExpirationDate = Parse.parseXMLTimestamp(text.trim());
                }
                break;
            }
            case TAG_PRODUCT_OFFERING_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    productOffering = text.trim();
                }
                break;
            }
            case TAG_PROFILE_STATUS_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    profileStatus = Parse.safeParseInteger(text.trim());
                }
                break;
            }
            case TAG_REMOTE_WIPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    remoteWipe = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ROLES_MOBILE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    rolesMobile = text.trim();
                }
                break;
            }
            case TAG_USER_CURRENCY_CODE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    userCurrencyCode = text.trim();
                }
                break;
            }
            case TAG_USER_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    userId = text.trim();
                }
                break;
            }
            case TAG_DISABLE_AUTOLOGIN_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    disableAutoLogin = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            case TAG_ANALYTICS_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    analyticsId = text.trim();
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Assemble the results from any sub-parsers.
                accessToken = accessTokenItemParser.getItem();
                session = sessionItemParser.getItem();
                siteSettings = siteSettingListParser.getList();
                userContact = userContactItemParser.getItem();
                permissions = permissionsItemParser.getItem();
            }
        }
    }
}
