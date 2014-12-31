/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.expense.activity.BasicListActivity;

public class TotalsAndTravelsActivity extends BasicListActivity {

    private String gtmTyp;
    private static final String GTM_AUTH = "AUTH";
    private static final String GTM_VCH = "VCH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gtmTyp = bundle.getString(DocumentListActivity.GTM_TYPE_FOR_DETAIL);
    }

    @Override
    protected int getLayout() {
        return R.layout.drill_in_traveltotals_option;
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.gov_docdetail_totals_travel);
    }

    @Override
    protected void configureListItems() {
        // Estimated Cost
        // Non-Reimbursable Expenses
        // Advance Requested
        if (gtmTyp == null || gtmTyp.length() == 0 || gtmTyp.equalsIgnoreCase(GTM_VCH)) {
            setAuthScreen();
            setVchScreen();
        } else if (gtmTyp.equalsIgnoreCase(GTM_AUTH)) {
            setAuthScreen();
        }
    }

    private void setAuthScreen() {
        // set layout visibility
        LinearLayout authLayout = (LinearLayout) findViewById(R.id.sub_auth_scroll_layout);
        LinearLayout vchLayout = (LinearLayout) findViewById(R.id.sub_auth_vch_scroll_layout);
        authLayout.setVisibility(View.VISIBLE);
        vchLayout.setVisibility(View.GONE);
        // set epenses
        View view = findViewById(R.id.total_travel_expenses);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_expenses, R.id.totalstravel_row_name);
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_docdetail_tnt_expense));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_name!");
            }
            txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_expenses, R.id.totalstravel_row_amount);
            if (txtView != null) {
                setAmount(docDetailInfo.totalEstCost, txtView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_amount!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".configureListItems : unable to locate total_travel_expenses!");
        }
        // set non-reimburse expenses
        view = findViewById(R.id.total_travel_nonreimburse_expenses);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_nonreimburse_expenses, R.id.totalstravel_row_name);
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_docdetail_tnt_non_rimexpense));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_name!");
            }
            txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_nonreimburse_expenses, R.id.totalstravel_row_amount);
            if (txtView != null) {
                setAmount(docDetailInfo.nonReimbursableAmount, txtView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_amount!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".configureListItems : unable to locate total_travel_nonreimburse_expenses!");
        }
        // set advance applied
        view = findViewById(R.id.total_travel_advance_applied);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_advance_applied, R.id.totalstravel_row_name);
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_docdetail_tnt_advance_applied));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_name!");
            }
            txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_advance_applied, R.id.totalstravel_row_amount);
            if (txtView != null) {
                setAmount(docDetailInfo.advApplied, txtView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_amount!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".configureListItems : unable to locate total_travel_advance_applied!");
        }
    }

    private void setVchScreen() {
        LinearLayout authLayout = (LinearLayout) findViewById(R.id.sub_auth_scroll_layout);
        LinearLayout vchLayout = (LinearLayout) findViewById(R.id.sub_auth_vch_scroll_layout);
        authLayout.setVisibility(View.VISIBLE);
        vchLayout.setVisibility(View.VISIBLE);
        // set pay to traveler
        View view = findViewById(R.id.total_travel_payto_traveler);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_payto_traveler, R.id.totalstravel_row_name);
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_docdetail_tnt_paytotravel));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_name!");
            }
            txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_payto_traveler, R.id.totalstravel_row_amount);
            if (txtView != null) {
                setAmount(docDetailInfo.payToTraveler, txtView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_amount!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".configureListItems : unable to locate total_travel_payto_traveler!");
        }
        // pay to charge card
        view = findViewById(R.id.total_travel_paytocharge_card);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_paytocharge_card, R.id.totalstravel_row_name);
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_docdetail_tnt_paytocard));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_name!");
            }
            txtView = (TextView) ViewUtil
                .findSubView(this, R.id.total_travel_paytocharge_card, R.id.totalstravel_row_amount);
            if (txtView != null) {
                setAmount(docDetailInfo.payToChargeCard, txtView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems : unable to locate totalstravel_row_amount!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".configureListItems : unable to locate total_travel_paytocharge_card!");
        }
    }

    private void setAmount(Double amt, TextView txtView) {
        String retVal = FormatUtil
            .formatAmount(amt, com.concur.mobile.gov.util.Const.GOV_LOCALE, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, true, true);
        txtView.setText(FormatUtil.nullCheckForString(retVal));
    }
}
