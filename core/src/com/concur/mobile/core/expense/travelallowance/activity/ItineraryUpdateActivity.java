package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.concur.mobile.core.expense.travelallowance.controller.IServiceRequestListener;
import com.concur.mobile.core.expense.travelallowance.controller.ItineraryUpdateController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.fragment.TimePickerFragment;
import com.concur.mobile.core.expense.travelallowance.service.AbstractItineraryDeleteRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRowRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.ui.common.widget.CalendarPicker;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialogV1;

import java.util.Calendar;
import java.util.Date;

public class ItineraryUpdateActivity extends BaseActivity implements IServiceRequestListener {

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
    private ItineraryUpdateController updateController;
    private ItineraryUpdateListAdapter adapter;
    private PositionInfoTag currentPosition;
    private CalendarPickerDialogV1.OnDateSetListener onDateSetListener;
    private TimePickerFragment.OnTimeSetListener onTimeSetListener;

    private CalendarPickerDialogV1 calendarDialog;
    private TimePickerFragment timeDialog;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        View.OnClickListener onTimeClickListener;
        View.OnClickListener onDateClickListener;
        View.OnClickListener onLocationClickListener;

        String itineraryId = null;
        super.onCreate(savedInstanceState);
        ConcurCore app = (ConcurCore) getApplication();
        app.getTAConfigController().refreshConfiguration();

        this.updateController = app.getItineraryUpdateController();
        String expenseReportName = StringUtilities.EMPTY_STRING;

        this.setContentView(R.layout.ta_update_activity);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            expenseReportName = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
        }

        if (getIntent().hasExtra(Const.EXTRA_ITINERARY_KEY)) {
            itineraryId = getIntent().getStringExtra(Const.EXTRA_ITINERARY_KEY);
        }

        updateController.refreshItinerary(itineraryId);

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

        onDateSetListener = new CalendarPickerDialogV1.OnDateSetListener() {
            @Override
            public void onDateSet(CalendarPicker view, int year, int month, int day) {
                if (currentPosition != null) {
                    Date date;
                    Calendar cal;
                    ItinerarySegment segment = updateController.getItinerarySegment(currentPosition.getPosition());
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
                    ItinerarySegment segment = updateController.getItinerarySegment(currentPosition.getPosition());
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
        if (listView != null) {
            adapter = new ItineraryUpdateListAdapter(this, onLocationClickListener,
                    onDateClickListener, onTimeClickListener);
            listView.setAdapter(adapter);
        }

        this.updateController.registerListener(this);
        renderDefaultValues();

        registerForContextMenu(listView);
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
                        ItinerarySegment segment = this.updateController.getItinerarySegment(currentPosition.getPosition());
                        ItineraryLocation itinLocation = new ItineraryLocation();
                        itinLocation.setName(selectedListItemText);
                        itinLocation.setCode(selectedListItemKey);
                        itinLocation.setRateLocationKey(segment.getArrivalLocation().getRateLocationKey());
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
        if (etItinerary != null && updateController.getItinerary() != null) {
            etItinerary.setText(updateController.getItinerary().getName());
        }
    }

    private void showTimePickerDialog() {
        timeDialog = new TimePickerFragment();
        timeDialog.setOnTimeSetListener(onTimeSetListener);
        timeDialog.show(getSupportFragmentManager(), TAG_TIME_DIALOG_FRAGMENT);
    }

    private void showCalendarDialog() {
        Bundle bundle = new Bundle();

        Calendar date = Calendar.getInstance();

        calendarDialog = new CalendarPickerDialogV1();

        bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_YEAR, date.get(Calendar.YEAR));
        bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_MONTH, date.get(Calendar.MONTH));
        bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_DAY, date.get(Calendar.DAY_OF_MONTH));
        bundle.putInt(CalendarPickerDialogV1.KEY_TEXT_COLOR, Color.parseColor("#a5a5a5"));
        calendarDialog.setArguments(bundle);
        calendarDialog.setOnDateSetListener(onDateSetListener);
        calendarDialog.show(getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);

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
        if (item.getItemId() == R.id.menuSave) {
            updateControllerData();
            updateController.executeUpdate(this.expenseReportKey);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConcurCore app = (ConcurCore) getApplication();
        app.getTaItineraryController().unregisterListener(this);
        this.updateController.unregisterListener(this);
    }

    private void updateControllerData() {
        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
        Itinerary itinerary = updateController.getItinerary();
        if (itinerary == null) {
            return;
        }
        if (etItinerary != null) {
            itinerary.setName(etItinerary.getText().toString());
        }
        itinerary.setExpenseReportID(expenseReportKey);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.add("@DELETE@");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
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

        DeleteItineraryRowRequest deleteRequest = new DeleteItineraryRowRequest(this, receiver, updateController.getItinerary().getItineraryID(), segment.getId());
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
    @Override
    public void onRequestSuccess(final String controllerTag) {
        Log.d(Const.LOG_TAG, CLASS_TAG + ".onRequestSuccess: " + controllerTag);
        if (ItineraryUpdateController.CONTROLLER_TAG_UPDATE.equals(controllerTag)) {
            Toast.makeText(this, R.string.general_succeeded, Toast.LENGTH_SHORT).show();
            this.adapter.clear();
            this.adapter.addAll(updateController.getItinerarySegments());
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestFail(final String controllerTag) {
        if (ItineraryUpdateController.CONTROLLER_TAG_UPDATE.equals(controllerTag)) {
            Toast.makeText(this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }
    }
}
