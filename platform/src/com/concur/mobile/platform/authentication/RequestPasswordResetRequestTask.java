/**
 * 
 */
package com.concur.mobile.platform.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.XmlUtil;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of requesting a password reset.
 * 
 * @author andrewk
 */
public class RequestPasswordResetRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "RequestPasswordResetRequestTask";

    /**
     * Contains the bundle extra data key to retrieve the response "key part A".
     */
    public static final String EXTRA_KEY_PART_A_KEY = "request.password.reset.key.part.a";

    /**
     * Contains the bundle extra data key to retrieve the response "good password description".
     */
    public static final String EXTRA_GOOD_PASSWORD_DESCRIPTION_KEY = "request.password.reset.good.password.description";

    // Contains the service end-point for the <code>RequestPasswordReset</code> MWS call.
    private static final String SERVICE_END_POINT = "/mobile/MobileSession/RequestPasswordReset";

    /**
     * Contains the email address.
     */
    protected String email;

    /**
     * Contains the locale.
     */
    protected Locale locale;

    /**
     * Contains the result.
     */
    protected RequestPasswordResetResultParser response;

    /**
     * Constructs an instance of <code>ResetPasswordRequestTask</code> for the purpose of requesting a password reset.
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
    public RequestPasswordResetRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
            Locale locale, String email) {
        super(context, requestId, receiver);

        final String MTAG = CLS_TAG + ".<init>: ";

        try {

            Assert.assertNotNull(MTAG + "email is null.", email);
            Assert.assertNotNull(MTAG + "locale is null.", locale);

            this.email = email;
            this.locale = locale;
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, MTAG + afe.getMessage());
            throw afe;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#getServiceEndPoint()
     */
    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    protected String getPostBody() {
        String postBody = null;

        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<RequestPasswordReset>");
        XmlUtil.addXmlElement(strBldr, "Email", email);
        XmlUtil.addXmlElement(strBldr, "Locale", locale.toString());
        strBldr.append("</RequestPasswordReset>");
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
                response = new RequestPasswordResetResultParser();
                parser.registerParser(response, RequestPasswordResetResultParser.TAG_REQUEST_PASSWORD_RESET_RESULT);

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
                if (response.status != null) {
                    if (response.status) {
                        setResetPasswordResponseIntoDataBundle();
                    } else {
                        result = RESULT_ERROR;
                    }
                } else {
                    setResetPasswordResponseIntoDataBundle();
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
    private void setResetPasswordResponseIntoDataBundle() {

        // Add the key part, good password
        resultData.putString(EXTRA_KEY_PART_A_KEY, response.keyPartA);
        resultData.putString(EXTRA_GOOD_PASSWORD_DESCRIPTION_KEY, response.goodPasswordDescription);
    }

}
