package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.FormatUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FixedTravelAllowanceDetailsActivity extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

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
    private String expenseReportKey;

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
            if (callerIntent.hasExtra(BundleId.EXPENSE_REPORT_KEY)) {
                expenseReportKey = callerIntent.getStringExtra(BundleId.EXPENSE_REPORT_KEY);
            }
        }
        if (StringUtilities.isNullOrEmpty(expenseReportKey) || allowance.isLocked()) {
            isEditable = false;
        }

        this.dateFormatter = new DefaultDateFormat(this);

        ConcurCore app = (ConcurCore) getApplication();
        this.allowanceController = app.getFixedTravelAllowanceController();
//        this.configController = app.getTAConfigController();
//        config = app.getTAConfig();
        controlData = allowanceController.getControlData();

        renderHeader(allowance);
        renderBreakfast(allowance);
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

            allowanceController.executeUpdate(this.allowance, expenseReportKey);

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

    private void renderBreakfast(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_breakfast_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_breakfast_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_breakfast_provided);

        if (tvProvision != null) {
            if (isEditable) {
                tvProvision.setVisibility(View.GONE);
            }else {
                tvProvision.setVisibility(View.VISIBLE);
                if (allowance.getBreakfastProvision() != null) {
                    tvProvision.setText(allowance.getBreakfastProvision().toString());
                }
            }
        }

        if (svSwitch != null && allowance.getBreakfastProvision() != null) {
            if (isEditable) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX) == true) {
                    svSwitch.setVisibility(View.VISIBLE);
                    svSwitch.setTextOn(getString(MealProvisionEnum.PROVIDED.getResourceId()));
                    svSwitch.setTextOff(getString(MealProvisionEnum.NOT_PROVIDED.getResourceId()));

                    if (MealProvision.NOT_PROVIDED_CODE.equals(allowance.getBreakfastProvision().getCode())) {
                        svSwitch.setChecked(false);
//                        svSwitch.setTextOff(allowance.getBreakfastProvision().toString());
                    } else {
                        svSwitch.setChecked(true);
//                        svSwitch.setTextOn(allowance.getBreakfastProvision().toString());
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
            if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX ) == false) {
                Map<String, ICode> map = controlData.getProvidedMealValues();
                List<String> list = new ArrayList<String>();
//                Collection col = map.values();
                int index = 0;
                int selectedIndex = -1;
                for (Map.Entry<String,ICode> entry : map.entrySet()){
                    if (entry.getValue().getCode().equals(allowance.getBreakfastProvision().getCode())){
                        selectedIndex = index;
                    }
                    list.add(entry.getValue().toString());
                    index++;
                }
//                for (Object entry: col){
//                    if (entry.toString().equals(allowance.getBreakfastProvision().getDescription())){
//                        selectedIndex = index;
//                    }
//                    list.add(entry.toString());
//                    index++;
//                }
////                for (int i = 0; i < map.size(); i++){
//                    list.add(col.getClass())
//                }
//                list.add("list 1");
//                list.add("list 2");
//                list.add("list 3");
                ArrayAdapter<String> testAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
                testAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(testAdapter);
                if (selectedIndex > -1) {
                    spinner.setSelection(selectedIndex);
                }
                spinner.setVisibility(View.VISIBLE);
                spinner.setOnItemSelectedListener(this);
            }
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
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_lunch_provided);
        if (tvProvision != null && allowance.getLunchProvision() != null) {
            if(isEditable){
             tvProvision.setVisibility(View.GONE);
            }else {
                tvProvision.setVisibility(View.VISIBLE);
                tvProvision.setText(allowance.getLunchProvision().toString());
            }
        }
        if (svSwitch != null && allowance.getLunchProvision() != null) {
            if (isEditable) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX) == true) {
                    svSwitch.setVisibility(View.VISIBLE);
                    svSwitch.setTextOn(getString(MealProvisionEnum.PROVIDED.getResourceId()));
                    svSwitch.setTextOff(getString(MealProvisionEnum.NOT_PROVIDED.getResourceId()));
                    if (MealProvision.NOT_PROVIDED_CODE.equals(allowance.getLunchProvision().getCode())) {
                        svSwitch.setChecked(false);
//                        svSwitch.setTextOff(allowance.getLunchProvision().toString());
                    } else {
                        svSwitch.setChecked(true);
//                        svSwitch.setTextOn(allowance.getLunchProvision().toString());
                    }
//                    ;
                } else {
                    svSwitch.setVisibility(View.GONE);
                }
            } else {
                svSwitch.setVisibility(View.GONE);
            }
        }

        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX ) == false){
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_lunch_provided);
            renderSpinner(spinner, allowance.getLunchProvision());
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
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_dinner_provided);

        if (tvProvision != null && allowance.getDinnerProvision() != null) {
            if(isEditable){
                tvProvision.setVisibility(View.GONE);
            }else {
                tvProvision.setText(allowance.getDinnerProvision().toString());
            }
        }

        if (svSwitch != null && allowance.getDinnerProvision() != null) {
            if (isEditable) {
                if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX) == true) {
                    svSwitch.setVisibility(View.VISIBLE);
                    svSwitch.setTextOn(getString(MealProvisionEnum.PROVIDED.getResourceId()));
                    svSwitch.setTextOff(getString(MealProvisionEnum.NOT_PROVIDED.getResourceId()));
                    if (MealProvision.NOT_PROVIDED_CODE.equals(allowance.getDinnerProvision().getCode())) {
                        svSwitch.setChecked(false);
//                        svSwitch.setTextOff(allowance.getDinnerProvision().toString());
                    } else {
                        svSwitch.setChecked(true);
//                        svSwitch.setTextOn(allowance.getDinnerProvision().toString());
                    }
                    ;
                } else {
                    svSwitch.setVisibility(View.GONE);
                }
            } else {
                svSwitch.setVisibility(View.GONE);
            }

            if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX) == true) {
                Switch svSwitch1 = (Switch) this.findViewById(R.id.sv_dinner_provided);
//                renderSwitch(svSwitch1, allowance.getDinnerProvision());
            }

            if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX ) == false){
                Spinner spinner = (Spinner) this.findViewById(R.id.sp_dinner_provided);
                renderSpinner(spinner, allowance.getDinnerProvision());
            }

            if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
                tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL));
            }
        }
    }

    private void renderLodging(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        if (allowance.getLodgingType() == null || allowance.getOvernightIndicator()) {
            return;
        }
        View vgLodging = this.findViewById(R.id.rl_lodging);
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

    private void renderSwitch(Switch svSwitch, ICode provisionCode){
        if (svSwitch != null && provisionCode != null ){
            if (isEditable){
                svSwitch.setVisibility(View.VISIBLE);
                svSwitch.setTextOn(getString(MealProvisionEnum.PROVIDED.getResourceId()));
                svSwitch.setTextOff(getString(MealProvisionEnum.NOT_PROVIDED.getResourceId()));
                if (MealProvision.NOT_PROVIDED_CODE.equals(provisionCode.getCode())) {
                    svSwitch.setChecked(false);
                } else {
                    svSwitch.setChecked(true);
                }

            }else {
                svSwitch.setVisibility(View.GONE);
            }
        }

    }

    private void renderSpinner(Spinner spinner, ICode provisionCode){
        if (spinner != null && provisionCode != null) {
            if (isEditable){
                Map<String, ICode> map = controlData.getProvidedMealValues();
                List<String> list = new ArrayList<String>();
                int index = 0;
                int selectedIndex = -1;
                for (Map.Entry<String,ICode> entry : map.entrySet()){
                    if (entry.getValue().getCode().equals(provisionCode.getCode())){
                        selectedIndex = index;
                    }
                    list.add(entry.getValue().toString());
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                if (selectedIndex > -1) {
                    spinner.setSelection(selectedIndex);
                }
                spinner.setVisibility(View.VISIBLE);
//                    spinner.setOnItemSelectedListener(this);

            }else {
                spinner.setVisibility(View.GONE);
            }

        }
    }

    private void updateAllowanceFromUI(){

        MealProvision provision;

        //******* Breakfast Provision **************************************************************
        provision = null;
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX) == true) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
            provision = deriveMealProvisionFromSwitch(svSwitch);
        } else if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX) == false) {
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_breakfast_provided);
            provision = deriveMealProvisionFromSpinner(spinner);
        }
        if (provision != null) {
            allowance.setBreakfastProvision(provision);
        }


        //******* Lunch Provision ******************************************************************
        provision = null;
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX) == true) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_lunch_provided);
            provision = deriveMealProvisionFromSwitch(svSwitch);
        }else if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX) == false) {
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_lunch_provided);
            provision = deriveMealProvisionFromSpinner(spinner);
        }
        if (provision != null) {
            allowance.setLunchProvision(provision);
        }


        //******* Dinner Provision *****************************************************************
        provision = null;
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX) == true) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_dinner_provided);
            provision = deriveMealProvisionFromSwitch(svSwitch);
        }else if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX) == false){
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_dinner_provided);
            provision = deriveMealProvisionFromSpinner(spinner);
        }
        if (provision != null) {
            allowance.setDinnerProvision(provision);
        }

    }

    private MealProvision deriveMealProvisionFromSwitch(Switch svSwitch ){
        if (svSwitch != null){
            if (svSwitch.isChecked()){
                return new MealProvision(MealProvision.PROVIDED_CODE, "");
            }else {
                return new MealProvision(MealProvision.NOT_PROVIDED_CODE, "");
            }
        }
        return null;
    }

    private MealProvision deriveMealProvisionFromSpinner(Spinner spinner ){
        if (spinner != null) {
            int i = spinner.getSelectedItemPosition();

            Map<String, ICode> map = controlData.getProvidedMealValues();

            int index = 0;

            for (Map.Entry<String,ICode> entry : map.entrySet()){
                if (i == index){
                    return new MealProvision(entry.getKey(), "");
                }
                index++;
            }
        }
        return null;
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Object o = adapterView.getItemAtPosition(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}