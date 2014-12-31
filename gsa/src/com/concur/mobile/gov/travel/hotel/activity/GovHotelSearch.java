package com.concur.mobile.gov.travel.hotel.activity;

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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.hotel.activity.HotelSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovLocationSearch;
import com.concur.mobile.gov.travel.activity.GovSegmentList;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.data.PerDiemRateListRow;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.travel.service.PerDiemRateRequest;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.platform.util.Format;

public class GovHotelSearch extends HotelSearch implements OnClickListener {

    private static final String CLS_TAG = GovHotelSearch.class.getSimpleName();

    public static final String PER_DIEM_RATE = "perdiem rate";
    public static final int SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE = 99;

    private String authNum, location;
    private PerDiemRateRequest perDiemRateRequest;
    private PerdiemLocationReceiver perDiemLocationListReceiver;
    private IntentFilter perDiemLocationFilter;

    private Intent searchProgressIntent;

    private boolean isHotelAdd = false;

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
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        Bundle bundle = getIntent().getExtras();
        setFieldName(R.id.hotel_search_selected_auth, R.string.gov_travel_authorization_selected_auth);
        setFieldName(R.id.hotel_search_selected_perdiem, R.string.gov_travel_authorization_perdiem_location);
        if (bundle != null) {
            isHotelAdd = bundle.getBoolean(GovSegmentList.IS_ADD_HOTEL);
            if (isHotelAdd) {
                setFieldValue(R.id.hotel_search_selected_auth, getString(R.string.gov_travel_authorization_selected__existing_auth).toString());
            } else {
                if (cache.isGenerateAuthUsed()) {
                    if (!isCreatedAuthAvial) {
                        setFieldValue(R.id.hotel_search_selected_auth, getString(R.string.gov_travel_authorization_use_created_auth).toString());
                    }
                } else {
                    authNum = cache.getSelectedAuthItem().getItem().taNumber;
                    setFieldValue(R.id.hotel_search_selected_auth, authNum);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".setAuthorizationView: bundle is null..calling finish(); !");
            finish();
        }
        location = cache.getSelectedPerDiemItem().getPerDiemItem().locate;
        setFieldValue(R.id.hotel_search_selected_perdiem, location);
        // Add listener to the per-diem location.
        findViewById(R.id.hotel_search_selected_perdiem).setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearch#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.hotel_search_selected_perdiem: {
            Intent i = new Intent(this, GovLocationSearch.class);
            i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, GovLocationSearch.SEARCH_CUSTOM);
            startActivityForResult(i, SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE);

            break;
        }
        default:
            super.onClick(v);
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
            GovAppMobile app = (GovAppMobile) getApplication();
            TravelBookingCache cache = app.trvlBookingCache;
            setFieldValue(R.id.hotel_search_selected_perdiem, cache.getSelectedPerDiemItem().getPerDiemItem().locate);

            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void setFieldName(int parentView, int textId) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_name));
        tv.setText(textId);
    }

    protected void setFieldValue(int parentView, CharSequence text) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_value));
        tv.setText(text);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Distance unit spinner methods - end
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void doSearch() {
        // Get the values
        String latitude = currentLocation.latitude;
        String longitude = currentLocation.longitude;

        String distanceUnit = currentDistanceUnit.id;
        String distanceValue = currentDistanceAmount.id;
        String namesContaining = null;
        if (withNamesContaining != null) {
            namesContaining = withNamesContaining.getText().toString().trim().toLowerCase();
        }

        searchProgressIntent = new Intent(this, GovHotelSearchProgress.class);
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, currentLocation.getName());
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_AMOUNT, currentDistanceAmount.name);
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID, distanceValue);
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_NAME, currentDistanceUnit.name);
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnit);

        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, latitude);
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, longitude);

        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING, namesContaining);

        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN, Format.safeFormatCalendar(
            FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkInDate));
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDate);
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT, Format.safeFormatCalendar(
            FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkOutDate));
        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDate);

        searchProgressIntent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            searchProgressIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        if ((!isHotelAdd) && (isCreatedAuthAvial && (authNum == null || authNum.length() <= 0))) {
            FormatUtil.setColorForLabelField(R.id.hotel_search_selected_auth, R.id.field_name, R.color.ErrorTextColor, GovHotelSearch.this);
        } else if ((!isHotelAdd) && (location == null || location.length() <= 0)) {
            FormatUtil.setColorForLabelField(R.id.hotel_search_selected_auth, R.id.field_name, R.color.ErrorTextColor, GovHotelSearch.this);
        } else {
            sendPerdiemRateRequest();
        }

    }

    private void sendPerdiemRateRequest() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerPerdiemLocationReceiver();
                GovAppMobile app = (GovAppMobile) getApplication();
                TravelBookingCache cache = app.trvlBookingCache;
                PerDiemListRow perDiemLocation = cache.getSelectedPerDiemItem().getPerDiemItem();
                if (perDiemLocation != null) {
                    perDiemRateRequest = govService.getPerdiemRateLocationList(perDiemLocation.locst, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, checkInDate, checkOutDate, perDiemLocation.locate);
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
        BaseBroadcastReceiver<GovHotelSearch, PerDiemRateRequest>
    {

        private final String CLS_TAG = GovHotelSearch.CLS_TAG + "."
            + PerdiemLocationReceiver.class.getSimpleName();
        private List<PerDiemRateListRow> ratelist;

        protected PerdiemLocationReceiver(GovHotelSearch activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(GovHotelSearch activity) {
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
                ratelist = reply.rateList;
                if (ratelist != null) {
                    final int size = ratelist.size();
                    if (size == 0) {
                        handleFailure(context, intent);
                    } else {
                        PerDiemRateListRow obj = ratelist.get(0);
                        if (obj != null) {
                            onHandleSuccessPerDiemLocation(reply, app);
                        } else {
                            handleFailure(context, intent);
                        }
                    }

                } else {
                    handleFailure(context, intent);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
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
        getConcurCore().setHotelSearchResults(null);
        PerDiemRateListRow rateObj = reply.rateList.get(0);
        Double rate;
        if (rateObj != null) {
            rate = rateObj.ldgrate;
            searchProgressIntent.putExtra(PER_DIEM_RATE, rate);
            startActivityForResult(searchProgressIntent, Const.REQUEST_CODE_BOOK_HOTEL);
        }

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

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
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

                    @Override
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

                    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
