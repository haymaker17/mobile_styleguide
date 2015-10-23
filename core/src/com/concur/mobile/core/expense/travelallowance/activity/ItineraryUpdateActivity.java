package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.CodeListManager;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.fragment.DatePickerFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.MessageDialogFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TimePickerFragment;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@EventTracker.EventTrackerClassName(getClassName = ItineraryUpdateActivity.SCREEN_NAME_TRAVEL_ALLOWANCE_ITIN_UPDATE)
public class ItineraryUpdateActivity extends BaseActivity implements IControllerListener, IFragmentCallback {

    public static final String SCREEN_NAME_TRAVEL_ALLOWANCE_ITIN_UPDATE = "Itin-View (Create/Edit) Expense-Report-TravelAllowances-Itinerary";

    /**
     * The name of this {@code Class}  logging purpose.
     */
    private static final String CLASS_TAG = ItineraryUpdateActivity.class
            .getSimpleName();

    private static final String TAG_CALENDAR_DIALOG_FRAGMENT =
            CLASS_TAG + ".calendar.dialog.fragment";

    private static final String TAG_TIME_DIALOG_FRAGMENT =
            CLASS_TAG + ".time.dialog.fragment";

    private static final String TAG_DELETE_DIALOG_FRAGMENT =
            CLASS_TAG + ".delete.dialog.fragment";

    private static final String TAG_CONFIRM_DIALOG_FRAGMENT =
            CLASS_TAG + ".confirm.dialog.fragment";

    private static final String TAG_ERROR_DIALOG_FRAGMENT =
            CLASS_TAG + ".error.dialog.fragment";

    private static final String MSG_DIALOG_DELETE_POSITIVE =
            CLASS_TAG + ".message.dialog.delete.positive";

    private static final String MSG_DIALOG_DELETE_NEUTRAL =
            CLASS_TAG + ".message.dialog.delete.neutral";

    private static final String MSG_DIALOG_DIRTY_POSITIVE =
            CLASS_TAG + ".message.dialog.dirty.positive";

    private static final String MSG_DIALOG_ERROR_POSITIVE =
            CLASS_TAG + ".message.dialog.error.positive";

    private static final String MSG_DIALOG_DIRTY_NEUTRAL =
            CLASS_TAG + ".message.dialog.dirty.neutral";

    private static final String MSG_DIALOG_DIRTY_NEGATIVE =
            CLASS_TAG + ".message.dialog.dirty.negative";

    private static final String MSG_DATE_PICKER_SET =
            CLASS_TAG + ".message.date.picker.set";

    private static final String MSG_TIME_PICKER_SET =
            CLASS_TAG + ".message.time.picker.set";

    private static final String TASK_CHAIN = "taskchain";

    private static final String ONLINE_CHECK_ACTIVE = "onlinecheckactive";

    private static final String ADDING_DISABLED = "addingdisabled";

    private static final int TIME_INTERVAL = 5;

    private Itinerary itinerary;
    private int taskChain;
    private String expenseReportKey;
    private TravelAllowanceItineraryController itinController;
    private FixedTravelAllowanceController allowanceController;
    private ItineraryUpdateListAdapter adapter;
    private PositionInfoTag currentPosition;
    private DatePickerFragment calendarDialog;
    private TimePickerFragment timeDialog;
    private Date defaultDate;
    private boolean expenseReportIsSubmitted;
    private boolean onlineCheckActive;
    private boolean addingDisabled;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.ta_update_activity);

        ConcurCore app = (ConcurCore) getApplication();
        this.itinController = app.getTaController().getTaItineraryController();
        this.allowanceController = app.getTaController().getFixedTravelAllowanceController();
        this.onlineCheckActive = false;

        if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY);
        }

        this.expenseReportIsSubmitted = getIntent().getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false);
        String expenseReportName = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_NAME);

        if (savedInstanceState == null) {//very first create
            this.itinerary = (Itinerary) getIntent().getExtras().getSerializable(BundleId.ITINERARY);
            if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_DATE)) {
                this.defaultDate = (Date) getIntent().getExtras().getSerializable(BundleId.EXPENSE_REPORT_DATE);
            }
        } else {
            Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onCreate", "Restoring itinerary from instance state"));
            //Get itinerary from instance state
            this.itinerary = (Itinerary) savedInstanceState.getSerializable(BundleId.ITINERARY);
            this.taskChain = savedInstanceState.getInt(TASK_CHAIN, 0);
            this.currentPosition = (PositionInfoTag) savedInstanceState.getSerializable(BundleId.POSITION_INFO_TAG);
            this.onlineCheckActive = savedInstanceState.getBoolean(ONLINE_CHECK_ACTIVE, false);
            this.addingDisabled = savedInstanceState.getBoolean(ADDING_DISABLED, false);
        }

        if (this.itinerary == null) {
            this.itinerary = new Itinerary();
            itinerary.setExpenseReportID(this.expenseReportKey);
            itinerary.setName(expenseReportName);
        }

        /* To hide the keyboard after the custom spinner selection was done. */
        Window window = this.getWindow();
        final int inputMode =
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        window.setSoftInputMode(inputMode);

        View.OnClickListener onDeleteItemClickListener;
        View.OnClickListener onTimeClickListener;
        View.OnClickListener onDateClickListener;
        View.OnClickListener onLocationClickListener;
        View.OnClickListener onReturnToHomeListener;
        View.OnClickListener onMessageAreaClickListener;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            TextView tvToolbarText = (TextView) findViewById(R.id.tv_toolbar_text);
            if (tvToolbarText != null) {
                tvToolbarText.setVisibility(View.GONE);
            }
            if (StringUtilities.isNullOrEmpty(this.itinerary.getItineraryID())) {
                actionBar.setTitle(R.string.ta_new_itinerary);
                if (tvToolbarText != null) {
                    tvToolbarText.setVisibility(View.VISIBLE);
                    tvToolbarText.setPadding(toolbar.getContentInsetStart(), 0, 0, 0);
                }
            } else {
                actionBar.setTitle(R.string.ta_edit_itinerary);
            }
        }

        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
        if (etItinerary != null) {
            etItinerary.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    return;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    return;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (itinerary != null && !s.toString().equals(itinerary.getName())) {
                        itinerary.setName(s.toString());
                        Message msg = itinerary.getMessage();
                        if ((msg == null && StringUtilities.isNullOrEmpty(s.toString()))
                                || (!StringUtilities.isNullOrEmpty(s.toString()) && addingDisabled)) {
                            if (msg != null) {
                                itinerary.setMessage(null);
                            }
                            checkConsistency();
                            renderNameLabel();
                        }
                    }
                }
            });
        }

        onDeleteItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                showDeleteDialog();
            }
        };

        onDateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                showCalendarDialog();
            }
        };

        onTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                showTimePickerDialog();
            }
        };

        onLocationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                Intent locationIntent = new Intent(ItineraryUpdateActivity.this, ItineraryListSearch.class);
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_IS_MRU, true);
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, "LocName");
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE, "RPTINFO");
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, getText(R.string.location)
                        .toString());
                startActivityForResult(locationIntent, Const.REQUEST_CODE_LOCATION);
            }
        };

        onReturnToHomeListener = null;
        if (!this.expenseReportIsSubmitted) {
            onReturnToHomeListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewRow(true);
                }
            };
        }

        onMessageAreaClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                showErrorDialog();
            }
        };

        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null && this.itinerary != null) {
            adapter = new ItineraryUpdateListAdapter(this, onDeleteItemClickListener, onLocationClickListener,
                    onDateClickListener, onTimeClickListener, onReturnToHomeListener, onMessageAreaClickListener, this.itinerary.getSegmentList());
            listView.setAdapter(adapter);
            adapter.setAddingDisabled(this.addingDisabled);
        }

        // In case of Create also create the first segment
        if (StringUtilities.isNullOrEmpty(this.itinerary.getItineraryID() ) && this.itinerary.getSegmentList().size() == 0){
            addNewRow(false);
        }

        //Register Controller Backend Communication Listeners
        itinController.registerListener(this);
        allowanceController.registerListener(this);

        renderNameLabel();
        renderDefaultValues();
        renderFAB();

        View fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewRow(false); //Adds a segment, which is not consistent -> disable FAB
                    addingDisabled = true;
                    renderFAB();
                }
            });
        }
    }

    private void addNewRow(boolean returnToHome) {
        ItinerarySegment emptySegment = new ItinerarySegment();
        //Get current date/time
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        if (itinerary.getSegmentList() != null) {
            if (itinerary.getSegmentList().size() > 0) {
                ItinerarySegment lastSegment = itinerary.getSegmentList().get(itinerary.getSegmentList().size() - 1);
                emptySegment.setDepartureLocation(lastSegment.getArrivalLocation());
                if (lastSegment.getArrivalDateTime() != null) {
                    cal.setTime(lastSegment.getArrivalDateTime());
                    cal.add(Calendar.MINUTE, TIME_INTERVAL);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    emptySegment.setDepartureDateTime(cal.getTime());
                    cal.add(Calendar.MINUTE, TIME_INTERVAL);
                    emptySegment.setArrivalDateTime(cal.getTime());
                }
                if (returnToHome && itinerary.getSegmentList().size() == 1 &&
                    lastSegment.getDepartureLocation() != null && lastSegment.getArrivalLocation() != null) {//Return to Home
                    emptySegment.setArrivalLocation(lastSegment.getDepartureLocation());
                    int timeZoneOffsetDifference = lastSegment.getDepartureLocation().getTimeZoneOffset().intValue() -
                                                   lastSegment.getArrivalLocation().getTimeZoneOffset().intValue();
                    cal.add(Calendar.MINUTE, timeZoneOffsetDifference);
                    emptySegment.setArrivalDateTime(cal.getTime());
                }
            } else {
                if (defaultDate != null) {
                    cal.setTime(this.defaultDate);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    emptySegment.setDepartureDateTime(cal.getTime());
                    cal.add(Calendar.MINUTE, TIME_INTERVAL);
                    emptySegment.setArrivalDateTime(cal.getTime());
                } else {
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    emptySegment.setDepartureDateTime(cal.getTime());
                    cal.add(Calendar.MINUTE, TIME_INTERVAL);
                    emptySegment.setArrivalDateTime(cal.getTime());
                }
            }
        }
        this.itinerary.getSegmentList().add(emptySegment);
        adapter.add(emptySegment);
        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            listView.smoothScrollToPosition(adapter.getCount());
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Const.REQUEST_CODE_LOCATION:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedListItemKey = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY);
                    String selectedListItemText = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_TEXT);
                    Long selectedTimeZoneOffset = null;
                    try {
                        selectedTimeZoneOffset = Long.parseLong(data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_TIME_ZONE_OFFSET), 10);
                    } catch (NumberFormatException nfe) {
                        Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onActivityResult", "Time Zone Offset is not a number!"));
                    }
                    if (this.currentPosition != null) {
                        ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                        ItineraryLocation itinLocation = new ItineraryLocation();
                        itinLocation.setName(selectedListItemText);
                        itinLocation.setCode(selectedListItemKey);
                        itinLocation.setTimeZoneOffset(selectedTimeZoneOffset);
                        if (segment.getArrivalLocation() != null) {
                            itinLocation.setRateLocationKey(segment.getArrivalLocation().getRateLocationKey());
                        }
                        //Update the cache
                        CodeListManager clmgr = CodeListManager.getInstance();
                        itinLocation = clmgr.updateItineraryLocation(itinLocation);
                        if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                            segment.setDepartureLocation(itinLocation);
                        } else {
                            segment.setArrivalLocation(itinLocation);
                        }
                        itinController.resetSegmentMessage(itinerary, currentPosition.getPosition());
                        checkConsistency();
                    }
                }
                break;

            default: break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BundleId.ITINERARY, this.itinerary);
        outState.putInt(TASK_CHAIN, this.taskChain);
        outState.putSerializable(BundleId.POSITION_INFO_TAG, this.currentPosition);
        outState.putBoolean(ONLINE_CHECK_ACTIVE, this.onlineCheckActive);
        outState.putBoolean(ADDING_DISABLED, this.addingDisabled);
    }

    private void renderDefaultValues() {
        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
        if (etItinerary == null || this.itinerary == null) {
            return;
        }
        etItinerary.setText(itinerary.getName());
        etItinerary.setEnabled(!itinerary.isLocked());
    }

    private void renderNameLabel() {
        TextView tvItineraryLabel = (TextView) findViewById(R.id.tv_itinerary_label);
        if (tvItineraryLabel == null || this.itinerary == null) {
            return;
        }
        tvItineraryLabel.setTextAppearance(this, R.style.TALabel);
        if (itinerary.getMessage() != null) {
            Message msg = itinerary.getMessage();
            if (msg.containsField(Itinerary.Field.NAME.getName())) {
                tvItineraryLabel.setTextAppearance(this, R.style.TALabel_Red);
            }
        }
    }

    private void renderFAB() {
        View fab = findViewById(R.id.fab);
        if (fab == null) {
            return;
        }
        if (this.expenseReportIsSubmitted) {
            fab.setVisibility(View.GONE);
        } else {
            if (this.addingDisabled) {
                fab.setVisibility(View.GONE);
            } else {
                fab.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showTimePickerDialog() {
        Bundle arguments = new Bundle();

        Calendar date = Calendar.getInstance();
        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getDepartureDateTime());
        }

        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_INBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getArrivalDateTime());
        }

        arguments.putSerializable(TimePickerFragment.ARG_DATE, date.getTime());
        arguments.putInt(TimePickerFragment.ARG_INTERVAL, TIME_INTERVAL);
        arguments.putString(TimePickerFragment.ARG_SET_BUTTON, MSG_TIME_PICKER_SET);

        timeDialog = new TimePickerFragment();
        timeDialog.setArguments(arguments);
        timeDialog.show(getSupportFragmentManager(), TAG_TIME_DIALOG_FRAGMENT);
    }

    private void showCalendarDialog() {
        Bundle arguments = new Bundle();

        Calendar date = Calendar.getInstance();
        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getDepartureDateTime());
        }

        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_INBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getArrivalDateTime());
        }

        arguments.putSerializable(DatePickerFragment.ARG_DATE, date.getTime());
        arguments.putString(DatePickerFragment.ARG_SET_BUTTON, MSG_DATE_PICKER_SET);

        calendarDialog = new DatePickerFragment();
        calendarDialog.setArguments(arguments);
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    private void showDeleteDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.MESSAGE_TEXT, getResources().getString(R.string.itin_delete_stop));
        bundle.putString(MessageDialogFragment.POSITIVE_BUTTON, MSG_DIALOG_DELETE_POSITIVE);
        bundle.putString(MessageDialogFragment.NEUTRAL_BUTTON, MSG_DIALOG_DELETE_NEUTRAL);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(bundle);
        messageDialog.show(getSupportFragmentManager(), TAG_DELETE_DIALOG_FRAGMENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ta_itinerary_save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (this.expenseReportIsSubmitted) {
            for (int i = 0; i < menu.size(); i++){
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == R.id.menuSave) {
                    item.setVisible(false);
                }
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Itinerary originItinerary = null;
        if (this.itinerary != null) {
            originItinerary = itinController.getItinerary(this.itinerary.getItineraryID());
        }
        if (this.itinerary == null || !this.itinerary.equals(originItinerary)) {//is dirty
            showIsDirtyDialog();
            return;
        }
        itinController.resetMessages(itinerary);
        if (itinController != null) {
            Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onBackPressed", "Unregister myself as listener at TravelAllowanceItineraryController."));
            itinController.unregisterListener(this);
        }
        if (allowanceController != null) {
            Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onBackPressed", "Unregister myself as listener at FixedTravelAllowanceController."));
            allowanceController.unregisterListener(this);
        }
        super.onBackPressed();
    }

    private void showErrorDialog() {
        if (this.currentPosition == null) {
            return;
        }
        Message msg = itinerary.getSegmentList().get(currentPosition.getPosition()).getMessage();
        if (msg == null) {
            return;
        }
        Bundle arguments = new Bundle();
        arguments.putString(MessageDialogFragment.POSITIVE_BUTTON, MSG_DIALOG_ERROR_POSITIVE);
        arguments.putSerializable(MessageDialogFragment.MESSAGE_OBJECT, msg);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(arguments);
        messageDialog.show(getSupportFragmentManager(), TAG_ERROR_DIALOG_FRAGMENT);
    }

    private void showIsDirtyDialog() {
        Bundle arguments = new Bundle();
        String msgText = getResources().getString(R.string.confirm_save_report_message);
        arguments.putString(MessageDialogFragment.MESSAGE_TEXT, msgText);
        arguments.putString(MessageDialogFragment.POSITIVE_BUTTON, MSG_DIALOG_DIRTY_POSITIVE);
        arguments.putString(MessageDialogFragment.NEUTRAL_BUTTON, MSG_DIALOG_DIRTY_NEUTRAL);
        arguments.putString(MessageDialogFragment.NEGATIVE_BUTTON, MSG_DIALOG_DIRTY_NEGATIVE);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(arguments);
        messageDialog.show(getSupportFragmentManager(), TAG_CONFIRM_DIALOG_FRAGMENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onOptionsItemSelected", "item = " + item));
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menuSave && this.itinerary != null) {
            onSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkConsistency() {
        if (itinController == null) {
            return true;
        }
        //Start check sequence. Prio 1 mandatory fields, Prio 2 overlaps, Prio 3 backend errors
        if (!itinController.areAllMandatoryFieldsFilled(itinerary, onlineCheckActive)) {
            addingDisabled = true;
            renderFAB();
            renderNameLabel();
            if (adapter != null) {
                adapter.setAddingDisabled(true);
                adapter.notifyDataSetChanged();
            }
            return false; //leave sequence
        }
        if (itinController.areDatesOverlapping(itinerary, onlineCheckActive)) {
            addingDisabled = true;
            renderFAB();
            if (adapter != null) {
                adapter.setAddingDisabled(true);
                adapter.notifyDataSetChanged();
            }
            return false; //leave sequence
        }
        if (itinController.hasErrors(this.itinerary)) {
            addingDisabled = true;
            renderFAB();
            if (adapter != null) {
                adapter.setAddingDisabled(true);
                adapter.notifyDataSetChanged();
            }
            return false; //leave sequence
        }

        //Sequence detected no errors
        addingDisabled = false;
        renderFAB();
        onlineCheckActive = false;
        if (adapter != null) {
            adapter.setAddingDisabled(false);
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    private void onSave() {
        if (itinController == null) {
            return;
        }
        onlineCheckActive = true;
        if (checkConsistency() == true) {
            showProgressBar(true);
            itinController.executeUpdate(this.itinerary);
            taskChain = 1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (itinController != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onDestroy", "Unregister myself as listener at TravelAllowanceItineraryController."));
            itinController.unregisterListener(this);
        }

        if (allowanceController != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onDestroy", "Unregister myself as listener at FixedTravelAllowanceController."));
            allowanceController.unregisterListener(this);
        }
    }

    private void onDelete() {
        if (itinerary == null || itinerary.getSegmentList() == null || currentPosition == null) {
            return;
        }
        ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
        if (segment == null) {
            return;
        }
        if (segment.getId() == null) {
            Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG,
                    "onDelete", "Segment id is null - transient. So only removing from Itinerary."));
            itinController.resetSegmentMessage(itinerary, currentPosition.getPosition());
            itinerary.getSegmentList().remove(segment);
            onlineCheckActive = true;
            checkConsistency();
            refreshAdapter();
        } else {
            Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG,
                    "onDelete", "Segment is persistent. Trigger executeDeleteSegment on itinController."));
            showProgressBar(true);
            itinController.executeDeleteSegment(itinerary.getItineraryID(), segment);
        }
    }

    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished", "controller = " + controller.getClass().getSimpleName() +
                ", action = " + action + ", isSuccess = " + isSuccess));
        if (action == ControllerAction.REFRESH) {
            if (controller instanceof TravelAllowanceItineraryController && result == null) {
                // Result is null in case there is an automatic delete due to errors.
                return;
            }
            if (controller instanceof FixedTravelAllowanceController) {
                showProgressBar(false);

                if (taskChain != 1) {
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished", "Got not needed notification... Ignoring"));
                    return;
                }
                if (isSuccess) {
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                            "Allowances need to be updated in order to generate expenses"));
                    if (result != null) {
                        List<FixedTravelAllowance> allowances = (List<FixedTravelAllowance>) result.getSerializable(BundleId.ALLOWANCE_LIST);
                        if (allowanceController.executeUpdate(allowances, this.expenseReportKey, null)) {
                           showProgressBar(true);
                        }
                    }
                }
            }
        }
        if (action == ControllerAction.UPDATE) {
            if (controller instanceof TravelAllowanceItineraryController) {
                if (isSuccess) {
                    Itinerary createdItinerary = (Itinerary) result.getSerializable(BundleId.ITINERARY);
                    this.itinerary = createdItinerary;
                    ConcurCore app = (ConcurCore) getApplication();
                    allowanceController = app.getTaController().getFixedTravelAllowanceController();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
                    this.setResult(RESULT_OK, resultIntent);
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                            "Itinerary Update caused changes to Allowances. Need to refresh..."));
                    if (allowanceController.refreshFixedTravelAllowances(this.expenseReportKey, null)) {
                       showProgressBar(true);
                    }
                } else {
                    taskChain = 0; //Important due to auto delete and error situations. -> Abort chain.
                    showProgressBar(false);
                    addingDisabled = true;
                    onlineCheckActive = true;
                    renderFAB();
                    adapter.setAddingDisabled(addingDisabled);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, R.string.general_save_fail, Toast.LENGTH_SHORT).show();
                }
                refreshAdapter();
            }
            if (controller instanceof FixedTravelAllowanceController) {
                showProgressBar(false);
                if (isSuccess) {
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                            "Allowances have been saved successfully in order to generate expenses"));
                    super.onBackPressed(); //Leave the screen on successfully process chain.
                }
            }
        }
        if (action == ControllerAction.DELETE) {
            if (controller instanceof TravelAllowanceItineraryController) {
                showProgressBar(false);
                if (isSuccess) {
                    if (result != null) {//We get null for auto delete
                        ItinerarySegment deletedSegment = (ItinerarySegment) result.getSerializable(BundleId.SEGMENT);
                        int position = this.itinerary.getSegmentList().indexOf(deletedSegment);
                        itinController.resetSegmentMessage(itinerary, position);
                        this.itinerary.getSegmentList().remove(deletedSegment);
                        onlineCheckActive = true;
                        checkConsistency();
                    }
                } else {
                    Toast.makeText(this, R.string.general_delete_fail, Toast.LENGTH_SHORT).show();
                }
                refreshAdapter();
            }
        }
    }

    private void refreshAdapter() {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshAdapter", "Refreshing adapter."));
        this.adapter.clear();
        this.adapter.addAll(this.itinerary.getSegmentList());
        adapter.notifyDataSetChanged();
    }

    private void showProgressBar(boolean show) {
        if (show) {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.et_itinerary).setEnabled(false);
            findViewById(R.id.list_view).setEnabled(false);
        } else {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.et_itinerary).setEnabled(true);
            findViewById(R.id.list_view).setEnabled(true);
        }
    }

    @Override
    public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", "message = " + fragmentMessage));
        if (MSG_DIALOG_DELETE_POSITIVE.equals(fragmentMessage)) {
            onDelete();
        }
        if (MSG_DIALOG_DIRTY_NEGATIVE.equals(fragmentMessage)) {
            if (itinController != null) {
                itinController.resetMessages(itinerary);
                Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", "Unregister myself as listener at TravelAllowanceItineraryController."));
                itinController.unregisterListener(ItineraryUpdateActivity.this);
            }
            if (allowanceController != null) {
                Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", "Unregister myself as listener at FixedTravelAllowanceController."));
                allowanceController.unregisterListener(ItineraryUpdateActivity.this);
            }
            ItineraryUpdateActivity.super.onBackPressed();
        }
        if (MSG_DIALOG_DIRTY_POSITIVE.equals(fragmentMessage)) {
            onSave();
        }
        if (MSG_DATE_PICKER_SET.equals(fragmentMessage) && extras != null) {
            if (currentPosition == null) {
                return;
            }
            Date date;
            Calendar cal;
            ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
            if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                date = segment.getDepartureDateTime();
            } else {
                date = segment.getArrivalDateTime();
            }
            if (date != null) {
                int year = extras.getInt(DatePickerFragment.EXTRA_YEAR);
                int month = extras.getInt(DatePickerFragment.EXTRA_MONTH);
                int day = extras.getInt(DatePickerFragment.EXTRA_DAY);
                cal = DateUtils.getCalendarKeepingTime(date, year, month, day);
                date = cal.getTime();
                if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                    segment.setDepartureDateTime(date);
                } else {
                    segment.setArrivalDateTime(date);
                }
                itinController.resetSegmentMessage(itinerary, currentPosition.getPosition());
                adapter.notifyDataSetChanged();
                checkConsistency();
            }
        }
        if (MSG_TIME_PICKER_SET.equals(fragmentMessage) && extras != null) {
            if (currentPosition == null) {
                return;
            }
            Date date;
            Calendar cal;
            ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
            if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                date = segment.getDepartureDateTime();
            } else {
                date = segment.getArrivalDateTime();
            }
            if (date != null) {
                int hourOfDay = extras.getInt(TimePickerFragment.EXTRA_HOUR);
                int minute = extras.getInt(TimePickerFragment.EXTRA_MINUTE);
                cal = DateUtils.getCalendarKeepingDate(date, hourOfDay, minute, 0, 0);
                date = cal.getTime();
                if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                    segment.setDepartureDateTime(date);
                } else {
                    segment.setArrivalDateTime(date);
                }
                itinController.resetSegmentMessage(itinerary, currentPosition.getPosition());
                adapter.notifyDataSetChanged();
                checkConsistency();
            }
        }
    }
}