package com.concur.mobile.gov.travel.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.air.activity.GovAirSearch;
import com.concur.mobile.gov.travel.car.activity.GovCarSearch;
import com.concur.mobile.gov.travel.data.TANumberListRow;
import com.concur.mobile.gov.travel.hotel.activity.GovHotelSearch;
import com.concur.mobile.gov.travel.rail.activity.GovRailSearch;
import com.concur.mobile.gov.travel.service.AuthNumsReply;
import com.concur.mobile.gov.travel.service.AuthNumsRequest;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.gov.util.TravelBookingCache.BookingSelection;

public class TravelAuthType extends BaseActivity implements OnClickListener {

    protected final static String CLS_TAG = DocumentListActivity.class.getSimpleName();

    public final static String WHICH_LIST = "which list has been selected";
    public final static String ISOPEN = "OPEN or Group Auth list";
    public final static String ISEXISTING = "Elisting Auth list";

    protected static final int SELECTED_PERDIEM_LOCATION = 1;
    protected static final int SELECTED_AUTH = 2;
    protected static final int DEFAULT_RANGE = 50;

    private List<TANumberListRow> openGroupAutList;
    private List<TANumberListRow> existingAuthList;

    private AuthNumsRequest authNumsRequest;
    private AuthNumReceiver authNumReceiver;
    private IntentFilter authNumFilter;

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_authorization);
        restoreReceivers();
        initScreenHeader();
        // initValue(savedInstanceState);
        // buildViewDelay
        // Check for an orientation change.
        if (!orientationChange) {
            // checkForRefetchData(true);
            sendAuthListRequest();
        } else {
            // Clear the orientation change flag.
            GovAppMobile app = (GovAppMobile) getApplication();
            AuthNumsReply reply = app.trvlBookingCache.getAuthNumsReply();
            if (reply != null) {
                onHandleSuccessListofAuths(reply, app);
            }
            orientationChange = false;
        }

    }

    /** set screen title */
    private void initScreenHeader() {
        getSupportActionBar().setTitle(getString(R.string.gov_travel_authorization_title).toString());

        scrollView = (ScrollView) findViewById(R.id.gov_ta_scroll_view);
        if (scrollView != null) {
            scrollView.setVisibility(View.INVISIBLE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".initScreenHeader: unable to create find scrollview (ID : R.id.gov_ta_scroll_view)!");
        }
    }

    /**
     * Send request to get list of auth which are existing or open or group auths
     * */
    private void sendAuthListRequest() {
        GovAppMobile app = (GovAppMobile) getApplication();
        AuthNumsReply reply = app.trvlBookingCache.getAuthNumsReply();
        if (reply != null) {
            onHandleSuccessListofAuths(reply, app);
        }
        else {
            if (GovAppMobile.isConnected()) {
                GovService govService = (GovService) getConcurService();
                if (govService != null) {
                    registerAuthReceiver();
                    authNumsRequest = govService.getAuthNums();
                    if (authNumsRequest == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                            + ".sendAuthListRequest: unable to create request to get list of available auth for traveler!");
                        unregisterAuthReceiver();
                    } else {
                        // set service request.
                        authNumReceiver.setServiceRequest(authNumsRequest);
                        // Show the progress dialog.
                        showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_AUTH_NUM);
                    }
                } else {
                    Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    /**
     * register travel auth receiver
     * */
    protected void registerAuthReceiver() {
        if (authNumReceiver == null) {
            authNumReceiver = new AuthNumReceiver(this);
            if (authNumFilter == null) {
                authNumFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_AUTH_NUMS);
            }
            getApplicationContext().registerReceiver(authNumReceiver, authNumFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAuthReceiver not null");
        }
    }

    /**
     * un-register travel auth receiver
     * */
    protected void unregisterAuthReceiver() {
        if (authNumReceiver != null) {
            getApplicationContext().unregisterReceiver(authNumReceiver);
            authNumReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAuthReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for auth nums.
     */
    class AuthNumReceiver extends
        BaseBroadcastReceiver<TravelAuthType, AuthNumsRequest>
    {

        private final String CLS_TAG = TravelAuthType.CLS_TAG + "."
            + AuthNumReceiver.class.getSimpleName();

        protected AuthNumReceiver(TravelAuthType activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(TravelAuthType activity) {
            activity.authNumsRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_AUTH_NUM);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_AUTH_NUMS_FAIL);
            logFlurryEvents(false);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final AuthNumsReply reply = app.trvlBookingCache.getAuthNumsReply();
            if (reply != null) {
                onHandleSuccessListofAuths(reply, app);
            } else {
                logFlurryEvents(false);
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(AuthNumsRequest request) {
            activity.authNumsRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAuthReceiver();
        }
    }

    /**
     * get open/group auth list and existing auth list. Then build the view.
     * 
     * @param app
     *            : reference of GovAppMobile
     * 
     * @param {@link AuthNumsReply} reply
     * 
     * */
    protected void onHandleSuccessListofAuths(AuthNumsReply reply, GovAppMobile app) {
        List<TANumberListRow> list = reply.taNumberList;
        TravelBookingCache cache = app.trvlBookingCache;
        openGroupAutList = cache.getOpenGroupAuth(list);
        existingAuthList = cache.getExistingAuth(list);
        logFlurryEvents(true);
        buildView(openGroupAutList, existingAuthList);
    }

    /**
     * Log flurry events that do you successfully get number of auths or not
     * */
    private void logFlurryEvents(boolean isSuccessful) {
        Map<String, String> params = new HashMap<String, String>();
        if (isSuccessful) {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
        } else {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_NO);
        }
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_AUTH_NUMBER_LIST,
            Flurry.EVENT_NAME_LIST, params);
    }

    /**
     * Build view.
     * 
     * @param openGroupAutList
     *            : list of openGroup Auth
     * @param existingAuthList
     *            : list of existing auth.
     */
    private void buildView(List<TANumberListRow> openGroupAutList, List<TANumberListRow> existingAuthList) {
        // visible scrollview
        if (scrollView != null) {
            scrollView.setVisibility(View.VISIBLE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".buildView: unable to create find scrollview (ID : R.id.gov_ta_scroll_view)!");
        }

        LinearLayout openGroupAuth = (LinearLayout) findViewById(R.id.gov_ta_open_group_auth_layout);
        LinearLayout existingAuth = (LinearLayout) findViewById(R.id.gov_ta_existing_auth_layout);
        // visibility of group auth tokens
        if (openGroupAutList == null || openGroupAutList == null || openGroupAutList.size() <= 0) {
            openGroupAuth.setVisibility(View.GONE);
        } else {
            openGroupAuth.setVisibility(View.VISIBLE);
            View view = findViewById(R.id.gov_ta_add_to_open_group);
            if (view != null) {
                view.setOnClickListener(this);
                TextView txtView = (TextView) ViewUtil
                    .findSubView(this, R.id.gov_ta_add_to_open_group, R.id.row_name);
                if (txtView != null) {
                    txtView.setText(getString(R.string.gov_travel_authorization_open_group_auth_row_title));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".buildView : unable to locate gov_ta_add_to_open_group.row_name!");
                }
            }
        }
        // set field names
        View view = findViewById(R.id.gov_ta_create_new);
        if (view != null) {
            view.setOnClickListener(this);
            TextView txtView = (TextView) ViewUtil
                .findSubView(this, R.id.gov_ta_create_new, R.id.row_name);
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_travel_authorization_create_new_auth_row_title));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".buildView : unable to locate gov_ta_create_new.row_name!");
            }
        }

        // visibility of existing auth tokens
        if (existingAuth == null || existingAuthList == null || existingAuthList.size() <= 0) {
            existingAuth.setVisibility(View.GONE);
        } else {
            existingAuth.setVisibility(View.VISIBLE);
            view = findViewById(R.id.gov_ta_add_to_existing);
            if (view != null) {
                view.setOnClickListener(this);
                TextView txtView = (TextView) ViewUtil
                    .findSubView(this, R.id.gov_ta_add_to_existing, R.id.row_name);
                if (txtView != null) {
                    txtView.setText(getString(R.string.gov_travel_authorization_add_to_existing_auth_row_title));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".buildView : unable to locate gov_ta_add_to_existing.row_name!");
                }
            }
        }
    }

    /** Call search location with custom mode */
    private void searchLocation() {
        // Location search
        Intent i = new Intent(this, GovLocationSearch.class);
        i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, GovLocationSearch.SEARCH_CUSTOM);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_LOCATION_SEARCH);
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_AUTH_NUMBER_LIST, Flurry.EVENT_NAME_ACTION, params);
        startActivityForResult(i, SELECTED_PERDIEM_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log flurry event
        // restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (authNumReceiver != null) {
                // Clear activity and we will reassigned.
                authNumReceiver.setActivity(null);
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_GET_AUTH_NUMS, authNumReceiver);
            }

        }
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        if (retainer != null) {
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_GET_AUTH_NUMS)) {
                authNumReceiver = (AuthNumReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_GET_AUTH_NUMS);
                // Reset the activity reference.
                authNumReceiver.setActivity(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent it;
        GovAppMobile app = (GovAppMobile) getApplication();
        AuthNumsReply authReply = app.trvlBookingCache.getAuthNumsReply();
        int id = v.getId();
        switch (id) {
        case R.id.gov_ta_create_new: {
            app.trvlBookingCache.setGenerateAuthUsed(true);
            app.trvlBookingCache.setExistingAuthUsed(false);
            app.trvlBookingCache.setGroupAuthUsed(false);
            // Flurry Notification
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_CREATE_NEW_AUTH);
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_AUTH_NUMBER_LIST, Flurry.EVENT_NAME_ACTION,
                params);
            searchLocation();
            break;
        }
        case R.id.gov_ta_add_to_open_group: {
            app.trvlBookingCache.setGenerateAuthUsed(false);
            app.trvlBookingCache.setExistingAuthUsed(false);
            app.trvlBookingCache.setGroupAuthUsed(true);
            if (authReply != null) {
                it = new Intent(TravelAuthType.this, OpenOrExistingAuthList.class);
                it.putExtra(WHICH_LIST, ISOPEN);
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_OPEN_GROPUP_AUTH);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_AUTH_NUMBER_LIST, Flurry.EVENT_NAME_ACTION,
                    params);
                startActivityForResult(it, SELECTED_AUTH);
            }

            break;
        }
        case R.id.gov_ta_add_to_existing: {
            app.trvlBookingCache.setGenerateAuthUsed(false);
            app.trvlBookingCache.setExistingAuthUsed(true);
            app.trvlBookingCache.setGroupAuthUsed(false);
            if (authReply != null) {
                it = new Intent(TravelAuthType.this, OpenOrExistingAuthList.class);
                it.putExtra(WHICH_LIST, ISEXISTING);
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_EXISTING_AUTH);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_AUTH_NUMBER_LIST, Flurry.EVENT_NAME_ACTION,
                    params);
                startActivityForResult(it, SELECTED_AUTH);
            }
            break;
        }
        default:
            break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_GET_AUTH_NUM: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_get_auth_nums));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Attempt to cancel the request.
                    if (authNumsRequest != null) {
                        authNumsRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: authNumsRequest is null!");
                    }
                }
            });
            dialog = pDialog;
            break;
        }

        case com.concur.mobile.gov.util.Const.DIALOG_GET_AUTH_NUMS_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_get_auth_nums_fail_title);
            dlgBldr.setMessage(R.string.gov_get_auth_nums_fail_msg);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        }

        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case SELECTED_PERDIEM_LOCATION: {
            Intent i;
            if (resultCode == RESULT_OK) {
                GovAppMobile app = (GovAppMobile) getApplication();
                TravelBookingCache cache = app.trvlBookingCache;
                BookingSelection selection = cache.getSelectedBookingType();
                Intent launchIntent = getIntent();
                switch (selection) {
                case AIR:
                    i = new Intent(this, GovAirSearch.class);
                    if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                    }
                    startActivity(i);
                    finish();
                    break;
                case CAR:
                    i = new Intent(this, GovCarSearch.class);
                    if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                    }
                    startActivity(i);
                    finish();
                    break;
                case HOTEL:
                    i = new Intent(this, GovHotelSearch.class);
                    if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                    }
                    startActivity(i);
                    finish();
                    break;
                case RAIL:
                    i = new Intent(this, GovRailSearch.class);
                    if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                        i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                    }
                    startActivity(i);
                    finish();
                    break;

                default:
                    break;
                }
            }
            break;
        }

        case SELECTED_AUTH: {
            if (resultCode == RESULT_OK) {
                searchLocation();
            }
            break;
        }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean retVal = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            if (authNumsRequest != null) {
                authNumsRequest.cancel();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onKeyDown: authNumsRequest is null!");
            }
            finish();
            break;
        }
        default: {
            retVal = super.onKeyDown(keyCode, event);
            break;
        }
        }
        return retVal;
    }
}
