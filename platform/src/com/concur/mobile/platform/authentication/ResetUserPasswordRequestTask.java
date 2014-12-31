package com.concur.mobile.platform.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.XmlUtil;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purposes of sending a reset user password request.
 * 
 * @author andrewk
 */
public class ResetUserPasswordRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "ResetUserPasswordRequestTask";

    /**
     * Contains the result data bundle key for obtaining the text value "login id" from the response.
     */
    public static final String EXTRA_LOGIN_ID_KEY = "reset.user.password.login.id";

    /**
     * Contains the result data bundle key for obtaining the integer value "minimum pin length" from the response.
     */
    public static final String EXTRA_MINIMUM_PIN_LENGTH_KEY = "reset.user.password.min.pin.length";

    /**
     * Contains the result data bundle key for obtaining the boolean value for "requires mixed case" from the response.
     */
    public static final String EXTRA_REQUIRES_MIXED_CASE_KEY = "reset.user.password.requires.mixed.case";

    // Contains the service end-point for the <code>ResetUserPassword</code> MWS call.
    private static final String SERVICE_END_POINT = "/mobile/MobileSession/ResetUserPassword";

    /**
     * Contains the email address.
     */
    protected String email;

    /**
     * Contains part A of the key.
     */
    protected String keyPartA;

    /**
     * Contains part B of the key.
     */
    protected String keyPartB;

    /**
     * Contains the password.
     */
    protected String password;

    /**
     * Contains the parsed results.
     */
    protected ResetUserPasswordResultParser response;

    /**
     * Constructs an instance of <code>ResetUserPasswordRequestTask</code> for the purpose of requesting a password reset.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the receiver.
     * @param locale
     *            contains a reference to the locale.
     * @param email
     *            contains the end-users email address.
     */

    public ResetUserPasswordRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String email,
            String keyPartA, String keyPartB, String password) {

        super(context, requestId, receiver);

        final String MTAG = CLS_TAG + ".<init>: ";

        try {

            Assert.assertNotNull(MTAG + "email is null.", email);
            Assert.assertNotNull(MTAG + "keyPartA is null.", keyPartA);
            Assert.assertNotNull(MTAG + "keyPartB is null.", keyPartB);
            Assert.assertNotNull(MTAG + "password is null.", password);

            this.email = email;
            this.keyPartA = keyPartA;
            this.keyPartB = keyPartB;
            this.password = password;
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, MTAG + afe.getMessage());
            throw afe;
        }
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    protected String getPostBody() {
        String postBody = null;

        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<ResetUserPassword>");
        XmlUtil.addXmlElement(strBldr, "Email", email);
        XmlUtil.addXmlElement(strBldr, "KeyPartA", keyPartA);
        XmlUtil.addXmlElement(strBldr, "KeyPartB", keyPartB);
        XmlUtil.addXmlElement(strBldr, "Password", password);
        strBldr.append("</ResetUserPassword>");
        postBody = strBldr.toString();

        return postBody;
    }

    @Override
    protected boolean requiresSessionId() {
        return false;
    }

    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                response = new ResetUserPasswordResultParser();
                parser.registerParser(response, ResetUserPasswordResultParser.TAG_RESET_USER_PASSWORD_RESULT);

                // Parse.
                parser.parse();
            } else {
                result = BaseAsyncRequestTask.RESULT_ERROR;
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to construct common parser!");
            }
        } catch (XmlPullParserException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } catch (IOException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                }
            }
        }
        return result;
    }

    @Override
    protected int onPostParse() {

        int result = super.onPostParse();
        if (result == RESULT_OK) {
            if (response != null) {
                setResetUserPasswordResponseIntoDataBundle();
                if (response.status != null && !response.status) {
                    result = RESULT_ERROR;
                }
            } else {
                result = RESULT_ERROR;
            }
        }
        return result;
    }

    /**
     * Will load into the result data bundle the outcome of the response.
     */
    private void setResetUserPasswordResponseIntoDataBundle() {

        // Add any login id.
        if (response.loginId != null) {
            resultData.putString(EXTRA_LOGIN_ID_KEY, response.loginId);
        }

        // Add min length.
        if (response.minLength != null) {
            resultData.putInt(EXTRA_MINIMUM_PIN_LENGTH_KEY, response.minLength);
        }

        // Add requires mixed case.
        if (response.requiresMixedCase != null) {
            resultData.putBoolean(EXTRA_REQUIRES_MIXED_CASE_KEY, response.requiresMixedCase);
        }

        // Add any errors.
        if (response.status != null) {

            // Set the success boolean value.
            resultData.putBoolean(EXTRA_MWS_RESPONSE_STATUS_SUCCESS_KEY, response.status);

            // Set the error list.
            if (!TextUtils.isEmpty(response.errorMessage)) {
                com.concur.mobile.platform.service.parser.Error error = new com.concur.mobile.platform.service.parser.Error();
                error.setSystemMessage(response.errorMessage);
                error.setUserMessage(response.errorMessage);
                ArrayList<com.concur.mobile.platform.service.parser.Error> errors = new ArrayList<com.concur.mobile.platform.service.parser.Error>(
                        1);
                errors.add(error);
                resultData.putSerializable(EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY, errors);
            }
        }
    }

}
