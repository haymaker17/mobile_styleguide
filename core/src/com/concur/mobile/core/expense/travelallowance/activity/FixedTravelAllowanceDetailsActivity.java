package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

import java.util.Locale;

public class FixedTravelAllowanceDetailsActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLS_TAG = FixedTravelAllowanceDetailsActivity.class
            .getSimpleName();

    /**
     * The fixed travel allowance this activity is dealing with taken from intent
     */
    //private FixedTravelAllowance allowance;

    private IDateFormat dateFormatter;

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
        getSupportActionBar().setTitle(R.string.itin_daily_allowance);


        Intent callerIntent = this.getIntent();
        FixedTravelAllowance allowance = null;
        if (callerIntent != null) {
            allowance = (FixedTravelAllowance)
                    callerIntent.getSerializableExtra(INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE);
        }
        this.dateFormatter = new DefaultDateFormat(this);
        renderHeader(allowance);
        renderBreakfast(allowance);
        renderLunch(allowance);
        renderDinner(allowance);
        renderLodging(allowance);
        renderOvernight(allowance);
    }

    private void renderBreakfast(FixedTravelAllowance allowance){
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_breakfast_provision);
        if (tvProvision != null) {
            tvProvision.setText(allowance.getBreakfastProvision().toString());
        }
    }
    private void renderLunch(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_lunch_provision);
        if (tvProvision != null) {
            tvProvision.setText(allowance.getLunchProvision().toString());
        }
    }

    private void renderDinner(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_dinner_provision);
        if (tvProvision != null) {
            tvProvision.setText(allowance.getDinnerProvision().toString());
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
        Switch swOvernight = (Switch) this.findViewById(R.id.sw_overnight);
        if (swOvernight != null) {
            swOvernight.setChecked(true);
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
            Log.e(Const.LOG_TAG, CLS_TAG + ".renderAmount: TextView null reference!");
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