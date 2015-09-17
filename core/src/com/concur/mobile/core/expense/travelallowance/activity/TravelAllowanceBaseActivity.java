package com.concur.mobile.core.expense.travelallowance.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.fragment.ProgressDialogFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.ServiceRequestListenerFragment;

import java.util.List;

/**
 * Created by D049515 on 16.09.2015.
 */
public class TravelAllowanceBaseActivity extends BaseActivity {

    /**
     * Request codes for start activity with result calls.
     */
    protected static final int REQUEST_CODE_UPDATE_ITINERARY = 0x01;
    protected static final int REQUEST_CODE_FIXED_TRAVEL_ALLOWANCE_DETAILS = 0x02;
    protected static final int REQUEST_CODE_CREATE_ITINERARY = 0x03;

    /**
     * Tags for fragments
     */
    protected static final String TAG_DELETE_DIALOG_FRAGMENT = ".message.dialog.fragment";
    protected static final String TAG_PROGRESS_DIALOG = "progress.dialog";
    protected static final String TAG_UNASSIGN_REQUEST_LISTENER = "unassign.request";
    protected static final String TAG_REFRESH_ITIN_LISTENER = "refresh.itinerary.request";
    protected static final String TAG_REFRESH_FIXED_TA_LISTENER = "refresh.fixed.ta.request";
    protected static final String TAG_DELETE_ITIN_LISTENER = "delete.itin.request";


    /**
     * Message strings for fragment callbacks
     */
    protected static final String MSG_UNASSIGN_ITIN_SUCCESS = "unassignItinSuccessMsg";
    protected static final String MSG_UNASSIGN_ITIN_FAILED = "unassignItinFailedMsg";

    protected static final String MSG_REFRESH_ITIN_FINISHED = "refreshItinFinisheMsg";
    protected static final String MSG_REFRESH_TA_FINISHED = "refreshTAFinishedMsg";

    protected static final String MSG_DELETE_ITIN_SUCCESS = "deleteItinSuccessMsg";
    protected static final String MSG_DELETE_ITIN_FAILED = "deleteItinFailedMsg";

    /**
     * IDs for the saved instance state bundle.
     */
    protected static final String SAVED_INSTANCE_STATE_ID_ITIN_REFRESH_DONE = "itin.refresh.done";
    protected static final String SAVED_INSTANCE_STATE_ID_FIXED_TA_REFRESH_DONE = "fixed.ta.refresh.done";


    protected TravelAllowanceItineraryController itineraryController;
    protected FixedTravelAllowanceController fixedTaController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConcurCore app = (ConcurCore) getApplication();
        this.itineraryController = app.getTaController().getTaItineraryController();
        this.fixedTaController = app.getTaController().getFixedTravelAllowanceController();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void initializeToolbar(int titleResId) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(titleResId);
        }

        TextView toolBarText = (TextView) findViewById(R.id.tv_toolbar_text);
        if (toolBarText != null && toolbar != null) {
            toolBarText.setPadding(toolbar.getContentInsetStart(), 0, 0, 0);
        }
    }

    protected void showProgressDialog() {
        ProgressDialogFragment progressDialig = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialig == null) {
            progressDialig = new ProgressDialogFragment();
        }
        progressDialig.show(getSupportFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    protected void dismissProgressDialog() {
        ProgressDialogFragment dialog = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    protected synchronized Fragment getFragmentByClass(Class fragmentClass) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.getClass().equals(fragmentClass)) {
                return fragment;
            }
        }
        return null;
    }

    protected ServiceRequestListenerFragment getServiceRequestListenerFragment(String tag, String successMessage, String failedMessage) {
        FragmentManager fm = getSupportFragmentManager();
        ServiceRequestListenerFragment f = (ServiceRequestListenerFragment) fm.findFragmentByTag(tag);
        if (f == null) {
            f = new ServiceRequestListenerFragment();
            Bundle args = new Bundle();
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_SUCCESS_MSG, successMessage);
            args.putString(ServiceRequestListenerFragment.BUNDLE_ID_REQUEST_FAILED_MSG, failedMessage);
            f.setArguments(args);
            fm.beginTransaction().add(f, tag).commit();
            fm.executePendingTransactions();
        }

        return f;
    }

    protected void refreshItineraries(String expenseReportKey, boolean isManager) {
        ServiceRequestListenerFragment f = getServiceRequestListenerFragment(TAG_REFRESH_ITIN_LISTENER,
                MSG_REFRESH_ITIN_FINISHED, MSG_REFRESH_ITIN_FINISHED);
        itineraryController.refreshItineraries(expenseReportKey, isManager, f);
    }

    protected void refreshFixedTravelAllowances(String expenseReportKey) {
        ServiceRequestListenerFragment f = getServiceRequestListenerFragment(TAG_REFRESH_FIXED_TA_LISTENER,
                MSG_REFRESH_TA_FINISHED, MSG_REFRESH_TA_FINISHED);
        fixedTaController.refreshFixedTravelAllowances(expenseReportKey, f);
    }

    protected void refreshAssignableItineraries(String expenseReportKey) {
        ConcurCore app = (ConcurCore) getApplication();
        TravelAllowanceItineraryController controller = app.getTaController().getTaItineraryController();
        controller.refreshAssignableItineraries(expenseReportKey, null);
    }

}
