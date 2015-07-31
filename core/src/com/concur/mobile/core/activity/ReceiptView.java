/**
 *
 */
package com.concur.mobile.core.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.expenseit.ExpenseItGetImageUrlResponse;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;
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

    private ExpenseItPostReceipt expenseItPostReceipt = null;

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
                    if (expenseItPostReceipt != null){
                        expenseItPostReceipt.setImageData(bitmap);
                    }

                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onRequestFail(Bundle resultData) {

        }

        @Override
        public void onRequestCancel(Bundle resultData) {

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

        imageView = (ImageView) findViewById(R.id.imgvMain);

        intent = getIntent();
        if (intent.hasExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID)) {
            expenseItReceiptId = intent.getLongExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID, 0);
        }

        screenTitle = getText(R.string.expense_receipt).toString();

        getSupportActionBar().setTitle(screenTitle);

        //TODO Harold - Fix Database query to check for existing image
        if (expenseItReceiptId != 0) {
            ExpenseItReceipt tyu = new ExpenseItReceipt(ConcurCore.getContext(), getUserId());
            for(ExpenseItPostReceipt receipt: tyu.getReceipts()){
                if (receipt.getId() == expenseItReceiptId){
                    expenseItPostReceipt = receipt;
                }
            }

            if (expenseItPostReceipt == null || expenseItPostReceipt.getImageData() == null){
                getImage();
            }else {
                imageView.setImageBitmap(tyu.getImageData());
            }
        }
    }

    private void getImage() {
        getReceiptListReplyListener.setListener(asyncReplyListener);

        GetExpenseItImageUrlAsyncTask hg = new GetExpenseItImageUrlAsyncTask
                (ConcurCore.getContext(), 0, getReceiptListReplyListener, expenseItReceiptId);
        hg.execute();
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
