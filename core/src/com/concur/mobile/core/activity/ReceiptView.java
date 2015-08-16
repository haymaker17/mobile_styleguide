/**
 *
 */
package com.concur.mobile.core.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expenseit.ExpenseItGetImageUrlResponse;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.expenseit.GetExpenseItImageUrlAsyncTask;

import java.io.InputStream;
import java.net.URL;

/**
 * @author Harold Frazier, Jr.
 */
public class ReceiptView extends BaseActivity {

    private static final String CLS_TAG = ReceiptView.class.getSimpleName();

    private Long expenseItReceiptId = null;

    private ImageView imageView = null;

    private ProgressBar progressBar = null;

    private ExpenseItReceipt expenseItReceipt = null;

    private TextView receiptImageUnavailable = null;

    BaseAsyncRequestTask.AsyncReplyListener asyncReplyListener = new BaseAsyncRequestTask
            .AsyncReplyListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            final ExpenseItGetImageUrlResponse response = (ExpenseItGetImageUrlResponse)resultData
                    .get(GetExpenseItImageUrlAsyncTask
                            .GET_EXPENSEIT_IMAGE_URL_RESULT_KEY);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap;
                    bitmap = getBitmapFromURL(response.getImages().get(0).getImageDataUrl());
                    if (expenseItReceipt != null){
                        expenseItReceipt.setImageData(bitmap);
                        if (expenseItReceipt.update(ReceiptView.this, getUserId()) == false){
                            Log.e(Const.LOG_TAG, CLS_TAG + "Failed updating receipt in database");
                        }
                    }

                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            receiptImageUnavailable.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            receiptImageUnavailable.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            EventTracker.INSTANCE.track(Flurry.CATEGORY_EXPENSE_EXPENSEIT,
                    Flurry.EVENT_SHOW_ANALYZING_RECEIPT_FAILED);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            receiptImageUnavailable.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void cleanup() {

        }
    };

    private BaseAsyncResultReceiver getReceiptListReplyListener =
            new BaseAsyncResultReceiver(new Handler());

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String screenTitle;
        Intent intent = null;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.receipt_view);
        buildViews();

        intent = getIntent();
        if (intent.hasExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID)) {
            expenseItReceiptId = intent.getLongExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID, 0);
        }

        screenTitle = getText(R.string.expense_receipt).toString();

        getSupportActionBar().setTitle(screenTitle);

        if (expenseItReceiptId != 0) {
            StringBuilder statement = new StringBuilder();
            statement.append(Expense.ExpenseItReceiptColumns.USER_ID);
            statement.append(" = ? AND ");
            statement.append(Expense.ExpenseItReceiptColumns.ID);
            statement.append(" = ?");
            String[] whereArgs = {getUserId(), expenseItReceiptId.toString()};
            Cursor cursor = getContentResolver().query(Expense.ExpenseItReceiptColumns.CONTENT_URI,
                    null, statement.toString(), whereArgs, Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                expenseItReceipt = new ExpenseItReceipt(this,
                        cursor);
                cursor.close();

                if (expenseItReceipt == null || expenseItReceipt.getImageData() == null) {
                    getImage();
                } else {
                    progressBar.setVisibility(View.GONE);
                    imageView.setImageBitmap(expenseItReceipt.getImageData());
                }
            }else{
                receiptImageUnavailable.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }

        EventTracker.INSTANCE.track(Flurry.CATEGORY_EXPENSE_EXPENSEIT,
                Flurry.ACTION_VIEW_RECEIPT);
    }

    private void buildViews() {
        imageView = (ImageView) findViewById(R.id.imgvMain);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        receiptImageUnavailable = (TextView) findViewById(R.id.txtvReceiptImageUnavailable);
    }

    private void getImage() {
        getReceiptListReplyListener.setListener(asyncReplyListener);

        GetExpenseItImageUrlAsyncTask getExpenseItImageUrlAsyncTask = new GetExpenseItImageUrlAsyncTask
                (ConcurCore.getContext(), 0, getReceiptListReplyListener, expenseItReceiptId);
        getExpenseItImageUrlAsyncTask.execute();
    }

    private Bitmap getBitmapFromURL(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Bitmap d = BitmapFactory.decodeStream(is);
            is.close();
            return d;
        } catch (Exception e) {
            return null;
        }
    }

}
