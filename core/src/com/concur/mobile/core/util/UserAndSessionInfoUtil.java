package com.concur.mobile.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.net.ExpenseItServerUtil;
import com.concur.mobile.core.util.net.SiteSettings;
import com.concur.mobile.platform.authentication.*;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.platform.ExpenseItProperties;
import com.concur.mobile.platform.service.MWSPlatformManager;
import com.concur.platform.PlatformProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAndSessionInfoUtil {

    public static String CLS_TAG = UserAndSessionInfoUtil.class.getName();

    public static void updateUserAndSessionInfo(Context ctx, Bundle emailLookUpBundle) {
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(ctx);

        // MOB-21232 - There can be a race condition where session is being renewed/re-authenticated
        // and then the user logs out. In that case, the SessionInfo can be null, so just return.
        if (sessionInfo == null) {
            Log.w(Const.LOG_TAG,
                    CLS_TAG + ".updateUserAndSessionInfo() - SessionInfo is null! Perhaps the user has logged out.");
            return;
        }

        // Save the login response information.
        Preferences.setSessionInfo(PreferenceManager.getDefaultSharedPreferences(ctx), sessionInfo.getAccessToken(),
                sessionInfo.getSessionId(), sessionInfo.getSessionTimeout(), sessionInfo.getSessionExpirationTime());

        List<SiteSettingInfo> siteSettings = ConfigUtil.getSiteSettingInfo(ctx, sessionInfo.getUserId());
        SiteSettings.initWithSiteSetting(siteSettings);

        final Permissions permissions = ConfigUtil.getPermissionsInfo(ctx, sessionInfo.getUserId());

        UserInfo userInfo = ConfigUtil.getUserInfo(ctx, sessionInfo.getUserId());
        final Map<String, Object> parseMap = new HashMap<String, Object>();

        // MOB-19313 Add Session Info stuff.
        // NOTE: There was a bug in 9.11.x because the AccessToken was being over-written
        // in the call to ConcurCore.saveLoginResponsePreferences().
        parseMap.put(Const.LR_ACCESS_TOKEN, sessionInfo.getAccessToken());
        parseMap.put(Const.LR_SESSION_DURATION, sessionInfo.getSessionTimeout());
        parseMap.put(Const.LR_SESSION_EXPIRATION, sessionInfo.getSessionExpirationTime());

        // Set whether or not user account is configure for location check in.
        final SiteSettings ssInstance = SiteSettings.getInstance();
        boolean isLocationCheckInSupported = ssInstance.isLocationCheckInEnabled();
        parseMap.put(Const.LR_SITE_SETTINGS_LOCACTION_CHECK_IN, isLocationCheckInSupported);
        // Add other site setting information to the response.
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
        parseMap.put(Const.LR_SITE_SETTINGS_MOBILE_HAS_FIXED_TA, ssInstance.hasFixedTa());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_SPDY, ssInstance.isSpdyEnabled());

        parseMap.put(Const.LR_PERMISSIONS_TR, permissions.getAreasPermissions().hasTravelRequest);
        parseMap.put(Const.LR_PERMISSIONS_TR_USER, permissions.getTravelRequestPermissions().isRequestUser);
        parseMap.put(Const.LR_PERMISSIONS_TR_APPROVER, permissions.getTravelRequestPermissions().isRequestApprover);

        parseMap.put(Const.LR_TRAVEL_PROFILE_STATUS, userInfo.getProfileStatus());
        parseMap.put(Const.LR_REQUIRED_CUSTOM_FIELDS, userInfo.hasRequiredCustomFields());
        parseMap.put(Const.LR_DISABLE_AUTO_LOGIN, userInfo.getDisableAutoLogin());
        parseMap.put(Const.LR_USER_ID, userInfo.getUserId());
        parseMap.put(Const.LR_ROLES, userInfo.getRolesMobile());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_CONDITIONAL_FIELD_EVALUATION,
                ssInstance.isConditionalFieldEvaluationEnabled());
        String userCrnCode = userInfo.getUserCurrencyCode();
        if (userCrnCode != null) {
            parseMap.put(Const.LR_USER_CRN_CODE, userCrnCode);
        }
        String entityType = userInfo.getEntityType();
        if (entityType != null) {
            parseMap.put(Const.LR_ENTITY_TYPE, entityType);
        } else {
            parseMap.put(Const.LR_ENTITY_TYPE, Const.ENTITY_TYPE_CORPORATE);
        }
        parseMap.put(Const.LR_PRODUCT_OFFERING, userInfo.getProductOffering());
        // parseMap.put(Const.LR_ACCOUNT_EXPIRATION_DATE, accountExpirationDate);
        String userCompanyName = userInfo.getContactCompanyName();
        if (userCompanyName != null) {
            parseMap.put(Const.LR_CONTACT_COMPANY_NAME, userCompanyName);
        }

        parseMap.put(Const.LR_SITE_SETTINGS_SHOW_JARVIS_HOTEL_UI, ssInstance.shouldShowHotelJarvisUI());

        if (emailLookUpBundle != null) {
            // Get the login id.
            String loginId = emailLookUpBundle.getString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY);
            // Get the server url.
            String serverUrl = emailLookUpBundle.getString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY);
            if (serverUrl == null) {
                serverUrl = PlatformProperties.getServerAddress();
            }
            // Get the sign-in method. {Values: Password/MobilePin/SSO}
            String signInMethod = emailLookUpBundle.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
            // Get the sso url.
            String ssoUrl = emailLookUpBundle.getString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY);
            // set server address to preferences.
            parseMap.put(Const.LR_SERVER_URL, serverUrl);
            // set emailLookupbundle info to SessionInfo
            sessionInfo.setLoginId(loginId);
            sessionInfo.setServerUrl(serverUrl);
            sessionInfo.setSSOUrl(ssoUrl);
            sessionInfo.setSignInMethod(signInMethod);
            // update session info so you will get it once everything is updated.
            ConfigUtil.updateSessionInfo(ctx, sessionInfo);

            //
            setLoginRequestFields(ctx.getApplicationContext(), sessionInfo);
        }
        ConcurCore.saveLoginResponsePreferences(sessionInfo.getSessionId(), (ConcurCore) ctx.getApplicationContext(),
                parseMap);

    }

    public static void setServerAddress(String serverUrl) {
        // set server url to preferences and PlatformProperties
        if (!TextUtils.isEmpty(serverUrl)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            Editor e = prefs.edit();
            e.putString(Const.PREF_MWS_ADDRESS, serverUrl);
            e.commit();
            PlatformProperties.setServerAddress(serverUrl);
            Pair<String, String> expenseItServerAddress = ExpenseItServerUtil.getMatchingConcurExpenseItServer(serverUrl);
            ExpenseItProperties.setServerAddress(expenseItServerAddress.first);
            ExpenseItProperties.setConsumerKey(expenseItServerAddress.second);
        }
    }

    // will retrieve the required fields to use in PPLoginRequestTask request for auto login scenario
    public static void setLoginRequestFields(Context context, SessionInfo sessionInfo) {

        String loginId = sessionInfo.getLoginId();
        String email= sessionInfo.getEmail();
        String serverURL = sessionInfo.getServerUrl();
        String ssoURL = sessionInfo.getSSOUrl();
        String signInMethod = sessionInfo.getSignInMethod();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        MWSPlatformManager platformManager = (MWSPlatformManager) PlatformProperties.getPlatformSessionManager();

        // set auto login
        boolean disableAutoLogin = prefs.getBoolean(Const.PREF_DISABLE_AUTO_LOGIN, false);
        boolean autoLogin = prefs.getBoolean(Const.PREF_AUTO_LOGIN, false);
        if (disableAutoLogin) {
            autoLogin = false;
        }

        // If auto-login is enabled and company sign-on is being used, then force autoLogin to 'false'.
        // Company Sign-on auto-login is not currently supported.
        if(autoLogin) {
            if ("SSO".equalsIgnoreCase(signInMethod) || !(TextUtils.isEmpty(ssoURL))) {
                platformManager.setAutoLoginEnabled(false);
                platformManager.setAuthenticationType(MWSPlatformManager.AuthenticationType.SSO);
            } else {
                platformManager.setAutoLoginEnabled(true);
                if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD)) {
                    platformManager.setAuthenticationType(MWSPlatformManager.AuthenticationType.Password);
                } else {
                    platformManager.setAuthenticationType(MWSPlatformManager.AuthenticationType.MobilePassword);
                }

                // set other login information into Platformmanager so that you can retrieve it during the AutoLoginRequestTask
                platformManager.setLoginId(loginId);
                platformManager.setEmail(email);
                platformManager.setServerURL(serverURL);
                platformManager.setSsoURL(ssoURL);

            }
        } else {
            platformManager.setAutoLoginEnabled(false);
        }

        // Update the platform session manager.
        PlatformProperties.setPlatformSessionManager(platformManager);
    }
}
