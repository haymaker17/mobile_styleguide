/**
 * 
 */
package com.concur.mobile.platform.authentication;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purposes of performing a mobile session login given a web-session
 * ID.
 *
 * @author andrewk
 */
public class CorpSSOLoginRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "CorpSSOLoginRequestTask";

    // Contains the service end-point for the <code>CorpSsoLogin</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/MobileSession/CorpSsoLogin";

    /**
     * Contains the web-session id.
     */
    private String webSessionId;

    /**
     * Contains the locale.
     */
    private String locale;

    /**
     * Contains the login response information.
     */
    protected LoginResult loginResult;

    /**
     * Constructs an instance of <code>CorpSSOLoginRequestTask</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the result receiver.
     * @param webSessionId
     *            contains the web session id.
     * @param locale
     *            contains the locale.
     */
    public CorpSSOLoginRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
            String webSessionId, String locale) {
        super(context, requestId, receiver);

        this.webSessionId = webSessionId;
        this.locale = locale;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#requiresSessionId()
     */
    @Override
    protected boolean requiresSessionId() {
        return false;
    }

    /**
     * Gets the login response.
     * 
     * @return returns the login response object.
     */
    public LoginResult getLoginResponse() {
        return loginResult;
    }

    @Override
    protected String getPostBody() {

        String content = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<WebSession>");
        strBldr.append("<SessionId>").append(Format.escapeForXML(webSessionId)).append("</SessionId>");
        strBldr.append("<Locale>").append(Format.escapeForXML(locale.toString())).append("</Locale>");
        strBldr.append("</WebSession>");
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
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseContentAsXML: unable to construct common parser!");
            }
        } catch (XmlPullParserException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseContentAsXML: ", e);
        } catch (IOException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseContentAsXML: ", e);
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

        if (loginResult != null) {

            // Add the outcome of remote wipe to the response.
            Boolean remoteWipe = (loginResult.remoteWipe != null) ? loginResult.remoteWipe : Boolean.FALSE;
            resultData.putBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, remoteWipe);

            if (remoteWipe) {
                // Perform a remote wipe.
                ConfigUtil.remoteWipe(getContext());
                // Clear information out of Platform properties.
                PlatformProperties.setAccessToken(null);
                PlatformProperties.setSessionId(null);
            } else {
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
                // Add the outcome of remote wipe to the response.
                Boolean disableAutologin = (loginResult.disableAutoLogin != null) ? loginResult.disableAutoLogin
                        : Boolean.FALSE;
                resultData.putBoolean(LoginResponseKeys.DISABLE_AUTO_LOGIN, disableAutologin);
            }
        }
        return result;
    }

}
