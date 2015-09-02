package com.concur.mobile.platform.expenseit;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
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

public class PutExpenseItNoteAsyncTask extends ExpenseItAsyncRequestTask {

    private static final String CLS_TAG = PutExpenseItNoteAsyncTask.class.getSimpleName();

    private static final String EXPENSES_URL = "v1/expenses";

    public static final String PUT_EXPENSEIT_NOTE_RESULT_KEY = "PUT_EXPENSEIT_NOTE_RESULT_KEY";
    private ExpenseItNote note;

    protected PutExpenseItNoteResponse noteResponse;

    public PutExpenseItNoteAsyncTask(Context context, int requestId, BaseAsyncResultReceiver receiver, ExpenseItNote note) {
        super(context, requestId, receiver);
        this.note = note;
    }

    @Override
    protected RequestMethod getRequestMethod() {
        return RequestMethod.PUT;
    }

    @Override
    protected String getServiceEndPoint() {
        if (note != null && note.getNote().getId() > 0L) {
            Long expenseId = note.getNote().getId();
            final Uri.Builder builder = Uri.parse(EXPENSES_URL).buildUpon();
            String endpoint = builder.build().toString();
            StringBuffer buf = new StringBuffer();
            buf.append("/").append(expenseId);
            return endpoint.concat(buf.toString());
        }
        return EXPENSES_URL;
    }

    @Override
    protected String getPostBody() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(note);
    }

    @Override
    protected int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;

        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            if (connection.getResponseCode() >= HttpStatus.SC_OK &&
                    connection.getResponseCode() < HttpStatus.SC_MULTIPLE_CHOICES) {

                PutExpenseItNoteResponse tmpResp = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                        "UTF-8"), PutExpenseItNoteResponse.class);

                if (tmpResp != null) {
                    noteResponse = tmpResp;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWS response was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }

            } else {
                ErrorResponse errorResponse = gson.fromJson(new InputStreamReader(new BufferedInputStream(is),
                        "UTF-8"), ErrorResponse.class);
                if (errorResponse != null) {
                    ExpenseItPostReceipt expenseItPostReceipt = new ExpenseItPostReceipt();
                    expenseItPostReceipt.setExpenseError(errorResponse);
                    ExpenseItPostReceipt[] list = new ExpenseItPostReceipt[] {expenseItPostReceipt};
                    noteResponse.setExpenses(list);
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: MWS response was null!");
                    result = BaseAsyncRequestTask.RESULT_ERROR;
                }
            }
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O Exception parsing data.", e);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: IO Exception closing input stream.", ioe);
                }
            }
        }
        return result;
    }

    @Override
    protected int onPostParse() {
        int result = super.onPostParse();
        if (noteResponse != null) {
            resultData.putSerializable(PUT_EXPENSEIT_NOTE_RESULT_KEY, noteResponse);
        }

        return result;
    }
}
