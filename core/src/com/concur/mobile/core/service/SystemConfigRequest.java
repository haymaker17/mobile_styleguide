/**
 * 
 */
package com.concur.mobile.core.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> for retrieving
 * 
 * @author AndrewK
 */
public class SystemConfigRequest extends GetServiceRequest {

    public static final String CLS_TAG = SystemConfigRequest.class.getSimpleName();

    public static final String SERVICE_END_POINT = "/Mobile/Config/SystemConfig";
    // The server-computed hash of the current system configuration information.
    String hash;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {

        SystemConfigReply reply = new SystemConfigReply();

        // Parse the response or log an error.
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            InputStream is = new BufferedInputStream(response.getInputStream());
            String encodingHeader = response.getContentEncoding();
            String encoding = "UTF-8";
            if (encodingHeader != null) {
                encoding = encodingHeader;
            }
            reply.xmlReply = readStream(is, encoding);
            try {
                reply.sysConfig = SystemConfig.parseSystemConfigXml(reply.xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }
            reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            if (reply.sysConfig != null) {
                List<ExpenseType> expTypeList = reply.sysConfig.getExpenseTypes();
                if (expTypeList != null) {
                    MobileDatabase mdb = concurService.getMobileDatabase();
                    if (mdb != null) {
                        Log.d(Const.LOG_TAG, CLS_TAG
                                + ".SystemConfigRequest.processResponse: getting database from service");
                        if (concurService.prefs != null) {
                            String userId = concurService.prefs.getString(Const.PREF_USER_ID, null);
                            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            for (ExpenseType expenseType : expTypeList) {
                                expenseType.setuseCount(0);
                                expenseType.setLastUsed(now);
                                expenseType.setUserID(userId);
                                expenseType.setPolKey("-1");
                            }
                            if (!(mdb.insertExpenseType(expTypeList, userId, "-1"))) {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".SystemConfigRequest.processResponse: insert into EXP_TYPE is falied");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".SystemConfigRequest.processResponse: concurService.prefs is null");
                        }

                    } else {
                        Log.d(Const.LOG_TAG, CLS_TAG + ".SystemConfigRequest.processResponse: database is null.");
                    }
                }
            }
        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;
    }

}
