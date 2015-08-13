package com.concur.mobile.core.activity;

import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.service.BaseRequestPasswordReset;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.util.*;
import com.concur.mobile.platform.authentication.*;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.util.Parse;
import com.concur.platform.PlatformProperties;

@EventTracker.EventTrackerClassName(getClassName = "Settings")
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    protected static final String PREF_LOGIN_OBSOLETE = "pref_saved_login_key";
    protected static final String PREF_LOGIN_ENC = "pref_saved_login_enc_key";
    protected static final String PREF_LOGIN_HASH = "pref_digest_1";
    protected static final String PREF_PIN_OBSOLETE = "pref_saved_pin_key";
    protected static final String PREF_PIN_ENC = "pref_saved_pin_enc_key";
    protected static final String PREF_PIN_HASH = "pref_digest_2";

    protected static final String PREF_FB_ACCESS_TOKEN = "pref_digest_3";
    protected static final String PREF_FB_ACCESS_TOKEN_EXPIRES = "pref_digest_3_e";

    public static final String PREF_SESSION_OBSOLETE = "pref_session_key";
    public static final String PREF_SESSION_ENC = "pref_session_key_enc";
    public static final String PREF_PLATFORM_DATA_MIGRATION = "pref_platform_data_migration";

    public static final Crypt PREF_CRYPT;
    private static boolean hideAutoLogin = false;

    public static final String OPEN_SOURCE_LIBRARY_CLASS = "open_source_library_class";

    // Switches for OCR, ***REMOVE FROM BRANCH*** begin
    // public static int timesClicked = 0;
    // public static final String PREF_OCR_FLAG = "pref_ocr_flag";
    // Switches for OCR, ***REMOVE FROM BRANCH*** end

    static {
        ContentResolver cr = ConcurCore.getContext().getContentResolver();
        String id = Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID);
        String fixedKey = new String(new byte[] { (byte) 0x37, (byte) 0x48, (byte) 0xAE, (byte) 0x1E, (byte) 0xDF,
                (byte) 0x23, (byte) 0x45, (byte) 0xA9, (byte) 0x8C, (byte) 0x33, (byte) 0x24, (byte) 0xE4 });
        String key;
        if (id != null) {
            key = id + fixedKey;
        } else {
            key = fixedKey;
        }

        PREF_CRYPT = new Crypt(key);
    }

    private static MobileDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Grab our default preferences
        boolean isSSOUser = isSSOUser();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceCategory autoLoginPrefCat = (PreferenceCategory) findPreference("pref_cat_auto_login_key");
        Preference autoLoginPref = findPreference(Const.PREF_AUTO_LOGIN);
        if (hideAutoLogin || isSSOUser) {

            // MOB-18627 - Hide the Sign In category for SSO users.
            if (autoLoginPrefCat != null) {
                getPreferenceScreen().removePreference(autoLoginPrefCat);
            }

            // Disable AutoLogin.
            disableAutoLogin(prefs);

            // Don't save the login id.
            Editor e = prefs.edit();
            e.putBoolean(Const.PREF_SAVE_LOGIN, false);
            e.commit();

        } else {
            if (autoLoginPref != null && autoLoginPrefCat != null) {
                // Determine if we need to disable auto-login
                boolean disableAutoLogin = prefs.getBoolean(Const.PREF_DISABLE_AUTO_LOGIN, false);
                if (disableAutoLogin) {
                    autoLoginPref.setEnabled(false);
                }
            }
        }

        // Grab the server address
        String serverAdd = prefs.getString(Const.PREF_MWS_ADDRESS, Const.DEFAULT_MWS_ADDRESS);

        // Setup the server pref to display the value in the summary field
        Preference sslServer = findPreference(Const.PREF_MWS_ADDRESS);
        sslServer.setSummary(serverAdd);
        sslServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference p, Object val) {
                String serverAddress = val.toString();
                PlatformProperties.setServerAddress(serverAddress);
                p.setSummary(serverAddress);
                return true;
            }
        });

        ConcurCore app = (ConcurCore) getApplication();

        // Grab the product info
        String prodName = app.getProduct().getName();
        StringBuilder versionString;
        PackageInfo pi;
        try {
            pi = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            versionString = new StringBuilder(pi.versionName);
            versionString.append(" (").append(pi.versionCode).append(')');
        } catch (NameNotFoundException e) {
            versionString = new StringBuilder();
        }

        // Set the preference text for version
        Preference version = findPreference(Const.PREF_VERSION);
        version.setTitle(prodName);
        version.setSummary(versionString.toString());

        // Switches for OCR, ***REMOVE FROM BRANCH*** begin
        // version.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        //
        // @Override
        // public boolean onPreferenceClick(Preference preference) {
        // timesClicked++;
        //
        // if (timesClicked >= 8) {
        // timesClicked = 0;
        // boolean showingNewOCR = Preferences.shouldUseNewOcrFeatures();
        // setShouldUseNewOcrFeatures(!showingNewOCR);
        //
        // // Reverse logic since we just flipped the flag
        // if (!showingNewOCR) {
        // Toast.makeText(getBaseContext(), "New UI Enabled", Toast.LENGTH_SHORT).show();
        // } else {
        // Toast.makeText(getBaseContext(), "New UI Disabled", Toast.LENGTH_SHORT).show();
        // }
        // }
        // return false;
        // }
        // });
        // Switches for OCR, ***REMOVE FROM BRANCH*** end

        // Set the default Voice Search Language based on the device locale.
        String voiceLang = prefs.getString(Const.PREF_VOICE_SEARCH_LANGUAGE, null);
        boolean hideVoiceSearch = false;
        if (Product.GOV.equals(app.getProduct())) {
            hideVoiceSearch = true;
        }
        if (hideVoiceSearch) {
            PreferenceCategory voice = (PreferenceCategory) findPreference(Const.PREF_CAT_GENERAL);
            if (voice != null) {
                getPreferenceScreen().removePreference(voice);
            }
        } else {
            if (voiceLang == null) {

                // If the default device language is not supported,
                // then nothing will be selected in the preference.
                String defaultLang = Locale.getDefault().toString();
                ListPreference voiceLangPref = (ListPreference) findPreference(Const.PREF_VOICE_SEARCH_LANGUAGE);
                voiceLangPref.setDefaultValue(defaultLang);
                voiceLangPref.setValue(defaultLang);

                // Saved to the preference store.
                Editor e = prefs.edit();
                e.putString(Const.PREF_VOICE_SEARCH_LANGUAGE, defaultLang);
                e.commit();
            }
        }

        Preference openSourceLibs = findPreference(Const.PREF_OPEN_SOURCE_LIBRARIES);
        openSourceLibs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Class<?> openSrcLibInfo = (Class<?>) getIntent().getSerializableExtra(OPEN_SOURCE_LIBRARY_CLASS);
                if (openSrcLibInfo != null) {
                    Intent i = new Intent(Preferences.this, openSrcLibInfo);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Will set the instance of <code>MobileDatabase</code> used by this <code>Preferences</code> class to store information.
     * 
     * @param mdb
     *            a reference to the <code>MobileDatabase</code> instance.
     */
    public static void setMobileDatabase(MobileDatabase mdb) {
        db = mdb;
    }

    /**
     * Catch toggles of the checkbox preferences and clear values as needed
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen ps, Preference p) {

        String key = p.getKey();
        if (Const.PREF_SAVE_LOGIN.equals(key) || Const.PREF_AUTO_LOGIN.equals(key)) {
            boolean value = ps.getSharedPreferences().getBoolean(key, false);
            if (!value) {
                boolean clearLoginID = Const.PREF_SAVE_LOGIN.equals(key);

                // Clear pin
                clearPin(ps.getSharedPreferences());

                if (clearLoginID || Const.PREF_AUTO_LOGIN.equals(key)){
                    // If not saving login then unset auto
                    CheckBoxPreference autoLogin = (CheckBoxPreference) ps.findPreference(Const.PREF_AUTO_LOGIN);
                    if (autoLogin != null) {
                        autoLogin.setChecked(false);
                    }
                }
            } else {
                // If saving login then set auto login to true
                CheckBoxPreference autoLogin = (CheckBoxPreference) ps.findPreference(Const.PREF_AUTO_LOGIN);
                if (autoLogin != null) {
                    autoLogin.setChecked(true);
                }
            }

            // Flurry Notification
            String paramValue = null;
            if (Const.PREF_SAVE_LOGIN.equals(key)) {
                paramValue = Flurry.PARAM_VALUE_SAVE_USER_NAME;
            } else if (Const.PREF_AUTO_LOGIN.equals(key)) {
                paramValue = Flurry.PARAM_VALUE_AUTO_LOGIN;
            }
            if (paramValue != null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, paramValue);
                params.put(Flurry.PARAM_NAME_NEW_VALUE, ((value) ? Flurry.PARAM_VALUE_YES : Flurry.PARAM_VALUE_NO));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
            }
        } else if (Const.PREF_PUSH_ALLOW.equals(key)) {
            boolean enabled = ps.getSharedPreferences().getBoolean(key, false);
            Notifications notifications = new Notifications(ConcurCore.getContext());
            if (enabled) {
                notifications.initAWSPushService();
            } else {
                notifications.stopAWSPushService();
            }
        } else if (Const.PREF_PUSH_VIBRATE.equals(key)) {
            boolean enabled = ps.getSharedPreferences().getBoolean(key, false);
            if (enabled) {
                setAllowVibration(true);
            } else {
                setAllowVibration(false);
            }
        }
        return super.onPreferenceTreeClick(ps, p);
    }

    /**
     * Do stuff. For now, make these things rerunnable so we don't have to put together a deep version update stack. Since a user
     * could be upgrading from one of many previous versions we either have to build an upgrade stack or just make our upgrades
     * rerunnable. Since these changes are small and infrequent we will make them rerunnable.
     */
    public static void upgradePreferences(ConcurCore app) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());

        encryptLoginAndPin(prefs);
        shiftSession(prefs);
        removePreferences(prefs);
        platFormDataMigration(prefs, app);
        doAutoLogin(prefs, app);
    }

    private static void platFormDataMigration(SharedPreferences prefs, ConcurCore app) {
        if (!prefs.contains(PREF_PLATFORM_DATA_MIGRATION) && (!TextUtils.isEmpty(getSessionId()))) {

            SessionInfo sessionInfo = ConfigUtil.getSessionInfo(ConcurCore.getContext());

            // Login data migration for 9.11 release as 9.11 release supports platform.
            LoginResult loginResult = new LoginResult();
            // access token
            AccessToken token = new AccessToken();
            String accessToken = getAccessToken();
            if (TextUtils.isEmpty(accessToken) && sessionInfo != null) {
                // MOB-19313 - There was a bug in 9.11.x where the AccessToken was
                // being overriden in the old Preference db (see UserAndSessionInfoUtil.updateUserAndSessionInfo() method).
                // Basically, the AccessToken was being saved (initially), but then over-written with
                // null, which deletes the previously saved AccessToken.
                accessToken = sessionInfo.getAccessToken();
            }
            token.key = accessToken;
            loginResult.accessToken = token;
            // authentication type or sign in method
            loginResult.authenticationType = getPin(prefs, null);
            // other login info
            loginResult.entityType = prefs.getString(Const.PREF_ENTITY_TYPE, null);
            loginResult.expenseCountryCode = null;
            loginResult.hasRequiredCustomFields = prefs.getBoolean(Const.PREF_REQUIRED_CUSTOM_FIELDS, false);
            loginResult.pinExpirationDate = null;
            loginResult.productOffering = prefs.getString(Const.PREF_PRODUCT_OFFERING, null);
            loginResult.profileStatus = prefs.getInt(Const.PREF_TRAVEL_PROFILE_STATUS, 0);
            loginResult.remoteWipe = false;
            loginResult.rolesMobile = prefs.getString(Const.PREF_ROLES, null);
            // sesssion info
            String sesssionId = getSessionId();
            Long expireTime = prefs.getLong(Const.PREF_SESSION_EXPIRATION, 0L);
            Session session = new Session();
            session.id = sesssionId;
            session.expirationTime = expireTime;
            loginResult.session = session;
            // user id
            loginResult.userId = prefs.getString(Const.PREF_USER_ID, null);
            // Update/Create the config content provider for session info.
            ConfigUtil.updateSessionInfo(ConcurCore.getContext().getContentResolver(), loginResult, true);

            // After the update above, we should get the latest (updated) SessionInfo.
            sessionInfo = ConfigUtil.getSessionInfo(ConcurCore.getContext());
            // set login id
            String loginId = Preferences.getLogin(prefs, null);
            sessionInfo.setLoginId(loginId);
            // set server url
            String serverUrl = getServerAddress();
            sessionInfo.setServerUrl(serverUrl);
            // set sign in method and SSO url
            String pinOrPassword = Preferences.getPin(prefs, null);
            String signInMethod = com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD;
            String ssoUrl = null;

            ConcurService service = app.getService();
            if (service != null) {
                CorpSsoQueryReply ssoQueryReply = service.getCorpSsoQueryReply();
                if (ssoQueryReply != null && ssoQueryReply.ssoEnabled && (!TextUtils.isEmpty(ssoQueryReply.ssoUrl))) {
                    signInMethod = com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO;
                    ssoUrl = ssoQueryReply.ssoUrl;
                    // set server url
                    serverUrl = ssoQueryReply.serverUrl;
                    if(serverUrl!=null && !serverUrl.isEmpty()){
                        //set platformproperties
                        sessionInfo.setServerUrl(serverUrl);
                    }
                }
            }
            if (TextUtils.isEmpty(pinOrPassword)) {
                signInMethod = com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD;
            }
            sessionInfo.setSignInMethod(signInMethod);
            sessionInfo.setSSOUrl(ssoUrl);
            // update session info so you will get it once everything is updated.
            ConfigUtil.updateSessionInfo(ConcurCore.getContext(), sessionInfo);
            // Finally set platform-properties
            PlatformProperties.setAccessToken(sessionInfo.getAccessToken());
            PlatformProperties.setSessionId(sessionInfo.getSessionId());
            // commit chnages in preferences.
            Editor e = prefs.edit();
            e.putBoolean(PREF_PLATFORM_DATA_MIGRATION, true);
            e.commit();
        } else {
            Log.d(Const.LOG_TAG, "This version is greater than 9.11");
        }
    }

    private static void shiftSession(SharedPreferences prefs) {
        if (prefs.contains(PREF_SESSION_ENC)) {
            // Move it out of here
            String sessionId = prefs.getString(PREF_SESSION_ENC, null);

            if (sessionId != null) { // Just paranoia
                sessionId = PREF_CRYPT.decrypt(sessionId);

                // This now writes to the DB
                saveSessionId(sessionId);

                Editor e = prefs.edit();
                e.remove(PREF_SESSION_ENC);
                e.commit();
            }
        }
    }

    /**
     * This should only be called when running a new version for the first time. It will ensure that login/pin are encrypted in
     * the proper location and that the plaintext versions are deleted.
     * 
     * @param prefs
     */
    private static void encryptLoginAndPin(SharedPreferences prefs) {
        if (prefs.contains(PREF_LOGIN_OBSOLETE)) {
            // Yep, plaintext still there. Fix it.
            String login = prefs.getString(PREF_LOGIN_OBSOLETE, null);
            String pin = prefs.getString(PREF_PIN_OBSOLETE, null);

            if (login != null) {
                login = PREF_CRYPT.encrypt(login);
            }

            if (pin != null) {
                pin = PREF_CRYPT.encrypt(pin);
            }

            Editor e = prefs.edit();

            e.remove(PREF_LOGIN_OBSOLETE);
            e.remove(PREF_PIN_OBSOLETE);

            if (login != null) {
                e.putString(PREF_LOGIN_ENC, login);
            }
            if (pin != null) {
                e.putString(PREF_PIN_ENC, pin);
            }

            e.commit();
        }

        // It would be most efficient to capture and move these up above but we have to
        // handle upgrades from multiple older versions and we want to remain rerunnable
        // so just go with this brute approach to keep it clean and simple.
        if (prefs.contains(PREF_LOGIN_ENC)) {
            // Move these out of here
            String login = prefs.getString(PREF_LOGIN_ENC, null);
            String pin = prefs.getString(PREF_PIN_ENC, null);

            if (login != null && pin != null) { // Just paranoia
                login = PREF_CRYPT.decrypt(login);
                pin = PREF_CRYPT.decrypt(pin);

                // These now write to the DB and generate the hash
                saveLogin(prefs, login);
                savePin(prefs, pin);

                Editor e = prefs.edit();
                e.remove(PREF_LOGIN_ENC);
                e.remove(PREF_PIN_ENC);
                e.commit();
            }
        }

    }

    private static void removePreferences(SharedPreferences prefs) {
        Editor e = prefs.edit();

        // Remove the roles preference. It is parsed at login and the desired component parts are stored. We never read
        // the string back so there is no need to store it. If it is ever stored again then it must be encrypted.
        e.remove(Const.PREF_ROLES);

        // Remove the old, unencrypted session id.
        e.remove(PREF_SESSION_OBSOLETE);

        // Remove any stored A/B Test information.
        e.remove(Const.PREF_ABTEST_ID);
        e.remove(Const.PREF_ABTEST_EXP);

        // Reset message center badging display
        e.remove(Const.PREF_MSG_CENTER_BADGE);
        e.commit();
    }

    public static boolean isHideAutoLogin(boolean isHide) {
        hideAutoLogin = isHide;
        return isHide;
    }

    public static void saveLogin(SharedPreferences prefs, String login) {
        if (db != null) {
            db.writeComComponent(Const.COM_COMPONENT_1, login);
            saveLoginHash(prefs, login);
        }
    }

    protected static void saveLoginHash(SharedPreferences prefs, String login) {
        Editor e = prefs.edit();
        String digest = PREF_CRYPT.hash(login);
        e.putString(PREF_LOGIN_HASH, digest);
        e.commit();
    }

    public static String getLogin(SharedPreferences prefs, String defValue) {
        if (db != null) {
            return db.readComComponent(Const.COM_COMPONENT_1);
        } else {
            return "";
        }
    }

    protected static boolean loginMatches(String login) {
        boolean match = false;

        String enteredDigest = PREF_CRYPT.hash(login);
        if (enteredDigest.length() > 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            String storedDigest = prefs.getString(PREF_LOGIN_HASH, null);
            if (enteredDigest.equals(storedDigest)) {
                match = true;
            }
        }

        return match;
    }

    public static void savePin(SharedPreferences prefs, String pin) {
        if (db != null) {
            db.writeComComponent(Const.COM_COMPONENT_2, pin);
            savePinHash(prefs, pin);
        }
    }

    protected static void savePinHash(SharedPreferences prefs, String pin) {
        String digest = PREF_CRYPT.hash(pin);
        Editor e = prefs.edit();
        e.putString(PREF_PIN_HASH, digest);
        e.commit();
    }

    public static String getPin(SharedPreferences prefs, String defValue) {
        if (db != null) {
            return db.readComComponent(Const.COM_COMPONENT_2);
        } else {
            return "";
        }
    }

    protected static boolean pinMatches(String pin) {
        boolean match = false;

        String enteredDigest = PREF_CRYPT.hash(pin);
        if (enteredDigest.length() > 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            String storedDigest = prefs.getString(PREF_PIN_HASH, null);
            if (enteredDigest.equals(storedDigest)) {
                match = true;
            }
        }

        return match;
    }

    public static void saveSessionId(String sessionId) {
        if (db != null) {
            db.writeComComponent(Const.COM_COMPONENT_3, sessionId);
        }
    }

    public static String getSessionId() {
        if (db != null) {
            return db.readComComponent(Const.COM_COMPONENT_3);
        } else {
            return "";
        }
    }

    /**
     * Gets the current server address set on the application context.
     * 
     * @return returns the current server address set on the application context.
     */
    public static String getServerAddress() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        String serverAddress = prefs.getString(Const.PREF_MWS_ADDRESS, Const.DEFAULT_MWS_ADDRESS);
        return serverAddress;
    }

    /**
     * Saves the oAuth token.
     * 
     * If <code>accessToken</code> is <code>null</code> or the empty string, then the access token com component will be deleted.
     * 
     * @param accessToken
     *            the oAuth token to save.
     */
    public static void saveAccessToken(String accessToken) {
        if (db != null) {
            if (accessToken != null && accessToken.length() > 0) {
                db.writeComComponent(Const.COM_COMPONENT_4, accessToken);
            } else {
                db.deleteComComponent(Const.COM_COMPONENT_4);
            }
        }
    }

    /**
     * Attempts to retrieve the saved oAuth token or <code>null</code> if there is none.
     * 
     * @return the saved oAuth token or <code>null</code> if there is none.
     * 
     */
    public static String getAccessToken() {
        if (db != null) {
            return db.readComComponent(Const.COM_COMPONENT_4);
        } else {
            return null;
        }
    }

    /**
     * @deprecated - use {@link com.concur.platform.PlatformProperties#setAccessToken(String)
     *             PlatformProperties.setAccessToken(null)} instead.
     * 
     *             Deletes the oAuth token from the data store.
     */
    @Deprecated
    public static void clearAccessToken() {
        // Clear the access token
        if (db != null) {
            db.deleteComComponent(Const.COM_COMPONENT_4);
        }
    }

    /**
     * @deprecated - use {@link com.concur.platform.PlatformProperties#setAccessToken(String)
     *             PlatformProperties.setAccessToken(null)} and
     *             {@link com.concur.platform.PlatformProperties#setSessionId(String) PlatformProperties.setSessionId(null)}
     *             instead.
     * 
     * @param prefs
     */
    @Deprecated
    public static void clearUser(SharedPreferences prefs) {

        // Clear everything
        clearSession(prefs);
        clearAccessToken();
    }

    public static void clearPin(SharedPreferences prefs) {
        if (db != null) {
            // Clear pin (maybe) and session
            db.deleteComComponent(Const.COM_COMPONENT_2);
        }
    }

    /**
     * @deprecated - use {@link com.concur.platform.PlatformProperties#setSessionId(String) PlatformProperties.setSessionId(null)}
     *             instead.
     * @param prefs
     */
    @Deprecated
    public static void clearSession(SharedPreferences prefs) {
        clearSession(prefs, false);
    }

    /**
     * @deprecated - use {@link com.concur.platform.PlatformProperties#setSessionId(String) PlatformProperties.setSessionId(null)}
     *             instead.
     * 
     * @param prefs
     * @param keepPin
     */
    @Deprecated
    public static void clearSession(SharedPreferences prefs, boolean keepPin) {

        // Clear pin (maybe) and session
        Editor e = prefs.edit();
        if (db != null) {
            if (!keepPin) {
                db.deleteComComponent(Const.COM_COMPONENT_2);
            }
            db.deleteComComponent(Const.COM_COMPONENT_3);
        }
        e.remove(Const.PREF_SESSION_DURATION);
        e.remove(Const.PREF_SESSION_EXPIRATION);
        e.remove(Const.PREF_USER_ID); // MOB-16792

        e.commit();

        // MOB-16792 - Note we must set the Flurry User ID again
        // here in case the user logs out or their session has expired.
        // In this case, the Flurry User ID should be: |{deviceId}
        EventTracker.INSTANCE.setUserId(ConcurCore.getTrackingUserId());
    }

    /**
     * Will clear any A/B Test information.
     * 
     * @param prefs
     *            contains the <code>SharedPreferences</code> object.
     */
    public static void clearABTestInfo(SharedPreferences prefs) {
        // Clear everything
        Editor e = prefs.edit();
        e.remove(Const.PREF_ABTEST_ID);
        e.remove(Const.PREF_ABTEST_EXP);
        e.commit();
    }

    public static void disableAutoLogin(SharedPreferences prefs) {
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_AUTO_LOGIN, false);
        e.putBoolean(Const.PREF_DISABLE_AUTO_LOGIN, true);
        e.commit();

        Log.d(Const.LOG_TAG, "Disallowed auto-login");
    }

    public static void enableAutoLogin(SharedPreferences prefs) {
        Editor e = prefs.edit();

        e.putBoolean(Const.PREF_DISABLE_AUTO_LOGIN, false);
        e.commit();

        Log.d(Const.LOG_TAG, "Allowed auto-login");
    }

    /**
     * @deprecated - use {@link com.concur.platform.PlatformProperties#setSessionId(String) PlatformProperties.setSessionId(null)}
     *             instead.
     * 
     *             Will clear the session pin, id and expiration if the session id in <code>prefs<code> matches
     * on <code>sessionId</code>.
     * 
     * @param prefs
     * @param sessionId
     */
    @Deprecated
    public static void clearSessionIfCurrent(SharedPreferences prefs, String sessionId) {
        synchronized (Preferences.class) {
            String prefSessionId = getSessionId();
            if (prefSessionId == null || prefSessionId.equalsIgnoreCase(sessionId)) {
                clearSession(prefs);
            }
        }
    }

    /**
     * Will set the session id and expiration time. This method sets the information from within a <code>Preferences</code> class
     * synchronized block.
     * 
     * @param prefs
     *            the shared preferences object.
     * @param sessionId
     *            the session id.
     * @param duration
     *            the duration.
     * @param sessionExpiration
     *            the session expiration time.
     */
    public static void setSessionInfo(SharedPreferences prefs, String accessToken, String sessionId, Integer duration,
            Long sessionExpiration) {

        synchronized (Preferences.class) {

            // Set the accessToken
            saveAccessToken(accessToken);

            // Set session id and expiration.
            saveSessionId(sessionId);
            if (duration != null && sessionExpiration != null) {
                Editor e = prefs.edit();
                e.putInt(Const.PREF_SESSION_DURATION, duration);
                e.putLong(Const.PREF_SESSION_EXPIRATION, sessionExpiration);
                e.commit();
            }
        }
    }

    public static void extendSesssionExpiration(SharedPreferences prefs, int duration) {
        Calendar now = Calendar.getInstance(FormatUtil.UTC);
        long expire = now.getTimeInMillis() + (duration * 60000);
        String sessionId = PlatformProperties.getSessionId();
        if (!TextUtils.isEmpty(sessionId)) { // MOB-21741
            synchronized (Preferences.class) {
                // Update expiration
                Editor e = prefs.edit();
                e.putLong(Const.PREF_SESSION_EXPIRATION, expire);
                e.commit();
                ConfigUtil.updateSessionExpirationTime(ConcurCore.getContext(), sessionId, expire);
            }
        }
    }

    /**
     * Determine if this is the first time the application has run since install.
     * 
     * @param prefs
     */
    public static boolean isFirstTimeRunning(SharedPreferences prefs) {
        return !prefs.contains(Const.PREF_FIRST_TIME_RUNNING);
    }

    /**
     * Store a preference indicating that the application has been run since install.
     * 
     * @param prefs
     */
    public static void setNotFirstTimeRunning(SharedPreferences prefs) {
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_FIRST_TIME_RUNNING, true);
        e.commit();
    }

    public static boolean isTestDriveUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        String productOffering = prefs.getString(Const.PREF_PRODUCT_OFFERING, null);
        if (productOffering != null && productOffering.length() > 0 && productOffering.equalsIgnoreCase("HK-TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTestDriveAccountExpired() {
        if (isTestDriveUser()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            Calendar accountExpirationDate = Parse.parseXMLTimestamp(
                    prefs.getString(Const.PREF_ACCOUNT_EXPIRATION_DATE, null));
            if (accountExpirationDate != null) {
                if (Calendar.getInstance().compareTo(accountExpirationDate) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the test drive user should show overlay tips for the current activity
     * 
     * @param targetInPrefs
     *            is the string in const that relates to the calling activity (IE Const.PREF_TD_HOME)
     * 
     * @return whether or not the tips overlay should show. If the user has seen them, we will return false.
     */
    public static boolean shouldShowTestDriveTips(String targetInPrefs) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return !prefs.contains(targetInPrefs);
    }

    /**
     * Puts a value into prefs so shouldShow (above) returns false if it has been shown.
     * 
     * @param targetInPrefs
     */
    public static void setShouldNotShowTestDriveTips(String targetInPrefs) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(targetInPrefs, true);
        e.commit();
    }

    public static boolean shouldPromptToRate(SharedPreferences prefs, boolean useDelay) {
        boolean shouldPrompt = false;
        boolean delayExpired = false;

        if (useDelay) {
            // Use a 14-day delay before showing this prompt the first time.
            // Currently only used by the itin view (from SegmentList)
            final long days14 = 14l * 86400000l;
            final long now = Calendar.getInstance().getTimeInMillis();
            final long delayStart = prefs.getLong(Const.PREF_START_DELAY_PROMPT_TO_RATE, -1);
            if (delayStart == -1) {
                // First time trigger. Write now as the start. We will not prompt for 14 days.
                Editor e = prefs.edit();
                e.putLong(Const.PREF_START_DELAY_PROMPT_TO_RATE, now);
                e.commit();
            } else if (delayStart == -2) {
                // Flag indicating the delay is competely done. Don't consider it at all anymore.
                delayExpired = true;
            } else {
                shouldPrompt = now > (delayStart + days14);
                if (shouldPrompt) {
                    // Delay is done. Write out a flag value.
                    Editor e = prefs.edit();
                    e.putLong(Const.PREF_START_DELAY_PROMPT_TO_RATE, -2);
                    e.commit();
                }
            }
        }

        if (!useDelay || delayExpired) {
            final long days90 = 90l * 86400000l;
            final long lastPrompt = prefs.getLong(Const.PREF_LAST_PROMPT_TO_RATE, -1);
            if (lastPrompt == -1) {
                shouldPrompt = true;
            } else {
                final long now = Calendar.getInstance().getTimeInMillis();
                shouldPrompt = now > (lastPrompt + days90);
            }
        }

        return shouldPrompt;
    }

    public static void setPromptedToRate(SharedPreferences prefs) {
        // Indicate that the user was prompted to rate.
        // Also expire the delay. Delays don't matter anymore.
        long now = Calendar.getInstance().getTimeInMillis();
        Editor e = prefs.edit();
        e.putLong(Const.PREF_LAST_PROMPT_TO_RATE, now);
        e.putLong(Const.PREF_START_DELAY_PROMPT_TO_RATE, -2);
        e.commit();
    }

    public static void enableShowOffers() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_TRAVEL_SHOW_OFFERS, true);
        e.commit();

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_SHOW_OFFERS);
        params.put(Flurry.PARAM_NAME_NEW_VALUE, Flurry.PARAM_VALUE_YES);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
    }

    public static void disableShowOffers() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_TRAVEL_SHOW_OFFERS, false);
        e.commit();

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_SHOW_OFFERS);
        params.put(Flurry.PARAM_NAME_NEW_VALUE, Flurry.PARAM_VALUE_NO);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
    }

    public static boolean shouldShowOffers() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_TRAVEL_SHOW_OFFERS, true);
    }

    public static void enableOfferValidityCheck() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_TRAVEL_CHECK_OFFER_VALIDITY, true);
        e.commit();

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_OFFER_VALIDITY);
        params.put(Flurry.PARAM_NAME_NEW_VALUE, Flurry.PARAM_VALUE_YES);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
    }

    public static void disableOfferValidityCheck() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_TRAVEL_CHECK_OFFER_VALIDITY, false);
        e.commit();

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, Flurry.PARAM_VALUE_OFFER_VALIDITY);
        params.put(Flurry.PARAM_NAME_NEW_VALUE, Flurry.PARAM_VALUE_NO);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
    }

    public static boolean shouldCheckOfferValidity() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_TRAVEL_CHECK_OFFER_VALIDITY, true);
    }

    public static String getInstallID() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());

        String installId;
        installId = prefs.getString(Const.PREF_INSTALL_ID, null);
        if (installId == null) {
            installId = UUID.randomUUID().toString();
            Editor e = prefs.edit();
            e.putString(Const.PREF_INSTALL_ID, installId);
            e.commit();
        }

        return installId;
    }

    public static boolean isTripItLinked() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_TRIPIT_LINKED, false);
    }

    public static void setTripItLinkStatus(boolean isLinked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_TRIPIT_LINKED, isLinked);
        e.commit();
    }

    public static boolean isTripItEmailConfirmed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_TRIPIT_EMAIL_CONFIRMED, false);
    }

    public static void setTripItEmailConfirmed(boolean isConfirmed) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_TRIPIT_EMAIL_CONFIRMED, isConfirmed);
        e.commit();
    }

    public static boolean shouldShowTripItAd() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_SHOW_TRIPIT_AD, true);
    }

    public static boolean isExpenseItUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_SHOW_EXPENSEIT_AD, true);
    }

    public static boolean isCardAgreementAccepted() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_CARD_AGREEMENT_ACCEPTED, false);
    }

    public static void setCardAgreementAccepted(boolean accepted) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_CARD_AGREEMENT_ACCEPTED, accepted);
        e.commit();
    }

    public static void setFacebookAccessToken(String token) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putString(PREF_FB_ACCESS_TOKEN, PREF_CRYPT.encrypt(token));
        e.commit();
    }

    public static String getFacebookAccessToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        String enc = prefs.getString(PREF_FB_ACCESS_TOKEN, null);
        String token = null;
        if (enc != null) {
            token = PREF_CRYPT.decrypt(enc);
        }
        return token;
    }

    public static void setFacebookAccessTokenExpires(long expires) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putLong(PREF_FB_ACCESS_TOKEN_EXPIRES, expires);
        e.commit();
    }

    public static long getFacebookAccessTokenExpires() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getLong(PREF_FB_ACCESS_TOKEN_EXPIRES, -1);
    }

    public static void removeFacebookAccess() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.remove(PREF_FB_ACCESS_TOKEN);
        e.remove(PREF_FB_ACCESS_TOKEN_EXPIRES);
        e.commit();
    }

    public static boolean shouldShowListCodes() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_SHOW_LIST_CODES, false);
    }

    // MOB-13863-removed
    // public static boolean shouldPromptForNotifications() {
    // if (isInternalUser()) {
    // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
    // return !prefs.contains(Const.PREF_PUSH_ALLOW);
    // } else {
    // return false;
    // }
    // }

    public static boolean shouldPromptForNotifications() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return !prefs.contains(Const.PREF_PUSH_ALLOW);
    }

    public static boolean allowNotifications() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_PUSH_ALLOW, false);
    }

    public static void setAllowNotifications(boolean allow) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_PUSH_ALLOW, allow);
        e.commit();
        Notifications notifications = new Notifications(ConcurCore.getContext());
        if (allow) {
            notifications.initAWSPushService();
        } else {
            notifications.stopAWSPushService();
        }

    }

    public static void setAllowVibration(boolean allow) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_PUSH_VIBRATE, allow);
        e.commit();
    }

    public static boolean shouldVibrateNotifications() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_PUSH_VIBRATE, false);
    }

    /**
     * Sets whether or not we should show message center badge.
     * 
     * @param allow
     *            Indicates whether or not we should show message center badge
     */
    public static void setShowNotificationBadge(boolean allow) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_MSG_CENTER_BADGE, allow);
        e.commit();
    }

    /**
     * Checks SharedPreferences for whether or not the user already visit new updates in message center
     * 
     * @return whether or not user has viewed new updates in message center; by default its true
     */
    public static boolean shouldShowNotificationBadge() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_MSG_CENTER_BADGE, true);
    }

    /**
     * Gets whether or not report approvals is allowed.
     * 
     * @return returns whether report approvals is allowed.
     */
    public static boolean shouldAllowReportApprovals() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_ALLOW_APPROVALS, false);
    }

    /**
     * Gets whether or not reports are allowed.
     * 
     * @return returns whether reports are allowed.
     */
    public static boolean shouldAllowReports() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_ALLOW_REPORTS, false);
    }

    /**
     * Gets whether or not travel booking is allowed.
     * 
     * @return returns whether or not travel booking is allowed.
     */
    public static boolean shouldAllowTravelBooking() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_ALLOW_TRAVEL_BOOKING, false);
    }

    /**
     * Gets whether or not travel search/booking by Voice is enabled.
     * 
     * @return returns whether or not travel search/booking by Voice is enabled.
     */
    public static boolean shouldAllowVoiceBooking() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_ALLOW_VOICE_BOOKING, false);
    }

    /**
     * Gets whether or not Spdy is enabled.
     * 
     * @return returns whether or not Spdy is enabled..
     */
    public static boolean shouldEnableSpdy() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_ENABLE_SPDY, false);
    }

    /**
     * Gets whether or not ShowHotelJarvisUIOnAndroid is enabled.
     *
     * @return returns whether or not ShowHotelJarvisUIOnAndroid is enabled..
     */
    public static boolean shouldShowHotelJarvisUI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.SHOW_JARVIS_HOTEL_UI, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the preferences change listener.
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Register the preferences change listener.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Will listen for preference changes and generate Flurry notifications.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String paramValue = null;
        String newParamValue = null;
        // Flurry Notification
        if (Const.PREF_SAVE_LOGIN.equals(key)) {
            paramValue = Flurry.PARAM_VALUE_SAVE_USER_NAME;
            boolean newVal = sharedPreferences.getBoolean(key, false);
            if (newVal) {
                newParamValue = Flurry.PARAM_VALUE_YES;
            } else {
                newParamValue = Flurry.PARAM_VALUE_NO;
            }
        } else if (Const.PREF_AUTO_LOGIN.equals(key)) {
            paramValue = Flurry.PARAM_VALUE_AUTO_LOGIN;
            boolean newVal = sharedPreferences.getBoolean(key, false);
            if (newVal) {
                newParamValue = Flurry.PARAM_VALUE_YES;
            } else {
                newParamValue = Flurry.PARAM_VALUE_NO;
            }
        } else if (Const.PREF_PUSH_ALLOW.equals(key)) {
            paramValue = Flurry.PARAM_VALUE_PUSH_ALLOW;
            boolean newVal = sharedPreferences.getBoolean(key, false);
            if (newVal) {
                newParamValue = Flurry.PARAM_VALUE_YES;
            } else {
                newParamValue = Flurry.PARAM_VALUE_NO;
            }
        } else if (Const.PREF_PUSH_VIBRATE.equals(key)) {
            paramValue = Flurry.PARAM_VALUE_PUSH_ALLOW_VIBRATE;
            boolean newVal = sharedPreferences.getBoolean(key, false);
            if (newVal) {
                newParamValue = Flurry.PARAM_VALUE_YES;
            } else {
                newParamValue = Flurry.PARAM_VALUE_NO;
            }
        } else if (Const.PREF_MWS_ADDRESS.equals(key)) {
            paramValue = Flurry.PARAM_VALUE_CONNECTION;
        } else if (Const.PREF_VOICE_SEARCH_LANGUAGE.equals(key)) {
            paramValue = Flurry.PARAM_VALUE_VOICE_SEARCH_LANGUAGE;
            newParamValue = sharedPreferences.getString(Const.PREF_VOICE_SEARCH_LANGUAGE, "");
        }

        if (paramValue != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, paramValue);
            if (newParamValue != null) {
                params.put(Flurry.PARAM_NAME_NEW_VALUE, newParamValue);
            }
            EventTracker.INSTANCE.track(Flurry.CATEGORY_SETTINGS, Flurry.EVENT_NAME_ACTION, params);
        }

    }

    public static boolean shouldUpdateCityscape() {
        boolean shouldUpdate = false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        final long updateTime = prefs.getLong(Const.PREF_CITYSCAPE_UPDATE_TIME, -1);
        if (updateTime == -1) {
            // First time, update
            shouldUpdate = true;
        } else {
            final long hours12 = 60 * 60 * 12 * 1000;
            final long now = Calendar.getInstance().getTimeInMillis();
            shouldUpdate = now > (updateTime + hours12);
        }

        return shouldUpdate;
    }

    public static String getCurrentCityscape() {
        Formatter formatter = null;
        int CurrentCityscapeIndex = -1;
        int AppActivatesCount = 0;
        int MAX_ACTIVATE_BEFORE_SWAP = 5;
        int TOTAL_CITYSCAPE_IMAGES = 10;
        SharedPreferences prefs;
        Editor editor;
        String CurrentCityScape = "";

        prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        editor = prefs.edit();

        // Get the Index of the currently loaded cityscape
        CurrentCityscapeIndex = prefs.getInt(Const.PREF_CURRENT_CITYSCAPE, 0);

        // Get the number of times the current cityscape has been viewed
        AppActivatesCount = prefs.getInt(Const.PREF_CITYSCAPE_ACTIVATES_SINCE_SWAP, 0) + 1;

        // Move to next cityscape image if necessary
        if (AppActivatesCount >= MAX_ACTIVATE_BEFORE_SWAP) {
            CurrentCityscapeIndex++;
            CurrentCityscapeIndex %= TOTAL_CITYSCAPE_IMAGES;
            AppActivatesCount = 0;
        }

        // Save cityscape state to preferences
        editor.putInt(Const.PREF_CURRENT_CITYSCAPE, CurrentCityscapeIndex);
        editor.putInt(Const.PREF_CITYSCAPE_ACTIVATES_SINCE_SWAP, AppActivatesCount);
        editor.commit();

        // Format response
        formatter = new Formatter();
        if (CurrentCityscapeIndex == 0) {
            CurrentCityScape = "cityscape_placeholder";
        } else {
            CurrentCityScape = "city_" + formatter.format("%02d", CurrentCityscapeIndex).toString();
        }
        formatter.close();

        return CurrentCityScape;
    }

    public static void setCityscapeUpdated(int cityscapeResourceId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());

        long now = Calendar.getInstance().getTimeInMillis();
        Editor e = prefs.edit();
        e.putLong(Const.PREF_CITYSCAPE_UPDATE_TIME, now);
        e.putInt(Const.PREF_CURRENT_CITYSCAPE, cityscapeResourceId);
        e.commit();
    }

    public static boolean isInternalUser() {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        String loginId = Preferences.getLogin(sharedPrefs, "");
        if (loginId != null) {
            loginId = loginId.toLowerCase();
            if (loginId.endsWith("@democoncur.com") || loginId.endsWith("@concur.com") || loginId.contains("@snw")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public static void setPinResetKeyPart(String keyPart) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putString(Const.PREF_PIN_RESET_KEY_PART, keyPart);
        e.commit();
    }

    public static String getPinResetKeyPart() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getString(Const.PREF_PIN_RESET_KEY_PART, null);
    }

    public static void clearPinResetKeyPart() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.remove(Const.PREF_PIN_RESET_KEY_PART);
        e.commit();
    }

    public static void setPinResetEmail(String keyPart) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putString(Const.PREF_PIN_RESET_EMAIL, keyPart);
        e.commit();
    }

    public static String getPinResetEmail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getString(Const.PREF_PIN_RESET_EMAIL, null);
    }

    public static void clearPinResetEmail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.remove(Const.PREF_PIN_RESET_EMAIL);
        e.commit();
    }

    public static void setLoginTryAgainCount(int count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putInt(Const.PREF_LOGIN_TRY_AGAIN_COUNT, count);
        e.commit();
    }

    public static void incrementLoginTryAgainCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        int count = prefs.getInt(Const.PREF_LOGIN_TRY_AGAIN_COUNT, 0);
        Editor e = prefs.edit();
        e.putInt(Const.PREF_LOGIN_TRY_AGAIN_COUNT, ++count);
        e.commit();
    }

    public static int getLoginTryAgainCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getInt(Const.PREF_LOGIN_TRY_AGAIN_COUNT, 0);
    }

    public static void setTestDriveSigninTryAgainCount(int count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putInt(Const.PREF_TEST_DRIVE_SIGNIN_TRY_AGAIN_COUNT, count);
        e.commit();
    }

    public static void incrementTestDriveSigninTryAgainCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        int count = prefs.getInt(Const.PREF_TEST_DRIVE_SIGNIN_TRY_AGAIN_COUNT, 0);
        Editor e = prefs.edit();
        e.putInt(Const.PREF_TEST_DRIVE_SIGNIN_TRY_AGAIN_COUNT, ++count);
        e.commit();
    }

    public static int getTestDriveSigninTryAgainCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getInt(Const.PREF_TEST_DRIVE_SIGNIN_TRY_AGAIN_COUNT, 0);
    }

    public static void setTestDriveRegistrationAttemptCount(int count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putInt(Const.PREF_TEST_DRIVE_REGISTRATION_ATTEMPT_COUNT, count);
        e.commit();
    }

    public static void incrementTestDriveRegistrationAttemptCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        int count = prefs.getInt(Const.PREF_TEST_DRIVE_REGISTRATION_ATTEMPT_COUNT, 0);
        Editor e = prefs.edit();
        e.putInt(Const.PREF_TEST_DRIVE_REGISTRATION_ATTEMPT_COUNT, ++count);
        e.commit();
    }

    public static int getTestDriveRegistrationAttemptCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getInt(Const.PREF_TEST_DRIVE_REGISTRATION_ATTEMPT_COUNT, 0);
    }

    public static void setGoodPasswordMessageString(String goodPasswordMessage) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putString(BaseRequestPasswordReset.GOOD_PASSWORD_DESCRIPTION, goodPasswordMessage);
        e.commit();
    }

    public static String getGoodPasswordMessageString() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getString(BaseRequestPasswordReset.GOOD_PASSWORD_DESCRIPTION, null);
    }

    private boolean isSSOUser() {
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(ConcurCore.getContext());
        return sessionInfo != null
                && com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO.equalsIgnoreCase(sessionInfo
                        .getSignInMethod());
    }

    public static boolean hasShownMinSDKIncreaseMessage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_MIN_SDK_INCREASE_MSG, false);
    }

    public static void setShownMinSDKIncreaseMessage(boolean messageShown) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_MIN_SDK_INCREASE_MSG, messageShown);
        e.commit();
    }

    public static boolean shouldUseNewOcrFeatures() {
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        // return prefs.getBoolean(PREF_OCR_FLAG, false);
        return false;
    }
    
    /*
    public static void setShouldUseNewOcrFeatures(boolean ocrPref) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(PREF_OCR_FLAG, ocrPref);
        e.commit();
    }
	*/

    public static void setUserLoggedOnToExpenseIt(boolean isLoggedIn) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        Editor e = prefs.edit();
        e.putBoolean(Const.PREF_USER_LOGGED_IN_EXPENSE_IT, isLoggedIn);
        e.commit();
    }

    public static boolean isUserLoggedInExpenseIt() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        return prefs.getBoolean(Const.PREF_USER_LOGGED_IN_EXPENSE_IT, false);
    }

    // need to pull this out to some other appropriate class
    public static void doAutoLogin(SharedPreferences prefs, final ConcurCore app) {
        //BaseAsyncResultReceiver autoLoginReceiver = new BaseAsyncResultReceiver(new Handler());
        final Bundle emailLookupBundle;
        final SessionInfo sessionInfo = ConfigUtil.getSessionInfo(app.getApplicationContext());
        boolean disableAutoLogin = prefs.getBoolean(Const.PREF_DISABLE_AUTO_LOGIN, false);
        boolean autoLogin = prefs.getBoolean(Const.PREF_AUTO_LOGIN, false);
        if(disableAutoLogin) {
            autoLogin = false;
        }
        Log.d(Const.LOG_TAG,
                "-------------------------------------------------------------------------------------------autoLogin from prefs = "
                        + autoLogin);
        // If auto-login is enabled and company sign-on is being used, then force autoLogin to 'false'.
        // Company Sign-on auto-login is not currently supported.
        if (autoLogin) {
            String signInMethod = sessionInfo.getSignInMethod();
            Log.d(Const.LOG_TAG,
                    "---------------------------------------------------------------------------------------- signInMethod = "
                            + signInMethod);
            Log.d(Const.LOG_TAG,
                    "---------------------------------------------------------------------------------------- SSO Url = "
                            + sessionInfo.getSSOUrl());
            // NOTE - sometimes loginMethod is null and hence autoLogin is set to false, then AutoLogin is not called. does the code setting emailLookupBundle in the below onRequestSuccess causing this?
            if ("SSO".equalsIgnoreCase(signInMethod) || !(TextUtils.isEmpty(sessionInfo.getSSOUrl()))) {
                autoLogin = false;
            }
        }

        if (autoLogin) {
            if(sessionInfo != null) {
                // create a bundle and persist the value in the bundle
                emailLookupBundle = new Bundle();
                // Set the login id.
                emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY, sessionInfo.getLoginId());
                // Set the server url.
                emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY,
                        (sessionInfo.getServerUrl() != null ?
                                sessionInfo.getServerUrl() :
                                PlatformProperties.getServerAddress()));
                // Set the sign-in method.
                emailLookupBundle
                        .putString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, sessionInfo.getSignInMethod());
                // Set the sso url.
                emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY, sessionInfo.getSSOUrl());
            } else {
                // need to expire the login
                app.expireLogin(true);
                return;
            }

            Log.d(Const.LOG_TAG, "attempting autologin");
            app.autoLoginReceiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {

                                              public void onRequestSuccess(Bundle resultData) {
                                                  Log.d(Const.LOG_TAG,
                                                          ".onRequestSucess ++++++++++++++++++++++++++++++++");

                                                  Log.d(Const.LOG_TAG,
                                                          "attempting to build emailLookupBundle, sessionInfo not null ? "
                                                                  + (sessionInfo != null));
                                                  if(emailLookupBundle ==null) {
                                                      onRequestFail(resultData);
                                                  } else {
                                                      UserAndSessionInfoUtil
                                                              .updateUserAndSessionInfo(ConcurCore.getContext(),
                                                                      emailLookupBundle);
                                                  }
                                              }

                                              public void onRequestFail(Bundle resultData) {

                                                      Log.d(Const.LOG_TAG, "expire login as autoLogin is disabled");
                                                      // need to expire the login
                                                      app.expireLogin(true);

                                              }

                                              public void onRequestCancel(Bundle resultData) {

                                                      Log.d(Const.LOG_TAG, "expire login as autoLogin is disabled");
                                                      // need to expire the login
                                                      app.expireLogin(true);

                                              }

                                              public void cleanup() {
                                              }

                                          }

            );

            UserAndSessionInfoUtil.setServerAddress(PlatformProperties.getServerAddress());

            // Attempt to authenticate
            // Re-entry to the UI will be via handleMessage() below
            // perform an full AutoLoginRequest in order to get
            // all the user roles, site settings, car configs, etc. and all that other good stuff.
            AutoLoginRequestTask autoLoginRequestTask = new AutoLoginRequestTask(ConcurCore.getContext(), 0,
                    app.autoLoginReceiver, Locale.getDefault());
            autoLoginRequestTask.execute();
        } else {

                Log.d(Const.LOG_TAG,
                        "---------------------------------------------------------------------------expire login as autoLogin is disabled");
                // need to expire the login, will this take back to log in screen?
                app.expireLogin(true);

        }
    }
}
