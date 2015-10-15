/**
 * 
 */
package com.concur.mobile.platform.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of invoking the "light" version of the
 * <code>PPLogin2</code> MWS request. <br>
 * <br>
 * <b>NOTE:</b>&nbsp;&nbsp; This request will clear-out the session ID in both the <code>PlatformProperties</code> object and the
 * <code>Config</code> content provider. Clients of this request should follow-up with an immediate call to
 * <code>AutoLoginRequestTask</code> passing the authorization token in order to retrieve a session ID for later requests.
 * 
 * @author andrewk
 */
public class PPLoginLightRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "PPLoginLightRequestTask";

    // Contains the service end-point for the <code>PPLogin2Light</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/MobileSession/PPLoginV3Light";

    private static final String PREF_USER_ID = "pref_saved_user_id";

    /**
     * Contains the locale.
     */
    protected Locale locale;

    /**
     * Contains the login id.
     */
    protected String loginId;

    /**
     * Contains either the pin or password.
     */
    protected String pinOrPassword;

    /**
     * Contains the login response information.
     */
    protected LoginResult loginResult;

    /**
     * Contains the MWS response status.
     */
    protected MWSResponseStatus responseStatus;

    /**
     * Constructs an instance of <code>PPLoginLightRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param receiver
     *            contains the result receiver.
     * @param id
     *            contains the request id.
     * @param locale
     *            contains the locale.
     * @param loginId
     *            contains the login id.
     * @param pinOrPassword
     *            contains the pin/password.
     */
    public PPLoginLightRequestTask(Context context, BaseAsyncResultReceiver receiver, int id, Locale locale,
            String loginId, String pinOrPassword) {

        super(context, id, receiver);
        this.locale = locale;
        this.loginId = loginId;
        this.pinOrPassword = pinOrPassword;
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
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        // Set timeout values
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(45000);
    }

    @Override
    protected String getPostBody() {

        String content = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<Credentials>");
        strBldr.append("<Locale>").append(locale.toString()).append("</Locale>");
        strBldr.append("<LoginID>").append(Format.escapeForXML(loginId)).append("</LoginID>");
        strBldr.append("<Password>").append(Format.escapeForXML(pinOrPassword)).append("</Password>");
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
                // Initialize the MWS response parser used by 'parser'.
                MWSResponseParser mwsResponseParser = new MWSResponseParser();
                parser.registerParser(mwsResponseParser, MWSResponseParser.TAG_MWS_RESPONSE);
                // Construct the login response parser used by 'parser'.
                loginResult = new LoginResult(parser, MWSResponseParser.TAG_RESPONSE);
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
    public int onPostParse() {

        int result = super.onPostParse();

        if (loginResult != null) {

            // Add the outcome of remote wipe to the response.
            Boolean remoteWipe = (loginResult.remoteWipe != null) ? loginResult.remoteWipe : Boolean.FALSE;
            resultData.putBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, remoteWipe);

            if (result == RESULT_OK) {
                if (responseStatus != null) {
                    // Set any MWS response data.
                    setMWSResponseStatusIntoResultBundle(responseStatus);
                    if (responseStatus.isSuccess()) {
                        // Update the config content provider.
                        ConfigUtil.updateSessionInfo(getContext().getContentResolver(), loginResult, false);
                        // update client data
                        ConfigUtil.updateAnalyticsIdInClientData(getContext(), loginResult);
                        // Ensure the session ID is cleared out of from platform properties.
                        PlatformProperties.setSessionId(null);
                        // Update Platform properties.
                        if (loginResult.accessToken != null) {
                            PlatformProperties.setAccessToken(loginResult.accessToken.key);
                        } else {
                            PlatformProperties.setAccessToken(null);
                        }
                        //MOB-25817: In some instance while we are logging in. the app goes to the background just
                        //after successful parse of the login result but the app do not get the chance of writing the
                        //user info to the prefs. The assumption across all codebase is when sessioninfo is valid then
                        //prefs.userId is valid.
                        if (loginResult.userId != null) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            if (prefs.getString(PREF_USER_ID, null) == null) {
                                SharedPreferences.Editor e = prefs.edit();
                                e.putString(PREF_USER_ID, loginResult.userId);
                                e.commit();
                            }
                        }

                        // Set up GLS server URL.
                        if (loginResult.serverUrl != null) {
                            resultData.putString(LoginResponseKeys.SERVER_URL_KEY, loginResult.serverUrl);
                            PlatformProperties.setServerAddress(loginResult.serverUrl);
                        }

                        result = RESULT_OK;
                    } else {
                        result = RESULT_ERROR;
                    }
                } else {
                    // Update the config content provider.
                    ConfigUtil.updateSessionInfo(getContext().getContentResolver(), loginResult, false);
                    // Ensure the session ID is cleared out of from platform properties.
                    PlatformProperties.setSessionId(null);
                    // Update Platform properties.
                    if (loginResult.accessToken != null) {
                        PlatformProperties.setAccessToken(loginResult.accessToken.key);
                    } else {
                        PlatformProperties.setAccessToken(null);
                    }
                    result = RESULT_OK;
                }
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: MWS login result is null.");
            result = RESULT_ERROR;
        }

        return result;
    }

}
