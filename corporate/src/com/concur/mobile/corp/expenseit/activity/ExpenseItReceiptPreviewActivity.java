package com.concur.mobile.corp.expenseit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.concur.breeze.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.corp.expenseit.fragment.ExpenseItReceiptPreviewFragment;

public class ExpenseItReceiptPreviewActivity extends ActionBarActivity
        implements ExpenseItReceiptPreviewFragment.ExpenseItPreviewCallbacks{

    private String receiptImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_it_receipt_preview);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH)) {

            receiptImageFilePath = getIntent().getStringExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);

            if (getSupportFragmentManager()
                    .findFragmentByTag(ExpenseItReceiptPreviewFragment.EXPENSEIT_RECEIPT_PREVIEW_FRAGMENT_TAG) == null) {
                ExpenseItReceiptPreviewFragment frag = ExpenseItReceiptPreviewFragment.newInstance(receiptImageFilePath);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.expenseit_receipt_image_container, frag, ExpenseItReceiptPreviewFragment.EXPENSEIT_RECEIPT_PREVIEW_FRAGMENT_TAG)
                        .commit();
            }
        }

        String title = getText(R.string.preview_image_title).toString();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onReceiptPreviewResult(boolean retake) {
        Intent resultIntent = new Intent().putExtra(ExpenseItReceiptPreviewFragment.EXPENSEIT_PREVIEW_RETAKE_RESULT_KEY, retake);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
