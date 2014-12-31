package com.concur.mobile.core.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.util.Const;

public class UserConfigRequest extends GetServiceRequest {

    public static final String CLS_TAG = UserConfigRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Config/UserConfigV2";

    // The server-computed hash of the current user configuration information.
    public String hash;

    @Override
    protected String getServiceEndpointURI() {
        String endPoint = SERVICE_END_POINT;
        if (hash != null && hash.length() > 0) {
            StringBuilder strBldr = new StringBuilder(endPoint);
            strBldr.append('/');
            strBldr.append(hash);
            endPoint = strBldr.toString();
        }
        return endPoint;
    }

    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        UserConfigReply reply = new UserConfigReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            String xmlReply = readStream(is, encoding);

            try {
                reply = UserConfigReply.parseXMLReply(xmlReply);
            } catch (RuntimeException re) {
                // Log it. In this case we'll be using the new'd UCR which will have a blank
                // config member which will cause the service handler code to fall through without
                // doing anything.
                Log.e(Const.LOG_TAG, CLS_TAG + ".processResponse: runtime exception during parse", re);
            } finally {
                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
                reply.xmlReply = xmlReply;
                if (reply.config != null) {
                    ConcurCore app = (ConcurCore) concurService.getApplication();
                    IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
                    expEntCache.putListItemInCacheForMRU(concurService, userId, ListItem.DEFAULT_KEY_CURRENCY);
                }
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }
}
