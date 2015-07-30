/**
 * 
 */
package com.concur.mobile.core.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.expenseit.GetExpenseItImageUrlAsyncTask;

/***
 * @author Harold Frazier, Jr.
 */
public class ReceiptView extends BaseActivity {

    private static final String CLS_TAG = ReceiptView.class.getSimpleName();

    private String expenseItReceiptId = null;

    private ImageView imageView = null;

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
        if (intent.hasExtra(Const.EXTRA_EXPENSE_IT_ID)) {
            expenseItReceiptId = intent.getStringExtra(Const.EXTRA_EXPENSE_IT_ID);
        }

        screenTitle = getText(R.string.expense_receipt).toString();

        getSupportActionBar().setTitle(screenTitle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Notifies the view that it should register any broadcast receivers it has registered.
     */
    public void registerReceivers() {

    }

    /**
     * Notifies the view that it should unregister any broadcast receivers it has registered.
     */
    public void unregisterReceivers() {

    }

    private void getImage(String ReceiptID){
        BaseAsyncRequestTask.AsyncReplyListener listener = new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {

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

        BaseAsyncResultReceiver getReceiptListReplyListener =
                new BaseAsyncResultReceiver(new Handler());
        getReceiptListReplyListener.setListener(listener);

        GetExpenseItImageUrlAsyncTask hg = new GetExpenseItImageUrlAsyncTask
                (ConcurCore.getContext(), 0, getReceiptListReplyListener, Long.parseLong
                        (expenseItReceiptId));
    }

}
