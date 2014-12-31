package com.concur.mobile.platform.expense.smartexpense;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ClientData;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.list.PersonalCard;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.BooleanDeserializer;
import com.concur.mobile.platform.util.CalendarDeserializer;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.DoubleDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of retrieving a smart expense list.
 * 
 * @author yiwenw
 * 
 */
public class SmartExpenseListRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "SmartExpenseListRequestTask";

    // Contains the service end-point for the <code>SmartExpenses</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/expense/v1.0/smartexpenses";

    /**
     * Contains the client data key that stores the ETag value for the smart expense list.
     * 
     * @see {@link ClientData}
     */
    public static final String SMART_EXPENSE_LIST_ETAG_CLIENT_DATA_KEY = "_SMART_EXPENSE_LIST_ETAG";

    /**
     * Contains the parsed expense list.
     */
    protected SmartExpenseList expenseList;

    /**
     * Contains the client data that stores the smart expense list ETag value.
     */
    protected ClientData eTagClientData;

    /**
     * Contains the reset value.
     */
    protected boolean reset;

    /**
     * Constructs an instance of <code>ExpenseListRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     * @param reset
     *            contains whether the smart expense list should be regenerated on the server prior to the list being returned.
     */
    public SmartExpenseListRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, boolean reset) {
        super(context, requestId, receiver);
        this.reset = reset;
    }

    /**
     * Constructs an instance of <code>ExpenseListRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     */
    public SmartExpenseListRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        this(context, requestId, receiver, false);
    }

    @Override
    protected String getServiceEndPoint() {
        if (reset) {
            return SERVICE_END_POINT + "?reset=Y";
        } else {
            return SERVICE_END_POINT;
        }
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {

        super.configureConnection(connection);

        // Set the accept header to JSON.
        connection.setRequestProperty(HEADER_ACCEPT, CONTENT_TYPE_JSON);

        // Set the ETag.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        String userId = sessInfo.getUserId();
        eTagClientData = new ClientData(getContext());
        eTagClientData.key = SMART_EXPENSE_LIST_ETAG_CLIENT_DATA_KEY;
        eTagClientData.userId = userId;
        if (eTagClientData.load()) {
            // Set the etag header, if needbe.
            if (!TextUtils.isEmpty(eTagClientData.text)) {
                connection.setRequestProperty(HEADER_IF_NONE_MATCH, eTagClientData.text);
            }
        }

        // MOB-21025 - Set timeout values to 2.5 minutes (server will be set to 2 minutes).
        connection.setConnectTimeout(150000);
        connection.setReadTimeout(150000);
    }

    @Override
    public int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {

            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // Build the parser with type deserializers.
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Calendar.class, new CalendarDeserializer());
                builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
                builder.registerTypeAdapter(Double.class, new DoubleDeserializer());
                Gson gson = builder.create();

                expenseList = gson.fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"),
                        SmartExpenseList.class);

                // Obtain the ETag value.
                String eTagField = connection.getHeaderField(HEADER_ETAG);
                if (!TextUtils.isEmpty(eTagField)) {
                    eTagClientData.text = eTagField;
                    if (!eTagClientData.update()) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to save ETag client data!");
                    }
                }

            }

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        } catch (JsonSyntaxException jse) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: exception parsing JSON", jse);
            result = BaseAsyncRequestTask.RESULT_ERROR;
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

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        if (sessInfo == null) {
            Log.w(Const.LOG_TAG, CLS_TAG
                    + ".onPostParse() - SessionInfo is null! Probably trying to perform a request after user logs out.");
            return result;
        }
        String userId = sessInfo.getUserId();

        if (expenseList != null) {

            // Reconcile the current persisted smart expense list with the
            // newly parsed list.
            SmartExpenseList.reconcile(getContext(), userId, expenseList.expenses);

            // Reconcile the current persisted smart personal card list with the newly
            // parsed list.
            // NOTE: If the list of smart expenses coming down does not contain any personal card
            // transactions, then the list of personal cards will be missing. Hence, the code
            // below will remove them. This call can't be used in combination with
            // the GetAllExpenses call as this will remove personal card entries!
            // Perhaps the "smart expenses get" end-point should return the personal card
            // list regardless of whether there are any smart expenses containing personal card
            // transactions. That would permit the client to not worry about the above! Reason being
            // is that the 'GetAllExpenses' end-point will return personal cards regardless of whether
            // any personal card transactions exist.
            List<PersonalCard> persCards = null;
            if (expenseList.personalCards != null) {
                persCards = new ArrayList<PersonalCard>(expenseList.personalCards.size());
                for (SmartPersonalCard smartPersCard : expenseList.personalCards) {
                    persCards.add(smartPersCard);
                }
            }
            PersonalCard.reconcile(getContext(), userId, persCards);

            if (expenseList.expenses != null) {
                for (SmartExpense exp : expenseList.expenses) {
                    // Set the user id and context.
                    exp.userId = userId;
                    exp.context = getContext();
                    // Update the expense.
                    if (!exp.update(exp.context, exp.userId)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: unable to save smart expense!");
                    }
                }
            }

            if (expenseList.personalCards != null) {
                for (SmartPersonalCard smartPersCard : expenseList.personalCards) {
                    // Update the personal card.
                    if (!smartPersCard.update(getContext(), userId)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: unable to save personal card!");
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: smart expense list was not parsed.");
            result = BaseAsyncRequestTask.RESULT_ERROR;
        }
        return result;
    }
}
