package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.concur.mobile.core.expense.travelallowance.fragment.MessageDialogFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.ProgressDialogFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TimePickerFragment;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItineraryUpdateActivity extends BaseActivity implements IControllerListener {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = ItineraryUpdateActivity.class
            .getSimpleName();

    private static final String TAG_CALENDAR_DIALOG_FRAGMENT =
            CLASS_TAG + ".calendar.dialog.fragment";

    private static final String TAG_TIME_DIALOG_FRAGMENT =
            CLASS_TAG + ".time.dialog.fragment";

    private static final String TAG_MESSAGE_DIALOG_FRAGMENT =
            CLASS_TAG + ".message.dialog.fragment";

    private static final String TAG_DELETE_DIALOG_FRAGMENT =
            CLASS_TAG + ".delete.dialog.fragment";

    private static final String TAG_PROGRESS_DIALOG_FRAGMENT =
            CLASS_TAG + ".progress.dialog.fragment";

    private Itinerary itinerary;
    private int taskChain;
    private String expenseReportKey;
    private TravelAllowanceItineraryController itinController;
    private FixedTravelAllowanceController allowanceController;
    private ItineraryUpdateListAdapter adapter;
    private PositionInfoTag currentPosition;

    private DialogInterface.OnClickListener onDeleteOkClickListener;
    private DatePickerFragment.OnDateSetListener onDateSetListener;
    private TimePickerFragment.OnTimeSetListener onTimeSetListener;

    private DatePickerFragment calendarDialog;
    private TimePickerFragment timeDialog;
    private ProgressDialogFragment progressDialog;

    private Date defaultDate;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.ta_update_activity);

        ConcurCore app = (ConcurCore) getApplication();
        this.itinController = app.getTaItineraryController();
        this.allowanceController = app.getFixedTravelAllowanceController();
        app.getTAConfigController().refreshConfiguration();

        if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY);
        }

        if (savedInstanceState == null) {//very first create
            this.itinerary = (Itinerary) getIntent().getExtras().getSerializable(BundleId.ITINERARY);
            if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_DATE)) {
                this.defaultDate = (Date) getIntent().getExtras().getSerializable(BundleId.EXPENSE_REPORT_DATE);
            }
            this.itinController.setItineraryStage(itinerary);
        } else {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onCreate", "Restoring itinerary Stage from itinController"));
            this.itinerary = this.itinController.getItineraryStage();
            this.taskChain = savedInstanceState.getInt(BundleId.TASK_CHAIN, 0);
        }

        if (this.itinerary == null) {
            Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onCreate", "Mandatory itinerary not found in intent!"));
            throw new IllegalArgumentException(
                    "onCreate Activity: Expected and Itinerary in the intent extras with bundle id: "
                            + BundleId.ITINERARY + " but nothing found.");
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
                if (tvToolbarText != null && (itinController.getItineraryList() == null
                        || itinController.getItineraryList().size() == 0)) {
                        tvToolbarText.setVisibility(View.VISIBLE);
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
                    if (itinerary != null && itinerary.getMessage() != null) {
                        Message msg = itinerary.getMessage();
                        if (Message.MSG_UI_MISSING_DATES.equals(msg.getCode())) {
                            itinController.resetMessages(itinerary, msg.getCode());
                            renderNameLabel();
                            adapter.notifyDataSetChanged();
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

        onDeleteOkClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                deleteSegment(segment);
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

        onDateSetListener = new DatePickerFragment.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int requestCode, int year, int month, int day) {
                if (currentPosition != null) {
                    Date date;
                    Calendar cal;
                    int datePosition = 0;
                    ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                    segment.setMessage(null);
                    itinController.resetMessages(itinerary, Message.MSG_UI_MISSING_DATES);
                    if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                        date = segment.getDepartureDateTime();
                    } else {
                        date = segment.getArrivalDateTime();
                    }
                    if (date != null) {
                        cal = DateUtils.getCalendarKeepingTime(date, year, month, day);
                        date = cal.getTime();
                        if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                            segment.setDepartureDateTime(date);
                            datePosition = -1;
                        } else {
                            segment.setArrivalDateTime(date);
                            datePosition = 1;
                        }
                        itinController.checkOverlapping(itinerary, currentPosition.getPosition(), datePosition);
                        adapter.notifyDataSetChanged();
                        calendarDialog.dismiss();
                    }
                }
            }
        };

        onTimeSetListener = new TimePickerFragment.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (currentPosition != null) {
                    Date date;
                    Calendar cal;
                    int datePosition = 0;
                    ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                    segment.setMessage(null);
                    itinController.resetMessages(itinerary, Message.MSG_UI_MISSING_DATES);
                    if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                        date = segment.getDepartureDateTime();
                    } else {
                        date = segment.getArrivalDateTime();
                    }
                    if (date != null) {
                        cal = DateUtils.getCalendarKeepingDate(date, hourOfDay, minute, 0, 0);
                        date = cal.getTime();
                        if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                            segment.setDepartureDateTime(date);
                            datePosition = -1;
                        } else {
                            segment.setArrivalDateTime(date);
                            datePosition = 1;
                        }
                        itinController.checkOverlapping(itinerary, currentPosition.getPosition(), datePosition);
                        adapter.notifyDataSetChanged();
                        timeDialog.dismiss();
                    }
                }
            }
        };

        onReturnToHomeListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!isDataInconsistent()) {
                    addNewRow();
                }
            }
        };

        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null && this.itinerary != null) {
            adapter = new ItineraryUpdateListAdapter(this, onDeleteItemClickListener, onLocationClickListener,
                    onDateClickListener, onTimeClickListener, onReturnToHomeListener, this.itinerary.getSegmentList());
            listView.setAdapter(adapter);
        }

        // In case of Create also create the first segment
        if (StringUtilities.isNullOrEmpty(this.itinerary.getItineraryID() ) && this.itinerary.getSegmentList().size() == 0){
            addNewRow();
        }

        //Register Controller Backend Communication Listeners
        itinController.registerListener(this);
        allowanceController.registerListener(this);

        renderDefaultValues();

        registerForContextMenu(listView);

        View fab = findViewById(R.id.fab);
        if (fab != null) {
            if (itinerary != null) {
                if (itinerary.isLocked()) {
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                }
            }
            fab.setOnClickListener (new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isDataInconsistent()) {
                        addNewRow();
                    }
                }
            });
        }
    }


    private void addNewRow() {
        ItinerarySegment emptySegment = new ItinerarySegment();
        //Get current date/time
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        if (itinerary.getSegmentList() != null) {
            if (itinerary.getSegmentList().size() > 0) {
                ItinerarySegment lastSegment = itinerary.getSegmentList().get(itinerary.getSegmentList().size() - 1);
                emptySegment.setDepartureLocation(lastSegment.getArrivalLocation());
                if (lastSegment.getArrivalDateTime() != null) {
                    cal.setTime(lastSegment.getArrivalDateTime());
                    cal.add(Calendar.MINUTE, 1);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    emptySegment.setDepartureDateTime(cal.getTime());
                    cal.add(Calendar.MINUTE, 1);
                    emptySegment.setArrivalDateTime(cal.getTime());
                }
            } else {
                if(defaultDate != null) {
                    cal.setTime(this.defaultDate);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    emptySegment.setDepartureDateTime(cal.getTime());
                    cal.add(Calendar.MINUTE, 1);
                    emptySegment.setArrivalDateTime(cal.getTime());
                } else {
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    emptySegment.setDepartureDateTime(cal.getTime());
                    cal.add(Calendar.MINUTE, 1);
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
                    int datePosition = 0;
                    if (this.currentPosition != null) {
                        ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                        segment.setMessage(null);
                        itinController.resetMessages(itinerary, Message.MSG_UI_MISSING_DATES);
                        ItineraryLocation itinLocation = new ItineraryLocation();
                        itinLocation.setName(selectedListItemText);
                        itinLocation.setCode(selectedListItemKey);
                        if (segment.getArrivalLocation() != null) {
                            itinLocation.setRateLocationKey(segment.getArrivalLocation().getRateLocationKey());
                        }
                        //Update the cache
                        CodeListManager clmgr = CodeListManager.getInstance();
                        itinLocation = clmgr.updateItineraryLocation(itinLocation);
                        if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                            segment.setDepartureLocation(itinLocation);
                            datePosition = -1;
                        } else {
                            segment.setArrivalLocation(itinLocation);
                            datePosition = 1;
                        }
                        //Due to location time zone offset changes
                        itinController.checkOverlapping(itinerary, currentPosition.getPosition(), datePosition);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;

            default: break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putSerializable(BundleId.ITINERARY, this.itinerary);
        outState.putInt(BundleId.TASK_CHAIN, this.taskChain);
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

    private void showTimePickerDialog() {
        Bundle bundle = new Bundle();

        Calendar date = Calendar.getInstance();
        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getDepartureDateTime());
        }

        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_INBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getArrivalDateTime());
        }

        bundle.putSerializable(DatePickerFragment.BUNDLE_ID_DATE, date.getTime());

        timeDialog = new TimePickerFragment();
        timeDialog.setArguments(bundle);
        timeDialog.setOnTimeSetListener(onTimeSetListener);
        timeDialog.show(getSupportFragmentManager(), TAG_TIME_DIALOG_FRAGMENT);
    }

    private void showCalendarDialog() {
        Bundle bundle = new Bundle();

        Calendar date = Calendar.getInstance();
        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getDepartureDateTime());
        }

        if (this.currentPosition != null && this.currentPosition.getInfo() == PositionInfoTag.INFO_INBOUND) {
            date.setTime(itinerary.getSegmentList().get(currentPosition.getPosition()).getArrivalDateTime());
        }

        bundle.putSerializable(DatePickerFragment.BUNDLE_ID_DATE, date.getTime());

        calendarDialog = new DatePickerFragment();

        calendarDialog.setArguments(bundle);
        calendarDialog.setListener(onDateSetListener);
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);

    }

    private void showDeleteDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(BundleId.MESSAGE_TEXT, getResources().getQuantityString(R.plurals.dlg_offline_remove_confirm_message, 1));
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(bundle);
        messageDialog.setOnOkListener(onDeleteOkClickListener);
        messageDialog.show(getSupportFragmentManager(), TAG_DELETE_DIALOG_FRAGMENT);
    }

    private void showErrorDialog(Message msg) {//Use Toast instead
        if (msg == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleId.MESSAGE, msg);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(bundle);
        messageDialog.show(getSupportFragmentManager(), TAG_MESSAGE_DIALOG_FRAGMENT);
    }

    private void showProgressDialog(String progressText) {
        if (StringUtilities.isNullOrEmpty(progressText)) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(BundleId.PROGRESS_DIALOG_TEXT, progressText);
        progressDialog = new ProgressDialogFragment();
        progressDialog.setArguments(bundle);
        progressDialog.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG_FRAGMENT);
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
        if (this.itinerary.isLocked()) {
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
        itinController.resetMessages(itinerary);
        if (itinController != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onBackPressed", "Unregister myself as listener at TravelAllowanceItineraryController."));
            itinController.unregisterListener(this);
        }

        if (allowanceController != null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onBackPressed", "Unregister myself as listener at FixedTravelAllowanceController."));
            allowanceController.unregisterListener(this);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onOptionsItemSelected", "item = " + item));
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menuSave && this.itinerary != null) {
            if (isDataInconsistent()) {
                return true;
            }
            itinController.resetMessages(this.itinerary);
            EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
            if (etItinerary != null) {
                this.itinerary.setName(etItinerary.getText().toString());
            }
            itinController.executeUpdate(this.itinerary);
            taskChain = 1;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDataInconsistent() {
        if (itinController.hasErrors(this.itinerary)) {
            //TODO: Add text Action not possible. Correct errors first
            Toast.makeText(this, R.string.general_data_inconsistent, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!itinController.areAllMandatoryFieldsFilled(itinerary)) {
//            Message msg = new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES,
//                    getString(R.string.general_fill_required_fields));
//            showErrorDialog(msg);
            Toast.makeText(this, R.string.general_fill_required_fields, Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            renderNameLabel();
            return true;
        }
        List<ItinerarySegment> periods = itinerary.getSegmentList();
        if (periods != null && periods.size() > 0) {
            if (!DateUtils.hasSubsequentDates(false, true, 2, periods)) //TODO: Border crossing
            {
                Toast.makeText(this, R.string.general_data_inconsistent, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
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


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // This works only with one menu item. In case there should be more items you need to check which one was selected!
        // The context menu implementation is only temporary until the final ui design is in place.

        if (this.itinerary == null) {
            return true;
        }

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ItinerarySegment segment = (ItinerarySegment) adapter.getItem(info.position);
        deleteSegment(segment);

        return true;
    }

    private void deleteSegment(ItinerarySegment segment) {
        if (segment.getId() == null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onContextItemSelected", "Delete Segment - Segment id is null so only removing from Itinerary."));
            itinerary.getSegmentList().remove(segment);
            refreshAdapter();
        } else {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onContextItemSelected", "Delete Segment - Trigger executeDeleteSegment on itinController."));
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
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (taskChain != 1) {
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished", "Got not needed notification... Ignoring"));
                    return;
                }
                if (isSuccess) {
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                            "Allowances need to be updated in order to generate expenses"));
                    if (result != null) {
                        List<FixedTravelAllowance> allowances = (List<FixedTravelAllowance>) result.getSerializable(BundleId.ALLOWANCE_LIST);
                        if (allowanceController.executeUpdate(allowances, this.expenseReportKey)) {
                            showProgressDialog(" "); //TODO: Add text Calculating Expenses
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
                    allowanceController = app.getFixedTravelAllowanceController();
                    Toast.makeText(this, R.string.general_save_success, Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
                    this.setResult(RESULT_OK, resultIntent);
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                            "Itinerary Update caused changes to Allowances. Need to refresh..."));
                    if (allowanceController.refreshFixedTravelAllowances(this.expenseReportKey)) {
                        showProgressDialog(" "); //TODO: Add text Calculating Allowances...
                    }
                } else {
                    taskChain = 0; //Important due to auto delete and error situations. -> Abort chain.
                    Toast.makeText(this, R.string.general_save_fail, Toast.LENGTH_SHORT).show();
                }
                refreshAdapter();
            }
            if (controller instanceof FixedTravelAllowanceController) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (isSuccess) {
                    Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished",
                            "Allowances have been saved successfully in order to generate expenses"));
                    this.onBackPressed(); //Leave the screen on successfully process chain.
                }
            }
        }
        if (action == ControllerAction.DELETE) {
            if (controller instanceof TravelAllowanceItineraryController) {
                if (isSuccess) {
                    if (result != null) {//We get null for auto delete
                        ItinerarySegment deletedSegment = (ItinerarySegment) result.getSerializable(BundleId.SEGMENT);
                        this.itinerary.getSegmentList().remove(deletedSegment);
                        Toast.makeText(this, R.string.general_delete_success, Toast.LENGTH_SHORT).show();
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
}
