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
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.TaConfig;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
//import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceConfigurationController;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
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

public class FixedTravelAllowanceDetailsActivity extends BaseActivity implements IControllerListener {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = FixedTravelAllowanceDetailsActivity.class
            .getSimpleName();

    /**
     * The fixed travel allowance this activity is dealing with taken from intent
     */
    private FixedTravelAllowance allowance;

    private IDateFormat dateFormatter;

    private FixedTravelAllowanceController allowanceController;

//    private TaConfig config;
    private FixedTravelAllowanceControlData controlData;

    private boolean isEditable;
//    private FixedTravelAllowance allowance;
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
        allowance = null;
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
        allowanceController.registerListener(this);

        controlData = allowanceController.getControlData();

        renderHeader(allowance);
        renderBreakfast(allowance);
        renderLunch(allowance);
        renderDinner(allowance);
        renderLodging(allowance);
        renderOvernight(allowance);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (allowanceController != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onDestroy", "Unregister myself as listener at FixedTravelAllowanceController."));
            allowanceController.unregisterListener(this);
        }
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

    }

    /**
     * Render breakfast section
     * @param allowance
     */
    private void renderBreakfast(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_breakfast_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_breakfast_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_breakfast_provided);

        renderTextViewProvision(tvProvision, allowance.getBreakfastProvision());

        renderSwitch( svSwitch, allowance.getBreakfastProvision(), controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX));

        renderSpinner( spinner, allowance.getBreakfastProvision(), !controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX));

        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL));
        }
    }

    /**
     * Render lunch section
     * @param allowance
     */
    private void renderLunch(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_lunch_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_lunch_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_lunch_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_lunch_provided);

        renderTextViewProvision(tvProvision, allowance.getLunchProvision());

        renderSwitch( svSwitch, allowance.getLunchProvision(), controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX) );

        renderSpinner( spinner, allowance.getLunchProvision(), !controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX ) );

        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL));
        }
    }

    /**
     * Render dinner section
     * @param allowance
     */
    private void renderDinner(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        TextView tvProvision = (TextView) this.findViewById(R.id.tv_dinner_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_dinner_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_dinner_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_dinner_provided);

        renderTextViewProvision(tvProvision, allowance.getDinnerProvision());

        renderSwitch(svSwitch, allowance.getDinnerProvision(), controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX));

        renderSpinner(spinner, allowance.getDinnerProvision(), !controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX ) );

        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL));
        }
    }

    /**
     * Render lodging section
     * @param allowance
     */
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

    /**
     *
     * @param tvProvision       text view
     * @param provisionCode     current meal provision code
     */
    private void renderTextViewProvision (TextView tvProvision, ICode provisionCode){
        if (tvProvision != null){
            if (isEditable){
                tvProvision.setVisibility(View.GONE);
            } else {
                tvProvision.setVisibility(View.VISIBLE);
                if (provisionCode != null){
                    tvProvision.setText(provisionCode.toString());
                }
            }
        }
    }

    /**
     * Renders the switches at least in the three meals sections
     * Switch is only rendered if isEditable is true
     *
     * @param svSwitch          switch view
     * @param provisionCode     current meal provision code
     * @param visible           indicates if either the switch or the spinner should be rendered
     */
    private void renderSwitch(Switch svSwitch, ICode provisionCode, boolean visible){
        if (svSwitch != null ){
            if (visible == false || provisionCode == null || isEditable == false ){
                svSwitch.setVisibility(View.GONE);
            }else {
                svSwitch.setVisibility(View.VISIBLE);
                svSwitch.setTextOn(getString(MealProvisionEnum.PROVIDED.getResourceId()));
                svSwitch.setTextOff(getString(MealProvisionEnum.NOT_PROVIDED.getResourceId()));

                if (MealProvision.NOT_PROVIDED_CODE.equals(provisionCode.getCode())) {
                    svSwitch.setChecked(false);
                } else {
                    svSwitch.setChecked(true);
                }
            }
        }
    }

    /**
     * Renders the spinners at least in the three meals sections
     * Spinner is only rendered if isEditable is true
     *
     * @param spinner           spinner view
     * @param provisionCode     current meal provision code
     * @param visible           indicates if either the switch or the spinner should be rendered
     */
    private void renderSpinner(Spinner spinner, ICode provisionCode, boolean visible){
        if (spinner != null) {
            if (visible == false || provisionCode == null || isEditable == false) {
                spinner.setVisibility(View.GONE);
            } else {
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
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {

        if (action == ControllerAction.UPDATE) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                    "Update Action callback finished with isSuccess: " + isSuccess));
            if (isSuccess) {
//                Itinerary createdItinerary = (Itinerary) result.getSerializable(BundleId.ITINERARY);
//                this.itinerary = createdItinerary;
//                refreshAdapter();
//                // Update, respectively the creation of itineraries will generate Fixed Travel Allowances.
//                // We need to refresh the buffered Allowances as navigation back to the allowance overview
//                // would show the old data.
//                ConcurCore app = (ConcurCore) getApplication();
//                FixedTravelAllowanceController allowanceController = app.getFixedTravelAllowanceController();
//                allowanceController.refreshFixedTravelAllowances(itinerary.getExpenseReportID());

                Toast.makeText(this, R.string.general_save_success, Toast.LENGTH_SHORT).show();

                allowanceController.refreshFixedTravelAllowances(expenseReportKey);

            } else {
                Toast.makeText(this, R.string.general_save_fail, Toast.LENGTH_SHORT).show();
            }
        }

        if (action == ControllerAction.REFRESH ) {
            allowance = allowanceController.getAllowanceByDate(allowance.getDate());
            renderHeader(allowance);
        }

    }

}