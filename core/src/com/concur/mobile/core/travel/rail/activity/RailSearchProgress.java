/**
 * 
 */
package com.concur.mobile.core.travel.rail.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

import org.apache.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.rail.data.RailChoice;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.rail.service.RailSearchRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity</code> for displaying an in-progress rail search.
 */
public class RailSearchProgress extends BaseActivity {

    private static final String CLS_TAG = RailSearchProgress.class.getSimpleName();

    private static final String RAIL_SEARCH_RECEIVER_KEY = "rail.search.receiver";

    protected Intent resultsIntent;
    protected Intent noResultsIntent;

    protected RailStation currentDepLocation;
    protected RailStation currentArrLocation;

    protected boolean roundTrip;

    protected Calendar depDateTime;
    protected Calendar retDateTime;

    // Contains a reference to the current outstanding car search request.
    // private RailSearchRequest railSearchRequest;

    // Contains a reference to a receiver for handling a search response.
    private RailSearchReceiver railSearchReceiver;

    // Contains the filter used to register the above receiver.
    protected final IntentFilter railSearchFilter = new IntentFilter(Const.ACTION_RAIL_SEARCH_RESULTS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rail_search_progress);

        initValues();
        initUI();

        // Restore any receivers.
        restoreReceivers();

        startSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (railSearchReceiver != null) {
            // Clear the activity reference, it will be set in the new HotelSearch instance.
            railSearchReceiver.setActivity(null);
            retainer.put(RAIL_SEARCH_RECEIVER_KEY, railSearchReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Check if due an orientation change, there's a saved search receiver.
        if (retainer.contains(RAIL_SEARCH_RECEIVER_KEY)) {
            railSearchReceiver = (RailSearchReceiver) retainer.get(RAIL_SEARCH_RECEIVER_KEY);
            // Reset the activity reference.
            railSearchReceiver.setActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_RAIL:
            setResult(resultCode, data);
            break;
        }
        finish();
    }

    protected void initValues() {
        Intent intent = getIntent();

        // Setup our future intents while we are here
        noResultsIntent = (Intent) getIntent().clone();
        noResultsIntent.setClass(this, RailSearchNoResults.class);
        resultsIntent = (Intent) getIntent().clone();
        resultsIntent.setClass(this, RailSearchResults.class);

        // From rail station.
        Bundle bundle = intent.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        if (bundle != null) {
            currentDepLocation = new RailStation(bundle);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: intent missing from railstation bundle!");
        }
        // To rail station.
        bundle = intent.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);
        if (bundle != null) {
            currentArrLocation = new RailStation(bundle);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: intent missing to railstation bundle!");
        }
        // Depart date/time.
        depDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);
        if (depDateTime == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValues: intent missing departure date/time!");
        }
        // Return date/time.
        retDateTime = (Calendar) intent.getSerializableExtra(RailSearch.RET_DATETIME);
        roundTrip = (retDateTime != null);
    }

    protected void initUI() {

        // Set the screen header.
        getSupportActionBar().setTitle(R.string.rail_search_title);

        // Set the departing information.
        TextView txtView = (TextView) findViewById(R.id.searchDepartingValue);
        if (txtView != null) {
            String stationCode = "";
            if (currentDepLocation != null) {
                stationCode = currentDepLocation.stationCode;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: departing location is null!");
            }
            String depDateStr = "";
            if (depDateTime != null) {
                depDateStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, depDateTime);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: departing date/time is null!");
            }
            txtView.setText(com.concur.mobile.base.util.Format.localizeText(this, R.string.rail_search_progress_label,
                    stationCode, depDateStr));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchDepartingValue text view!");
        }

        // Set the returning information if round trip.
        if (roundTrip) {
            txtView = (TextView) findViewById(R.id.searchReturningValue);
            if (txtView != null) {
                String stationCode = "";
                if (currentArrLocation != null) {
                    stationCode = currentArrLocation.stationCode;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: arriving location is null!");
                }
                String retDateStr = "";
                if (retDateTime != null) {
                    retDateStr = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, retDateTime);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: returning date/time is null!");
                }
                txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.rail_search_progress_label, stationCode, retDateStr));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchReturningValue text view!");
            }
        } else {
            // Hide the returning label and value.
            View view = findViewById(R.id.searchReturning);
            if (view != null) {
                view.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate searchReturning view!");
            }
        }
    }

    protected void startSearch() {
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // Make the call
            ConcurService svc = getConcurService();
            if (svc != null) {
                // Check for connectivity, if none, then display dialog and return.
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    return;
                }
                if (railSearchReceiver == null) {
                    railSearchReceiver = new RailSearchReceiver(this);
                }
                Calendar ret = (roundTrip) ? retDateTime : null;
                getApplicationContext().registerReceiver(railSearchReceiver, railSearchFilter);
                svc.searchForTrains(currentDepLocation, currentArrLocation, depDateTime, ret, 1);
            }
        }
    }

    @Override
    protected void updateOfflineHeaderBar(boolean available) {
        super.updateOfflineHeaderBar(available);
        if (!available) {
            // cancel task;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateOfflineHeaderBar: offline mode detect!");
            if (noResultsIntent == null) {
                noResultsIntent = (Intent) getIntent().clone();
                noResultsIntent.setClass(this, RailSearchNoResults.class);
            }
            // Launch the no results activity.
            startActivity(noResultsIntent);
            // finish.
            finish();
        }
    }

    /**
     * A broadcast receiver for handling the result of a car search.
     * 
     * @author AndrewK
     */
    static class RailSearchReceiver extends BroadcastReceiver {

        // A reference to the rail search activity.
        private RailSearchProgress activity;

        // A reference to the rail search request.
        private RailSearchRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>RailSearchReceiver</code> with a search request object.
         * 
         * @param activity
         */
        RailSearchReceiver(RailSearchProgress activity) {
            this.activity = activity;
        }

        /**
         * Sets the rail search activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the rail search activity associated with this broadcast receiver.
         */
        void setActivity(RailSearchProgress activity) {
            this.activity = activity;
            if (this.activity != null) {
                // this.activity.railSearchRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the rail search request object associated with this broadcast receiver.
         * 
         * @param request
         *            the rail search request object associated with this broadcast receiver.
         */
        void setRequest(RailSearchRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                activity.getApplicationContext().unregisterReceiver(this);
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                                    final ConcurCore app = activity.getConcurCore();
                                    final LinkedHashMap<String, ArrayList<RailChoice>> choiceMap = app
                                            .getRailSearchResults().choiceMap;
                                    if (choiceMap.size() > 0) {
                                        // Launch the results intent.
                                        activity.startActivityForResult(activity.resultsIntent,
                                                Const.REQUEST_CODE_BOOK_RAIL);
                                    } else {
                                        // Launch the no results activity.
                                        activity.startActivity(activity.noResultsIntent);

                                        // finish.
                                        activity.finish();
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");

                                    // Launch the no results activity.
                                    activity.startActivity(activity.noResultsIntent);

                                    // finish.
                                    activity.finish();
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");

                                // Launch the no results activity.
                                activity.startActivity(activity.noResultsIntent);

                                // finish.
                                activity.finish();
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");

                            // Launch the no results activity.
                            activity.startActivity(activity.noResultsIntent);

                            // finish.
                            activity.finish();
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));

                            // Launch the no results activity.
                            activity.startActivity(activity.noResultsIntent);

                            // finish.
                            activity.finish();
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");

                    // Launch the no results activity.
                    activity.startActivity(activity.noResultsIntent);

                    // finish.
                    activity.finish();
                }

                // Reset the search request object.
                // activity.railSearchRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }

    }

}
