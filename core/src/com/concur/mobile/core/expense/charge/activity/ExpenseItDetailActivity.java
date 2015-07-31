package com.concur.mobile.core.expense.charge.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.fragment.ExpenseItDetailActivityFragment;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;

/**
 * @author Elliott Jacobsen-Watts
 */
public class ExpenseItDetailActivity extends BaseActivity {

    public static final String FRAGMENT_EXPENSEIT_DETAIL = "FRAGMENT_EXPENSEIT_DETAIL";

    public static final String EXPENSEIT_ITEM_KEY = "EXPENSEIT_ITEM_KEY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildView();
    }

    public void buildView() {

        // set some kind of base layout that the fragment will sit on.
        setContentView(R.layout.expense_detail);

        // build the fragment
        FragmentManager fm = getSupportFragmentManager();
        PlatformFragment exItDetailsFragment =
                (PlatformFragment) fm.findFragmentByTag(FRAGMENT_EXPENSEIT_DETAIL);
        if (exItDetailsFragment == null) {
            exItDetailsFragment = new ExpenseItDetailActivityFragment();
            fm.beginTransaction()
                    .replace(R.id.container, exItDetailsFragment, FRAGMENT_EXPENSEIT_DETAIL)
                    .commit();
        }

        // configure header
        configureViewHeader();
    }

    protected void configureViewHeader() {
        String title = getText(R.string.quick_expense_title).toString();
        getSupportActionBar().setTitle(title);
    }
}
