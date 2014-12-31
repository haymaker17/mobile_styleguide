package com.concur.mobile.core.expense.receiptstore.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;

public class ExpenseReceiptCombined extends BaseActivity {

    protected ReceiptStoreFragment receiptFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.expense_receipt_combined);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        receiptFragment = new ReceiptStoreFragment();
        ft.add(R.id.fragment_container, receiptFragment);
        ft.commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = receiptFragment.onCreateDialog(id);
        if (d == null) {
            d = super.onCreateDialog(id);
        }

        return d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        receiptFragment.onPrepareDialog(id, dialog);
    }
}
