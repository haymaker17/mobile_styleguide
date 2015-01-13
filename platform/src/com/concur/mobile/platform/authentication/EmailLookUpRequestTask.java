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
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.XmlUtil;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> to perform an email-based look-up of authentication information.
 * 
 * @author andrewk
 * 
 */
public class EmailLookUpRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "EmailLookUpRequestTask";

    /**
     * Extra bundle key used to pass the a login bundle (result date from EmailLookupRequest).
     */
    public static final String EXTRA_LOGIN_BUNDLE = "extra_login_bundle";

    /**
     * Contains the bundle extra data key to look up the login id.
     */
    public static String EXTRA_LOGIN_ID_KEY = "email.lookup.login.id";

    /**
     * Contains the bundle extra data key to look up the server url.
     */
    public static String EXTRA_SERVER_URL_KEY = "email.lookup.server.url";
    /**
     * Contains the bundle extra data key to look up the sign in method. {Values: Password/MobilePassword/SSO}
     */
    public static String EXTRA_SIGN_IN_METHOD_KEY = "email.lookup.signin.method";
    /**
     * Contains the bundle extra data key to look up the SSO url.
     */
    public static String EXTRA_SSO_URL_KEY = "email.lookup.sso.url";

    // Contains the service end-point for the <code>EmailLookup</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/MobileSession/EmailLookupV2";

    /**
     * Contains the email address.
     */
    protected String email;

    /**
     * Contains the locale.
     */
    protected Locale locale;

    /**
     * Contains the MWS response status.
     */
    protected MWSResponseStatus responseStatus;

    /**
     * Contains the email look up response.
     */
    protected EmailLookUpResponseParser emailLookUpResponse;

    /**
     * Constructs an instance of <code>EmailLookUpRequestTask</code> for the purpose of performing an email lookup request to
     * determine authentication type.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the receiver.
     */
    public EmailLookUpRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Locale locale,
            String email) {
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
    protected boolean requiresSessionId() {
        return false;
    }

    @Override
    protected String getPostBody() {
        String postBody = null;

        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<EmailLookupRequest>");
        XmlUtil.addXmlElement(strBldr, "Email", email);
        XmlUtil.addXmlElement(strBldr, "Locale", locale.toString());
        strBldr.append("</EmailLookupRequest>");
        postBody = strBldr.toString();

        return postBody;
    }

    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Initialize the MWS response parser used by 'parser'.
                MWSResponseParser mwsResponseParser = new MWSResponseParser();
                parser.registerParser(mwsResponseParser, MWSResponseParser.TAG_MWS_RESPONSE);
                // Initialize the email look up response parser.
                emailLookUpResponse = new EmailLookUpResponseParser();
                parser.registerParser(emailLookUpResponse, MWSResponseParser.TAG_RESPONSE);

                // Parse.
                parser.parse();
                responseStatus = mwsResponseParser.getRequestTaskStatus();
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
            if (responseStatus != null) {
                // Set any MWS response data.
                setMWSResponseStatusIntoResultBundle(responseStatus);
                if (responseStatus.isSuccess()) {
                    if (emailLookUpResponse != null) {
                        setEmailLookUpResponseDataInBundle();
                        result = RESULT_OK;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onPostParse: MWS returned success, but email lookup response is null.");
                        result = RESULT_ERROR;
                    }
                } else {
                    result = RESULT_ERROR;

                    // populate the result data bundle with the list of errors from MWS
                    setMWSResponseStatusIntoResultBundle(responseStatus);

                }
            } else {
                if (emailLookUpResponse != null) {
                    setEmailLookUpResponseDataInBundle();
                    result = RESULT_OK;
                } else {
                    result = RESULT_ERROR;
                }
            }
        }
        return result;
    }

    /**
     * Will set the result data in <code>emailLookUpResponse</code> into the result bundle.
     */
    private void setEmailLookUpResponseDataInBundle() {
        // Set the login id.
        resultData.putString(EXTRA_LOGIN_ID_KEY, email);
        // Set the server url.
        resultData.putString(EXTRA_SERVER_URL_KEY, emailLookUpResponse.serverUrl);
        // Set the sign-in method.
        resultData.putString(EXTRA_SIGN_IN_METHOD_KEY, emailLookUpResponse.signInMethod);
        // Set the sso url.
        resultData.putString(EXTRA_SSO_URL_KEY, emailLookUpResponse.ssoUrl);
    }

}
