package com.concur.mobile.platform.config.user;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BasePlatformRequest</code> for the purpose of making a user config request.
 */
public class UserConfigRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = UserConfigRequestTask.class.getSimpleName();

    // UserConfig response constants.
    public static final String UC_RESPONSE_KEY = "user.config.response";

    // Contains a constant indicating user configuration information has been
    // updated.
    public static final String UC_RESPONSE_UPDATED_ID = "UPDATED";

    // Contains a constant indication user configuration information has not been changed.
    public static final String UC_RESPONSE_NO_CHANGE_ID = "NO_CHANGE";

    // Contains the service end-point for the <code>UserConfig</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/Config/UserConfigV2";

    /**
     * Contains the request id.
     */
    protected int requestId;

    /**
     * Contains the stored hash of the current user configuration information.
     */
    protected String hash;

    /**
     * Contains the MWS response status.
     */
    protected MWSResponseStatus responseStatus;

    /**
     * Contains the user config response.
     */
    protected UserConfig userConfig;

    /**
     * Constructs an instance of <code>UserConfigRequestTask</code>.
     * 
     * @param context
     *            contains the application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the result receiver.
     * @param hash
     *            contains the hash code, if any, of the current user configuration data.
     */
    public UserConfigRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String hash) {
        super(context, requestId, receiver);
        this.hash = hash;
    }

    @Override
    public String getServiceEndPoint() {
        String srvEndPoint = SERVICE_END_POINT;

        if (!TextUtils.isEmpty(hash)) {
            StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
            strBldr.append('/');
            strBldr.append(hash);
            srvEndPoint = strBldr.toString();
        }
        return srvEndPoint;
    }

    @Override
    public int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Initialize the MWS response parser used by 'parser'.
                MWSResponseParser mwsResponseParser = new MWSResponseParser();
                parser.registerParser(mwsResponseParser, MWSResponseParser.TAG_MWS_RESPONSE);

                // Construct the user config response parser used by 'parser' and register it.
                userConfig = new UserConfig(parser, MWSResponseParser.TAG_RESPONSE);

                // Parse.
                parser.parse();

                responseStatus = mwsResponseParser.getRequestTaskStatus();

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

        if (result == RESULT_OK) {
            if (responseStatus != null) {
                // Set any MWS response data.
                setMWSResponseStatusIntoResultBundle(responseStatus);
            }
            if (responseStatus == null || responseStatus.isSuccess()) {
                if (userConfig != null) {

                    boolean updated = true;
                    String updateResult = UC_RESPONSE_UPDATED_ID;
                    if (!TextUtils.isEmpty(userConfig.responseId)) {
                        updated = userConfig.responseId.equalsIgnoreCase(UC_RESPONSE_UPDATED_ID);
                        if (!updated) {
                            updateResult = UC_RESPONSE_NO_CHANGE_ID;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: no value for response id was parsed.");
                    }
                    // Perform the update, if need to be.
                    if (updated) {
                        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
                        ConfigUtil.updateUserConfigInfo(getContext(), userConfig, sessInfo.getUserId());
                    }

                    resultData.putString(UC_RESPONSE_KEY, updateResult);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: user config was not parsed.");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            } else {
                result = BaseAsyncRequestTask.RESULT_ERROR;
            }
        }
        return result;
    }
}
