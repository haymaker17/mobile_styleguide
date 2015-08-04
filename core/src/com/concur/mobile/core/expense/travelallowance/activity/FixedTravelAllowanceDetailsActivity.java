package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.TaConfig;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
//import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FixedTravelAllowanceDetailsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

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
//    private TravelAllowanceConfigurationController configController;

    private TaConfig config;
    private FixedTravelAllowanceControlData controlData;

    private Boolean isEditable;
    private FixedTravelAllowance allowance;

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
        this.allowance = null;
        isEditable = false;
        if (callerIntent != null) {
            allowance = (FixedTravelAllowance)
                    callerIntent.getSerializableExtra(INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE);
            isEditable = !callerIntent.getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, true)
                    && callerIntent.getBooleanExtra(BundleId.IS_EDIT_MODE, false);
        }
        //TODO XLV: remove subsequent line to activate coding
        isEditable = false;
        this.dateFormatter = new DefaultDateFormat(this);

        ConcurCore app = (ConcurCore) getApplication();
        this.allowanceController = app.getFixedTravelAllowanceController();
//        this.configController = app.getTAConfigController();
//        config = app.getTAConfig();
        controlData = allowanceController.getControlData();

        renderHeader(allowance);
        renderBreakfast(allowance, isEditable);
        renderLunch(allowance);
        renderDinner(allowance);
        renderLodging(allowance);
        renderOvernight(allowance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itinerary_save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!this.isEditable) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == R.id.menuSave) {
                    item.setVisible(false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.menuSave && this.allowance != null) {
            updateAllowanceFromUI();

            allowanceController.executeUpdate(this.allowance);
        }

        return super.onOptionsItemSelected(item);

//            switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
    }

    private void renderBreakfast(FixedTravelAllowance allowance, Boolean isEditable) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_breakfast_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_breakfast_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_breakfast_provided);
        if (tvProvision != null && allowance.getBreakfastProvision() != null) {
            tvProvision.setText(allowance.getBreakfastProvision().toString());
        }
        if (svSwitch != null && allowance.getBreakfastProvision() != null) {
            if (isEditable) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX) == true) {
                    svSwitch.setVisibility(View.VISIBLE);
                    if (MealProvision.NOT_PROVIDED_CODE.equals(allowance.getBreakfastProvision().getCode())) {
                        svSwitch.setChecked(false);
                        svSwitch.setTextOff(allowance.getBreakfastProvision().toString());
                    } else {
                        svSwitch.setChecked(true);
                        svSwitch.setTextOn(allowance.getBreakfastProvision().toString());
                    }
                    ;
                } else {
                    svSwitch.setVisibility(View.GONE);
                }
                // TODO XLV: Check if listener is necessary (maybe we can just read the values during SAVE
                svSwitch.setOnCheckedChangeListener(this);
            } else {
                svSwitch.setVisibility(View.GONE);
            }

        }
        if (spinner != null && allowance.getBreakfastProvision() != null) {
            if (isEditable) {
//            if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX ) == false) {
                List<String> list = new ArrayList<String>();
                list.add("list 1");
                list.add("list 2");
                list.add("list 3");
                ArrayAdapter<String> testAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
                testAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(testAdapter);
                spinner.setVisibility(View.VISIBLE);
//            }
            } else {
                spinner.setVisibility(View.GONE);
            }
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
    private void renderHeader(FixedTravelAllowance allowance) {

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
        View vDividerBottom = this.findViewById(R.id.v_divider_bottom);

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
     * @param amount   The amount to be rendered
     * @param crnCode  the currency code to be rendered
     */
    private void renderAmount(TextView tvAmount, Double amount, String crnCode) {

        if (tvAmount == null) {
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

    private void updateAllowanceFromUI(){
        MealProvision provided    =  new MealProvision(MealProvision.PROVIDED_CODE, "");
        MealProvision notProvided =  new MealProvision(MealProvision.NOT_PROVIDED_CODE, "");

        Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
        if (svSwitch != null) {
            if (svSwitch.isChecked()){
                allowance.setBreakfastProvision(provided);
            }else {
                allowance.setBreakfastProvision(notProvided);
            }
        }
    }

    private void updateFromSwitch(int resourceId){
        Switch svSwitch = (Switch) this.findViewById(resourceId);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton != null) {
            return;
        }
        int i = compoundButton.getId();
        if (i == R.id.sv_breakfast_provided) {
            if (isChecked) {

            } else {

            }

        } else {
        }
        if (isChecked) {

        } else {

        }

    }

}