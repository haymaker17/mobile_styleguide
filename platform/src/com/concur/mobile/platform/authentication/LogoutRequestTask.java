package com.concur.mobile.platform.authentication;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.ActionResponseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of performing logout.
 * 
 * Clients should check the value of <code>ActionResponseParser.ACTION_RESULT_KEY</code> in the result bundle. If the value of
 * <code>ActionResponseParser.RESULT_KEY</code> is <code>false</code>, then clients may find an error message in
 * <code>ActionResponseParser.ACTION_ERROR_MESSAGE_KEY</code>.
 */
public class LogoutRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "LogoutRequestTask";

    // Contains the service end-point for the <code>Logout</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/MobileSession/Logout";

    /**
     * Contains whether the logout request succeeded.
     */
    private boolean success;

    /**
     * Contains any error message.
     */
    private String errorMessage;

    /**
     * Contains the action response.
     */
    private ActionResponseParser actionResponseParser;

    /**
     * Constructs an instance of <code>LogoutRequestTask</code> with a context, request id and receiver.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains a request id.
     * @param receiver
     *            contains the receiver.
     */
    public LogoutRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    @Override
    public String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    protected String getPostBody() {
        return "";
    }

    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Construct the logout response parser used by 'parser'.
                actionResponseParser = new ActionResponseParser();
                parser.registerParser(actionResponseParser, ActionResponseParser.TAG_ACTION_STATUS);
                // Parse.
                parser.parse();
                success = actionResponseParser.isSuccess();
                errorMessage = actionResponseParser.getErrorMessage();
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

        super.onPostParse();

        int result = BaseAsyncRequestTask.RESULT_OK;

        // Set the values from 'actionResponseParser' into the result data.
        setActionResultIntoResultBundle(actionResponseParser);

        // NOTE: The code below is "legacy" platform code. The above method
        // places success, error codes, etc., into result data bundle in a standard
        // fashion eliminating platform clients from knowing which type of response
        // comes back from the server, i.e., MWSResponse or ActionStatus

        // Add the action response status.
        resultData.putBoolean(ActionResponseParser.ACTION_RESULT_KEY, success);

        // Add any error message.
        if (!success) {
            if (!TextUtils.isEmpty(errorMessage)) {
                resultData.putString(ActionResponseParser.ACTION_ERROR_MESSAGE_KEY, errorMessage);
            }
        }

        // NOTE: If we got this far, then the logout request made it to the server.
        // Even if an error got returned, still treat the result as a logout.
        ConfigUtil.removeLoginInfo(getContext());

        // Clear out relative platform properties values.
        PlatformProperties.setAccessToken(null);
        PlatformProperties.setSessionId(null);

        return result;
    }

}
