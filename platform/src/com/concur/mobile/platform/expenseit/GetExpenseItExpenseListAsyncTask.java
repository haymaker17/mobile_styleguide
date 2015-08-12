/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.platform.expenseit;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.service.ExpenseItAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Calendar;

public class GetExpenseItExpenseListAsyncTask extends ExpenseItAsyncRequestTask {

    private static String CLS_TAG = GetExpenseItExpenseListAsyncTask.class.getSimpleName();

    private static final String EXPENSES_URL = "v1/expenses";

    static final String PARAM_INCLUDE_EXPENSE_ITEMS = "should_include_expense_items";

    static final String PARAM_ONLY_ACTIVE = "only_active";

    static final String PARAM_VALUE_TRUE = "1";

    static final String PARAM_VALUE_FALSE = "0";

    static final String PARAM_LIMIT = "limit";

    public static final String GET_EXPENSEIT_EXPENSES_LIST = "GET_EXPENSEIT_EXPENSES_LIST";

    protected ExpenseItPostReceiptResponse receiptResponse;

    protected final String userId;

    public GetExpenseItExpenseListAsyncTask(Context context, int requestId, String userId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
        this.userId = userId;
    }

    @Override
    protected String getServiceEndPoint() {
        final Uri.Builder builder = Uri.parse(EXPENSES_URL).buildUpon();
        builder.appendQueryParameter(PARAM_INCLUDE_EXPENSE_ITEMS, PARAM_VALUE_FALSE);
        builder.appendQueryParameter(PARAM_ONLY_ACTIVE, PARAM_VALUE_TRUE);
        //builder.appendQueryParameter(PARAM_LIMIT, Long.toString(limit));
        return builder.build().toString();
    }

    @Override
    protected String getPostBody() {
        return super.getPostBody();
    }

    @Override
    protected RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }

    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;

        try {
            // Build the parser with type deserializers.
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            if (connection.getResponseCode() >= HttpStatus.SC_OK &&
                connection.getResponseCode() < HttpStatus.SC_MULTIPLE_CHOICES) {

                ExpenseItPostReceiptResponse tmpResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ExpenseItPostReceiptResponse.class);

                if (tmpResp != null) {
                    receiptResponse = tmpResp;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            } else {
                // prepare the object Type expected in MWS response 'data' element
                ErrorResponse errResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                    "UTF-8"), ErrorResponse.class);
                if (errResp != null) {
                    ExpenseItPostReceipt receipt = new ExpenseItPostReceipt();
                    receipt.setExpenseError(errResp);
                    ExpenseItPostReceipt[] list = new ExpenseItPostReceipt[]{receipt};
                    receiptResponse.setExpenses((list));
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWSResponse Error response was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
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
        if (receiptResponse != null) {
            resultData.putSerializable(GET_EXPENSEIT_EXPENSES_LIST, receiptResponse);
        }

        if (receiptResponse != null) {
            Calendar tmp;

            // TODO: EXPIT - need to reconcile data so we're not wiping out all the rows, we should just updated changed ones!
            String where = Expense.ExpenseItReceiptColumns.USER_ID + " = ?";
            String[] selectionArgs = new String[]{userId};
            getContext().getContentResolver().delete(Expense.ExpenseItReceiptColumns.CONTENT_URI, where, selectionArgs);

            for (ExpenseItPostReceipt receipt : receiptResponse.getExpenses()) {

                ExpenseItReceipt expenseItReceipt = new ExpenseItReceipt();

                if (receipt.getId() != null) {
                    expenseItReceipt.setId(receipt.getId());
                }
                expenseItReceipt.setCcType(receipt.getCcType());
                if (receipt.getReportId() != null) {
                    expenseItReceipt.setReportId(receipt.getReportId());
                }
                expenseItReceipt.setNote(receipt.getNote());
                expenseItReceipt.setCcType(receipt.getCcType());
                if (receipt.getCreatedAt() != null) {
                    tmp = Calendar.getInstance();
                    tmp.setTime(receipt.getCreatedAt());
                    expenseItReceipt.setCreatedAt(tmp);
                }
                if (receipt.getSendToCteAt() != null) {
                    tmp = Calendar.getInstance();
                    tmp.setTime(receipt.getSendToCteAt());
                    expenseItReceipt.setSendToCteAt(tmp);
                }
                expenseItReceipt.setTotalImageCount(receipt.getTotalImageCount());
                expenseItReceipt.setTotalImagesUploaded(receipt.getTotalImagesUploaded());
                expenseItReceipt.setParsingStatusCode(receipt.getParsingStatusCode());
                expenseItReceipt.setProcessingEngine(receipt.getProcessingEngine());
                expenseItReceipt.setEta(receipt.getEta());

                // Handle rubicon errors.
                if(receipt.getExpenseError() != null) {
                    expenseItReceipt.setErrorCode(receipt.getExpenseErrorCode());
                    expenseItReceipt.setErrorMessage(receipt.getExpenseErrorMessage());
                }

                if (!expenseItReceipt.update(getContext(), userId)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: failed to update ExpenseIt Receipt DAO!");
                }
            }
        }
        return result;
    }
}
