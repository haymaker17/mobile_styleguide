package com.concur.mobile.gov.travel.rail.activity;

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

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.rail.activity.RailSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovLocationSearch;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.data.PerDiemRateListRow;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.travel.service.PerDiemRateRequest;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.gov.R;

public class GovRailSearch extends RailSearch {

    private static final String CLS_TAG = GovRailSearch.class.getSimpleName();

    protected static final String RET_DATETIME = "ret_datetime";
    protected static final int SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE = 99;

    private Intent searchIntent;

    private String authNum, location;

    private PerDiemRateRequest perDiemRateRequest;
    private PerdiemLocationReceiver perDiemLocationListReceiver;
    private IntentFilter perDiemLocationFilter;

    private boolean isCreatedAuthAvial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setAuthorizationView();
    }

    /**
     * Set Authorizationview.
     * */
    protected void setAuthorizationView() {
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        setFieldName(R.id.rail_search_selected_auth, R.string.gov_travel_authorization_selected_auth);
        if (cache.isGenerateAuthUsed()) {
            if (!isCreatedAuthAvial) {
                setFieldValue(R.id.rail_search_selected_auth, getString(R.string.gov_travel_authorization_use_created_auth).toString());
                authNum = null;
            }
        } else {
            authNum = cache.getSelectedAuthItem().getItem().taNumber;
            setFieldValue(R.id.rail_search_selected_auth, authNum);
        }
        setFieldName(R.id.rail_search_selected_perdiem, R.string.gov_travel_authorization_perdiem_location);
        location = cache.getSelectedPerDiemItem().getPerDiemItem().locate;
        setFieldValue(R.id.rail_search_selected_perdiem, location);

        // Add listener to the per-diem location.
        findViewById(R.id.rail_search_selected_perdiem).setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearch#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.rail_search_selected_perdiem: {
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
            setFieldValue(R.id.rail_search_selected_perdiem, cache.getSelectedPerDiemItem().getPerDiemItem().locate);

            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void doSearch() {

        // Launch the activity that will actually kick-off the search and
        // display a progress message.
        searchIntent = new Intent(this, GovRailSearchProgress.class);
        searchIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocation.getBundle());
        searchIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocation.getBundle());
        searchIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);
        if (searchMode == SearchMode.RoundTrip) {
            searchIntent.putExtra(RET_DATETIME, returnDateTime);
        }
        searchIntent.putExtra(DEP_LOCATION, departLocation.stationCode);
        searchIntent.putExtra(ARR_LOCATION, arriveLocation.stationCode);
        searchIntent.putExtra(DEP_DATETIME, departDateTime);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            searchIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }

        if ((isCreatedAuthAvial) && (authNum == null || authNum.length() <= 0)) {
            FormatUtil.setColorForLabelField(R.id.car_search_selected_auth, R.id.field_name, R.color.ErrorTextColor, GovRailSearch.this);
        } else if (location == null || location.length() <= 0) {
            FormatUtil.setColorForLabelField(R.id.car_search_selected_perdiem, R.id.field_name, R.color.ErrorTextColor, GovRailSearch.this);
        } else {
            findPerDiemRate();
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

                perDiemRateRequest = govService.getPerdiemRateLocationList(perDiemLocation.locst, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, departDateTime, returnDateTime, perDiemLocation.locate);
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
        BaseBroadcastReceiver<GovRailSearch, PerDiemRateRequest>
    {

        private final String CLS_TAG = GovRailSearch.CLS_TAG + "."
            + PerdiemLocationReceiver.class.getSimpleName();

        protected PerdiemLocationReceiver(GovRailSearch activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(GovRailSearch activity) {
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
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
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
        getConcurCore().setRailSearchResults(null);
        startActivityForResult(searchIntent, Const.REQUEST_CODE_BOOK_RAIL);
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
