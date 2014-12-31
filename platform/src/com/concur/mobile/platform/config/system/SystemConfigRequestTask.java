package com.concur.mobile.platform.config.system;

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
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BasePlatformRequest</code> for the purposes of making a system config request.
 */
public class SystemConfigRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "SystemConfigRequest";

    // SystemConfig response constants.
    public static final String SC_RESPONSE_KEY = "system.config.response";

    // Contains a constant indicating system configuration information has been
    // updated.
    public static final String SC_RESPONSE_UPDATED_ID = "UPDATED";

    // Contains a constant indication system configuration information has not been changed.
    public static final String SC_RESPONSE_NO_CHANGE_ID = "NO_CHANGE";

    // Contains the service end-point for the <code>SystemConfig</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/Config/SystemConfig";

    /**
     * Contains the request id.
     */
    protected int requestId;

    /**
     * Contains the stored hash of the current system configuration information.
     */
    protected String hash;

    /**
     * Contains the system config response.
     */
    protected SystemConfig systemConfig;

    /**
     * Constructs an instance of <code>SystemConfigRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the receiver.
     * @param hash
     *            contains the hash code, if any, of the current system configuration data.
     */
    public SystemConfigRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String hash) {
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
                // Construct the system config parser used by 'parser'.
                systemConfig = new SystemConfig(parser, SystemConfig.TAG_SYSTEM_CONFIG);
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

        if (systemConfig != null) {

            boolean updated = true;
            String updateResult = SC_RESPONSE_UPDATED_ID;
            if (!TextUtils.isEmpty(systemConfig.responseId)) {
                updated = systemConfig.responseId.equalsIgnoreCase(SC_RESPONSE_UPDATED_ID);
                if (!updated) {
                    updateResult = SC_RESPONSE_NO_CHANGE_ID;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: no value for response id was parsed.");
            }
            // Perform the update, if needbe.
            if (updated) {
                SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
                ConfigUtil.updateSystemConfigInfo(getContext(), systemConfig, sessInfo.getUserId());
            }

            resultData.putString(SC_RESPONSE_KEY, updateResult);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: system config was not parsed.");
            result = BaseAsyncRequestTask.RESULT_ERROR;
        }
        return result;
    }

}
