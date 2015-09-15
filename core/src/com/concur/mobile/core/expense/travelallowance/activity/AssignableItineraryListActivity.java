package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.AssignableItineraryListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.ProgressDialogFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.ServiceRequestListenerFragment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by D049515 on 07.09.2015.
 */
public class AssignableItineraryListActivity extends BaseActivity implements IFragmentCallback{


    private static final String CLASS_TAG = AssignableItineraryListActivity.class.getSimpleName();

    private static final String PROGRESS_DIALOG_TAG = "progress";

    private static final String REFRESH_ASSIGNABLE_ITIN_REQUEST_LISTENER_FRAGMENT_TAG = "refreshAssignableItinServiceRequest";
    private static final String ASSIGN_ITIN_REQUEST_LISTENER_FRAGMENT_TAG = "assignItinServiceRequest";
    private static final String REFRESH_ITIN_REQUEST_LISTENER_FRAGMENT_TAG = "refreshItinServiceRequest";
    private static final String REFRESH_TA_REQUEST_LISTENER_FRAGMENT_TAG = "refreshTAServiceRequest";

    private static final String REFRESH_ASSIN_ITIN_SUCCESS_MSG = "refreshAssignableItinSuccessMsg";
    private static final String REFRESH_ASSIN_ITIN_FAILED_MSG = "refreshAssignableItinFailedMsg";

    private static final String ASSIGN_ITIN_SUCCESS_MSG = "assignItinSuccessMsg";
    private static final String ASSIGN_ITIN_FAILED_MSG = "assignItinFailedMsg";

    private static final String REFRESH_ITIN_SUCCESS_MSG = "refreshItinSuccessMsg";
    private static final String REFRESH_ITIN_FAILED_MSG = "refreshItinFailedMsg";

    private static final String REFRESH_TA_SUCCESS_MSG = "refreshTASuccessMsg";
    private static final String REFRESH_TA_FAILED_MSG = "refreshTAFailedMsg";

    private static final int CREATE_ITINERARY_ACTIVITY_REQUEST_CODE = 1;

    private List<AssignableItinerary> assignableItineraryList;

    private String expenseReportKey;

    private boolean itinRefreshDone = false;
    private static final String INSTANCE_STATE_KEY_ITIN_REFRESH_DONE = "itinRefreshDone";
    private boolean taRefreshDone = false;
    private static final String INSTANCE_STATE_KEY_TA_REFRESH_DONE = "taRefreshDone";

    private AssignableItineraryListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ta_assignable_itin_list_activity);

        expenseReportKey = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ta_travel_allowances);

        TextView tv = (TextView) findViewById(R.id.tv_list_title);
        String s = tv.getText().toString();
        tv.setText(s.toUpperCase(Locale.getDefault()));

        ConcurCore app = (ConcurCore) getApplication();
        assignableItineraryList = app.getTaController().getTaItineraryController().getAssignableItineraryList(expenseReportKey);

        ListView listView = (ListView) findViewById(R.id.listView);
        this.adapter = new AssignableItineraryListAdapter(this, new ArrayList<AssignableItinerary>(assignableItineraryList));
        listView.setAdapter(adapter);
        registerViewListener();


        if (savedInstanceState == null) {
            showDialog();
            refreshAssignableItineraries(true);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
        setResult(RESULT_OK, resultIntent);
    }

    private void registerViewListener() {
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssignableItinerary itin = adapter.getItem(position);
                assignItinerary(itin);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssignableItineraryListActivity.this, ItineraryUpdateActivity.class);
                intent.putExtras(getIntent());
                startActivityForResult(intent, CREATE_ITINERARY_ACTIVITY_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSTANCE_STATE_KEY_ITIN_REFRESH_DONE, itinRefreshDone);
        outState.putBoolean(INSTANCE_STATE_KEY_TA_REFRESH_DONE, taRefreshDone);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.itinRefreshDone = savedInstanceState.getBoolean(INSTANCE_STATE_KEY_ITIN_REFRESH_DONE);
        this.taRefreshDone = savedInstanceState.getBoolean(INSTANCE_STATE_KEY_TA_REFRESH_DONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_ITINERARY_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, TravelAllowanceActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
            this.startActivity(intent);
            this.finish();
        }
    }

    private void refreshAssignableItineraries(boolean needRequestMessage) {
        ServiceRequestListenerFragment f = (ServiceRequestListenerFragment) getFragment(REFRESH_ASSIGNABLE_ITIN_REQUEST_LISTENER_FRAGMENT_TAG);
        if (f == null) {
            f = new ServiceRequestListenerFragment();

            if (needRequestMessage) {
                Bundle args = new Bundle();
                args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_SUCCESS_MSG, REFRESH_ASSIN_ITIN_SUCCESS_MSG);
                args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_FAILED_MSG, REFRESH_ASSIN_ITIN_FAILED_MSG);
                f.setArguments(args);
            }

            addFragment(f, REFRESH_ASSIGNABLE_ITIN_REQUEST_LISTENER_FRAGMENT_TAG);
        }

        ConcurCore app = (ConcurCore) getApplication();
        TravelAllowanceItineraryController controller = app.getTaController().getTaItineraryController();
        controller.refreshAssignableItineraries(expenseReportKey, f);
    }


    private void assignItinerary(AssignableItinerary itinerary) {
        ServiceRequestListenerFragment f = (ServiceRequestListenerFragment) getFragment(ASSIGN_ITIN_REQUEST_LISTENER_FRAGMENT_TAG);
        if (f == null) {
            f = new ServiceRequestListenerFragment();
            Bundle args = new Bundle();
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_SUCCESS_MSG, ASSIGN_ITIN_SUCCESS_MSG);
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_FAILED_MSG, ASSIGN_ITIN_FAILED_MSG);
            f.setArguments(args);
            addFragment(f, ASSIGN_ITIN_REQUEST_LISTENER_FRAGMENT_TAG);
        }

        ConcurCore app = (ConcurCore) getApplication();
        showDialog();
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "assignItinerary", "Task Start"));
        app.getTaController().getTaItineraryController().assignItinerary(expenseReportKey, itinerary.getItineraryID(), f);

    }


    private void refreshItineraries() {
        ServiceRequestListenerFragment f = (ServiceRequestListenerFragment) getFragment(REFRESH_ITIN_REQUEST_LISTENER_FRAGMENT_TAG);
        if (f == null) {
            f = new ServiceRequestListenerFragment();
            Bundle args = new Bundle();
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_SUCCESS_MSG, REFRESH_ITIN_SUCCESS_MSG);
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_FAILED_MSG, REFRESH_ITIN_FAILED_MSG);
            f.setArguments(args);
            addFragment(f, REFRESH_ITIN_REQUEST_LISTENER_FRAGMENT_TAG);
        }

        ConcurCore app = (ConcurCore) getApplication();
        TravelAllowanceItineraryController controller = app.getTaController().getTaItineraryController();
        controller.refreshItineraries(expenseReportKey, false, f);
    }

    private void refreshFixedTravelAllowances() {
        ServiceRequestListenerFragment f = (ServiceRequestListenerFragment) getFragment(REFRESH_TA_REQUEST_LISTENER_FRAGMENT_TAG);
        if (f == null) {
            f = new ServiceRequestListenerFragment();
            Bundle args = new Bundle();
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_SUCCESS_MSG, REFRESH_TA_SUCCESS_MSG);
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_FAILED_MSG, REFRESH_TA_FAILED_MSG);
            f.setArguments(args);
            addFragment(f, REFRESH_TA_REQUEST_LISTENER_FRAGMENT_TAG);
        }

        ConcurCore app = (ConcurCore) getApplication();
        FixedTravelAllowanceController controller = app.getTaController().getFixedTravelAllowanceController();
        controller.refreshFixedTravelAllowances(expenseReportKey, f);
    }

    private void refreshListAdapter() {
        ListView listView = (ListView) findViewById(R.id.listView);
        AssignableItineraryListAdapter adapter = (AssignableItineraryListAdapter) listView.getAdapter();
        adapter.clear();
        ConcurCore app = (ConcurCore) getApplication();
        TravelAllowanceItineraryController controller = app.getTaController().getTaItineraryController();
        this.assignableItineraryList = controller.getAssignableItineraryList(expenseReportKey);
        adapter.addAll(assignableItineraryList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public synchronized void sendMessage(String message) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "sendMessage", message));

        if (REFRESH_ASSIN_ITIN_SUCCESS_MSG.equals(message)) {
            refreshListAdapter();
            dismissDialog();
        }

        if (ASSIGN_ITIN_SUCCESS_MSG.equals(message)) {
            refreshAssignableItineraries(false);
            refreshItineraries();
            refreshFixedTravelAllowances();
        }

        if (REFRESH_ITIN_SUCCESS_MSG.equals(message)) {
            itinRefreshDone = true;
            onTaDataRefreshDone();
        }

        if (REFRESH_TA_SUCCESS_MSG.equals(message)) {
            taRefreshDone = true;
            onTaDataRefreshDone();
        }

    }

    private synchronized void onTaDataRefreshDone() {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onTaDataRefreshDone", "ItinDone:" + itinRefreshDone + " TADone:" + taRefreshDone));
        if (taRefreshDone && itinRefreshDone) {
            dismissDialog();
            taRefreshDone = false;
            itinRefreshDone = false;
            Intent intent = new Intent(this, TravelAllowanceActivity.class);
            intent.putExtras(getIntent());
            intent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
            this.startActivity(intent);
            this.finish();
        }
    }


    private Fragment getFragment(String tag) {
        FragmentManager fManager = getSupportFragmentManager();
        Fragment f = fManager.findFragmentByTag(tag);
        return f;
    }

    private void addFragment(Fragment f, String tag) {
        FragmentManager fManager = getSupportFragmentManager();
        fManager.beginTransaction().add(f, tag).commit();
        fManager.executePendingTransactions();
    }

    private void showDialog() {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "showDialog", "Call"));
        ProgressDialogFragment dialog = (ProgressDialogFragment) getFragment(PROGRESS_DIALOG_TAG);
        FragmentManager fm = getSupportFragmentManager();
        if (dialog == null) {
            dialog = new ProgressDialogFragment();
        }
        dialog.show(fm, PROGRESS_DIALOG_TAG);
        fm.executePendingTransactions();
    }

    private void dismissDialog() {
        ProgressDialogFragment dialog = (ProgressDialogFragment) getFragment(PROGRESS_DIALOG_TAG);
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "dismissDialog", "Call"));
        if (dialog != null) {
            dialog.dismiss();
        } else {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "dismissDialog", "dialog is null."));
        }
    }


}
