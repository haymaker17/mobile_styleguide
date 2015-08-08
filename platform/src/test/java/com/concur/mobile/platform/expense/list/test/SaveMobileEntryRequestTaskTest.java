package com.concur.mobile.platform.expense.list.test;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.list.MobileEntry;
import com.concur.mobile.platform.expense.list.SaveMobileEntryRequestTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.Const;
import com.concur.mobile.platform.test.PlatformTestApplication;

public class SaveMobileEntryRequestTaskTest extends AsyncRequestTest {

    private static final String CLS_TAG = "SaveMobileEntryRequestTaskTest";

    private static final boolean DEBUG = false;

    /**
     * Will perform the test throwing an exception if the test fails.
     * 
     * @throws Exception
     *             throws an exception if the test fails.
     */
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        // Set the mock response if the mock server is being used.
        if (PlatformTestApplication.useMockServer()) {
            // Set the mock response for the test.
            setMockResponse(mockServer, HttpStatus.SC_OK, "expense/SaveMobileEntryResponse.xml");
        }

        // Initiate the save mobile entry request.
        BaseAsyncResultReceiver saveMEReplyReceiver = new BaseAsyncResultReceiver(getHander());
        saveMEReplyReceiver.setListener(new AsyncReplyListenerImpl());

        MobileEntry mobileEntry = new MobileEntry();
        Calendar now = Calendar.getInstance();
        Calendar datePickerDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        datePickerDate.clear();
        datePickerDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        mobileEntry.setTransactionDate(datePickerDate);
        mobileEntry.setLocationName("Seattle, Washington");
        mobileEntry.setCrnCode("USD");

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);

        Uri mobileEntryUri = null;

        try {
            // Construct a local DAO object.
            if (mobileEntry.update(context, sessInfo.getUserId())) {
                mobileEntryUri = mobileEntry.getContentURI(context);
            }

            Assert.assertNotNull("failed to save new mobile entry in sqlite", mobileEntryUri);

            SaveMobileEntryRequestTask reqTask = new SaveMobileEntryRequestTask(context, 1, saveMEReplyReceiver,
                    mobileEntryUri, false);
            reqTask.setRetainResponse(true);

            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launching the request.");
            }

            // Launch the request.
            launchRequest(reqTask);

            if (DEBUG) {
                ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: launched the request.");
            }

            try {
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: waiting for result.");
                }
                // Wait for the result.
                waitForResult();
                if (DEBUG) {
                    ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: obtained result.");
                }
            } catch (InterruptedException intExc) {
                ShadowLog.e(Const.LOG_TAG, CLS_TAG + ".doTest: interrupted while acquiring login result.");
                result.resultCode = BaseAsyncRequestTask.RESULT_CANCEL;
            }

            // Examine the result.
            if (result != null) {

                // Verify result code.
                verifyExpectedResultCode(CLS_TAG);

                switch (result.resultCode) {
                case BaseAsyncRequestTask.RESULT_CANCEL: {
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result cancelled.");
                    }
                    break;
                }
                case BaseAsyncRequestTask.RESULT_ERROR: {
                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result error.");
                    }
                    break;
                }
                case BaseAsyncRequestTask.RESULT_OK: {

                    // Verify the result.

                    // Grab the response.
                    String response = getResponseString(reqTask);
                    Assert.assertNotNull("request response is null", response);

                    // Use SimpleXML framework to deserialize the response.
                    Serializer serializer = new Persister();
                    SaveMobileEntryResponse saveMEResponse = serializer.read(SaveMobileEntryResponse.class, response,
                            false);

                    if (saveMEResponse != null) {
                        // Convert any mobile entry transaction dates/boolean values.
                        if (saveMEResponse.meKey != null) {

                        }
                    }

                    final String MTAG = CLS_TAG + ".verify";
                    Assert.assertNotNull(MTAG + ": reponse is null", saveMEResponse);
                    Assert.assertNotNull(MTAG + " mekey is null", saveMEResponse.meKey);

                    // Perform the verification.
                    // Verify meKey of mobile entry.
                    MobileEntry updatedME = new MobileEntry(context, mobileEntryUri);

                    Assert.assertEquals(MTAG + ": mobile entry meKey", updatedME.getMeKey(), saveMEResponse.meKey);

                    if (DEBUG) {
                        ShadowLog.d(Const.LOG_TAG, CLS_TAG + ".doTest: result ok.");
                    }
                    break;
                }
                }

            }
        } catch (Exception e) {
        } finally {
            // Clean up the mobile entry
            if (mobileEntryUri != null) {
                try {
                    ContentResolver resolver = context.getContentResolver();
                    resolver.delete(mobileEntryUri, null, null);
                } catch (Exception e) {
                }
            }
        }
    }
}
