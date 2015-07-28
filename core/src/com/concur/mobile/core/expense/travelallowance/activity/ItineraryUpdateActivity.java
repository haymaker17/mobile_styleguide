package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.CodeListManager;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.fragment.DatePickerFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TimePickerFragment;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;

import java.util.Calendar;
import java.util.Collections;
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


    // Clone of the original itinerary in the controller.
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
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.ta_update_activity);

        ConcurCore app = (ConcurCore) getApplication();
        this.controller = app.getTaItineraryController();
        app.getTAConfigController().refreshConfiguration();

        if (savedInstanceState == null) {
            this.itinerary = (Itinerary) getIntent().getExtras().getSerializable(BundleId.ITINERARY);
        } else {
            Log.d(CLASS_TAG, "Restoring itinerary from instance state.");
            this.itinerary = (Itinerary) savedInstanceState.getSerializable(BundleId.ITINERARY);
        }

        if (this.itinerary == null) {
            Log.e(CLASS_TAG, "onCreate: No itinerary found in intent.");
            throw new IllegalArgumentException(
                    "onCreate Activity: Expected and Itinerary in the intent extras with bundle id: "
                            + BundleId.ITINERARY + " but nothing found.");
        }

        View.OnClickListener onTimeClickListener;
        View.OnClickListener onDateClickListener;
        View.OnClickListener onLocationClickListener;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (StringUtilities.isNullOrEmpty(this.itinerary.getItineraryID())) {
                actionBar.setTitle(R.string.ta_new_itinerary);
            } else {
                actionBar.setTitle(R.string.ta_edit_itinerary);
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
                        cal = DateUtils.getCalendarKeepingDate(date, hourOfDay, minute, 0, 0);
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

        // In case of Create also create the first segment
        if (StringUtilities.isNullOrEmpty(this.itinerary.getItineraryID() ) && this.itinerary.getSegmentList().size() == 0){
            addNewRow();
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
        //Get current date/time without seconds and milliseconds as default value for arrival and departure
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Log.d(CLASS_TAG, String.valueOf(cal.getTime()));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        Log.d(CLASS_TAG, String.valueOf(cal.getTime()));
        emptySegment.setDepartureDateTime(cal.getTime());
        emptySegment.setArrivalDateTime(cal.getTime());
        if (itinerary.getSegmentList().size() > 0){
            emptySegment.setDepartureLocation((itinerary.getSegmentList().get(itinerary.getSegmentList().size() - 1)).getArrivalLocation());
        }
        this.itinerary.getSegmentList().add(emptySegment);
        adapter.resetSaveMode();
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
                        //Update the cache
                        CodeListManager clmgr = CodeListManager.getInstance();
                        clmgr.updateItineraryLocation(itinLocation);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BundleId.ITINERARY, this.itinerary);
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
                    //item.setEnabled(false);
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
        if (item.getItemId() == R.id.menuSave && this.itinerary != null) {
            EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
            this.itinerary.setName(etItinerary.getText().toString());

            Collections.sort(itinerary.getSegmentList());

            adapter.setSaveMode();
            List<ItinerarySegment> periods = itinerary.getSegmentList();
            if (!DateUtils.hasSubsequentDates(false, true, 1, periods)
                    || (controller.checkItinerarySegmentsConsistency(itinerary))) {
                Toast.makeText(this, R.string.general_data_inconsistent, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
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
            Log.d(CLASS_TAG, "OnDestroy: Unregister Controller Listener.");
            controller.unregisterListener(this);
            controller.resetMessages();
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add("@DELETE@");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // This works only with one menu item. In case there should be more items you need to check which one was selected!
        // The context menu implementation is only temporary until the final ui design is in place.

        if (this.itinerary == null) {
            Log.e(CLASS_TAG,
                    "Delete Segment: Itinerary is null. Should never happen. Check initialization of Activity.");
            return true;
        }

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ItinerarySegment segment = (ItinerarySegment) adapter.getItem(info.position);
        if (segment.getId() == null) {
            Log.d(CLASS_TAG, "Delete Segment: Segment id is null so only removing from Itinerary.");
            itinerary.getSegmentList().remove(segment);
            refreshAdapter();
        } else {
            Log.d(CLASS_TAG, "Delete Segment: Trigger executeDeleteSegment on controller.");
            controller.executeDeleteSegment(itinerary.getItineraryID(), segment);
        }

        return true;
    }



    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        if (action == ControllerAction.REFRESH || result == null) {
            Log.d(CLASS_TAG, "Controller Action ignoring: " + "Action: " + action + " result: " + result);
            // Refresh should not be triggered from this UI!
            // Fresh data should ONLY come in the result bundle.
            // Result is null in case there is an automatic delete due to errors.
            return;
        }

        if (action == ControllerAction.UPDATE) {
            Log.d(CLASS_TAG, "Update action callback finished: isSuccess: " + isSuccess);
            if (isSuccess) {
                Itinerary createdItinerary = (Itinerary) result.getSerializable(BundleId.ITINERARY);
                this.itinerary = createdItinerary;
                refreshAdapter();
                Toast.makeText(this, R.string.general_save_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.general_save_fail, Toast.LENGTH_SHORT).show();
            }
        }

        if (action == ControllerAction.DELETE) {
            Log.d(CLASS_TAG, "Delete action callback finished: isSuccess: " + isSuccess);
            if (isSuccess) {
                ItinerarySegment deletedSegment = (ItinerarySegment) result.getSerializable(BundleId.SEGMENT);
                this.itinerary.getSegmentList().remove(deletedSegment);
                Toast.makeText(this, R.string.general_delete_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.general_delete_fail, Toast.LENGTH_SHORT).show();
            }
        }

        refreshAdapter();
    }

    private void refreshAdapter() {
        Log.d(CLASS_TAG, "Refreshing adapter.");
        this.adapter.clear();
        this.adapter.addAll(this.itinerary.getSegmentList());
        this.adapter.setMessageList(this.controller.getMessages());
        adapter.notifyDataSetChanged();
    }
}
