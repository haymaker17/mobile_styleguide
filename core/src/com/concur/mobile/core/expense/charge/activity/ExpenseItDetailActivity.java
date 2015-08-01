package com.concur.mobile.core.expense.charge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.ReceiptView;
import com.concur.mobile.core.expense.charge.data.ExpenseItItem;
import com.concur.mobile.core.expense.fragment.ExpenseItDetailActivityFragment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;

/**
 * @author Elliott Jacobsen-Watts
 */
public class ExpenseItDetailActivity extends BaseActivity implements ExpenseItDetailActivityFragment.ExpenseItDetailsViewReceiptCallback {

    private static final String FRAGMENT_EXPENSEIT_DETAIL = "FRAGMENT_EXPENSEIT_DETAIL";

    public static final String EXPENSEIT_ITEM_KEY = "EXPENSEIT_ITEM_KEY";

    @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_detail);

        ExpenseItItem item = new ExpenseItItem(new ExpenseItReceipt());
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(ExpenseItDetailActivity.EXPENSEIT_ITEM_KEY)) {
            item = (ExpenseItItem) getIntent().getExtras().getSerializable(ExpenseItDetailActivity.EXPENSEIT_ITEM_KEY);
        }

        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_EXPENSEIT_DETAIL) == null) {
            ExpenseItDetailActivityFragment frag = ExpenseItDetailActivityFragment.newInstance(item);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, frag, FRAGMENT_EXPENSEIT_DETAIL)
                .commit();
        }

        configureViewHeader();
    }

    protected void configureViewHeader() {
        String title = getText(R.string.quick_expense_title).toString();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void initializeViewReceipt(long receiptId) {
        Intent intent = new Intent(this, ReceiptView.class);
        intent.putExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID, receiptId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expenseit_details_options, menu);
        return true;
    }
}
