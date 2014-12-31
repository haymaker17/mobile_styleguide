/**
 * This Class shows you the list of items which you can upload once you get data connectivity.
 * 
 * @author sunill
 * 
 * **/

package com.concur.mobile.core.activity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.expense.charge.activity.CashExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.ExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.QuickExpense;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.charge.service.SaveMobileEntryRequest;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.receiptstore.activity.OfflineReceiptListItem;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;

public class OffLineUploadList extends BaseActivity {

    private static final String CLS_TAG = OffLineUploadList.class.getSimpleName();

    private static final int DIALOG_UPLOAD_PROGRESS = 1;

    private static final int EXPENSE_VIEW_TYPE = 1;
    private static final int RECEIPT_VIEW_TYPE = 2;

    private static final String SAVE_EXPENSE_RECEIVER_KEY = "save.expense.receiver";
    private static final String SAVE_RECEIPT_RECEIVER_KEY = "save.receipt.receiver";

    private static final String SELECTED_EXPENSES_KEY = "selected.expenses.key";
    private static final String SELECTED_RECEIPTS_KEY = "selected.receipts.key";
    private static final String UPLOAD_COUNT_KEY = "upload.count.key";
    private static final String UPLOAD_TOTAL_KEY = "upload.total.key";

    protected ListView listView;
    protected View emptyView;

    protected MenuItem uploadActionItem;

    protected ListItemAdapter<ListItem> listItemAdapter;

    protected int totalItemCount;
    protected int uploadingItemCount;

    protected TextView progressText;

    protected boolean uploadCancelled;

    /**
     * A reference to a listener for handling checkbox state changes.
     */
    private OnCheckChange onCheckChange;

    /**
     * Contains the set of checked expenses.
     */
    private HashSet<Expense> checkedExpenses;

    /**
     * Contains a map from <code>Expense</code> to <code>CompoundButton</code> objects.
     */
    private HashMap<Expense, CompoundButton> expenseButtonMap;

    /**
     * Contains the set of checked receipts.
     */
    private HashSet<ReceiptInfo> checkedReceipts;

    /**
     * Contains a map from <code>ReceiptInfo</code> to <code>CompoundButton</code> objects.
     */
    private HashMap<ReceiptInfo, CompoundButton> receiptButtonMap;

    /**
     * Contains whether or not the button toolbar is currently visible on screen.
     */
    private boolean barVisible;

    /**
     * A reference to the slideable button bar.
     */
    private View buttonBar;

    /**
     * Contains an outstanding request to save an expense.
     */
    protected SaveMobileEntryRequest saveExpenseRequest;
    /**
     * Contains a receiver to handle the result of saving an expense.
     */
    protected SaveExpenseReceiver saveExpenseReceiver;
    /**
     * Contains a filter used to register the save expense receiver.
     */
    protected IntentFilter saveExpenseFilter;

    /**
     * Contains an outstanding request to save a receipt.
     */
    protected SaveReceiptRequest saveReceiptRequest;
    /**
     * Contains a receiver to handle the result of saving a receipt.
     */
    protected SaveReceiptReceiver saveReceiptReceiver;
    /**
     * Contains a filter used to register the save receipt receiver.
     */
    protected IntentFilter saveReceiptFilter;
    /**
     * Contains the upload quick expense count.
     */
    protected int uploadQuickExpenseCount;
    /**
     * Contains the upload receipt count.
     */
    protected int uploadReceiptCount;
    /**
     * Contains the upload start time in milliseconds.
     */
    protected long uploadStartTimeMillis;
    /**
     * Contains the upload stop time in milliseconds.
     */
    protected long uploadStopTimeMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_uploadqueue_list);

        if (savedInstanceState != null && savedInstanceState.containsKey(UPLOAD_COUNT_KEY)) {
            totalItemCount = savedInstanceState.getInt(UPLOAD_TOTAL_KEY);
            uploadingItemCount = savedInstanceState.getInt(UPLOAD_COUNT_KEY);
            showDialog(DIALOG_UPLOAD_PROGRESS);
        }

        restoreReceivers(false);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new GetListItemsAsync(new ListItemsPostExec() {

            @Override
            public void run() {
                listItemAdapter.setItems(listItems);
                updateUI();

            }

        }).execute();

        restoreReceivers(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Check for 'SaveReceiptReceiver'.
        if (saveExpenseReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            saveExpenseReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(SAVE_EXPENSE_RECEIVER_KEY, saveExpenseReceiver);
        }

        // Check for 'SaveReceiptReceiver'.
        if (saveReceiptReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            saveReceiptReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(SAVE_RECEIPT_RECEIVER_KEY, saveReceiptReceiver);
        }

    }

    public void restoreReceivers(boolean displayDialog) {
        boolean displayedDialog = false;
        if (retainer != null) {
            if (retainer.contains(SAVE_EXPENSE_RECEIVER_KEY)) {
                // Check whether this method should check for a dialog to
                // display.
                if (displayDialog && totalItemCount > 0) {
                    displayedDialog = true;
                    showDialog(DIALOG_UPLOAD_PROGRESS);
                }
                // Get the receiver.
                saveExpenseReceiver = (SaveExpenseReceiver) retainer.get(SAVE_EXPENSE_RECEIVER_KEY);
                // Reset the activity reference.
                saveExpenseReceiver.setActivity(this);
            }

            if (retainer.contains(SAVE_RECEIPT_RECEIVER_KEY)) {
                if (displayDialog && totalItemCount > 0 && !displayedDialog) {
                    displayedDialog = true;
                    showDialog(DIALOG_UPLOAD_PROGRESS);
                }
                saveReceiptReceiver = (SaveReceiptReceiver) retainer.get(SAVE_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                saveReceiptReceiver.setActivity(this);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os. Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (totalItemCount > 0) {
            outState.putInt(UPLOAD_COUNT_KEY, uploadingItemCount);
            outState.putInt(UPLOAD_TOTAL_KEY, totalItemCount);
            removeDialog(DIALOG_UPLOAD_PROGRESS);
        }

        if (checkedExpenses != null && checkedExpenses.size() > 0) {
            Iterator<Expense> ckItemIter = checkedExpenses.iterator();
            ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
            while (ckItemIter.hasNext()) {
                Expense exp = ckItemIter.next();
                int adapterPos = -1;
                for (int listItemInd = 0; listItemInd < listItemAdapter.getCount(); ++listItemInd) {
                    if (listItemAdapter.getItem(listItemInd) instanceof ExpenseListItem) {
                        if (((CashExpenseListItem) listItemAdapter.getItem(listItemInd)).getExpense() == exp) {
                            adapterPos = listItemInd;
                            break;
                        }
                    }
                }
                if (adapterPos != -1) {
                    selectedPositions.add(adapterPos);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onSaveInstanceState: selected item has -1 position!");
                }
            }

            // Write out the list of selected item positions.
            outState.putIntegerArrayList(SELECTED_EXPENSES_KEY, selectedPositions);
        }

        if (checkedReceipts != null && checkedReceipts.size() > 0) {
            Iterator<ReceiptInfo> ckItemIter = checkedReceipts.iterator();
            ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
            while (ckItemIter.hasNext()) {
                ReceiptInfo ri = ckItemIter.next();
                int adapterPos = -1;
                for (int listItemInd = 0; listItemInd < listItemAdapter.getCount(); ++listItemInd) {
                    if (listItemAdapter.getItem(listItemInd) instanceof OfflineReceiptListItem) {
                        if (((OfflineReceiptListItem) listItemAdapter.getItem(listItemInd)).getReceiptInfo() == ri) {
                            adapterPos = listItemInd;
                            break;
                        }
                    }
                }
                if (adapterPos != -1) {
                    selectedPositions.add(adapterPos);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onSaveInstanceState: selected item has -1 position!");
                }
            }

            // Write out the list of selected item positions.
            outState.putIntegerArrayList(SELECTED_RECEIPTS_KEY, selectedPositions);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);

        if (inState != null) {

            if (inState.containsKey(SELECTED_EXPENSES_KEY)) {
                ArrayList<Integer> selItemList = inState.getIntegerArrayList(SELECTED_EXPENSES_KEY);
                // Clear out the set of checked items.
                if (checkedExpenses != null) {
                    checkedExpenses.clear();
                } else {
                    checkedExpenses = new HashSet<Expense>();
                }
                for (int expInd = 0; expInd < selItemList.size(); ++expInd) {
                    int selItemInd = selItemList.get(expInd);
                    ListItem li = listItemAdapter.getItem(selItemInd);
                    if (li != null) {
                        if (li instanceof CashExpenseListItem) {
                            checkedExpenses.add(((CashExpenseListItem) li).getExpense());
                            // It's possible that the view for the item hasn't
                            // actually been built
                            // yet so there won't be an entry in 'itemButtonMap'
                            // for this expense.
                            CompoundButton cmpBut = expenseButtonMap.get(li);
                            if (cmpBut != null && !cmpBut.isChecked()) {
                                cmpBut.setChecked(true);
                            }
                        }
                    }
                }
                toggleButtonBar();
            }
            if (inState.containsKey(SELECTED_RECEIPTS_KEY)) {
                ArrayList<Integer> selItemList = inState.getIntegerArrayList(SELECTED_RECEIPTS_KEY);
                // Clear out the set of checked items.
                if (checkedReceipts != null) {
                    checkedReceipts.clear();
                } else {
                    checkedReceipts = new HashSet<ReceiptInfo>();
                }
                for (int expInd = 0; expInd < selItemList.size(); ++expInd) {
                    int selItemInd = selItemList.get(expInd);
                    ListItem li = listItemAdapter.getItem(selItemInd);
                    if (li != null) {
                        if (li instanceof OfflineReceiptListItem) {
                            checkedReceipts.add(((OfflineReceiptListItem) li).getReceiptInfo());
                            // It's possible that the view for the item hasn't
                            // actually been built
                            // yet so there won't be an entry in 'itemButtonMap'
                            // for this expense.
                            CompoundButton cmpBut = receiptButtonMap.get(li);
                            if (cmpBut != null && !cmpBut.isChecked()) {
                                cmpBut.setChecked(true);
                            }
                        }
                    }
                }
                toggleButtonBar();
            }
        }
    }

    protected void initUI() {

        // Init the listener to handle checkbox changes.
        onCheckChange = new OnCheckChange();

        // Init the screen header.
        initScreenHeader();

        // Configure the button bar.
        configureButtonBar();

        // Init the list.
        initListView();

    }

    /**
     * Will initialize the screen header.
     **/
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(R.string.offline_upload_list_header);
    }

    /**
     * Configures the button bar.
     */
    private void configureButtonBar() {

        // Obtain a reference to the bar for sliding purposes.
        buttonBar = findViewById(R.id.offlineListButtonBar);

        // Ensure bar is not initially visible.
        slideButtonBar(false);
        barVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_upload, menu);
        uploadActionItem = menu.findItem(R.id.menuUpload);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuUpload) {
            // Generate the Flurry Notification for average age of queued items.
            Calendar utcNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            long utcNowMillis = utcNow.getTimeInMillis();
            List<ListItem> listItems = listItemAdapter.getItems();
            long totalDiffTimeMillis = 0L;
            if (listItems != null && !listItems.isEmpty()) {
                for (ListItem listItem : listItems) {
                    if (listItem.getCalendar() != null) {
                        long liTimeMillis = listItem.getCalendar().getTimeInMillis();
                        totalDiffTimeMillis += (utcNowMillis - liTimeMillis);
                    }
                }
                long avgDiffTimeMillis = (totalDiffTimeMillis / listItems.size());
                int avgDiffTimeMinutes = (int) (avgDiffTimeMillis / 1000);

                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".initScreenHeader.onClick: average age (minutes) + "
                                + Integer.toString(avgDiffTimeMinutes) + ".");
            }

            uploadList();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected abstract class ListItemsPostExec implements Runnable {

        protected List<ListItem> listItems;

        protected void setListItems(List<ListItem> listItems) {
            this.listItems = listItems;
        }

        @Override
        abstract public void run();

    }

    protected class GetListItemsAsync extends AsyncTask<Void, Void, List<ListItem>> {

        private final ListItemsPostExec postExec;

        protected GetListItemsAsync(ListItemsPostExec postExec) {
            this.postExec = postExec;
        }

        @Override
        protected void onPreExecute() {
            // TODO Dialog
        }

        @Override
        protected List<ListItem> doInBackground(Void... params) {
            List<ListItem> listItems = new ArrayList<ListItem>();

            ConcurCore app = (ConcurCore) getApplication();
            IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
            ArrayList<MobileEntry> cacheList = expEntCache.getMobileEntries();
            if (cacheList != null && cacheList.size() > 0) {

                // Pull out all the offline expenses
                List<Expense> offlineExpenses = new ArrayList<Expense>();
                for (MobileEntry me : cacheList) {
                    if (MobileEntryStatus.NEW.equals(me.getStatus())) {
                        offlineExpenses.add(new Expense(me));
                    }
                }

                // Ensure any previously checked items that are still present in
                // our new data
                // appear selected.
                if (checkedExpenses != null) {
                    reselectCheckedExpenses(offlineExpenses);
                }

                // Ensure we clear out the expense to button map.
                if (expenseButtonMap != null) {
                    expenseButtonMap.clear();
                }

                // Then convert them to ListItems
                for (Expense oe : offlineExpenses) {
                    CashExpenseListItem listItem = new CashExpenseListItem(oe, expenseButtonMap, checkedExpenses,
                            onCheckChange, EXPENSE_VIEW_TYPE);
                    listItems.add(listItem);
                }

            }

            // Now get the receipts
            MobileDatabase mdb = app.getService().getMobileDatabase();
            List<ReceiptShareItem> rsiList = mdb.loadReceiptShareItems(ReceiptShareItem.Status.HOLD);
            if (rsiList != null && rsiList.size() > 0) {
                List<ReceiptInfo> riList = new ArrayList<ReceiptInfo>(rsiList.size());
                for (ReceiptShareItem rsi : rsiList) {
                    riList.add(new ReceiptInfo(rsi));
                }

                // Ensure any previously checked items that are still present in
                // our new data
                // appear selected.
                if (checkedReceipts != null) {
                    reselectCheckedReceipts(riList);
                }

                // Ensure we clear out the expense to button map.
                if (receiptButtonMap != null) {
                    receiptButtonMap.clear();
                }

                // Then convert them to list items
                for (ReceiptInfo ri : riList) {
                    ri.generateThumbnail();
                    OfflineReceiptListItem rli = new OfflineReceiptListItem(ri, receiptButtonMap, checkedReceipts,
                            onCheckChange, RECEIPT_VIEW_TYPE);
                    listItems.add(rli);
                }
            }

            // And sort everything
            Collections.sort(listItems, new ListItemComparator());

            return listItems;
        }

        @Override
        protected void onPostExecute(List<ListItem> listItems) {
            postExec.setListItems(listItems);
            postExec.run();

            // Toggle the bar.
            toggleButtonBar();

            // TODO Dialog
        }

    }

    protected class ListItemComparator implements Comparator<ListItem> {

        @Override
        public int compare(ListItem lhs, ListItem rhs) {
            Calendar lhsCal = null;
            Calendar rhsCal = null;

            // We are just sorting by timestamp.
            if (lhs != null) {
                lhsCal = lhs.getCalendar();
            }

            if (rhs != null) {
                rhsCal = rhs.getCalendar();
            }

            if (lhsCal != null && rhsCal != null) {
                return lhsCal.compareTo(rhsCal);
            } else if (rhsCal == null) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    protected void initListView() {
        listView = (ListView) findViewById(R.id.offline_upload_list);
        emptyView = findViewById(R.id.offline_no_item_view);

        // Clear out the mapping from compound buttons to expense entries.
        if (expenseButtonMap != null) {
            expenseButtonMap.clear();
        } else {
            expenseButtonMap = new HashMap<Expense, CompoundButton>();
        }
        if (receiptButtonMap != null) {
            receiptButtonMap.clear();
        } else {
            receiptButtonMap = new HashMap<ReceiptInfo, CompoundButton>();
        }

        // Clear out the current set of checked expenses.
        if (checkedExpenses != null) {
            checkedExpenses.clear();
        } else {
            checkedExpenses = new HashSet<Expense>();
        }
        if (checkedReceipts != null) {
            checkedReceipts.clear();
        } else {
            checkedReceipts = new HashSet<ReceiptInfo>();
        }

        listView.setOnItemClickListener(new OfflineItemClickListener());
        listItemAdapter = new ListItemAdapter<ListItem>(OffLineUploadList.this, null);
        listView.setAdapter(listItemAdapter);

    }

    protected void updateUI() {
        int itemCount = listItemAdapter.getCount();
        if (itemCount <= 0) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);

            if (uploadActionItem != null) {
                uploadActionItem.setEnabled(false);
            }
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            if (uploadActionItem != null) {
                uploadActionItem.setEnabled(true);
            }
        }
    }

    /**
     * Will display a dialog box
     * 
     * @param view
     */
    public void onDelete(View view) {

        DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {

            @Override
            @SuppressWarnings("unchecked")
            public void onClick(DialogInterface dialog, int which) {

                if (which == DialogInterface.BUTTON_POSITIVE) {

                    // List of selected cash transactions.
                    ArrayList<Object> items = new ArrayList<Object>();
                    Iterator<Expense> selExpIter = checkedExpenses.iterator();
                    while (selExpIter.hasNext()) {
                        Expense exp = selExpIter.next();
                        items.add(exp.getCashTransaction());
                        selExpIter.remove();
                    }

                    // List of selected receipts
                    Iterator<ReceiptInfo> selRecIter = checkedReceipts.iterator();
                    while (selRecIter.hasNext()) {
                        items.add(selRecIter.next());
                        selRecIter.remove();
                    }

                    if (!items.isEmpty()) {

                        new AsyncTask<ArrayList<Object>, Void, Void>() {

                            @Override
                            protected void onPreExecute() {
                                // Display the "deleting expense" dialog.
                                showDialog(Const.DIALOG_EXPENSE_DELETE);
                            }

                            @Override
                            protected Void doInBackground(ArrayList<Object>... params) {
                                ArrayList<Object> items = params[0];

                                ConcurCore app = (ConcurCore) ConcurCore.getContext();
                                IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
                                MobileDatabase mdb = app.getService().getMobileDatabase();

                                for (Object o : items) {
                                    if (o instanceof MobileEntry) {
                                        MobileEntry me = (MobileEntry) o;
                                        if (mdb.deleteMobileEntryByLocalKey(me.getLocalKey())) {
                                            // Remove it from the cache
                                            expEntCache.removeMobileEntry(me);
                                        }
                                    } else if (o instanceof ReceiptInfo) {
                                        ReceiptInfo ri = (ReceiptInfo) o;
                                        mdb.deleteOfflineReceipt(ri);
                                    }
                                }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {

                                dismissDialog(Const.DIALOG_EXPENSE_DELETE);

                                new GetListItemsAsync(new ListItemsPostExec() {

                                    @Override
                                    public void run() {
                                        listItemAdapter.setItems(listItems);
                                        updateUI();

                                    }

                                }).execute();
                            }

                        }.execute(items);

                    }

                    toggleButtonBar();

                }
            }
        };

        int qty = checkedExpenses.size() + checkedReceipts.size();

        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setMessage(getApplication().getResources().getQuantityText(
                R.plurals.dlg_offline_remove_confirm_message, qty));
        dlgBldr.setPositiveButton(R.string.cardlist_btn_delete, deleteListener);
        dlgBldr.setNegativeButton(R.string.general_cancel, deleteListener);

        dlgBldr.show();
    }

    protected void slideButtonBar(final boolean onScreen) {
        Float fromY = (onScreen) ? 1.0f : 0.0f;
        Float toY = (onScreen) ? 0.0f : 1.0f;
        TranslateAnimation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, fromY, Animation.RELATIVE_TO_SELF, toY);
        slide.setDuration(400);
        slide.setFillAfter(true);

        slide.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // No-op
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // No-op
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onScreen) {
                    buttonBar.setVisibility(View.VISIBLE);
                    buttonBar.bringToFront();
                } else {
                    buttonBar.setVisibility(View.GONE);
                    listView.bringToFront();
                }
            }
        });

        buttonBar.startAnimation(slide);

    }

    /**
     * Will either display or hide the slideable button bar depending upon whether any expense checkboxes have been checked.
     */
    protected void toggleButtonBar() {
        boolean onScreen;
        if (checkedExpenses.size() > 0 || checkedReceipts.size() > 0) {
            onScreen = true;
        } else {
            onScreen = false;
        }
        if (onScreen != barVisible) {
            slideButtonBar(onScreen);
            barVisible = onScreen;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        Dialog dlg = null;
        switch (id) {
        case DIALOG_UPLOAD_PROGRESS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(OffLineUploadList.this);
            dlgBldr.setTitle(getString(R.string.offline_upload_dialog_header));

            LayoutInflater inflater = LayoutInflater.from(OffLineUploadList.this);
            View customView = inflater.inflate(R.layout.offline_upload_dialog, null);
            dlgBldr.setView(customView);

            progressText = (TextView) customView.findViewById(R.id.itemsNumberUpdated);
            String text = getString(R.string.offline_upload_dialog_message, uploadingItemCount, totalItemCount);
            progressText.setText(text);

            dlgBldr.setNegativeButton(R.string.offline_upload_dialog_stop, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Set the flag. Will be picked up on next loop.
                    uploadCancelled = true;
                }
            });

            dlg = dlgBldr.create();
            break;
        }
        default: {
            // Fall through to letting the base create the dialog.
            dlg = super.onCreateDialog(id, args);
            break;
        }
        }

        return dlg;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_EXPENSE_SAVE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE: {
            ProgressDialog progDlg = (ProgressDialog) dialog;
            int quantity = 0;
            if (checkedExpenses != null) {
                quantity += checkedExpenses.size();
            }
            if (checkedReceipts != null) {
                quantity += checkedReceipts.size();
            }
            progDlg.setMessage(getApplication().getResources().getQuantityText(R.plurals.dlg_expense_delete_fragment,
                    quantity));
        }
        }
    }

    /** upload list of items. */
    protected void uploadList() {

        if (ConcurCore.isConnected()) {

            List<ListItem> items = listItemAdapter.getItems();

            // Flurry Notification.
            int count = 0;
            int quickExpenseCount = 0;
            int quickExpenseWithReceiptCount = 0;
            int receiptCount = 0;
            if (items != null) {
                for (ListItem listItem : items) {
                    ++count;
                    if (listItem instanceof CashExpenseListItem) {
                        CashExpenseListItem ciListItem = (CashExpenseListItem) listItem;
                        if (ciListItem.getExpense().getCashTransaction() != null) {
                            MobileEntry mobileEntry = ciListItem.getExpense().getCashTransaction();
                            if ((mobileEntry.getReceiptImageId() != null && mobileEntry.getReceiptImageId().length() > 0)
                                    || (mobileEntry.hasReceiptImage() || mobileEntry.hasReceiptImageDataLocal())) {
                                ++quickExpenseWithReceiptCount;
                            } else {
                                ++quickExpenseCount;
                            }
                        }
                    } else if (listItem instanceof OfflineReceiptListItem) {
                        ++receiptCount;
                    }
                }

            }

            // Register all the receivers once
            registerSaveExpenseReceiver();
            registerSaveReceiptReceiver();

            // Set our initial counts
            totalItemCount = items.size();
            uploadingItemCount = 0;
            uploadCancelled = false;
            // Flurry counter inits.
            uploadQuickExpenseCount = 0;
            uploadReceiptCount = 0;
            uploadStartTimeMillis = System.currentTimeMillis();
            uploadStopTimeMillis = 0L;

            // Spin up the dialog
            showDialog(DIALOG_UPLOAD_PROGRESS);

            // Upload the first item. This method is re-entered via the
            // receivers to process the entire list
            uploadItem();

        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    protected void uploadItem() {

        List<ListItem> items = listItemAdapter.getItems();

        // Get top item in the list. If nothing there, then be done.
        if (!uploadCancelled && (items != null && items.size() > 0)) {
            ListItem li = items.get(0);
            if (li instanceof ExpenseListItem) {
                final Expense e = ((ExpenseListItem) li).getExpense();
                final MobileEntry mobileEntry = e.getCashTransaction();

                // Increment and update as we are now uploading
                uploadingItemCount++;
                ++uploadQuickExpenseCount;
                updateProgressDialog();

                if (mobileEntry.hasReceiptImageDataLocal()) {
                    // If there is a receipt, send it first. The receipt
                    // receiver will send the expense.
                    sendSaveReceiptRequest(mobileEntry);
                } else {
                    // No receipt, just send the entry up. The receiver will
                    // remove it and call this method again.
                    sendSaveExpenseRequest(mobileEntry);
                }
            } else if (li instanceof OfflineReceiptListItem) {
                final ReceiptInfo ri = ((OfflineReceiptListItem) li).getReceiptInfo();

                // Increment and update as we are now uploading
                uploadingItemCount++;
                ++uploadReceiptCount;
                updateProgressDialog();

                // Send it
                sendSaveReceiptRequest(ri);
            }
        } else {

            uploadStopTimeMillis = System.currentTimeMillis();

            // Clear counts since they may be used as flags
            totalItemCount = 0;
            uploadingItemCount = 0;
            uploadStartTimeMillis = uploadStopTimeMillis = 0;
            uploadQuickExpenseCount = 0;
            uploadReceiptCount = 0;

            // Unregister receivers
            unregisterSaveExpenseReceiver();
            unregisterSaveReceiptReceiver();

            // Set the flag that the expense entry cache should be refetched.
            ConcurCore ConcurCore = getConcurCore();
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            expEntCache.setShouldFetchExpenseList();

            // Set our result. The parent activity may want to know this.
            setResult(Activity.RESULT_OK);

            // List is empty or cancelled. Get out.
            updateUI();
            removeDialog(DIALOG_UPLOAD_PROGRESS);

        }
    }

    protected void updateProgressDialog() {
        if (progressText != null) {
            String text = getString(R.string.offline_upload_dialog_message, uploadingItemCount, totalItemCount);
            progressText.setText(text);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_NO_CONNECTIVITY: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getString(R.string.offline_dialog_header).toString());
            dlgBldr.setMessage(getString(R.string.offline_dialog_message).toString());
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }

        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /**
     * Will send a request to save the receipt.
     */
    protected void sendSaveExpenseRequest(MobileEntry mobileEntry) {
        ConcurService concurService = getConcurService();

        // Save the mobile entry.
        saveExpenseRequest = concurService.saveMobileEntry(getUserId(), mobileEntry,
                ReceiptPictureSaveAction.NO_ACTION, null, false, true, true);

        if (saveExpenseRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendSaveExpenseRequest: unable to create request to save expense!");
        } else {
            // Set the request object on the receiver.
            saveExpenseReceiver.setServiceRequest(saveExpenseRequest);
        }
    }

    /**
     * Will register an instance of <code>SaveExpenseReceiver</code> with the application context and set the
     * <code>saveExpenseReceiver</code> attribute.
     */
    protected void registerSaveExpenseReceiver() {
        if (saveExpenseReceiver == null) {
            saveExpenseReceiver = new SaveExpenseReceiver(this);
            if (saveExpenseFilter == null) {
                saveExpenseFilter = new IntentFilter(Const.ACTION_EXPENSE_MOBILE_ENTRY_SAVED);
            }
            getApplicationContext().registerReceiver(saveExpenseReceiver, saveExpenseFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveExpenseReceiver: saveExpenseReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>SaveExpenseReceiver</code> with the application context and set the
     * <code>saveExpenseReceiver</code> to <code>null</code>.
     */
    protected void unregisterSaveExpenseReceiver() {
        if (saveExpenseReceiver != null) {
            getApplicationContext().unregisterReceiver(saveExpenseReceiver);
            saveExpenseReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveExpenseReceiver: saveExpenseReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to saving an expense.
     */
    static class SaveExpenseReceiver extends BaseBroadcastReceiver<OffLineUploadList, SaveMobileEntryRequest> {

        /**
         * Constructs an instance of <code>SaveExpenseReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected SaveExpenseReceiver(OffLineUploadList activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(OffLineUploadList activity) {
            activity.saveExpenseRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            // activity.dismissDialog(DIALOG_SAVE_EXPENSE);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_SAVE_FAILED);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {

            // The base class should ensure that we do not get here with a null
            // Activity
            // Unfortunately something is slipping through the cracks.
            if (activity != null) {
                // Remove the expense from the list
                ListItem li = activity.listItemAdapter.getItems().remove(0);
                activity.listItemAdapter.notifyDataSetChanged();
                Expense e = ((ExpenseListItem) li).getExpense();
                MobileEntry me = e.getCashTransaction();

                // Remove the expense from the DB
                ConcurService concurService = activity.getConcurService();
                MobileDatabase mdb = concurService.getMobileDatabase();
                boolean result = mdb.deleteMobileEntryByLocalKey(me.getLocalKey());
                if (!result) {
                    // hrrmmmm
                    // TODO
                }

                // Remove the expense from the cache
                ConcurCore app = (ConcurCore) ConcurCore.getContext();
                IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
                expEntCache.removeMobileEntry(me);

                // Go back for the next one.
                activity.uploadItem();
            } else {
                // Try to recover
                this.intent = intent;
            }
        }

        @Override
        protected void setActivityServiceRequest(SaveMobileEntryRequest request) {
            activity.saveExpenseRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            // activity.unregisterSaveExpenseReceiver();
        }

    }

    private void sendSaveReceiptRequest(MobileEntry mobileEntry) {
        ConcurService concurService = getConcurService();
        saveReceiptRequest = concurService.sendSaveReceiptRequest(getUserId(),
                mobileEntry.getReceiptImageDataLocalFilePath(), true, null, true);

        if (saveReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendSaveReceiptRequest: unable to create request to save receipt!");
        } else {
            // Set the entry and request object on the receiver.
            saveReceiptReceiver.setServiceRequest(saveReceiptRequest);
            saveReceiptReceiver.setEntry(mobileEntry);
        }
    }

    private void sendSaveReceiptRequest(ReceiptInfo ri) {
        ConcurService concurService = getConcurService();
        saveReceiptRequest = concurService.sendSaveReceiptRequest(getUserId(), ri.getFileName(), true, null, true);

        if (saveReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendSaveReceiptRequest: unable to create request to save receipt!");
        } else {
            // Set the entry and request object on the receiver.
            saveReceiptReceiver.setServiceRequest(saveReceiptRequest);
            saveReceiptReceiver.setReceiptInfo(ri);
        }
    }

    /**
     * Will register an instance of <code>SaveReceiptReceiver</code> with the application context and set the
     * <code>saveReceiptReceiver</code> attribute.
     */
    protected void registerSaveReceiptReceiver() {
        if (saveReceiptReceiver == null) {
            saveReceiptReceiver = new SaveReceiptReceiver(this);
            if (saveReceiptFilter == null) {
                saveReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_SAVE);
            }
            getApplicationContext().registerReceiver(saveReceiptReceiver, saveReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReceiptReceiver: saveReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>SaveReceiptReceiver</code> with the application context and set the
     * <code>saveReceiptReceiver</code> to <code>null</code>.
     */
    protected void unregisterSaveReceiptReceiver() {
        if (saveReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(saveReceiptReceiver);
            saveReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReceiptReceiver: saveReceiptReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to saving a receipt.
     */
    static class SaveReceiptReceiver extends BaseBroadcastReceiver<OffLineUploadList, SaveReceiptRequest> {

        protected MobileEntry currentEntry;
        protected ReceiptInfo currentReceipt;

        /**
         * Constructs an instance of <code>ReceiptSaveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected SaveReceiptReceiver(OffLineUploadList activity) {
            super(activity);
        }

        protected void setEntry(MobileEntry mobileEntry) {
            currentEntry = mobileEntry;
        }

        protected void setReceiptInfo(ReceiptInfo ri) {
            currentReceipt = ri;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(OffLineUploadList activity) {
            activity.saveReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            // activity.dismissDialog(DIALOG_SAVE_RECEIPT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            boolean handled = false;
            if (httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                if (mwsErrorMessage != null
                        && mwsErrorMessage.equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
                    activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
                    handled = true;
                }
            }
            return handled;
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {

            // The base class should ensure that we do not get here with a null
            // Activity
            // Unfortunately something is slipping through the cracks.
            if (activity != null) {
                if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                    String receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                    if (receiptImageId != null) {
                        receiptImageId = receiptImageId.trim();
                    }
                    if (currentEntry != null) {
                        // Set the Receipt Image ID on the local reference and
                        // in the mobile entry reference.
                        if (receiptImageId != null && receiptImageId.length() > 0) {
                            currentEntry.setReceiptImageId(receiptImageId);
                            // Proceed with saving the expense.
                            activity.sendSaveExpenseRequest(currentEntry);
                            currentEntry = null;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".handleSuccess: save receipt result intent has null/empty receipt image id!");
                            handleFailure(context, intent);
                            currentEntry = null;
                        }
                    } else if (currentReceipt != null) {
                        // Remove the receipt from the list
                        activity.listItemAdapter.getItems().remove(0);
                        activity.listItemAdapter.notifyDataSetChanged();

                        if (receiptImageId != null && receiptImageId.length() > 0) {
                            // Done. Receipt store will pull it down. Delete
                            // local.
                            ConcurService concurService = activity.getConcurService();
                            MobileDatabase mdb = concurService.getMobileDatabase();
                            mdb.deleteOfflineReceipt(currentReceipt);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".handleSuccess: save receipt result intent has null/empty receipt image id!");
                            handleFailure(context, intent);
                            currentEntry = null;
                        }

                        // Go back for the next one.
                        activity.uploadItem();
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".handleSuccess: save receipt succeeded but missing receipt image id!");
                    handleFailure(context, intent);
                }
            } else {
                // Try to recover
                this.intent = intent;
            }
        }

        @Override
        protected void setActivityServiceRequest(SaveReceiptRequest request) {
            activity.saveReceiptRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            // activity.unregisterSaveReceiptReceiver();
        }

    }

    class OfflineItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listItemAdapter != null) {
                ListItem li = listItemAdapter.getItem(position);
                if (li instanceof CashExpenseListItem) {
                    Expense e = ((ExpenseListItem) li).getExpense();
                    MobileEntry mobileEntry = e.getCashTransaction();
                    Intent intent = new Intent(getApplicationContext(), QuickExpense.class);
                    intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, e.getExpenseEntryType().name());
                    intent.putExtra(Const.EXTRA_EXPENSE_LOCAL_KEY, mobileEntry.getLocalKey());

                    startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                } else if (li instanceof OfflineReceiptListItem) {
                    ReceiptInfo ri = ((OfflineReceiptListItem) li).getReceiptInfo();
                    File receiptContentPath = new File(ri.getFileName());
                    if (ri.getFileType().equalsIgnoreCase("JPG") || ri.getFileType().equalsIgnoreCase("PNG")) {
                        File extStoreDir = Environment.getExternalStorageDirectory();
                        File destFile = new File(extStoreDir, "receipt." + ri.getFileType().toLowerCase());
                        ViewUtil.copyFile(receiptContentPath, destFile, (64 * 1024));
                        String receiptContentPathStr = URLEncoder.encode(receiptContentPath.getAbsolutePath());
                        receiptContentPathStr = "file:/" + receiptContentPathStr;
                        Intent intent = new Intent(OffLineUploadList.this, ViewImage.class);
                        try {
                            receiptContentPathStr = destFile.toURL().toExternalForm();
                        } catch (MalformedURLException mlfUrlExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".showDownloadedReceiptContent: malformed URL ", mlfUrlExc);
                        }
                        intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
                        intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, receiptContentPathStr);
                        intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
                        intent.putExtra(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_QUEUED_RECEIPT);

                        startActivity(intent);
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: null expense list adapter!");
            }
        }
    }

    /**
     * Upon an update in the list of expenses, newly parsed data can actually represent the same expense, but with two different
     * instances of <code>Expense</code> objects. So, this method will iterate over the old set of selected expenses and ensure
     * any new expense object representing the same data is placed within a new hash set of checked expenses becoming the official
     * set.
     */
    private void reselectCheckedExpenses(List<Expense> expenses) {
        Iterator<Expense> ckExpIter = checkedExpenses.iterator();
        HashSet<Expense> newCheckedExpenses = new HashSet<Expense>();

        while (ckExpIter.hasNext()) {
            Expense ckExp = ckExpIter.next();
            Iterator<Expense> expIter = expenses.iterator();
            while (expIter.hasNext()) {
                Expense listExp = expIter.next();

                // Do the expense keys match?
                if (ckExp.getCashTransaction().getLocalKey()
                        .equalsIgnoreCase(listExp.getCashTransaction().getLocalKey())) {
                    newCheckedExpenses.add(listExp);
                }
                break;
            }
        }

        // Reset the the list of checked expenses based on the above.
        checkedExpenses = newCheckedExpenses;

    }

    private void reselectCheckedReceipts(List<ReceiptInfo> receipts) {
        Iterator<ReceiptInfo> ckReceiptIter = checkedReceipts.iterator();
        HashSet<ReceiptInfo> newCheckedReceipts = new HashSet<ReceiptInfo>();

        while (ckReceiptIter.hasNext()) {
            ReceiptInfo ckRi = ckReceiptIter.next();
            Iterator<ReceiptInfo> riIter = receipts.iterator();
            while (riIter.hasNext()) {
                ReceiptInfo listRi = riIter.next();

                // Do the expense keys match?
                if (ckRi.getFileName().equalsIgnoreCase(listRi.getFileName())) {
                    newCheckedReceipts.add(listRi);
                }
                break;
            }
        }

        // Reset the the list of checked receipts based on the above.
        checkedReceipts = newCheckedReceipts;

    }

    /**
     * An implementation of <code>CompountButton.onCheckedChangeListener</code> to control showing/hiding of the button bar and
     * maintaining a set of selected expenses.
     */
    class OnCheckChange implements CompoundButton.OnCheckedChangeListener {

        private final String CLS_TAG = OffLineUploadList.CLS_TAG + "." + OnCheckChange.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged (android.widget.CompoundButton, boolean)
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            // Find the item in the map whose value is 'buttonView'.
            Expense exp = null;
            Iterator<Expense> expIter = expenseButtonMap.keySet().iterator();
            while (expIter.hasNext()) {
                exp = expIter.next();
                CompoundButton cmpBut = expenseButtonMap.get(exp);
                if (cmpBut == buttonView) {
                    break;
                }
                exp = null;
            }

            ReceiptInfo ri = null;
            if (exp == null) {
                // Check the receipt rows
                Iterator<ReceiptInfo> receiptIter = receiptButtonMap.keySet().iterator();
                while (receiptIter.hasNext()) {
                    ri = receiptIter.next();
                    CompoundButton cmpBut = receiptButtonMap.get(ri);
                    if (cmpBut == buttonView) {
                        break;
                    }
                    ri = null;
                }
            }

            if (exp != null) {
                if (isChecked) {
                    checkedExpenses.add(exp);
                } else {
                    checkedExpenses.remove(exp);
                }
                toggleButtonBar();
            } else if (ri != null) {
                if (isChecked) {
                    checkedReceipts.add(ri);
                } else {
                    checkedReceipts.remove(ri);
                }
                toggleButtonBar();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckedChanged: item not in button/expense map!");
            }
        }
    }

}
