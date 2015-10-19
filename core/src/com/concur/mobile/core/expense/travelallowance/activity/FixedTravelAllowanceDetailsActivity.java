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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.MessageDialogFragment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventTracker.EventTrackerClassName(getClassName = FixedTravelAllowanceDetailsActivity.SCREEN_NAME_TRAVEL_ALLOWANCE_FIXED_DETAIL)
public class FixedTravelAllowanceDetailsActivity extends BaseActivity implements IControllerListener, IFragmentCallback {

    public static final String SCREEN_NAME_TRAVEL_ALLOWANCE_FIXED_DETAIL = "Allowance Details: Expense-Report-TravelAllowances-DailyAllowance";

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

    private static final String TAG_CONFIRM_DIALOG_FRAGMENT =
            CLASS_TAG + ".confirm.dialog.fragment";

    private static final String MSG_DIALOG_DIRTY_POSITIVE =
            CLASS_TAG + ".message.dialog.dirty.positive";

    private static final String MSG_DIALOG_DIRTY_NEUTRAL =
            CLASS_TAG + ".message.dialog.dirty.neutral";

    private static final String MSG_DIALOG_DIRTY_NEGATIVE =
            CLASS_TAG + ".message.dialog.dirty.negative";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ta_fixed_travel_allowance_details_activity);



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
        this.allowanceController = app.getTaController().getFixedTravelAllowanceController();
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
        inflater.inflate(R.menu.ta_itinerary_save_menu, menu);
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
            item.setEnabled(false);
            onSave();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onSave() {
        if (this.allowance == null) {
            return;
        }
        List<FixedTravelAllowance> allowances = new ArrayList<FixedTravelAllowance>();
        allowances.add(this.allowance);
        allowanceController.executeUpdate(allowances, expenseReportKey, null);
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
        disableAllFields();
    }

    @Override
    public void onBackPressed() {
        FixedTravelAllowance originAllowance = null;
        if (this.allowance != null) {
            updateAllowanceFromUI();
            originAllowance = allowanceController.getFixedTA(allowance.getFixedTravelAllowanceId());
        }
        if (this.allowance == null || !this.allowance.equals(originAllowance)) {//is dirty
            showIsDirtyDialog();
            return;
        }
        super.onBackPressed();
    }

    private void showIsDirtyDialog() {
        Bundle bundle = new Bundle();
        String msgText = getResources().getString(R.string.confirm_save_report_message);
        bundle.putString(MessageDialogFragment.MESSAGE_TEXT, msgText);
        bundle.putString(MessageDialogFragment.POSITIVE_BUTTON, MSG_DIALOG_DIRTY_POSITIVE);
        bundle.putString(MessageDialogFragment.NEUTRAL_BUTTON, MSG_DIALOG_DIRTY_NEUTRAL);
        bundle.putString(MessageDialogFragment.NEGATIVE_BUTTON, MSG_DIALOG_DIRTY_NEGATIVE);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(bundle);
        messageDialog.show(getSupportFragmentManager(), TAG_CONFIRM_DIALOG_FRAGMENT);
    }

    /**
     * Render breakfast section
     * @param allowance
     */
    private void renderBreakfast(FixedTravelAllowance allowance) {
        if (allowance == null) {
            return;
        }
        RelativeLayout rlLayout = (RelativeLayout) this.findViewById(R.id.vg_breakfast);
        if (rlLayout == null){
            return;
        }

        //Section should not be rendered for traveller if the customizing for the traveller doesn't allow it
        if ((controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX) == false
                && controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST) == false)
                || allowance.getBreakfastProvision() == null) {
            rlLayout.setVisibility(View.GONE);
            makeDividerGone(R.id.v_divider_breakfast);
            return;
        }

        TextView tvProvision = (TextView) this.findViewById(R.id.tv_breakfast_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_breakfast_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_breakfast_provided);

        renderTextViewProvision(tvProvision, allowance.getBreakfastProvision());

        renderSwitch(svSwitch, allowance.getBreakfastProvision(),
                allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL),
                controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX));
        renderSpinner(spinner, allowance.getBreakfastProvision(), null, controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST));

        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL));
            if (isEditable == true && controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX)) {
                tvLabel.setVisibility(View.GONE);
            }
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
        RelativeLayout rlLayout = (RelativeLayout) this.findViewById(R.id.rl_lunch);
        if (rlLayout == null){
            return;
        }

        //Section should not be rendered for traveller if the customizing for the traveller doesn't allow it
        if (
                ( controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX) == false &&
                  controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST) == false)
                        || allowance.getLunchProvision() == null){
            rlLayout.setVisibility(View.GONE);
            makeDividerGone(R.id.v_divider_lunch);
            return;
        }

        TextView tvProvision = (TextView) this.findViewById(R.id.tv_lunch_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_lunch_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_lunch_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_lunch_provided);

        renderTextViewProvision(tvProvision, allowance.getLunchProvision());

        renderSwitch(svSwitch, allowance.getLunchProvision(),
                     allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL),
                     controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX));
        renderSpinner(spinner, allowance.getLunchProvision(), null, controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST));

        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL));
            if (isEditable == true && controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX)) {
                tvLabel.setVisibility(View.GONE);
            }

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
        RelativeLayout rlLayout = (RelativeLayout) this.findViewById(R.id.rl_dinner);
        if (rlLayout == null){
            return;
        }

        //Section should not be rendered for traveller if the customizing for the traveller doesn't allow it
        if (
                ( controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX) == false &&
                  controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST) == false)
                        || allowance.getDinnerProvision() == null){
            rlLayout.setVisibility(View.GONE);
            makeDividerGone(R.id.v_divider_dinner);
            return;
        }

        TextView tvProvision = (TextView) this.findViewById(R.id.tv_dinner_provision);
        TextView tvLabel = (TextView) this.findViewById(R.id.tv_dinner_label);
        Switch svSwitch = (Switch) this.findViewById(R.id.sv_dinner_provided);
        Spinner spinner = (Spinner) this.findViewById(R.id.sp_dinner_provided);

        renderTextViewProvision(tvProvision, allowance.getDinnerProvision());

        renderSwitch(svSwitch, allowance.getDinnerProvision(),
                allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL),
                controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX));
        renderSpinner(spinner, allowance.getDinnerProvision(), null, controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST));

        if (tvLabel != null && allowanceController != null && allowanceController.getControlData() != null) {
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL));
            if (isEditable == true && controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX)) {
                tvLabel.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Render lodging section
     * @param allowance
     */
    private void renderLodging(FixedTravelAllowance allowance) {
        if (allowance == null || allowance.getLodgingType() == null) {
            return;
        }
        //Section should not be rendered for approver if no relevant data is available
        if (( isEditable == false )&& (allowance.getLodgingType() == null || allowance.getOvernightIndicator())) {
            makeDividerGone(R.id.v_divider_lodging);
//            View divider = (View) this.findViewById(R.id.v_divider_lodging);
//            if (divider != null) {
//                divider.setVisibility(View.GONE);
//            }
            return;
        }
        //Section should not be rendered for traveller if the customizing for the traveller doesn't allow it
        if (isEditable == true && controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LODGING_TYPE_PICKLIST) == false){
            makeDividerGone(R.id.v_divider_lodging);
            return;
        }

        View vgLodging = this.findViewById(R.id.rl_lodging);
        if (vgLodging == null) {
            makeDividerGone(R.id.v_divider_lodging);
            return;
        }
        vgLodging.setVisibility(View.VISIBLE);

        Spinner spinner = (Spinner) this.findViewById(R.id.sp_lodging_provided);
        if (spinner == null) {
            return;
        }

        if (isEditable) {
//            ArrayAdapter<ICode> adapter = new ArrayAdapter<ICode>(this, android.R.layout.simple_spinner_item, new ArrayList<ICode>(controlData.getLodgingTypeValues().values()));
//            spinner.setAdapter(adapter);
            renderSpinner(spinner, allowance.getLodgingType(), controlData.getLodgingTypeValues(), true);
        } else {
            spinner.setVisibility(View.GONE);
        }
        TextView tvValue = (TextView) this.findViewById(R.id.tv_lodging_value);
        renderTextViewProvision(tvValue, allowance.getLodgingType());
            if (tvValue != null) {
                tvValue.setText(allowance.getLodgingType().toString());
            }

        TextView tvLabel = (TextView) this.findViewById(R.id.tv_lodging_label);
        if (tvLabel != null){
            tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.LODGING_TYPE_LABEL));
        }

    }

    private void renderOvernight(FixedTravelAllowance allowance) {
        if (allowance == null || allowance.isLastDay()) {
            // This section should not be rendered if the ta is for the last day of an itinerary.
            return;
        }
        //Section should not be rendered for approver if no relevant data is available
        if ((isEditable == false) && (!allowance.getOvernightIndicator())) {
            return;
        }
        //Section should not be rendered for traveller if the customizing for the traveller doesn't allow it
        if (isEditable == true && controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX) == false){
            return;
        }

        View vgOvernight = this.findViewById(R.id.vg_overnight);
        if (vgOvernight == null ) {
            return;
        }
        vgOvernight.setVisibility(View.VISIBLE);

        if (isEditable) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_overnight);
            if (svSwitch == null) {
                return;
            }
            if (isEditable) {
                svSwitch.setVisibility(View.VISIBLE);
                svSwitch.setTextOn(getResources().getString(R.string.general_yes));
                svSwitch.setTextOff(getResources().getString(R.string.general_no));
                svSwitch.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.OVERNIGHT_LABEL));
                svSwitch.setChecked(allowance.getOvernightIndicator());

            }

            TextView label = (TextView) findViewById(R.id.tv_overnight_label);
            if (label != null){
                label.setVisibility(View.GONE);
            }

        }else{
            TextView textView = (TextView) findViewById(R.id.tv_overnight_help);
            if (textView == null) {
                return;
            }
            if (isEditable == true) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                if (allowance.getOvernightIndicator()) {
                    textView.setText(R.string.general_yes);
                } else {
                    textView.setText(R.string.general_no);
                }
            }
            TextView tvLabel = (TextView) this.findViewById(R.id.tv_overnight_label);
            if (tvLabel != null){
                tvLabel.setText(allowanceController.getControlData().getLabel(FixedTravelAllowanceControlData.OVERNIGHT_LABEL));
            }

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
    private void renderSwitch(Switch svSwitch, ICode provisionCode, String label, boolean visible){
        if (svSwitch != null ){
            if (visible == false || provisionCode == null || isEditable == false ){
                svSwitch.setVisibility(View.GONE);
            }else {
                svSwitch.setVisibility(View.VISIBLE);
                svSwitch.setTextOn(getString(MealProvisionEnum.PROVIDED.getResourceId()));
                svSwitch.setTextOff(getString(MealProvisionEnum.NOT_PROVIDED.getResourceId()));
                svSwitch.setText(label);
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
    private void renderSpinner(Spinner spinner, ICode provisionCode, Map<String, ICode> map, boolean visible){
        if (spinner != null) {
            if (visible == false || provisionCode == null || isEditable == false) {
                spinner.setVisibility(View.GONE);
            } else {
                if (map == null) {
                    map = controlData.getProvidedMealValues();
                }
                List<ICode> list = new ArrayList<ICode>(map.values());

                ArrayAdapter<ICode> adapter = new ArrayAdapter<ICode>(this, android.R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                int selectedIndex = list.indexOf(provisionCode);
                if (selectedIndex > -1) {
                    spinner.setSelection(selectedIndex);
                }
                spinner.setVisibility(View.VISIBLE);
            }
        }
    }


    private void makeDividerGone(int resourceID){
        View divider = (View) this.findViewById(resourceID);
        if (divider != null) {
            divider.setVisibility(View.GONE);
        }
    }

    private void updateAllowanceFromUI(){
        MealProvision provision;

        if (isEditable == false){
            return;
        }

        //******* Breakfast Provision **************************************************************
        provision = (MealProvision) allowance.getBreakfastProvision();
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX) == true && provision != null) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_breakfast_provided);
            provision = deriveMealProvisionFromSwitch(svSwitch);
        } else if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST) == true && provision != null) {
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_breakfast_provided);
            provision = deriveMealProvisionFromSpinner(spinner);
        }
        if (provision != null) {
            allowance.setBreakfastProvision(provision);
        }

        //******* Lunch Provision ******************************************************************
        provision = (MealProvision) allowance.getLunchProvision();
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX) == true && provision != null) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_lunch_provided);
            provision = deriveMealProvisionFromSwitch(svSwitch);
        }else if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST) == true && provision != null) {
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_lunch_provided);
            provision = deriveMealProvisionFromSpinner(spinner);
        }
        if (provision != null) {
            allowance.setLunchProvision(provision);
        }

        //******* Dinner Provision *****************************************************************
        provision = (MealProvision) allowance.getDinnerProvision();
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX) == true && provision != null) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_dinner_provided);
            provision = deriveMealProvisionFromSwitch(svSwitch);
        }else if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST) == true && provision != null){
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_dinner_provided);
            provision = deriveMealProvisionFromSpinner(spinner);
        }
        if (provision != null) {
            allowance.setDinnerProvision(provision);
        }

        //******* Lodging **************************************************************************
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LODGING_TYPE_PICKLIST) == true
                && allowance.getLodgingType() != null) {
            LodgingType lodgingType = null;
            Spinner spinner = (Spinner) this.findViewById(R.id.sp_lodging_provided);
            lodgingType = deriveLodgingProvisionFromSpinner(spinner);
            if (lodgingType != null) {
                allowance.setLodgingType(lodgingType);
            }
        }

        //******* Overnight Indicator **************************************************************
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX) == true) {
            Switch svSwitch = (Switch) this.findViewById(R.id.sv_overnight);
            if(svSwitch != null){
                allowance.setOvernightIndicator(svSwitch.isChecked());
            }
        }

    }

    private MealProvision deriveMealProvisionFromSwitch(Switch svSwitch ){
        if (svSwitch != null){
            if (svSwitch.isChecked()){
                return MealProvisionEnum.fromCode(MealProvision.PROVIDED_CODE, this);
            }else {
                return MealProvisionEnum.fromCode(MealProvision.NOT_PROVIDED_CODE, this);
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
                    return new MealProvision(entry.getKey(), entry.getValue().getDescription());
                }
                index++;
            }
        }
        return null;
    }

    private LodgingType deriveLodgingProvisionFromSpinner(Spinner spinner ){
        if (spinner != null) {
            int i = spinner.getSelectedItemPosition();

            Map<String, ICode> map = controlData.getLodgingTypeValues();

            int index = 0;

            for (Map.Entry<String,ICode> entry : map.entrySet()){
                if (i == index){
                    return new LodgingType(entry.getKey(), entry.getValue().getDescription());
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
                //Toast.makeText(this, R.string.general_save_success, Toast.LENGTH_SHORT).show();
                //allowanceController.refreshFixedTravelAllowances(expenseReportKey);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
                resultIntent.putExtra(BundleId.REFRESH_FIXED_TA, true);
                this.setResult(RESULT_OK, resultIntent);
                super.onBackPressed(); //Leave the screen on success
            } else {
                Toast.makeText(this, R.string.general_save_fail, Toast.LENGTH_SHORT).show();
            }
        }

        if (action == ControllerAction.REFRESH ) {
            allowance = allowanceController.getAllowanceByDate(allowance.getDate());
            renderHeader(allowance);
        }

    }

    private void disableAllFields() {
        findViewById(R.id.sv_breakfast_provided).setEnabled(false);
        findViewById(R.id.sp_breakfast_provided).setEnabled(false);
        findViewById(R.id.sv_lunch_provided).setEnabled(false);
        findViewById(R.id.sp_lunch_provided).setEnabled(false);
        findViewById(R.id.sv_dinner_provided).setEnabled(false);
        findViewById(R.id.sp_dinner_provided).setEnabled(false);
        findViewById(R.id.sp_lodging_provided).setEnabled(false);
        findViewById(R.id.sv_overnight).setEnabled(false);
    }

    @Override
    public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", "message = " + fragmentMessage));
        if (MSG_DIALOG_DIRTY_NEGATIVE.equals(fragmentMessage)) {
            super.onBackPressed();
        }
        if (MSG_DIALOG_DIRTY_POSITIVE.equals(fragmentMessage)) {
            onSave();
        }
    }
}