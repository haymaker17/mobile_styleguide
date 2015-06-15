package com.concur.mobile.corp.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Base64;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Crypt;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.SiteSettings;
import com.concur.mobile.core.util.net.SiteSettings.SiteSetting;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.AccessToken;
import com.concur.mobile.platform.authentication.LoginResult;
import com.concur.mobile.platform.authentication.Session;
import com.concur.mobile.platform.authentication.UserContact;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.parser.Error;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

public class TestDriveRegistrationAsyncTask extends CoreAsyncRequestTask {

    private LoginResult loginResponseParser;
    private MWSResponseParser mwsRespParser;

    protected String email;
    protected Locale locale;
    protected String ctryCode;
    protected String password;

    protected int requestId;

    protected HashMap<String, Object> parseMap = new HashMap<String, Object>();

    public TestDriveRegistrationAsyncTask(Context context, int id, BaseAsyncResultReceiver receiver, String email,
            String password, String ctryCode, Locale locale) {
        super(context, id, receiver);
        this.email = email;
        this.password = password;
        this.locale = locale;
        this.ctryCode = ctryCode;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/mobile/MobileSession/RegisterTestDriveUser";
    }

    @Override
    protected String getPostBody() {
        String content = null;

        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<Credentials>");
        strBldr.append("<CtryCode>").append(ctryCode).append("</CtryCode>");
        strBldr.append("<Locale>").append(locale.toString()).append("</Locale>");
        strBldr.append("<LoginID>").append(Format.escapeForXML(email)).append("</LoginID>");
        strBldr.append("<Password>").append(Format.escapeForXML(password)).append("</Password>");
        strBldr.append("</Credentials>");
        content = strBldr.toString();

        return content;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        try {
            connection.addRequestProperty("Authorization", getAuthorizationHeader());
        } catch (URISyntaxException e) {
            Log.e(Const.LOG_TAG, "Failed to generate authorization header", e);
        }
        // Set timeout value
        connection.setReadTimeout(120000);
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        mwsRespParser = new MWSResponseParser();
        loginResponseParser = new LoginResult(parser, "Response");

        // register the parsers of interest
        parser.registerParser(loginResponseParser, "Response");
        parser.registerParser(mwsRespParser, "MWSResponse");

        try {
            parser.parse();
        } catch (XmlPullParserException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected int onPostParse() {
        int resultcode = RESULT_ERROR;

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();

        // check if response is success
        if (reqStatus.isSuccess()) {
            setLoginResponse();
            resultcode = RESULT_OK;
        } else {
            // log the error message
            List<Error> errors = reqStatus.getErrors();
            if (errors != null && errors.size() > 0) {
                // Talked to Yiwen and she conforms that for this service if you are getting list of error, always use first error
                // which has usermessage, code.
                Error error = errors.get(0);
                if (error != null) {
                    resultData.putSerializable(ERROR, error);
                }
            }
        }

        return resultcode;
    }

    protected String getAuthorizationHeader() throws URISyntaxException {

        final String consumerKey = "fMwVF9KN5rNTzXp5b2OZHJ";
        final String consumerSecret = "gDvJdQeJSopLnc6oljPIRLdbODpXKDqJ";

        final String nonce = getNonce();
        final String sigMethod = "HMAC-SHA1";
        final String token = "";
        final String tokenSecret = ""; // Blank for 2 leg
        final String oathVer = "1.0";

        final long time_t = System.currentTimeMillis() / 1000L;

        String requestURI = getURL();

        String authHeader = null;

        String sigParams = URLEncoder.encode(String.format((Locale) null,
                "oauth_consumer_key=%s&oauth_nonce=%s&oauth_signature_method=%s&oauth_timestamp=%d&oauth_version=%s",
                consumerKey, nonce, sigMethod, time_t, oathVer));

        String sigBase = String.format((Locale) null, "POST&%s&%s", URLEncoder.encode(requestURI), sigParams);

        String hashKey = String.format((Locale) null, "%s&%s", URLEncoder.encode(consumerSecret),
                URLEncoder.encode(tokenSecret));

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String sig = null;
        if (mac != null) {
            SecretKeySpec keySpec = new SecretKeySpec(hashKey.getBytes(), "HmacSHA1");
            try {
                mac.init(keySpec);
                byte[] sigBytes = mac.doFinal(sigBase.getBytes());
                sig = URLEncoder.encode(Base64.encodeBytes(sigBytes));
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }

        authHeader = String
                .format((Locale) null,
                        "OAuth realm=\"%s\",oauth_nonce=\"%s\",oauth_timestamp=\"%d\",oauth_consumer_key=\"%s\",oauth_signature_method=\"%s\",oauth_version=\"%s\",oauth_signature=\"%s\"",
                        requestURI, nonce, time_t, consumerKey, sigMethod, oathVer, sig);
        return authHeader;
    }

    protected String getNonce() {
        byte[] nonce = new byte[20];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(nonce);

        return Crypt.byteToHex(nonce);

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getCtryCode() {
        return ctryCode;
    }

    public void setCtryCode(String ctryCode) {
        this.ctryCode = ctryCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public HashMap<String, Object> getParseMap() {
        return parseMap;
    }

    public void setParseMap(HashMap<String, Object> parseMap) {
        this.parseMap = parseMap;
    }

    public void setLoginResponse() {

        ConcurMobile app = (ConcurMobile) contextRef.get().getApplicationContext();

        boolean shouldWipe = false;

        if (loginResponseParser.remoteWipe != null) {
            shouldWipe = loginResponseParser.remoteWipe;
        }
        // if should wipe is true just clear all the user data and return.
        if (shouldWipe) {
            Log.d(Const.LOG_TAG, "Remote wipe activated");
            parseMap.put(Const.LR_WIPED, shouldWipe);
            // Do the wipe
            app.clearLocalData();
            
            // NOTE: Calling clearContent() literally deletes the whole DB file,
            // so there is no need to delete anything else except Properties stuff.
            // ConfigUtil.removeLoginInfo(activity);
            // ConfigUtil.remoteWipe(activity);
            ConfigUtil.clearContent(app);

            // Clear Platform Properties.
            PlatformProperties.setAccessToken(null);
            PlatformProperties.setSessionId(null);

            // Clear any A/B Test information.
            Preferences.clearABTestInfo(PreferenceManager.getDefaultSharedPreferences(app));

            // Clear out the Web View cache and cookies.
            ViewUtil.clearWebViewCookies(app);
            
            return;
        }

        // Retrieve session death.
        int sessionDuration = 120;
        Calendar sessionDeath = null;
        Session session = loginResponseParser.session;
        String sessionId = session.id;
        Integer timeout = session.timeout;

        if (timeout != null) {
            try {
                sessionDuration = timeout;
                sessionDeath = Calendar.getInstance();
                sessionDeath.add(Calendar.MINUTE, sessionDuration);
            } catch (NumberFormatException numFormExc) {
                sessionDuration = 120;
            }
        }

        // If no session expiration time provided, then default to 120 minutes.
        if (sessionDeath == null) {
            sessionDeath = Calendar.getInstance();
            sessionDeath.add(Calendar.MINUTE, sessionDuration);
        }

        // Grab the required custom fields boolean value.
        Boolean requiredCustomFields = false;
        if (loginResponseParser.hasRequiredCustomFields != null) {
            requiredCustomFields = loginResponseParser.hasRequiredCustomFields;
        }

        // Default to complete profile.
        Integer travelProfileStatus = Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA;
        if (loginResponseParser.profileStatus != null) {
            travelProfileStatus = loginResponseParser.profileStatus;
        }

        // logged in user data
        String userId = loginResponseParser.userId;
        String roles = loginResponseParser.rolesMobile;
        String userCrnCode = loginResponseParser.userCurrencyCode;
        String entityType = loginResponseParser.entityType;
        String productOffering = loginResponseParser.productOffering;

        // logged user contact info.
        UserContact userContact = loginResponseParser.userContact;
        String companyName = null;
        String email = null;
        String fisrtname = null;
        String lastname = null;
        String middleInitial = null;

        if (userContact != null) {
            companyName = userContact.companyName;
            email = userContact.email;
            fisrtname = userContact.firstName;
            lastname = userContact.lastName;
            middleInitial = userContact.middleInitial;
        }

        // access token
        AccessToken access = loginResponseParser.accessToken;
        String token = access.key;
        String authType = loginResponseParser.authenticationType;

        // Initialize user SiteSettings.
        init(loginResponseParser.siteSettings);

        // Set whether or not user account is configure for location check in.
        final SiteSettings ssInstance = SiteSettings.getInstance();
        boolean isLocationCheckInSupported = ssInstance.isLocationCheckInEnabled();
        parseMap.put(Const.LR_SITE_SETTINGS_LOCACTION_CHECK_IN, isLocationCheckInSupported);

        // Add other login information to the map.
        parseMap.put(Const.LR_ACCESS_TOKEN, token);
        parseMap.put(Const.LR_ACCESS_TOKEN_SECRET, null);
        parseMap.put(Const.LR_AUTHENTICATION_TYPE, authType);
        parseMap.put(Const.LR_SITE_SETTINGS_CARD_TRANS_DATE_EDITABLE, ssInstance.isCardTransDateEditEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_CARD_ALLOW_TRANS_DELETE, ssInstance.isCardTransDeleteEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_MOBILE_PERSONAL_CAR_MILEAGE_ON_HOME,
                ssInstance.isPersonalCardMileageOnHomeEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_HIDE_RECEIPT_STORE, ssInstance.isReceiptStoreHidden());
        parseMap.put(Const.LR_SITE_SETTINGS_SHOW_NONREFUNDABLE_MESSAGE, ssInstance.isShowNonRefundableMessageEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_SHOW_LIST_CODES, ssInstance.shouldShowListCodes());
        parseMap.put(Const.LR_SITE_SETTINGS_ALLOW_APPROVALS, ssInstance.isAllowApprovalsEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_ALLOW_REPORTS, ssInstance.isAllowReportsEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_ALLOW_TRAVEL_BOOKING, ssInstance.isAllowTravelBookingEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_ALLOW_VOICE_BOOKING, ssInstance.isVoiceBookingEnabled());
        parseMap.put(Const.LR_TRAVEL_PROFILE_STATUS, travelProfileStatus);
        parseMap.put(Const.LR_REQUIRED_CUSTOM_FIELDS, requiredCustomFields);
        parseMap.put(Const.LR_SESSION_ID, sessionId);
        parseMap.put(Const.LR_USER_ID, userId);
        parseMap.put(Const.LR_ROLES, roles);
        parseMap.put(Const.LR_SESSION_DURATION, sessionDuration);
        parseMap.put(Const.LR_SESSION_EXPIRATION, sessionDeath.getTimeInMillis());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_CONDITIONAL_FIELD_EVALUATION,
                ssInstance.isConditionalFieldEvaluationEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_SPDY, ssInstance.isSpdyEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_SHOW_JARVIS_HOTEL_UI, ssInstance.shouldShowHotelJarvisUI());

        // add user currency code to map
        if (userCrnCode != null) {
            parseMap.put(Const.LR_USER_CRN_CODE, userCrnCode);
        }

        // add entity type to map
        if (entityType != null) {
            parseMap.put(Const.LR_ENTITY_TYPE, entityType);
        } else {
            parseMap.put(Const.LR_ENTITY_TYPE, Const.ENTITY_TYPE_CORPORATE);
        }

        // add product
        if (productOffering != null) {
            parseMap.put(Const.LR_PRODUCT_OFFERING, productOffering);
        }

        // add user contact info
        if (email != null) {
            parseMap.put(Const.LR_CONTACT_EMAIL, email);
        }

        if (companyName != null) {
            parseMap.put(Const.LR_CONTACT_COMPANY_NAME, companyName);
        }

        if (fisrtname != null) {
            parseMap.put(Const.LR_CONTACT_FIRST_NAME, fisrtname);
        }

        if (lastname != null) {
            parseMap.put(Const.LR_CONTACT_LAST_NAME, lastname);
        }

        if (middleInitial != null) {
            parseMap.put(Const.LR_CONTACT_MIDDLE_INITIAL, middleInitial);
        }

        // Update the config content provider.
        ConfigUtil.updateLoginInfo(getContext(), loginResponseParser);
        // Update Platform properties.
        if (loginResponseParser.accessToken != null) {
            PlatformProperties.setAccessToken(loginResponseParser.accessToken.key);
        } else {
            PlatformProperties.setAccessToken(null);
        }
        if (loginResponseParser.session != null) {
            PlatformProperties.setSessionId(loginResponseParser.session.id);
        } else {
            PlatformProperties.setSessionId(null);
        }        
    }

    /**
     * Initializes the user's <code>SiteSettings</code>. This should only be called from the Login reseponse.
     * 
     * @param siteSettings
     */
    public void init(List<com.concur.mobile.platform.authentication.SiteSetting> siteSettings) {

        SiteSetting.clear();

        if (siteSettings != null) {
            for (com.concur.mobile.platform.authentication.SiteSetting siteSetting : siteSettings) {
                if (siteSetting != null) {
                    SiteSetting.init(siteSetting.name, siteSetting.type, siteSetting.value);
                }

            }
        }
    }
}
