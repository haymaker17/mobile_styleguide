package com.concur.mobile.core.util.net;

import java.util.Calendar;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

public class LoginResponseParser {

    protected static String getNodeValue(Node node) {

        String val = null;
        if (node != null) {
            Node textNode = node.getFirstChild();
            if (textNode != null) {
                val = textNode.getNodeValue();
            }
        }

        return val;
    }

    public static HashMap<String, Object> parse(Document doc) {
        HashMap<String, Object> parseMap = new HashMap<String, Object>();

        // Check for disabling auto-login
        NodeList autoLoginNode = doc.getElementsByTagName("DisableAutoLogin");
        String autoLoginValue = getNodeValue(autoLoginNode.item(0));
        boolean disableAutoLogin = Parse.safeParseBoolean(autoLoginValue);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
        if (disableAutoLogin) {
            Preferences.disableAutoLogin(prefs);
        } else {
            Preferences.enableAutoLogin(prefs);
        }

        // Retrieve session death.
        int sessionDuration = 120;
        Calendar sessionDeath = null;
        NodeList timeoutNode = doc.getElementsByTagName("TimeOut");
        if (timeoutNode != null) {
            if (timeoutNode.item(0) != null) {
                String timeout = timeoutNode.item(0).getFirstChild().getNodeValue();
                if (timeout != null) {
                    try {
                        // Initialize session death. This will be updated in the service
                        // whenever a call is made.
                        sessionDuration = Integer.parseInt(timeout);
                        sessionDeath = Calendar.getInstance();
                        sessionDeath.add(Calendar.MINUTE, sessionDuration);
                    } catch (NumberFormatException numFormExc) {
                        sessionDuration = 120;
                    }
                }
            }
        }

        // If no session expiration time provided, then default to 120 minutes.
        if (sessionDeath == null) {
            sessionDeath = Calendar.getInstance();
            sessionDeath.add(Calendar.MINUTE, sessionDuration);
        }

        // Grab the required custom fields boolean value.
        NodeList reqCustomFieldsNode = doc.getElementsByTagName("HasRequiredCustomFields");
        Boolean requiredCustomFields = false;
        if (reqCustomFieldsNode != null) {
            if (reqCustomFieldsNode.item(0) != null) {
                requiredCustomFields = Parse.safeParseBoolean(reqCustomFieldsNode.item(0).getFirstChild()
                        .getNodeValue());
            }
        }

        // product offering
        NodeList product = doc.getElementsByTagName("ProductOffering");
        String productOffering = null;
        if (product != null) {
            Node item = product.item(0);
            if (item != null) {
                requiredCustomFields = Parse.safeParseBoolean(reqCustomFieldsNode.item(0).getFirstChild()
                        .getNodeValue());
                productOffering = item.getFirstChild().getNodeValue();
            }
        }

        // Test Drive user expiration date
        NodeList expirationNode = doc.getElementsByTagName("AccountExpirationDate");
        String accountExpirationDate = (expirationNode != null ? getNodeValue(expirationNode.item(0)) : null);

        // Grab the profile status value.
        NodeList travelProfileStatusNode = doc.getElementsByTagName("ProfileStatus");
        // Default to complete profile.
        Integer travelProfileStatus = Const.TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA;
        if (travelProfileStatusNode != null) {
            if (travelProfileStatusNode.item(0) != null) {
                travelProfileStatus = Parse.safeParseInteger(travelProfileStatusNode.item(0).getFirstChild()
                        .getNodeValue());
            }
        }

        // Grab the A/B Test ID, if any.
        NodeList abTestIDNode = doc.getElementsByTagName("ABTestID");
        String abTestID = null;
        if (abTestIDNode != null) {
            Node node = abTestIDNode.item(0);
            if (node != null) {
                node = node.getFirstChild();
                if (node != null) {
                    abTestID = node.getNodeValue();
                }
            }
        }
        // Grab the A/B Test Expiration, if any.
        NodeList abTestExpNode = doc.getElementsByTagName("ABTestExp");
        String abTestExp = null;
        if (abTestExpNode != null) {
            Node node = abTestExpNode.item(0);
            if (node != null) {
                node = node.getFirstChild();
                if (node != null) {
                    abTestExp = node.getNodeValue();
                }
            }
        }

        String serverUrl = null;
        NodeList serverUrlNode = doc.getElementsByTagName("ServerUrl");
        if (serverUrlNode != null) {
            if (serverUrlNode.item(0) != null) {
                serverUrl = serverUrlNode.item(0).getFirstChild().getNodeValue();
            }
        }

        NodeList idNode = doc.getElementsByTagName("ID");
        String sessionId = getNodeValue(idNode.item(0));
        NodeList userIdNode = doc.getElementsByTagName("UserId");
        String userId = getNodeValue(userIdNode.item(0));
        NodeList rolesNode = doc.getElementsByTagName("RolesMobile");
        String roles = getNodeValue(rolesNode.item(0));
        NodeList userCrnNode = doc.getElementsByTagName("UserCrnCode");
        String userCrnCode = getNodeValue(userCrnNode.item(0));
        NodeList entityTypeNode = doc.getElementsByTagName("EntityType");
        String entityType = getNodeValue(entityTypeNode.item(0));

        // NeedSafeHarborAgreement for gov user.
        NodeList nshAgreeNode = doc.getElementsByTagName("NeedSafeHarborAgreement");
        String nshAgree = getNodeValue(nshAgreeNode.item(0));

        // Initialize user SiteSettings.
        NodeList siteSettings = doc.getElementsByTagName("SiteSettings");
        SiteSettings.init(siteSettings.item(0));

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
        parseMap.put(Const.LR_SITE_SETTINGS_MOBILE_HAS_TRAVEL_ALLOWANCE_FIXED, ssInstance.hasTravelAllowanceFixed());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_CONDITIONAL_FIELD_EVALUATION,
                ssInstance.isConditionalFieldEvaluationEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_SPDY, ssInstance.isSpdyEnabled());
        parseMap.put(Const.LR_SITE_SETTINGS_ENABLE_EXPENSE_IT_EXPERIENCE, ssInstance.isExpenseItExperienceEnabled());
        if (abTestID != null) {
            parseMap.put(Const.LR_ABTEST_ID, abTestID);
        }
        if (abTestExp != null) {
            parseMap.put(Const.LR_ABTEST_EXP, abTestExp);
        }
        if (serverUrl != null) {
            parseMap.put(Const.LR_SERVER_URL, serverUrl);
        }
        parseMap.put(Const.LR_TRAVEL_PROFILE_STATUS, travelProfileStatus);
        parseMap.put(Const.LR_REQUIRED_CUSTOM_FIELDS, requiredCustomFields);
        parseMap.put(Const.LR_SESSION_ID, sessionId);
        parseMap.put(Const.LR_USER_ID, userId);
        parseMap.put(Const.LR_ROLES, roles);
        parseMap.put(Const.LR_SESSION_DURATION, sessionDuration);
        parseMap.put(Const.LR_SESSION_EXPIRATION, sessionDeath.getTimeInMillis());
        if (userCrnCode != null) {
            parseMap.put(Const.LR_USER_CRN_CODE, userCrnCode);
        }
        if (entityType != null) {
            parseMap.put(Const.LR_ENTITY_TYPE, entityType);
        } else {
            parseMap.put(Const.LR_ENTITY_TYPE, Const.ENTITY_TYPE_CORPORATE);
        }
        parseMap.put(Const.NEED_SAFE_HARBOR_AGREEMENT, nshAgree);
        parseMap.put(Const.LR_PRODUCT_OFFERING, productOffering);
        parseMap.put(Const.LR_ACCOUNT_EXPIRATION_DATE, accountExpirationDate);
        return parseMap;
    }

    public static HashMap<String, Object> parseV2(Document doc) {
        HashMap<String, Object> parseMap = new HashMap<String, Object>();

        parseMap.putAll(parse(doc));

        NodeList node = doc.getElementsByTagName("AccessTokenKey");
        String token = getNodeValue(node.item(0));
        node = doc.getElementsByTagName("AccessTokenSecret");
        String tokenSecret = getNodeValue(node.item(0));
        node = doc.getElementsByTagName("AuthenticationType");
        String authType = getNodeValue(node.item(0));

        parseMap.put(Const.LR_ACCESS_TOKEN, token);
        parseMap.put(Const.LR_ACCESS_TOKEN_SECRET, tokenSecret);
        parseMap.put(Const.LR_AUTHENTICATION_TYPE, authType);

        // If the user logged in using password,
        // we should *not* save it!
        if (authType != null && authType.equalsIgnoreCase("Password")) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext()
                    .getApplicationContext());
            Preferences.savePin(prefs, "");
        }

        return parseMap;
    }
}
