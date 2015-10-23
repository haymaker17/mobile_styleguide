package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.adapter.FixedTADetailAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.MessageDialogFragment;
import com.concur.mobile.core.expense.travelallowance.ui.model.SelectedTravelAllowancesList;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@EventTracker.EventTrackerClassName(getClassName = FixedTravelAllowanceDetailsActivity.SCREEN_NAME_TRAVEL_ALLOWANCE_FIXED_DETAIL)
public class FixedTravelAllowanceDetailsActivity extends TravelAllowanceBaseActivity implements IControllerListener, IFragmentCallback, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

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

    private boolean isEditable;
    private String expenseReportKey;
    
    private SelectedTravelAllowancesList massEditList;

    public static final String INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE =
            FixedTravelAllowanceDetailsActivity.class.getName() + "FixedTravelAllowance";
    
    public static final String INTENT_EXTRA_KEY_MASS_EDIT_LIST = "mass.edit.list";

    private static final String TAG_CONFIRM_DIALOG_FRAGMENT =
            CLASS_TAG + ".confirm.dialog.fragment";

    private static final String MSG_DIALOG_DIRTY_POSITIVE =
            CLASS_TAG + ".message.dialog.dirty.positive";

    private static final String MSG_DIALOG_DIRTY_NEUTRAL =
            CLASS_TAG + ".message.dialog.dirty.neutral";

    private static final String MSG_DIALOG_DIRTY_NEGATIVE =
            CLASS_TAG + ".message.dialog.dirty.negative";


    private static final String TAG_BREAKFAST = "breakfast";
    private static final String TAG_LUNCH = "lunch";
    private static final String TAG_DINNER = "dinner";
    private static final String TAG_LODGING = "lodging";
    private static final String TAG_OVERNIGHT = "overnight";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (massEditList != null && massEditList.size() > 0) {
            this.allowance = massEditList.getTemplate();
        }

        if (StringUtilities.isNullOrEmpty(expenseReportKey) || allowance.isLocked()) {
            isEditable = false;
        }

        fixedTaController.registerListener(this);

        populateUi();

    }

    protected void populateUi() {
        if (allowance == null) {
            return;
        }
        FixedTADetailAdapter adapter = new FixedTADetailAdapter(this, createValueHolderList());
        adapter.setSpinnerListener(this);
        adapter.setSwitchListener(this);
        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(llm);
        if (massEditList == null) {
            renderHeader(allowance);
        } else {
            findViewById(R.id.vg_details_header).setVisibility(View.GONE);
        }

    }


    @Override
    protected int getContentViewId() {
        return R.layout.ta_fixed_travel_allowance_details_activity;
    }

    @Override
    protected String getToolbarTitle() {
        if (massEditList == null) {
            return getString(R.string.ta_daily_allowance);
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(getString(R.string.ta_adjustments));
            sb.append(" (");
            sb.append(massEditList.size());
            sb.append(")");
            return sb.toString();
        }

    }

    @Override
    protected void handleCallerIntent() {
        super.handleCallerIntent();

        Intent callerIntent = this.getIntent();
        if (callerIntent != null) {
            allowance = (FixedTravelAllowance) callerIntent
                    .getSerializableExtra(INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE);
            isEditable = !callerIntent.getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, true)
                    && callerIntent.getBooleanExtra(BundleId.IS_EDIT_MODE, false);
            if (callerIntent.hasExtra(BundleId.EXPENSE_REPORT_KEY)) {
                expenseReportKey = callerIntent.getStringExtra(BundleId.EXPENSE_REPORT_KEY);
            }

            Object list = callerIntent.getSerializableExtra(INTENT_EXTRA_KEY_MASS_EDIT_LIST);
            if (list != null) {
                // I would expect a SelectedTravelAllowanceList object but there is a Android bug.
                // In case your Object implements List and Serializable and you put it to a Bundle, the getSerializable method
                // returns always an ArrayList object which cannot be casted to your original object.
                if (list instanceof ArrayList) {
                    massEditList = new SelectedTravelAllowancesList((ArrayList)list);
                } else if (list instanceof SelectedTravelAllowancesList) {
                    // This else branch will be passed only in case google provide a fix for the bug mentioned above.
                    // From experience I know google never provide fixed for such bugs but miracles can happen ;-)
                    // https://code.google.com/p/android/issues/detail?id=3847
                    massEditList = (SelectedTravelAllowancesList) list;
                }
            }

            if (massEditList != null && massEditList.size() > 0) {
                isEditable = true;
            }
        }
    }

    @Override
    protected void initializeToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
        if (massEditList != null && massEditList.size() > 0) {
            TextView toolbarText = (TextView) findViewById(R.id.tv_toolbar_text);
            if (toolbarText != null) {
                toolbarText.setText(getResources().getString(R.string.general_no_of_selected_list_items_android,  massEditList.size()));
                toolbarText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (fixedTaController != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onDestroy",
                    "Unregister myself as listener at FixedTravelAllowanceController."));
            fixedTaController.unregisterListener(this);
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

        if (item.getItemId() == R.id.menuSave && this.allowance != null) {
            item.setEnabled(false);
            onSave();
        }

        return super.onOptionsItemSelected(item);
    }


    private List<FixedTADetailAdapter.ValueHolder> createValueHolderList() {
        List<FixedTADetailAdapter.ValueHolder> result = new ArrayList<FixedTADetailAdapter.ValueHolder>();
        FixedTravelAllowanceControlData controlData = fixedTaController.getControlData();

        if (allowance.getBreakfastProvision() != null) {
            boolean isBrMulti = false;
            if (massEditList != null) {
                isBrMulti = massEditList.isBreakfastMultiSelected();
            }
            FixedTADetailAdapter.ValueHolder breakfast = fromCode( TAG_BREAKFAST,
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX),
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST),
                    fixedTaController.getControlData().getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL),
                    allowance.getBreakfastProvision(), false, false, isBrMulti);
            if (breakfast != null) {
                result.add(breakfast);
            }
        }

        if (allowance.getLunchProvision() != null) {
            boolean isLuMulti = false;
            if (massEditList != null) {
                isLuMulti = massEditList.isLunchMultiSelected();
            }
            FixedTADetailAdapter.ValueHolder lunch = fromCode( TAG_LUNCH,
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX),
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST),
                    fixedTaController.getControlData().getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL),
                    allowance.getLunchProvision(), false, false, isLuMulti);
            if (lunch != null) {
                result.add(lunch);
            }
        }

        if (allowance.getDinnerProvision() != null) {
            boolean isDiMulti = false;
            if (massEditList != null) {
                isDiMulti = massEditList.isDinnerMultiSelected();
            }
            FixedTADetailAdapter.ValueHolder dinner = fromCode( TAG_DINNER,
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX),
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST),
                    fixedTaController.getControlData().getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL),
                    allowance.getDinnerProvision(), false, false, isDiMulti);
            if (dinner != null) {
                result.add(dinner);
            }
        }

        if (allowance.getLodgingType() != null) {
            boolean isLoMulti = false;
            if (massEditList != null) {
                isLoMulti = massEditList.isLodgingTypeMultiSelected();
            }
            FixedTADetailAdapter.ValueHolder lodgingType = fromCode( TAG_LODGING,
                    false,
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LODGING_TYPE_PICKLIST),
                    fixedTaController.getControlData().getLabel(FixedTravelAllowanceControlData.LODGING_TYPE_LABEL),
                    allowance.getLodgingType(), true, false, isLoMulti);
            if (lodgingType != null) {
                result.add(lodgingType);
            }
        }

        boolean isLastDay = allowance.isLastDay();
        if (massEditList != null && !massEditList.hasOnlyLastDays()) {
            isLastDay = false;
        }
        if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_OVERNIGHT_CHECKBOX) == true && !isLastDay) {
            boolean isOnMulti = false;
            if (massEditList != null) {
                isOnMulti = massEditList.isOvernightMultiSelected();
            }
            FixedTADetailAdapter.ValueHolder overnight = fromCode( TAG_OVERNIGHT,
                    true,
                    false,
                    fixedTaController.getControlData().getLabel(FixedTravelAllowanceControlData.OVERNIGHT_LABEL),
                    null, false, true, isOnMulti);
            if (overnight != null) {
                result.add(overnight);
            }
        }

        return result;
    }


    private FixedTADetailAdapter.ValueHolder fromCode(String tag, boolean isCheckBox, boolean isPickList, String label, ICode code,
                                                      boolean isLodgingType, boolean isOvernight, boolean multiSelect) {
        if (!isCheckBox && !isPickList) {
            return null;
        }
        FixedTravelAllowanceControlData controlData = fixedTaController.getControlData();
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = tag;
        vh.multiValuesSelected = multiSelect;

        if (isCheckBox) {
            vh.rowType = FixedTADetailAdapter.RowType.SWITCH;
            if (!isOvernight && MealProvision.PROVIDED_CODE.equals(code.getCode())) {
                vh.isChecked = true;
            }
            if (isOvernight) {
                vh.isChecked = allowance.getOvernightIndicator();
            }
        } else if (isPickList) {
            vh.rowType = FixedTADetailAdapter.RowType.SPINNER;
            List<ICode> values = null;
            if (isLodgingType) {
                values = new ArrayList<ICode>(controlData.getLodgingTypeValues().values());
            } else {
                values = new ArrayList<ICode>(controlData.getProvidedMealValues().values());
            }
            vh.spinnerValues = values;
            vh.selectedSpinnerPosition = values.indexOf(code);
            vh.readOnlyValue = code.getDescription();
        }
        vh.isReadOnly = !isEditable;
        vh.label = label;
        return vh;
    }


    private void onSave() {
        if (this.allowance == null) {
            return;
        }
        List<FixedTravelAllowance> allowances = new ArrayList<FixedTravelAllowance>();
        if (massEditList != null && massEditList.size() > 0) {
            allowances = fixedTaController.applyTaValues(this.allowance, massEditList);
        } else {
            allowances.add(this.allowance);
        }
        fixedTaController.executeUpdate(allowances, expenseReportKey, null);
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        FixedTravelAllowance originAllowance = null;
        if (this.allowance != null) {
            originAllowance = fixedTaController.getFixedTA(allowance.getFixedTravelAllowanceId());
        }
        if (this.allowance == null || !this.allowance.equals(originAllowance)) {//is dirty
            showIsDirtyDialog();
            return;
        }
        setResult(RESULT_CANCELED);
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
            String dateString = DateUtils.formatDateTime(this, allowance.getDate().getTime(),
                                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR |
                                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY);
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
                this.finish();
            } else {
                Toast.makeText(this, R.string.general_save_fail, Toast.LENGTH_SHORT).show();
            }
        }

        if (action == ControllerAction.REFRESH ) {
            allowance = fixedTaController.getAllowanceByDate(allowance.getDate());
            renderHeader(allowance);
        }

    }


    @Override
    public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", "message = " + fragmentMessage));
        if (MSG_DIALOG_DIRTY_NEGATIVE.equals(fragmentMessage)) {
            setResult(RESULT_CANCELED);
            finish();
        }
        if (MSG_DIALOG_DIRTY_POSITIVE.equals(fragmentMessage)) {
            onSave();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (TAG_BREAKFAST.equals(buttonView.getTag())) {
            if (isChecked) {
                allowance.setBreakfastProvision(MealProvisionEnum.fromCode(MealProvision.PROVIDED_CODE, this));
            } else {
                allowance.setBreakfastProvision(MealProvisionEnum.fromCode(MealProvision.NOT_PROVIDED_CODE, this));
            }
        }
        if (TAG_LUNCH.equals(buttonView.getTag())) {
            if (isChecked) {
                allowance.setLunchProvision(MealProvisionEnum.fromCode(MealProvision.PROVIDED_CODE, this));
            } else {
                allowance.setLunchProvision(MealProvisionEnum.fromCode(MealProvision.NOT_PROVIDED_CODE, this));
            }
        }
        if (TAG_DINNER.equals(buttonView.getTag())) {
            if (isChecked) {
                allowance.setDinnerProvision(MealProvisionEnum.fromCode(MealProvision.PROVIDED_CODE, this));
            } else {
                allowance.setDinnerProvision(MealProvisionEnum.fromCode(MealProvision.NOT_PROVIDED_CODE, this));
            }
        }
        if (TAG_OVERNIGHT.equals(buttonView.getTag())) {
            allowance.setOvernightIndicator(isChecked);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        List<ICode> meals = new ArrayList<>(fixedTaController.getControlData().getProvidedMealValues().values());
        List<ICode> lodging = new ArrayList<>(fixedTaController.getControlData().getLodgingTypeValues().values());

        if (TAG_BREAKFAST.equals(parent.getTag()) && position < meals.size()) {
            MealProvision mealProvision = new MealProvision(meals.get(position).getCode(),
                    meals.get(position).getDescription());
            allowance.setBreakfastProvision(mealProvision);
        }
        if (TAG_LUNCH.equals(parent.getTag()) && position < meals.size()) {
            MealProvision mealProvision = new MealProvision(meals.get(position).getCode(),
                    meals.get(position).getDescription());
            allowance.setLunchProvision(mealProvision);
        }
        if (TAG_DINNER.equals(parent.getTag()) && position < meals.size()) {
            MealProvision mealProvision = new MealProvision(meals.get(position).getCode(),
                    meals.get(position).getDescription());
            allowance.setDinnerProvision(mealProvision);
        }
        if (TAG_LODGING.equals(parent.getTag())&& position < lodging.size()) {
            LodgingType lodgingType = new LodgingType(lodging.get(position).getCode(),
                    lodging.get(position).getDescription());
            allowance.setLodgingType(lodgingType);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
    }
}