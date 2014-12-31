/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.charge.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.gov.expense.charge.service.DeleteTMUnappliedExpenseRequest;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListReply;
import com.concur.mobile.gov.expense.doc.activity.DocumentDetail;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDateSortComparatorUtil;

public class UnAppliedExpenseListActivty extends UnAppliedList implements View.OnClickListener {

    protected final static String CLS_TAG = UnAppliedExpenseListActivty.class.getSimpleName();
    private final static String RETAIN_EXP_LIST = "retainer.expenselist.key";

    private ListItemAdapter<UnAppliedExpenseListItem> expenseItemAdapter;
    private UnAppliedExpenseListItem selectedExpenseItem;

    private DeleteTMUnappliedExpenseRequest deleteTMUnappliedExpenseRequest;
    private DeleteUnappliedExpenseReceiver deleteUnappliedExpenseReceiver;
    private IntentFilter deleteUnappliedExpenseIntentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore any receivers.
        restoreReceivers();
        initScreenHeader();
        initValue(savedInstanceState);
    }

    /**
     * initialize value for the view.
     * */
    @SuppressWarnings("unchecked")
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (retainer != null) {
                expList = (List<MobileExpense>) retainer.get(RETAIN_EXP_LIST);
                buildView(expList);
            } else {
                setViewConfiguration();
            }
        } else {
            setViewConfiguration();
        }
    }

    /** build view */
    @Override
    protected void buildView(List<MobileExpense> expList) {
        if (expList != null) {
            initExpenseList(expList);
        } else {
            showNoDataView();
        }
    }

    @Override
    protected void onHandleSuccessForActivity(MobileExpenseListReply reply) {
        super.onHandleSuccessForActivity(reply);
        expList = reply.mobExpList;
        Collections.sort(expList, new GovDateSortComparatorUtil());
        buildView(expList);
    }

    /** initialize list and set adapter */
    private void initExpenseList(List<MobileExpense> expList) {
        RelativeLayout noDataView = (RelativeLayout) findViewById(R.id.expense_no_list_view);
        final ListView listView = (ListView) findViewById(R.id.expense_list_view);
        if (expList.size() <= 0) {
            showNoDataView();
        } else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (listView != null) {
                List<UnAppliedExpenseListItem> listItem = null;
                if (expList != null) {
                    listItem = new ArrayList<UnAppliedExpenseListItem>(expList.size());
                    for (MobileExpense expense : expList) {
                        listItem.add(new UnAppliedExpenseListItem(expense));
                    }
                    expenseItemAdapter = new ListItemAdapter<UnAppliedExpenseListItem>(this, listItem);
                    listView.setAdapter(expenseItemAdapter);
                    listView.setOnItemClickListener(new OnItemClickListener() {

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedExpenseItem = (UnAppliedExpenseListItem) listView.getItemAtPosition(position);
                            // TODO
                            Intent i = new Intent(UnAppliedExpenseListActivty.this, com.concur.mobile.gov.expense.doc.activity.Expense.class);
                            bundle = new Bundle();
                            bundle.putString(UnAppliedList.CCEXPID, selectedExpenseItem.getExpense().getCcexpid());
                            bundle.putBoolean(UnAppliedList.ISFINISH, true);
                            bundle.putSerializable(UnAppliedList.QEOBJ, selectedExpenseItem.getExpense());
                            i.putExtra(DocumentDetail.BUNDLE, bundle);
                            startActivityForResult(i, REFRESH_CODE);
                        }
                    });

                    // Add long-press to delete the selected Expense.
                    listView.setLongClickable(true);
                    listView.setOnItemLongClickListener(new OnItemLongClickListener() {

                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedExpenseItem = (UnAppliedExpenseListItem) listView.getItemAtPosition(position);
                            registerForContextMenu(parent);
                            openContextMenu(parent);
                            return true;
                        }
                    });
                }
            }
        }
    }

    /** set the view where no data is available */
    @Override
    protected void showNoDataView() {
        RelativeLayout noDataView = (RelativeLayout) findViewById(R.id.expense_no_list_view);
        ListView listView = (ListView) findViewById(R.id.expense_list_view);
        noDataView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(R.id.expense_no_item_message);
        textView.setText(getString(R.string.gov_home_unapplied_expense_nodata).toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (deleteUnappliedExpenseReceiver != null) {
                // Clear activity and we will reassign.
                deleteUnappliedExpenseReceiver.setActivity(null);
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DELETE_UNAPPLIED_DOC_EXPENSE, deleteUnappliedExpenseReceiver);
            }
        }
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();

        if (retainer != null) {
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_DELETE_UNAPPLIED_DOC_EXPENSE)) {
                deleteUnappliedExpenseReceiver = (DeleteUnappliedExpenseReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DELETE_UNAPPLIED_DOC_EXPENSE);

                // Reset the activity reference
                deleteUnappliedExpenseReceiver.setActivity(this);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (expList != null) {
                retainer.put(RETAIN_EXP_LIST, expList);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.action_button:
            break;
        default:
            break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.action_button) {

        } else if (selectedExpenseItem != null) {

            menu.setHeaderTitle(R.string.expense_entry_action);
            MenuItem menuItem = menu.add(0, 0, 0, R.string.gov_delete_expense);
            menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    showDeleteExpenseConfirmation();
                    return true;
                }
            });
        }
    }

    protected void registerDeleteExpenseReceiver() {

        if (deleteUnappliedExpenseReceiver == null) {
            deleteUnappliedExpenseReceiver = new DeleteUnappliedExpenseReceiver(this);
            if (deleteUnappliedExpenseIntentFilter == null) {
                deleteUnappliedExpenseIntentFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_UNAPPLIED_EXPENSE_DELETED);
            }
            getApplicationContext().registerReceiver(deleteUnappliedExpenseReceiver, deleteUnappliedExpenseIntentFilter);
        }
    }

    protected void unregisterDeleteExpenseReceiver() {
        if (deleteUnappliedExpenseReceiver != null) {
            getApplicationContext().unregisterReceiver(deleteUnappliedExpenseReceiver);
            deleteUnappliedExpenseReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDeleteExpenseReceiver is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.gov.activity.UnAppliedList#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_EXPENSE_DELETE: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_deleting_expense));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            dialog = pDialog;
            break;
        }
        case Const.DIALOG_EXPENSE_DELETE_FAILED: {
            // Set the failure message.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.gov_dlg_title_delete_expense_error)
                .setMessage(actionStatusErrorMessage)
                .setPositiveButton(R.string.okay, null);
            dialog = builder.create();
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_UPDATING_EXPENSE_LIST: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_dlg_updating_unapplied_expenses));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            dialog = pDialog;
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
     * Displays a confirmation dialog of whether or not to delete the selected Expense Entry.
     */
    private void showDeleteExpenseConfirmation() {

        if (selectedExpenseItem != null) {

            final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dlg_delete_receipt_confirmation_title)
                .setMessage(R.string.gov_dlg_delete_unapplied_expense_confirmation)
                .setPositiveButton(R.string.general_ok,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            GovService svc = (GovService) getConcurService();
                            registerDeleteExpenseReceiver();
                            deleteTMUnappliedExpenseRequest = svc.deleteTMUnappliedExpense(selectedExpenseItem.getExpense().ccexpid);

                            if (deleteTMUnappliedExpenseRequest == null) {
                                unregisterDeleteExpenseReceiver();
                            } else {
                                deleteUnappliedExpenseReceiver.setServiceRequest(deleteTMUnappliedExpenseRequest);

                                // Display the "deleting expense" dialog.
                                showDialog(Const.DIALOG_EXPENSE_DELETE);
                            }

                            selectedExpenseItem = null;
                        }
                    }
                )
                .setNegativeButton(R.string.general_cancel, null).create();

            dialog.show();
        }

    }

    /**
     * 
     * An extension of <code>BroadcastReceiver</code> for responding to the result of
     * deleting a doc expense.
     * 
     * @author Chris N. DIaz
     */
    class DeleteUnappliedExpenseReceiver extends
        BaseBroadcastReceiver<UnAppliedExpenseListActivty, DeleteTMUnappliedExpenseRequest> {

        final String CLS_TAG = UnAppliedExpenseListActivty.CLS_TAG + "."
            + DeleteUnappliedExpenseReceiver.class.getSimpleName();

        protected DeleteUnappliedExpenseReceiver(UnAppliedExpenseListActivty activity) {
            super(activity);
        }

        @Override
        protected void setActivityServiceRequest(DeleteTMUnappliedExpenseRequest request) {
            activity.deleteTMUnappliedExpenseRequest = request;
        }

        @Override
        protected void clearActivityServiceRequest(UnAppliedExpenseListActivty activity) {
            activity.deleteTMUnappliedExpenseRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_DELETE);
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterDeleteExpenseReceiver();
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Invoke MWS to update the list of Unapplied Expenses.
            sendExpenseListReq();
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            activity.showDialog(Const.DIALOG_EXPENSE_DELETE_FAILED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REFRESH_CODE) {
            if (resultCode == RESULT_OK) {
                sendExpenseListReq();
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

}
