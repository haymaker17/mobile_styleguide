package com.concur.mobile.gov.travel.car.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.car.activity.CarSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovLocationSearch;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.data.PerDiemRateListRow;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.travel.service.PerDiemRateRequest;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.platform.util.Format;

public class GovCarSearch extends CarSearch implements View.OnClickListener {

    private static final String CLS_TAG = GovCarSearch.class.getSimpleName();

    protected static final int SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE = 99;

    private String authNum, location;
    private Intent searchIntent;

    private PerDiemRateRequest perDiemRateRequest;
    private PerdiemLocationReceiver perDiemLocationListReceiver;
    private IntentFilter perDiemLocationFilter;

    private boolean isAddCar = false;
    boolean isCreatedAuthAvial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuthorizationView();
    }

    /**
     * Set Authorizationview.
     * */

    protected void setAuthorizationView() {
        Bundle bundle = getIntent().getExtras();
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        setFieldName(R.id.car_search_selected_auth, R.string.gov_travel_authorization_selected_auth);
        setFieldName(R.id.car_search_selected_perdiem, R.string.gov_travel_authorization_perdiem_location);
        if (bundle != null) {
            isAddCar = bundle.getBoolean(GovSegmentList.IS_ADD_CAR);
            if (isAddCar) {
                setFieldValue(R.id.car_search_selected_auth, getString(R.string.gov_travel_authorization_selected__existing_auth).toString());
                setFieldValue(R.id.car_search_selected_perdiem, getString(R.string.gov_travel_authorization_existing_perdiem_location).toString());
            } else {
                if (cache.isGenerateAuthUsed()) {
                    if (!isCreatedAuthAvial) {
                        setFieldValue(R.id.car_search_selected_auth, getString(R.string.gov_travel_authorization_use_created_auth).toString());
                    }
                } else {
                    authNum = cache.getSelectedAuthItem().getItem().taNumber;
                    setFieldValue(R.id.car_search_selected_auth, authNum);
                }
                location = cache.getSelectedPerDiemItem().getPerDiemItem().locate;
                setFieldValue(R.id.car_search_selected_perdiem, location);

            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".setAuthorizationView: bundle is null..calling finish(); !");
            finish();
        }
        // Add listener to the per-diem location.
        findViewById(R.id.car_search_selected_perdiem).setOnClickListener(this);
    }

    protected void setFieldName(int parentView, int textId) {
        FormatUtil.setColorForLabelField(parentView, R.id.field_name, R.color.FormLabel, GovCarSearch.this);
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_name));
        tv.setText(textId);
    }

    protected void setFieldValue(int parentView, String text) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_value));
        tv.setText(FormatUtil.nullCheckForString(text));
    }

    @Override
    protected void doSearch() {
        // Launch the activity that will actually kick-off the search and
        // display a progress message.
        searchIntent = new Intent(this, GovCarSearchProgress.class);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_LOCATION, currentLocation.getName());
        searchIntent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, currentLocation.latitude);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, currentLocation.longitude);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP,
            Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, pickupDateTime));
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR, pickupDateTime);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF,
            Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, dropoffDateTime));
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR, dropoffDateTime);
        String carType;
        if (currentCarType != null) {
            carType = currentCarType.id;
        } else {
            // Somehow we got here without something being selected. Not sure how this happens
            // because there is always at least one car type value (Any) coming back from the server
            // and that should be selected when the spinner is created.
            // Set it to a blank string which equates to Any.
            carType = "";
        }
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CAR_TYPE, carType);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
        searchIntent.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);
        searchIntent.putExtra(PICKUP_DATETIME, pickupDateTime);
        searchIntent.putExtra(DROPOFF_DATETIME, dropoffDateTime);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            searchIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        TDYPerDiemLocationItem perDiemLocationItem = cache.getSelectedPerDiemItem();
        if (isAddCar && perDiemLocationItem == null) {
            onHandleSuccessPerDiemLocation(null, null);
        } else if (isAddCar && perDiemLocationItem != null) {
            findPerDiemRate();
        } else {
            if ((!isAddCar) && (isCreatedAuthAvial && (authNum == null || authNum.length() <= 0))) {
                FormatUtil.setColorForLabelField(R.id.car_search_selected_auth, R.id.field_name, R.color.ErrorTextColor, GovCarSearch.this);
            } else if ((!isAddCar) && (location == null || location.length() <= 0)) {
                FormatUtil.setColorForLabelField(R.id.car_search_selected_auth, R.id.field_name, R.color.ErrorTextColor, GovCarSearch.this);
            } else {
                findPerDiemRate();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.TravelBaseActivity#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.car_search_selected_perdiem:
            Intent i = new Intent(this, GovLocationSearch.class);
            i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, GovLocationSearch.SEARCH_CUSTOM);
            startActivityForResult(i, SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE);

            break;
        default:
            super.onClick(view);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearch#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE: {
            if (resultCode == RESULT_OK) {
                GovAppMobile app = (GovAppMobile) getApplication();
                TravelBookingCache cache = app.trvlBookingCache;
                location = cache.getSelectedPerDiemItem().getPerDiemItem().locate;
                setFieldValue(R.id.car_search_selected_perdiem, location);
            }
            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void findPerDiemRate() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerPerdiemLocationReceiver();
                GovAppMobile app = (GovAppMobile) getApplication();
                TravelBookingCache cache = app.trvlBookingCache;
                PerDiemListRow perDiemLocation = cache.getSelectedPerDiemItem().getPerDiemItem();
                if (perDiemLocation != null) {
                    perDiemRateRequest = govService.getPerdiemRateLocationList(perDiemLocation.locst, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, pickupDateTime, dropoffDateTime, perDiemLocation.locate);
                }
                if (perDiemRateRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendPerdiemRateRequest: unable to create request to find perdiem rate location!");
                    unregisterPerdiemLocationReceiver();
                } else {
                    // set service request.
                    perDiemLocationListReceiver.setServiceRequest(perDiemRateRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_RATE_LOCATION);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register perdiem location receiver
     * */
    protected void registerPerdiemLocationReceiver() {
        if (perDiemLocationListReceiver == null) {
            perDiemLocationListReceiver = new PerdiemLocationReceiver(this);
            if (perDiemLocationFilter == null) {
                perDiemLocationFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_PERDIEM_RATE_LOCATIONS);
            }
            getApplicationContext().registerReceiver(perDiemLocationListReceiver, perDiemLocationFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerPerdiemLocationReceiver not null");
        }
    }

    /**
     * un-register perdiem location receiver
     * */
    protected void unregisterPerdiemLocationReceiver() {
        if (perDiemLocationListReceiver != null) {
            getApplicationContext().unregisterReceiver(perDiemLocationListReceiver);
            perDiemLocationListReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterPerdiemLocationReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response after getting new perdiem location for selected location
     */
    class PerdiemLocationReceiver extends
        BaseBroadcastReceiver<GovCarSearch, PerDiemRateRequest>
    {

        private final String CLS_TAG = GovCarSearch.CLS_TAG + "."
            + PerdiemLocationReceiver.class.getSimpleName();

        protected PerdiemLocationReceiver(GovCarSearch activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(GovCarSearch activity) {
            activity.perDiemRateRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_RATE_LOCATION);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_RATE_LOCATION_FAIL);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();

            final PerDiemRateReply reply = app.trvlBookingCache.getPerDiemRateReply();
            if (reply != null) {
                List<PerDiemRateListRow> ratelist = reply.rateList;
                if (ratelist != null && ratelist.size() > 0 && ratelist.get(0) != null) {
                    PerDiemRateListRow obj = ratelist.get(0);
                    if (obj != null) {
                        onHandleSuccessPerDiemLocation(reply, app);
                    } else {
                        handleFailure(context, intent);
                    }
                } else {
                    handleFailure(context, intent);
                }
            } else {
                handleFailure(context, intent);
            }
        }

        @Override
        protected void setActivityServiceRequest(PerDiemRateRequest request) {
            activity.perDiemRateRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterPerdiemLocationReceiver();
        }
    }

    protected void onHandleSuccessPerDiemLocation(PerDiemRateReply reply, GovAppMobile app) {
        // Clear out any current results.
        getConcurCore().setCarSearchResults(null);
        startActivityForResult(searchIntent, Const.REQUEST_CODE_BOOK_CAR);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (perDiemLocationListReceiver != null) {
                perDiemLocationListReceiver = null;
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_PERDIEM_LOCATION, perDiemLocationListReceiver);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        if (retainer != null) {
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_PERDIEM_LOCATION)) {
                perDiemLocationListReceiver = (PerdiemLocationReceiver) retainer.get(com.concur.mobile.gov.util.Const.RETAINER_PERDIEM_LOCATION);
                perDiemLocationListReceiver.setActivity(this);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        dlg = super.onCreateDialog(id);
        if (dlg == null) {
            switch (id) {
            case com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_RATE_LOCATION: {
                ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage(this.getText(R.string.gov_get_per_diem_rate_location));
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(true);
                pDialog.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // Dismiss the dialog.
                        dialog.dismiss();
                        // Attempt to cancel the request.
                        if (perDiemRateRequest != null) {
                            perDiemRateRequest.cancel();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: perDiemRateRequest is null!");
                        }
                    }
                });
                dlg = pDialog;
                break;
            }

            case com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_RATE_LOCATION_FAIL: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.gov_get_per_diem_rate_location_fail_title);
                dlgBldr.setMessage(R.string.gov_get_per_diem_rate_location_fail_msg);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }

            default: {
                ConcurCore ConcurCore = (ConcurCore) getApplication();
                dlg = ConcurCore.createDialog(this, id);
            }
            }
        }
        return dlg;
    }

}
