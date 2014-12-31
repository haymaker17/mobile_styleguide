/**
 * 
 */
package com.concur.mobile.platform.authentication.test;

import java.util.List;

import org.junit.Assert;

import android.content.Context;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.authentication.SiteSettingInfo;
import com.concur.mobile.platform.authentication.UserInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;

/**
 * Provides a class to verify a <code>LoginResult</code> object against data stored in the content provider.
 * 
 * @author andrewk
 */
public class VerifyLoginResult {

    private static final String CLS_TAG = "VerifyLoginResult";

    /**
     * Will verify session information in <code>loginResult</code> against session information stored in the config content
     * provider optionally ignoring session id.
     * 
     * @param context
     *            contains an application context.
     * @param loginResult
     *            contains the login result.
     * @param ignoreSessionId
     *            contains whether or not to ignore session ID information.
     */
    public void verifySessionInfo(Context context, LoginResult loginResult) throws Exception {

        final String MTAG = CLS_TAG + ".verifySessionInfo";

        // Verify Session Information.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        if (sessInfo != null) {

            // Access token.
            Assert.assertEquals(MTAG + ": access token", loginResult.accessToken.key, sessInfo.getAccessToken());

            // Authentication type.
            Assert.assertEquals(MTAG + ": authentication type", loginResult.authenticationType,
                    sessInfo.getAuthenticationType());

            // Session ID
            Assert.assertEquals(MTAG + ": session ID", loginResult.session.id, sessInfo.getSessionId());

            // Session Timeout
            Assert.assertEquals(MTAG + ": session timeout", loginResult.session.timeout, sessInfo.getSessionTimeout());

            // User ID
            Assert.assertEquals(MTAG + ": user ID", loginResult.userId, sessInfo.getUserId());

        } else {
            throw new Exception(CLS_TAG + "." + MTAG + ": session info is null!");
        }

    }

    /**
     * Will verify login response information stored in the config content provider against information stored in
     * <code>loginResult</code>.
     * 
     * @param context
     *            contains a reference to the application context.
     * @param loginResult
     *            contains a reference to a login response.
     * @throws Exception
     *             throws an exception if the stored login result data does not match <code>loginResult</code>.
     */
    public void verify(Context context, LoginResult loginResult) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        // Verify Session Information.
        verifySessionInfo(context, loginResult);

        // Verify User Information.
        String matcherQuotedReplacementStr = loginResult.userId;
        UserInfo userInfo = ConfigUtil.getUserInfo(context, matcherQuotedReplacementStr);
        if (userInfo != null) {

            // Entity type.
            Assert.assertEquals(MTAG + ": entity type", loginResult.entityType, userInfo.getEntityType());

            // Expense country code.
            Assert.assertEquals(MTAG + ": expense country code", loginResult.expenseCountryCode,
                    userInfo.getExpenseCountryCode());

            // Has required custom fields.
            Assert.assertEquals(MTAG + ": has required custom fields", loginResult.hasRequiredCustomFields,
                    userInfo.hasRequiredCustomFields());

            // Pin expiration date.
            Assert.assertEquals(MTAG + ": pin expiration date", loginResult.pinExpirationDate,
                    userInfo.getPinExpirationDate());

            // Product offering.
            Assert.assertEquals(MTAG + ": product offering", loginResult.productOffering, userInfo.getProductOffering());

            // Profile status.
            Assert.assertEquals(MTAG + ": profile status", loginResult.profileStatus, userInfo.getProfileStatus());

            // Roles mobile.
            Assert.assertEquals(MTAG + ": roles mobile", loginResult.rolesMobile, userInfo.getRolesMobile());

            // Contact company name.
            Assert.assertEquals(MTAG + ": contact company name", loginResult.userContact.companyName,
                    userInfo.getContactCompanyName());

            // Contact email.
            Assert.assertEquals(MTAG + ": contact email", loginResult.userContact.email, userInfo.getContactEmail());

            // Contact first name.
            Assert.assertEquals(MTAG + ": contact first name", loginResult.userContact.firstName,
                    userInfo.getContactFirstName());

            // Contact last name.
            Assert.assertEquals(MTAG + ": contact last name", loginResult.userContact.lastName,
                    userInfo.getContactLastName());

            // Contact middle initial.
            Assert.assertEquals(MTAG + ": contact middle initial", loginResult.userContact.middleInitial,
                    userInfo.getContactMiddleInitial());

            // User currency code.
            Assert.assertEquals(MTAG + ": user currency code", loginResult.userCurrencyCode,
                    userInfo.getUserCurrencyCode());

            // User id.
            Assert.assertEquals(MTAG + ": user currency code", loginResult.userId, userInfo.getUserId());

        } else {
            throw new Exception(CLS_TAG + "." + MTAG + ": user info is null!");
        }

        // Verify site setting information.
        List<SiteSettingInfo> siteSettingInfos = ConfigUtil.getSiteSettingInfo(context, matcherQuotedReplacementStr);
        if (siteSettingInfos != null && loginResult.siteSettings != null) {
            // N-squared search!
            for (SiteSetting siteSetting : loginResult.siteSettings) {
                boolean foundSetting = false;
                for (SiteSettingInfo ssInfo : siteSettingInfos) {
                    if (siteSetting.name.equals(ssInfo.getName())) {
                        foundSetting = true;
                        Assert.assertEquals(MTAG + ": site setting type", siteSetting.type, ssInfo.getType());
                        Assert.assertEquals(MTAG + ": site setting value", siteSetting.value, ssInfo.getValue());
                        Assert.assertEquals(MTAG + ": site setting user id", loginResult.userId, ssInfo.getUserId());
                    }
                }
                Assert.assertTrue(MTAG + ": found site setting", foundSetting);
            }
        } else {
            // If both are 'null', then the following assertion will be considered equals.
            Assert.assertEquals(MTAG + ": site settings", loginResult.siteSettings, siteSettingInfos);
        }
    }
}
