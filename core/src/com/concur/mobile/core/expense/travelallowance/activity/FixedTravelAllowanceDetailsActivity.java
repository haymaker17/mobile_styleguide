package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.FormatUtil;

import java.util.Locale;

public class FixedTravelAllowanceDetailsActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = FixedTravelAllowanceDetailsActivity.class
            .getSimpleName();

    /**
     * The fixed travel allowance this activity is dealing with taken from intent
     */
    //private FixedTravelAllowance allowance;

    private IDateFormat dateFormatter;

    private FixedTravelAllowanceController allowanceController;

    public static final String INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE =
            FixedTravelAllowanceDetailsActivity.class.getName() + "FixedTravelAllowance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fixed_travel_allowance_details_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_daily_allowance);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ta_daily_allowance);

        Intent callerIntent = this.getIntent();
        FixedTravelAllowance allowance = null;
        boolean isEditable = false;
        if (callerIntent != null) {
            allowance = (FixedTravelAllowance)
                    callerIntent.getSerializableExtra(INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE);
            isEditable = callerIntent.getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false)
                    && callerIntent.getBooleanExtra(BundleId.IS_EDIT_MODE, false);
        }
        this.dateFormatter = new DefaultDateFormat(this);

        ConcurCore app = (ConcurCore) getApplication();
        this.allowanceController = app.getFixedTravelAllowanceController();

        renderHeader(allowance);
        renderBreakfast(allowance);
        renderLunch(allowance);
        renderDinner(allowance);
        renderLodging(allowance);
        renderOvernight(allowance);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renderBreakfast(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_breakfast_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_breakfast_label);
        if (tvProvision != null && allowance.getBreakfastProvision() != null) {
            tvProvision.setText(allowance.getBreakfastProvision().toString());
        }
        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL));
        }
    }
    private void renderLunch(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_lunch_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_lunch_label);
        if (tvProvision != null && allowance.getLunchProvision() != null) {
            tvProvision.setText(allowance.getLunchProvision().toString());
        }
        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL));
        }
    }

    private void renderDinner(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_dinner_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_dinner_label);
        if (tvProvision != null && allowance.getDinnerProvision() != null) {
            tvProvision.setText(allowance.getDinnerProvision().toString());
        }
        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL));
        }
    }

    private void renderLodging(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        if (allowance.getLodgingType() == null || allowance.getOvernightIndicator()) {
            return;
        }
        View vgLodging = this.findViewById(R.id.vg_lodging);
        if (vgLodging == null) {
            return;
        }
        vgLodging.setVisibility(View.VISIBLE);
        TextView tvValue = (TextView) this.findViewById(R.id.tv_lodging_value);
        if (tvValue != null) {
            tvValue.setText(allowance.getLodgingType().toString());
        }
    }

    private void renderOvernight(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        if (!allowance.getOvernightIndicator()) {
            return;
        }
        View vgOvernight = this.findViewById(R.id.vg_overnight);
        if (vgOvernight == null) {
            return;
        }
        vgOvernight.setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.tv_overnight_help);
        if (allowance.getOvernightIndicator()) {
            textView.setText(R.string.general_yes);
        } else {
            textView.setText(R.string.general_no);
        }
    }

    /**
     * Renders the header section
     */
    private void renderHeader(FixedTravelAllowance allowance){

        if (allowance == null) {
            return;
        }

        TextView tvSubtitle2 = (TextView) this.findViewById(R.id.tv_subtitle_2);
        if (tvSubtitle2 != null) {
            tvSubtitle2.setVisibility(View.GONE);
        }
        TextView tvTitle = (TextView) this.findViewById(R.id.tv_title);
        TextView tvSubtitle1 = (TextView) this.findViewById(R.id.tv_subtitle_1);
        TextView tvValue = (TextView) this.findViewById(R.id.tv_value);
        View vDividerBottom  =  this.findViewById(R.id.v_divider_bottom);

        if (tvTitle != null) {
            String dateString = dateFormatter.format(allowance.getDate(), false, true, true);
            tvTitle.setText(dateString);
        }
        if (tvSubtitle1 != null) {
            tvSubtitle1.setText(allowance.getLocationName());
        }
        if (vDividerBottom != null) {
            vDividerBottom.setVisibility(View.VISIBLE);
        }

        renderAmount(tvValue, allowance.getAmount(), allowance.getCurrencyCode());

    }
    /**
     * Renders the given amount currency pair into the given text view
     *
     * @param tvAmount The text view
     * @param amount The amount to be rendered
     * @param crnCode the currency code to be rendered
     */
    private void renderAmount(TextView tvAmount, Double amount, String crnCode) {

        if (tvAmount == null){
            Log.e(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "renderAmount", "TextView null reference!"));
            return;
        }
        if (amount != null) {
            Locale locale = this.getResources().getConfiguration().locale;
            tvAmount.setText(FormatUtil.formatAmount(amount, locale, crnCode, true, true));
        } else {
            tvAmount.setText(StringUtilities.EMPTY_STRING);
        }
    }
}