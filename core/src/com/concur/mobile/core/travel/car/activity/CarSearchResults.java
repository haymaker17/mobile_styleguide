package com.concur.mobile.core.travel.car.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.car.data.CarChain;
import com.concur.mobile.core.travel.car.data.CarChoice;
import com.concur.mobile.core.travel.car.data.CarDescription;
import com.concur.mobile.core.travel.car.service.CarSearchReply;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.util.Format;

public class CarSearchResults extends BaseActivity implements OnItemClickListener {

    private static final String CLS_TAG = CarSearchResults.class.getSimpleName();

    // Contains the itinerary locator passed into this activity.
    protected String cliqbookTripId;

    // Contains the client locator passed into this activity.
    protected String clientLocator;

    // Contains the record locator passed into this activity.
    protected String recordLocator;

    // Contains a reference to a list item adapter.
    protected ListItemAdapter<CarChoiceListItem> carChoiceAdapter;

    // Contains a reference to the list view.
    protected ListView carList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.car_search_results);

        // Set the title bar.
        getSupportActionBar().setTitle(R.string.car_results_title);

        Intent i = getIntent();

        // Set search location header title.
        String locationName = i.getStringExtra(Const.EXTRA_TRAVEL_LOCATION);
        ((TextView) findViewById(R.id.travel_name)).setText(locationName);

        // Set the pickup/dropoff date(s).
        Calendar pickupDateTime = (Calendar) i.getSerializableExtra(CarSearch.PICKUP_DATETIME);
        Calendar dropoffDateTime = (Calendar) i.getSerializableExtra(CarSearch.DROPOFF_DATETIME);
        StringBuilder dates = new StringBuilder(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA,
                pickupDateTime));
        dates.append(" - ");
        dates.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, dropoffDateTime));

        // Set departure/arrival dates
        ((TextView) findViewById(R.id.date_span)).setText(dates.toString());

        if (savedInstanceState != null) {
            cliqbookTripId = savedInstanceState.getString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            clientLocator = savedInstanceState.getString(Const.EXTRA_TRAVEL_CLIENT_LOCATOR);
            recordLocator = savedInstanceState.getString(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
        } else {
            Intent intent = getIntent();
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID)) {
                cliqbookTripId = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            }
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR)) {
                clientLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR);
            }
            if (intent.hasExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR)) {
                recordLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
            }
        }

        carList = (ListView) findViewById(R.id.car_choice_list);
        if (carList != null) {
            carList.setOnItemClickListener(this);
            CarSearchReply results = ((ConcurCore) getApplication()).getCarSearchResults();
            if (results != null) {
                // Construct a list of viewable cars choices.
                List<CarChoiceListItem> carListItems = new ArrayList<CarChoiceListItem>();
                if (results.carChoices != null) {
                    for (CarChoice carChoice : results.carChoices) {
                        int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(carChoice.violations);
                        if (ViewUtil.getRuleEnforcementLevel(enforcementLevel) != RuleEnforcementLevel.HIDE) {
                            carListItems.add(new CarChoiceListItem(carChoice, CarChain.findChainByCode(
                                    results.carChains, carChoice.chainCode), CarDescription.findDescByCode(
                                    results.carDescriptions, carChoice.carType)));
                        }
                    }
                }
                carChoiceAdapter = new ListItemAdapter<CarChoiceListItem>(this, carListItems);
                // Prior to setting the adapter on the view, init the image cache receiver to handle
                // updating the list based on images downloaded asychronously.
                imageCacheReceiver = new ImageCacheReceiver<CarChoiceListItem>(carChoiceAdapter, carList);
                registerImageCacheReceiver();
                carList.setAdapter(carChoiceAdapter);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'car_choice_list' view.");
        }

        // Set the total result count.
        String countStr = "";
        if (carChoiceAdapter.getCount() > 1) {
            countStr = com.concur.mobile.base.util.Format.localizeText(this, R.string.generic_results_choice_count,
                    new Object[] { carChoiceAdapter.getCount() });
        } else {
            countStr = getText(R.string.generic_results_one_count).toString();
        }
        ((TextView) findViewById(R.id.footer_navigation_bar_status)).setText(countStr);

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id) {

        CarChoiceListItem listItem = (CarChoiceListItem) list.getItemAtPosition(position);
        if (listItem != null) {
            CarChoice carChoice = listItem.getCarChoice();
            if (carChoice != null) {
                // MOB-14778 - if max enforcement level is 40 then show message and do not allow for reserve
                Violation maxEnforcementViolation = ViewUtil.getShowButNoBookingViolation(carChoice.violations,
                        carChoice.maxEnforcementLevel);
                if (maxEnforcementViolation != null) {
                    showReserveNotAllowed(maxEnforcementViolation.message);
                } else {

                    Intent i = new Intent(this, CarSearchDetail.class);
                    i.putExtra(Const.EXTRA_CAR_DETAIL_ID, carChoice.carId);
                    i.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
                    i.putExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
                    i.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);
                    Intent launchIntent = getIntent();
                    if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                                launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                    }
                    i.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED,
                            launchIntent.getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false));

                    startActivityForResult(i, 0);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the Cliqbook trip id.
        outState.putString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        // Save the client locator.
        outState.putString(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
        // Save the record locator.
        outState.putString(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // Propagate back
            setResult(resultCode, data);
            finish();
        }
    }
}
