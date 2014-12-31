/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.air.activity.AirSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.activity.GovLocationSearch;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.data.PerDiemRateListRow;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;
import com.concur.mobile.gov.travel.service.PerDiemRateRequest;
import com.concur.mobile.gov.util.TravelBookingCache;

/**
 * 
 * @author Chris N. Diaz
 * 
 */
public class GovAirSearch extends AirSearch {

    private static final String CLS_TAG = GovAirSearch.class.getSimpleName();

    protected static final int SELECTED_PERDIEM_LOCATION_ACTIVITY_CODE = 99;

    protected PerDiemRateRequest perDiemRateRequest;
    protected PerdiemLocationReceiver perDiemLocationListReceiver;
    protected IntentFilter perDiemLocationFilter;

    protected Intent searchIntent;
    protected String authNum;

    protected boolean isCreatedAuthAvial;

    /**
     * Default constructor.
     */
    public GovAirSearch() {
        super();
    }

    @Override
    protected void initUI() {
        super.initUI();
        setAuthorizationView();
    }

    /**
     * Set Authorization view.
     * */
    protected void setAuthorizationView() {
        GovAppMobile app = (GovAppMobile) getApplication();
        TravelBookingCache cache = app.trvlBookingCache;
        setFieldName(R.id.air_search_selected_auth, R.string.gov_travel_authorization_selected_auth);
        if (cache.isGenerateAuthUsed()) {
            // isCreatedAuthAvial = (cache.getGenerateAuthNum()==null) ? false : true;
            if (!isCreatedAuthAvial) {
                setFieldValue(R.id.air_search_selected_auth, getString(R.string.gov_travel_authorization_use_created_auth).toString());
                authNum = null;
            }
        } else {
            authNum = cache.getSelectedAuthItem().getItem().taNumber;
            setFieldValue(R.id.air_search_selected_auth, authNum);
        }
        setFieldName(R.id.air_search_selected_perdiem, R.string.gov_travel_authorization_perdiem_location);
        setFieldValue(R.id.air_search_selected_perdiem, cache.getSelectedPerDiemItem().getPerDiemItem().locate);
        // Add listener to the per-diem location.
        findViewById(R.id.air_search_selected_perdiem).setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearch#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.air_search_selected_perdiem: {
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
            setFieldValue(R.id.air_search_selected_perdiem, cache.getSelectedPerDiemItem().getPerDiemItem().locate);

            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearch#doSearch()
     */
    @Override
    protected void doSearch() {

        searchIntent = new Intent(this, GovAirSearchProgress.class);

        searchIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchMode.name());
        searchIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocation.getBundle());
        searchIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocation.getBundle());
        searchIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);
        searchIntent.putExtra(Const.EXTRA_SEARCH_CABIN_CLASS, curCabinClass.id);
        searchIntent.putExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, refundableOnly);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            searchIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        if (searchMode != SearchMode.OneWay) {
            searchIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateTime);
        }
        if ((isCreatedAuthAvial && (authNum == null || authNum.length() <= 0))) {
            FormatUtil.setColorForLabelField(R.id.car_search_selected_auth, R.id.field_name, R.color.ErrorTextColor, GovAirSearch.this);
        } else {
            // We need to get the per-diem ID based on the selected dates.
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
                TDYPerDiemLocationItem perDiemLocItem = cache.getSelectedPerDiemItem();
                if (perDiemLocItem != null && perDiemLocItem.getPerDiemItem() != null) {
                    PerDiemListRow perDiemLocation = perDiemLocItem.getPerDiemItem();
                    perDiemRateRequest = govService.getPerdiemRateLocationList(perDiemLocation.locst,
                        com.concur.mobile.gov.util.Const.GOV_CURR_CODE, departDateTime, returnDateTime, perDiemLocation.locate);
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
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_RATE_LOCATION_FAIL);
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
        BaseBroadcastReceiver<GovAirSearch, PerDiemRateRequest>
    {

        private final String CLS_TAG = GovAirSearch.CLS_TAG + "."
            + PerdiemLocationReceiver.class.getSimpleName();

        private List<PerDiemRateListRow> ratelist;

        protected PerdiemLocationReceiver(GovAirSearch activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(GovAirSearch activity) {
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
        getConcurCore().setAirSearchResults(null);

        // Pass the auth number.
        searchIntent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_EXISTING_TA_NUMBER, authNum);

        // Pass the per-diem location id.
        if (reply != null && reply.rateList != null && !reply.rateList.isEmpty()) {
            searchIntent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_PER_DIEM_LOC_ID,
                reply.rateList.get(0).tabRow);
        } else {
            // handleFailure(app, );
        }

        startActivityForResult(searchIntent, RESULTS_ACTIVITY_CODE);
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
