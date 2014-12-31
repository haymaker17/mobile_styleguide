package com.concur.mobile.gov.travel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.concur.gov.R;
import com.concur.mobile.core.travel.activity.TripList;
import com.concur.mobile.core.travel.activity.TripListItem;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.gov.util.TravelBookingCache.BookingSelection;

public class GovTripList extends TripList {

    private static final String CLS_TAG = GovTripList.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        Intent i;
        GovAppMobile app = ((GovAppMobile) getApplication());
        // clear cache..
        app.trvlBookingCache = new TravelBookingCache();
        boolean handled = true;
        final int itemId = item.getItemId();
        if (itemId == R.id.menuHomeBookCar) {
            // i = new Intent(this, CarSearch.class);
            i = new Intent(this, TravelAuthType.class);
            app.trvlBookingCache.setSelectedBookingType(BookingSelection.CAR);
            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
            startActivity(i);
        } else if (itemId == R.id.menuHomeBookHotel) {
            i = new Intent(this, TravelAuthType.class);
            app.trvlBookingCache.setSelectedBookingType(BookingSelection.HOTEL);
            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
            startActivity(i);
        } else if (itemId == R.id.menuHomeBookRail) {
            i = new Intent(this, TravelAuthType.class);
            app.trvlBookingCache.setSelectedBookingType(BookingSelection.RAIL);
            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
            startActivity(i);
        } else if (itemId == R.id.menuHomeBookAir) {
            // Check whether user has permission to book air via mobile.
            if (ViewUtil.isAirUser(this)) {
                // Check for a complete travel profile.
                if (ViewUtil.isTravelProfileComplete(this)) {
                    i = new Intent(this, TravelAuthType.class);
                    app.trvlBookingCache.setSelectedBookingType(BookingSelection.AIR);
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
                    startActivity(i);
                } else {
                    showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
                }
            } else {
                showDialog(Const.DIALOG_TRAVEL_NO_AIR_PERMISSION);
            }
        } else {
            handled = false;
        }

        return handled;
    }

    /**
     * set trip list adapter
     * */
    @Override
    protected void setTripListAdapter() {
        tripListAdapter = new TripListAdapter(this, null);
        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            listView.setAdapter(tripListAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Don't do anything for headers
                    ListItem item = tripListAdapter.getItem(position);
                    String itinLocator = ((TripListItem) item).trip.itinLocator;
                    if (itinLocator != null) {
                        IItineraryCache itinCache = getConcurCore().getItinCache();
                        if (itinCache != null) {
                            Trip itin = itinCache.getItinerary(itinLocator);
                            if (itin != null) {
                                Intent i = new Intent(GovTripList.this, GovSegmentList.class);
                                i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                                startActivity(i);
                            } else {
                                sendItineraryRequest(itinLocator);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildUI.onItemClick: itin cache is null!");
                        }
                    }

                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildUI: unable to locate ListView view in layout!");
        }

        return;
    }

    @Override
    protected void onHandleSuccessItinerary(Intent intent, TripList activity) {
        if (intent.hasExtra(Const.EXTRA_ITIN_LOCATOR)) {
            String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
            if (itinLocator != null) {
                Intent i = new Intent(activity, GovSegmentList.class);
                i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                activity.startActivity(i);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator has invalid value!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator missing!");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        final int id = v.getId();
        if (id == R.id.menuAdd) {

            GovBookTravelDialogFragment dialogFragment = new GovBookTravelDialogFragment();
            (dialogFragment).show(getSupportFragmentManager(), null);

        }
    }
}
