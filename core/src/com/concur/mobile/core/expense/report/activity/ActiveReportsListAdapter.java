/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ReportComparator;
import com.concur.mobile.core.expense.report.service.ActiveReportsRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.SortOrder;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>BaseAdapter</code> for the purposes of populating a list with active report information. This adapter is
 * also a listener for broadcast events for updates to the set of active reports.
 * 
 * @author AndrewK
 */
public class ActiveReportsListAdapter extends BaseAdapter {

    private static final String CLS_TAG = ActiveReportsListAdapter.class.getSimpleName();

    /**
     * Contains a reference to the list of active reports provided by this adapter.
     */
    private List<ExpenseReport> activeReports;

    /**
     * Contains a reference to a broadcast receiver to handle notification of data updates.
     */
    private BroadcastReceiver receiver;

    /**
     * Contains a reference to an intent filter used to register the receiver.
     */
    private IntentFilter receiverFilter;

    /**
     * Contains whether the receiver is currently registered.
     */
    private boolean receiverRegistered;

    /**
     * Contains the activity utilizing this adapter.
     */
    private Activity activity;

    /**
     * Contains whether this adapter should display an "add new" option within the list.
     */
    private boolean showNewOption;

    /**
     * Contains whether or not the dialog is currently showing.
     */
    private boolean dialogShowing;

    /**
     * Contains a reference to a progress bar used to indicate active reports are being loaded.
     */
    private ProgressBar progressBar;

    /**
     * Contains a reference to an outstanding request to retrieve an active report list.
     */
    private ActiveReportsRequest request;

    /**
     * Contains the list item type value representing a new report item.
     */
    private static final int NEW_REPORT_LIST_ITEM_TYPE = 0;

    /**
     * Contains the list item type for an existing report item.
     */
    private static final int REPORT_LIST_ITEM_TYPE = 1;

    /**
     * Constructs an instance of <code>ActiveReportsListAdapter</code> with an associated activity.
     * 
     * @param activity
     *            the associated activity.
     */
    public ActiveReportsListAdapter(Activity activity) {
        this(activity, false, false);
    }

    /**
     * Constructs an instance of <code>ActiveReportsListAdapter</code> with an associated activity and the option to include a
     * "new report" option.
     * 
     * @param activity
     *            the associated activity.
     * @param showNewOption
     *            whether the list should include an "add new" option.
     */
    public ActiveReportsListAdapter(Activity activity, boolean showNewOption, boolean handleDialog) {
        receiver = new ActiveReportsReceiver();
        receiverFilter = new IntentFilter(Const.ACTION_EXPENSE_ACTIVE_REPORTS_UPDATED);
        this.activity = activity;
        this.showNewOption = showNewOption;
        registerReceivers();
        ConcurCore ConcurCore = ((ConcurCore) activity.getApplication());

        // Set the list of active reports.
        activeReports = ConcurCore.getExpenseActiveCache().getReportListDetail();
        if (activeReports != null) {
            // Set the list.
            activeReports = new ArrayList<ExpenseReport>(activeReports);
            filterSubmittedReports(activeReports.iterator());
            Collections.sort(activeReports, new ReportComparator(SortOrder.DESCENDING));
        } else if (handleDialog) {
            activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_ACTIVE_REPORTS);
            dialogShowing = true;
        }
        checkForDataRefresh();
    }

    private void checkForDataRefresh() {
        // Check if the expense cache indicates the set of active reports
        // should be updated from the server.
        ConcurCore ConcurCore = (ConcurCore) this.activity.getApplication();
        IExpenseReportCache expRepCache = ConcurCore.getExpenseActiveCache();
        if (!expRepCache.hasLastReportList() || expRepCache.shouldRefetchReportList()
                || expRepCache.isLastReportListUpdateExpired(Const.CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS)) {
            // Send the request.
            request = ConcurCore.getService().sendActiveReportsRequest();
            // Clear the flag.
            expRepCache.clearShouldRefetchReportList();
            // Clear any refresh flag.
            expRepCache.clearShouldRefreshReportList();
        }
    }

    /**
     * Sets the progress bar used to indicate active reports are being loaded.
     * 
     * @param progressBar
     *            the progress bar used to indicate active reports are being loaded.
     */
    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        if (request != null) {
            this.progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Will register any broadcast receivers required by this adapter.
     */
    public void registerReceivers() {
        if (!receiverRegistered) {
            activity.registerReceiver(receiver, receiverFilter);
            receiverRegistered = true;
        }
    }

    /**
     * Will register any broadcast receivers required by this adapter.
     */
    public void unregisterReceivers() {
        if (receiverRegistered) {
            activity.unregisterReceiver(receiver);
            receiverRegistered = false;
        }
    }

    /**
     * Determines whether <code>position</code> within the list represents the "add new" option.
     * 
     * @param position
     *            the position within the list.
     * 
     * @return <code>true</code> if the "add new" option was selected; <code>false</code> otherwise.
     */
    public boolean isNewOptionSelected(int position) {

        boolean retVal = false;
        if (showNewOption) {
            retVal = (position == 0);
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        int count = 0;
        if (activeReports != null) {
            count = activeReports.size();
        }
        if (showNewOption) {
            ++count;
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        Object obj = null;
        if (showNewOption && position == 0) {
            obj = new Object();
        } else if (activeReports != null) {
            if (showNewOption) {
                --position;
            }
            obj = activeReports.get(position);
        }
        return obj;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int itemType = REPORT_LIST_ITEM_TYPE;
        if (showNewOption && position == 0) {
            itemType = NEW_REPORT_LIST_ITEM_TYPE;
        }
        return itemType;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (position == 0 && showNewOption) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(activity);
                view = inflater.inflate(R.layout.expense_new_active_report_row, null);
            } else {
                // There should only be one entry (position 0) for a new report item; hence just
                // assign convert view.
                view = convertView;
            }
        } else {
            if (showNewOption) {
                --position;
            }
            ExpenseReport expRep = activeReports.get(position);
            view = ViewUtil.buildActiveReportListEntryView(activity, convertView, expRep);
        }
        return view;
    }

    /**
     * Will filter out any submitted reports from the active report list backing <code>reportIterator</code>.
     * 
     * @param reportIterator
     */
    private void filterSubmittedReports(Iterator<ExpenseReport> reportIterator) {
        while (reportIterator.hasNext()) {
            ExpenseReport expRep = reportIterator.next();
            if (!(expRep.apsKey.equalsIgnoreCase("A_NOTF") || expRep.apsKey.equalsIgnoreCase("A_RESU"))) {
                try {
                    reportIterator.remove();
                } catch (UnsupportedOperationException unsupOpExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".<init>: unsupported operation exception while removing expense report '"
                            + expRep.reportName + "' from list.", unsupOpExc);
                } catch (IllegalStateException illStateExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: illegal state exception while removing expense report '"
                            + expRep.reportName + "' from list.", illStateExc);
                }
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling data updates.
     * 
     * @author AndrewK
     */
    class ActiveReportsReceiver extends BroadcastReceiver {

        private final String CLS_TAG = ActiveReportsListAdapter.CLS_TAG + "."
                + ActiveReportsReceiver.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Clear out the request and hide any progress bar.
            request = null;
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, Const.REPLY_HTTP_DEFAULT_STATUS_CODE);
            if (httpStatusCode == HttpStatus.SC_OK) {
                String mwsStatus = intent.getStringExtra(Const.REPLY_STATUS);
                if (mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                    // Dismiss any dialog that may have been presented.
                    try {
                        if (dialogShowing) {
                            try {
                                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_ACTIVE_REPORTS);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } catch (IllegalArgumentException illArgExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: activity did not show dialog - ", illArgExc);
                    }
                    // Clone the new list.
                    ConcurCore ConcurCore = ((ConcurCore) activity.getApplication());
                    activeReports = ConcurCore.getExpenseActiveCache().getReportListDetail();
                    if (activeReports != null) {
                        activeReports = new ArrayList<ExpenseReport>(activeReports);
                        filterSubmittedReports(activeReports.iterator());
                        Collections.sort(activeReports, new ReportComparator(SortOrder.DESCENDING));
                    } else {
                        activeReports = null;
                    }
                    // Notify any data observers.
                    notifyDataSetChanged();
                } else {
                    // Display MWS error message.
                }
            } else if (httpStatusCode != Const.REPLY_HTTP_DEFAULT_STATUS_CODE) {
                // TODO: handle non-200 status code.
            } else {
                // TODO: request not properly handled!
            }
        }

    }

}
