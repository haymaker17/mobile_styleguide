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

public class GroupConfigurationTaskTest extends AsyncRequestTest {

    private static final String PREF_USER_ID = "pref_saved_user_id";

    private RequestGroupConfigurationCache groupConfigurationCache = null;

    /*@Test */
    public void doTest() throws Exception {

        // setup
        if (groupConfigurationCache == null) {
            groupConfigurationCache = new RequestGroupConfigurationCache();
        }

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "request/GroupConfigurationResponse.json");
        }

        final BaseAsyncResultReceiver resListener = new BaseAsyncResultReceiver(getHander());
        resListener.setListener(new AsyncReplyListenerImpl());

        final RequestTask task = new RequestTask(PlatformTestApplication.getApplication(),
                1, resListener, ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.GROUP_CONFIGURATIONS,
                ConnectHelper.Action.LIST, null);

        if (populateTaskResult(task)) {
            // Verify the result.

            // Grab the response.
            final String response = getResponseString(task);
            Assert.assertNotNull("Request response is null", response);

            // --- parse the configurations received
            List<RequestGroupConfiguration> configurationList;
            try {
                configurationList = RequestParser.parseRequestGroupConfigurationsResponse(
                        result.resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
                Assert.assertNotNull("Response is empty", configurationList);
                Assert.assertTrue(configurationList.size() > 0);
                fillCacheTest(configurationList);
            } catch (Exception e) {
                Assert.fail("Response parsing failed.");
            }

        }
    }

    /**
     * Same behavior as what's done on RequestListActivity class
     * TODO: create a dedicated method somewhere suited and call it instead.
     *
     * @param configurationList
     */
    private void fillCacheTest(List<RequestGroupConfiguration> configurationList) {
        groupConfigurationCache.clear();
        // Perform the verification & store the group Configuration.
        if (configurationList != null && configurationList.size() > 0) {
            // --- add configurations to cache if there is any
            final String userId = getUserId();
            for (RequestGroupConfiguration rgc : configurationList) {
                groupConfigurationCache.addValue(userId, rgc);
            }
            Assert.assertTrue(groupConfigurationCache.hasCachedValues());
        }
    }

    public String getUserId() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(PlatformTestApplication.getApplication());
        final String userId = prefs.getString(PREF_USER_ID, null);
        return userId;
    }
}
