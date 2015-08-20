package com.concur.mobile.platform.expense.smartexpense;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.ActionStatus;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;
import com.concur.mobile.platform.util.XmlUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by AntonioA on 4/1/15.
 */

public class SaveSmartExpenseRequestTask extends PlatformAsyncRequestTask {

    /**
     * Contains the key that should be used to look up the URI of a mobile entry that has been saved to the server.
     */
    public static final String SMART_EXPENSE_UPDATE_RESULT_KEY = "smart.expense.update.result.key";
    private static final String CLS_TAG = SaveSmartExpenseRequestTask.class.getSimpleName();
    /**
     * Contains the service end-point for the <code>/mobile/Expense/SaveMobileEntry</code> MWS call.
     */
    private final String SERVICE_END_POINT = "/mobile/Expense/v1.0/SaveSmartExpense";
    /**
     * Contains whether or not an existing receipt image should be cleared.
     */
    public boolean clearImage;
    /**
     * Contains the mobile entry.
     */
    protected SmartExpense smartExpense;
    /**
     * Result object
     */
    protected SmartExpenseAction smartExpenseAction;

    /**
     * Constructs an instance of <code>SaveSmartExpenseRequestTask</code>.
     *
     * @param context
     * @param requestId
     * @param receiver
     * @param smartExpenseUri contains the Uri of the mobile entry being saved.
     */
    public SaveSmartExpenseRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
                                       Uri smartExpenseUri, boolean clearImage) {
        this(context, requestId, receiver, new SmartExpense(context, smartExpenseUri), clearImage);
    }

    /**
     * This constructor takes a <code>SmartExpense</code> object. When attempting to save the object to the server, it is
     * possible that an error occurs. The object is then kept in the instance so that it is saved in offline queue.
     *
     * @param context
     * @param requestId
     * @param receiver
     * @param smartExpense  Contains <code>SmartExpense</code> object
     * @param clearImage 
     */
    public SaveSmartExpenseRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
                                       SmartExpense smartExpense, boolean clearImage) {
        super(context, requestId, receiver);
        this.clearImage = clearImage;
        this.smartExpense = smartExpense;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#getServiceEndPoint()
     */
    @Override
    protected String getServiceEndPoint() {
        StringBuilder builder = new StringBuilder();
        builder.append(SERVICE_END_POINT);
        if (clearImage) {
            builder.append("/Y");
        }
        return builder.toString();
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

    /**
     * Builds post request body for saving smart expense, note that this is now using MobileEntry. This is temp
     * until expense team provides us with a new API for saving smart expenses
     * @return
     */
    @Override
    protected String getPostBody() {
        String postBody = null;
        if (smartExpense != null) {
            StringBuilder requestBody = new StringBuilder();
            requestBody.append("<SmartExpense>");

            if (!TextUtils.isEmpty(smartExpense.getSmartExpenseId())) {
                XmlUtil.addXmlElement(requestBody, "SmartExpenseId", smartExpense.getSmartExpenseId());
            }

            if (!TextUtils.isEmpty(smartExpense.getMeKey())) {
                XmlUtil.addXmlElement(requestBody, "MeKey", smartExpense.getMeKey());
            }

            XmlUtil.addXmlElement(requestBody, "CrnCode", smartExpense.getCrnCode());
            XmlUtil.addXmlElement(requestBody, "Comment", smartExpense.getComment() != null ? smartExpense.getComment() : "");
            XmlUtil.addXmlElement(requestBody, "ExpKey", smartExpense.getExpKey());
            XmlUtil.addXmlElement(requestBody, "LocName", smartExpense.getLocName());
            if (smartExpense.getReceiptImageId() != null && smartExpense.getReceiptImageId().length() > 0 && !clearImage) {
                XmlUtil.addXmlElement(requestBody, "ReceiptImageId", smartExpense.getReceiptImageId());
            }
            XmlUtil.addXmlElement(requestBody, "TransactionAmount", smartExpense.getTransactionAmount());

            XmlUtil.addXmlElement(requestBody, "TransactionDate",
                Format.safeFormatCalendar(Parse.LONG_YEAR_MONTH_DAY, smartExpense.getTransactionDate()));

            XmlUtil.addXmlElement(requestBody, "VendorDescription", smartExpense.getVendorDescription() != null ? smartExpense.getVendorDescription() : "");

            requestBody.append("</SmartExpense>");
            postBody = requestBody.toString();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildSaveSmartExpensePostBodyXML: Smart Expense is null!");
        }

        return postBody;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.io.InputStream)
     */
    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // Build the parser with type deserializers.
                Gson gson = new GsonBuilder().create();
                Type type = new TypeToken<SmartExpenseAction>() { }.getType();

                SmartExpenseAction seaResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                        "UTF-8"), type);

                if (seaResp == null) {
                    smartExpenseAction = new SmartExpenseAction();
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                } else {
                    smartExpenseAction = seaResp;
                }

            }
        } catch (Exception exc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", exc);
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
    protected int onPostParse() {
        int result = super.onPostParse();

        if (smartExpenseAction != null) {

            //Serialize Action so that UI responds appropriately for fail or success
            resultData.putSerializable(SMART_EXPENSE_UPDATE_RESULT_KEY, smartExpenseAction);

            if (ActionStatus.SUCCESS.equalsIgnoreCase(smartExpenseAction.getStatus())) {
                //All success, No need to save entry in local db since list will be refreshed
                result = BaseAsyncRequestTask.RESULT_OK;

                // Set the last sync time to show that this is not an offline item.
                smartExpense.setLastSyncTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                if(!smartExpense.update(smartExpense.context, smartExpense.userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse() - unable to update SmartExpense");
                }

            }
        } else {
            smartExpenseAction = new SmartExpenseAction();
            smartExpenseAction.setSmartExpenseId(ActionStatus.FAILURE);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        }

        smartExpenseAction.setSmartExpenseId(smartExpense.getSmartExpenseId());

        //The receiver is responsible for updating the SmartExpenseDb since we cannot access application Object
        return result;
    }


}
