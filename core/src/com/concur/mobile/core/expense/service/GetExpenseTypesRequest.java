/**
 * 
 */
package com.concur.mobile.core.expense.service;

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
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.GetServiceRequest;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>GetServiceRequest</code> for retrieving a list of expense types optionally scoped to a report policy key.
 * 
 * @author AndrewK
 */
public class GetExpenseTypesRequest extends GetServiceRequest {

    private static final String CLS_TAG = GetExpenseTypesRequest.class.getSimpleName();

    /**
     * Contains the service end-point.
     */
    public static final String SERVICE_END_POINT = "/mobile/Expense/GetExpenseTypesV3";

    /**
     * Contains the optional policy key.
     */
    public String policyKey;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#getServiceEndpointURI()
     */
    @Override
    protected String getServiceEndpointURI() {
        String retVal = SERVICE_END_POINT;
        if (policyKey != null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(SERVICE_END_POINT);
            strBldr.append('/');
            strBldr.append(policyKey);
            retVal = strBldr.toString();
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.service.ServiceRequest#processResponse(org.apache.http.HttpResponse,
     * com.concur.mobile.service.ConcurService)
     */
    @Override
    protected ServiceReply processResponse(HttpURLConnection response, ConcurService concurService) throws IOException {
        GetExpenseTypesReply reply = new GetExpenseTypesReply();

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
                reply = GetExpenseTypesReply.parseReply(xmlReply);
            } catch (Exception e) {
                // MOB-18684 re-throw XML parsing error as IOException to be processed by caller
                // Empty response will result in this exception, since a valid response should contain root element
                IOException ioe = new IOException("Fail to parse xml response");
                ioe.initCause(e);
                throw ioe;
            }

            List<ExpenseType> expTypeList = reply.expenseTypes;
            if (expTypeList != null) {
                MobileDatabase mdb = concurService.getMobileDatabase();
                if (mdb != null) {
                    Log.d(Const.LOG_TAG, CLS_TAG
                            + ".GetExpenseTypesRequest.processResponse: getting database from service");
                    if (concurService.prefs != null) {
                        String userId = concurService.prefs.getString(Const.PREF_USER_ID, null);
                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        for (ExpenseType expenseType : expTypeList) {
                            expenseType.setUserID(userId);
                            expenseType.setPolKey(policyKey);
                            expenseType.setLastUsed(now);
                            expenseType.setuseCount(0);
                        }
                        reply.expenseTypes = expTypeList;
                        if (!(mdb.insertExpenseType(expTypeList, userId, policyKey))) {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".GetExpenseTypesRequest.processResponse: updating EXP_TYPE is falied");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".GetExpenseTypesRequest.processResponse: concurService.prefs is null");
                    }

                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".GetExpenseTypesRequest.processResponse: database is null.");
                }
            }

        } else {
            // Log the error.
            logError(response, CLS_TAG + ".processResponse");
        }
        return reply;

    }

}
