package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.fragment.DatePickerFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TimePickerFragment;
import com.concur.mobile.core.expense.travelallowance.service.AbstractItineraryDeleteRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRowRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
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

    private String expenseReportKey;
    private Itinerary itinerary;
    private TravelAllowanceItineraryController controller;
    private ItineraryUpdateListAdapter adapter;
    private PositionInfoTag currentPosition;
    private DatePickerFragment.OnDateSetListener onDateSetListener;
    private TimePickerFragment.OnTimeSetListener onTimeSetListener;

    private DatePickerFragment calendarDialog;
    private TimePickerFragment timeDialog;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ConcurCore app = (ConcurCore) getApplication();
        this.controller = app.getTaItineraryController();

        View.OnClickListener onTimeClickListener;
        View.OnClickListener onDateClickListener;
        View.OnClickListener onLocationClickListener;

        super.onCreate(savedInstanceState);
        app.getTAConfigController().refreshConfiguration();

        String expenseReportName = StringUtilities.EMPTY_STRING;

        this.setContentView(R.layout.ta_update_activity);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            expenseReportName = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
        }

        String itineraryId = null;
        if (getIntent().hasExtra(Const.EXTRA_ITINERARY_KEY)) {
            itineraryId = getIntent().getStringExtra(Const.EXTRA_ITINERARY_KEY);
        }

        this.itinerary = controller.getItinerary(itineraryId);
        if (this.itinerary == null) {
            this.itinerary = new Itinerary();
            this.itinerary.setExpenseReportID(this.expenseReportKey);
            this.itinerary.setName(expenseReportName);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (StringUtilities.isNullOrEmpty(itineraryId)) {
                actionBar.setTitle("@New Itinerary@");
            } else {
                actionBar.setTitle("@Edit Itinerary@");
            }

        }

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
                Intent locationIntent = new Intent(ItineraryUpdateActivity.this, ListSearch.class);
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
                    ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
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
                        } else {
                            segment.setArrivalDateTime(date);
                        }
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
                    ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                    if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                        date = segment.getDepartureDateTime();
                    } else {
                        date = segment.getArrivalDateTime();
                    }
                    if (date != null) {
                        cal = DateUtils.getCalendarKeepingDate(date, hourOfDay, minute);
                        date = cal.getTime();
                        if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                            segment.setDepartureDateTime(date);
                        } else {
                            segment.setArrivalDateTime(date);
                        }
                        adapter.notifyDataSetChanged();
                        timeDialog.dismiss();
                    }
                }
            }
        };

        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null && this.itinerary != null) {
            adapter = new ItineraryUpdateListAdapter(this, onLocationClickListener,
                    onDateClickListener, onTimeClickListener, this.itinerary.getSegmentList());
            listView.setAdapter(adapter);
        }

        controller.registerListener(this);
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
                    addNewRow();
                }
            });
        }
    }

    private void addNewRow() {
        ItinerarySegment emptySegment = new ItinerarySegment();
        emptySegment.setDepartureDateTime(Calendar.getInstance(Locale.getDefault()).getTime());
        emptySegment.setArrivalDateTime(Calendar.getInstance(Locale.getDefault()).getTime());
        this.itinerary.getSegmentList().add(emptySegment);
        adapter.add(emptySegment);
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
                    if (this.currentPosition != null) {
                        ItinerarySegment segment = itinerary.getSegmentList().get(currentPosition.getPosition());
                        ItineraryLocation itinLocation = new ItineraryLocation();
                        itinLocation.setName(selectedListItemText);
                        itinLocation.setCode(selectedListItemKey);
                        if (segment.getArrivalLocation() != null) {
                            itinLocation.setRateLocationKey(segment.getArrivalLocation().getRateLocationKey());
                        }
                        if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                            segment.setDepartureLocation(itinLocation);
                        } else {
                            segment.setArrivalLocation(itinLocation);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                break;

            default: break;
        }
    }

    private void renderDefaultValues() {
        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
        if (etItinerary != null && this.itinerary != null) {
            etItinerary.setText(itinerary.getName());
            etItinerary.setEnabled(!itinerary.isLocked());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itinerary_save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menuSave && this.itinerary != null) {
            updateControllerData();

            List<ItinerarySegment> periods = itinerary.getSegmentList();
            if (!DateUtils.hasSubsequentDates(false, true, 1, periods)) {
                Toast.makeText(this, "@Dates of this itinerary are not consistent@", Toast.LENGTH_SHORT).show();
            } else if (controller.checkItinerarySegmentsConsistency(itinerary)){
                Toast.makeText(this, "@At least one location isn't consistent@", Toast.LENGTH_SHORT).show();
            } else {
                controller.executeUpdate(this.itinerary);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (controller != null) {
            controller.unregisterListener(this);
        }
    }

    private void updateControllerData() {
        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);

        if (this.itinerary == null) {
            return;
        }
        if (etItinerary != null) {
            this.itinerary.setName(etItinerary.getText().toString());
        }
        this.itinerary.setExpenseReportID(expenseReportKey);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.add("@DELETE@");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (this.itinerary == null) {
            return true;
        }

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ItinerarySegment segment = (ItinerarySegment) adapter.getItem(info.position);

        ConcurCore app = (ConcurCore) this.getApplication();
        final TravelAllowanceItineraryController itinController = app.getTaItineraryController();

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                if (isSuccess) {
                    Toast.makeText(ItineraryUpdateActivity.this, "@Success@", Toast.LENGTH_SHORT).show();
                    itinController.registerListener(ItineraryUpdateActivity.this);
                    itinController.refreshItineraries(
                            ItineraryUpdateActivity.this.expenseReportKey, false);
                } else {
                    Message msg = (Message) resultData
                            .getSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE);
                    if (msg != null) {
                        Toast.makeText(ItineraryUpdateActivity.this, "@Failed@", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Toast.makeText(ItineraryUpdateActivity.this, "@Failed@", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        DeleteItineraryRowRequest deleteRequest = new DeleteItineraryRowRequest(this, receiver,
                this.itinerary.getItineraryID(), segment.getId());
        deleteRequest.execute();

        return true;
    }

//    @Override
//    public void onRequestSuccess(String controllerTag) {
//        updateController.refreshItinerary(updateController.getItinerary().getItineraryID());
//        this.adapter.clear();
//        this.adapter.addAll(updateController.getItinerarySegments());
//        this.adapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public void onRequestFail(String controllerTag) {
//        Toast.makeText(this, "@List reftresh Failed@", Toast.LENGTH_SHORT).show();
//    }
    /**
     * {@inheritDoc}
     */
//    @Override
//    public void onRequestSuccess(final String controllerTag) {
//        Log.d(Const.LOG_TAG, CLASS_TAG + ".onRequestSuccess: " + controllerTag);
//        if (ItineraryUpdateController.CONTROLLER_TAG_UPDATE.equals(controllerTag)) {
//            Toast.makeText(this, R.string.general_succeeded, Toast.LENGTH_SHORT).show();
//            this.adapter.clear();
//            this.adapter.addAll(updateController.getItinerarySegments());
//            adapter.notifyDataSetChanged();
//        }
//    }

    /**
     * {@inheritDoc}
     */
//    @Override
//    public void onRequestFail(final String controllerTag) {
//        if (ItineraryUpdateController.CONTROLLER_TAG_UPDATE.equals(controllerTag)) {
//            Toast.makeText(this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
//            adapter.notifyDataSetChanged();
//        }
//    }

    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        Itinerary resultItin = (Itinerary) result.getSerializable(BundleId.ITINERARY);

        if (isSuccess) {
            Toast.makeText(this, R.string.general_succeeded, Toast.LENGTH_SHORT).show();
        }

        if (resultItin != null) {
            this.itinerary = resultItin;
            this.adapter.clear();
            this.adapter.addAll(this.itinerary.getSegmentList());
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
        }
    }
}
