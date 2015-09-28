package com.concur.mobile.platform.expense.smartexpense;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;


// An extension of <code>PlatformAsyncRequestTask</code> for deleting one or more Smart Expenses from the Smart
// Expense List.
public class RemoveSmartExpensesRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = RemoveSmartExpensesRequestTask.class.getSimpleName();

    // Service end-point call to MWS RemoveSmartExpenses
    private final String SERVICE_END_POINT = "/mobile/expense/v1.0/RemoveSmartExpenses";

    /**
     * List of Smart Expenses to delete
     */
    protected ArrayList<SmartExpense> smartExpenses;

    /**
     * Receiver for the request task.
     */
    protected BaseAsyncResultReceiver asyncResultReceiver;

    /**
     * Status code result from the request.
     */
    protected int httpStatusResult;

    public RemoveSmartExpensesRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
                                          ArrayList<SmartExpense> smartExpenses) {
        super(context, requestId, receiver);

        this.smartExpenses = smartExpenses;
        asyncResultReceiver = receiver;
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    protected String getPostBody() {
        String postBody = null;

        if (smartExpenses != null) {
            StringBuilder postBodyBuilder = new StringBuilder();
            postBodyBuilder.append("<a:ArrayOfstring xmlns:a=\"http://schemas.microsoft" +
                    ".com/2003/10/Serialization/Arrays\">");

            for (SmartExpense smartExpense : smartExpenses) {
                postBodyBuilder.append("<a:string>");
                postBodyBuilder.append(smartExpense.getSmartExpenseId());
                postBodyBuilder.append("</a:string>");
            }

            postBodyBuilder.append("</a:ArrayOfstring>");
            postBody = postBodyBuilder.toString();
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".getPostBody: SmartExpenseIds is null");
        }
        return postBody;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        try {
            connection.setRequestMethod(REQUEST_METHOD_POST);
        } catch (ProtocolException protExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: protocol exception setting request method to '"
                + REQUEST_METHOD_POST + "'", protExc);
        }

        // Set the accept header to JSON.
        connection.setRequestProperty(HEADER_ACCEPT, CONTENT_TYPE_JSON);
    }

    @Override
    public int parseStream(HttpURLConnection connection, InputStream is) {
        try {
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                httpStatusResult = BaseAsyncRequestTask.RESULT_OK;
            } else {
                httpStatusResult = BaseAsyncRequestTask.RESULT_ERROR;
            }
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
            httpStatusResult = BaseAsyncRequestTask.RESULT_ERROR;
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

        return httpStatusResult;
    }

    @Override
    protected int onPostParse() {
        int result = super.onPostParse();

        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(getContext());
        if (sessionInfo == null) {
            Log.w(Const.LOG_TAG, CLS_TAG
                    + ".onPostParse() - SessionInfo is null! Probably trying to perform a request after user logs out.");
            return result;
        }

        // We successfully parsed the delete function
        // Call content provider delete
        if (smartExpenses != null) {
            Context context = getContext();
            String userId = sessionInfo.getUserId();

            for (SmartExpense smartExpense : smartExpenses) {
                // TODO: CDIAZ - I'm calling delete here once we get back an http 200 and we know the items got
                // TODO: deleted on server side. However, for offline stuff, this might need some rework.
                smartExpense.delete(context, userId);
            }
        }

        return result;
    }
}
