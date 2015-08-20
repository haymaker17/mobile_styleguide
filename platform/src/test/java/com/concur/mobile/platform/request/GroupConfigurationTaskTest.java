package com.concur.mobile.platform.request;

/**
 * Created by OlivierB on 10/08/2015.
 */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.task.RequestTask;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.List;

//@RunWith(RobolectricGradleTestRunner.class) @Config(constants = BuildConfig.class, sdk = 21)
//@RunWith(ConcurPlatformTestRunner.class)
//@Config(manifest = "src/test/AndroidManifest.xml", assetDir = "assets", constants = BuildConfig.class, sdk = 21)
public class GroupConfigurationTaskTest
        extends AsyncRequestTest /*implements BaseAsyncRequestTask.AsyncReplyListener*/ {

    private static final String CLS_TAG = GroupConfigurationTaskTest.class.getName();
    private static final String PREF_USER_ID = "pref_saved_user_id";
    // --- Fill those with your VM params
    private static final String TEST_SRV_URL = "dev://172.27.64.163";
    private static final String TEST_LOGIN_ID = "acsontos@outtask.com";
    private static final String TEST_LOGIN_PWD = "1111";

    private RequestGroupConfigurationCache groupConfigurationCache = null;
    private final RequestParser requestParser = new RequestParser();

    /*@Before public void setUp() throws Exception {
        // --- Add -Duse.mock.server="false" to your VM params in Run/Debug Configuration to disable server mock
        if (System.getProperty(Const.USE_MOCK_SERVER, Boolean.TRUE.toString()).equals(Boolean.FALSE.toString())) {
            System.setProperty(Const.SERVER_ADDRESS, TEST_SRV_URL);
            System.setProperty(Const.PPLOGIN_ID, TEST_LOGIN_ID);
            System.setProperty(Const.PPLOGIN_PIN_PASSWORD, TEST_LOGIN_PWD);
        }

        new PlatformTestSuite().doAutoLogin();
    }*/

    /*@Test */
    public void doTest() throws Exception {

        // setup
        if (groupConfigurationCache == null) {
            groupConfigurationCache = new RequestGroupConfigurationCache();
        } else {
            groupConfigurationCache.clear();
        }

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "request/GroupConfigurationResponse.json");
        }

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication()/*RequestListActivity.this*/,
                1, resListener, ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.GROUP_CONFIGURATIONS,
                ConnectHelper.Action.LIST, null);

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            // --- parse the configurations received
            List<RequestGroupConfiguration> rgcc = null;
            try {
                rgcc = requestParser.parseRequestGroupConfigurationsResponse(
                        result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", rgcc);
                Assert.assertTrue(rgcc.size() > 0);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }

            // Perform the verification & store the group Configuration.
            if (rgcc != null && rgcc.size() > 0) {
                // --- add configurations to cache if there is any
                final String userId = getUserId();
                for (RequestGroupConfiguration rgc : rgcc) {
                    groupConfigurationCache.addValue(userId, rgc);
                }
                Assert.assertTrue(groupConfigurationCache.hasCachedValues());
            }
        }
    }

    public String getUserId() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(PlatformTestApplication.getApplication());
        final String userId = prefs.getString(PREF_USER_ID, null);
        return userId;
    }

    public RequestGroupConfigurationCache getGroupConfigurationCache() {
        return groupConfigurationCache;
    }
}
