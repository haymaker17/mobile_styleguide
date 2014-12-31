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
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of calling the <code>AutoLogin</code> MWS end-point.
 * 
 * @author andrewk
 */
public class AutoLoginRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "AutoLoginRequestTask";

    // Contains the service end-point for the <code>AutoLoginV3</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/MobileSession/AutoLoginV3";

    /**
     * Contains the locale.
     */
    protected Locale locale;

    /**
     * Contains the login response information.
     */
    protected LoginResult loginResult;

    /**
     * Constructs an instance of <code>AutoLoginRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param receiver
     *            contains the result receiver.
     * @param id
     *            contains the request id.
     * @param locale
     *            contains the locale.
     */
    public AutoLoginRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, Locale locale) {
        super(context, requestId, receiver);
        final String MTAG = CLS_TAG + ".<init>: ";

        try {
            Assert.assertNotNull(MTAG + "locale is null.", locale);
            this.locale = locale;
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
    protected boolean requiresSessionId() {
        return false;
    }

    @Override
    protected String getPostBody() {

        String content = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<Credentials>");
        strBldr.append("<Locale>").append(locale.toString()).append("</Locale>");
        strBldr.append("</Credentials>");
        content = strBldr.toString();

        return content;
    }

    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Construct the login response parser used by 'parser'.
                loginResult = new LoginResult(parser, LoginResult.TAG_LOGIN_RESULT);
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
    public int onPostParse() {

        int result = super.onPostParse();
        if (result == RESULT_OK) {
            result = handleLoginResult(result);
        }
        return result;
    }

    /**
     * Will handle the login result.
     * 
     * @param result
     *            contains the passed in result.
     * @return returns the result.
     */
    private int handleLoginResult(int result) {

        int retVal = result;

        if (loginResult != null) {
            // Add the outcome of remote wipe to the response.
            Boolean remoteWipe = (loginResult.remoteWipe != null) ? loginResult.remoteWipe : Boolean.FALSE;
            resultData.putBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, remoteWipe);
            
            if (!remoteWipe) {
                // Update the config content provider.
                ConfigUtil.updateLoginInfo(getContext(), loginResult);
                // Update Platform properties.
                if (loginResult.accessToken != null) {
                    PlatformProperties.setAccessToken(loginResult.accessToken.key);
                } else {
                    PlatformProperties.setAccessToken(null);
                }
                if (loginResult.session != null) {
                    PlatformProperties.setSessionId(loginResult.session.id);
                } else {
                    PlatformProperties.setSessionId(null);
                }
            }
            result = RESULT_OK;
        } else {
            result = RESULT_ERROR;
        }
        return retVal;
    }

}
