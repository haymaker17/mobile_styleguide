/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.activity.BasicListActivity;
import com.concur.mobile.gov.expense.activity.ExpenseListItem;
import com.concur.mobile.gov.expense.charge.activity.UnAppExpMultiChoiceListActivty;
import com.concur.mobile.gov.expense.charge.activity.UnAppliedList;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.GovExpense;
import com.concur.mobile.gov.expense.doc.service.DeleteTMExpenseRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovFlurry;

public class ExpenseListActivity extends BasicListActivity implements View.OnClickListener {

    private ListItemAdapter<ExpenseListItem> expenseDrillInOptionAdapter;
    private ExpenseListItem selectedDrillInOption;
    private DeleteTMExpenseRequest deleteTMExpenseRequest;
    private DeleteExpenseReceiver deleteExpenseReceiver;
    private IntentFilter deleteExpenseIntentFilter = null;

    private static final int REFRESH_CODE = 1;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore any receivers.
        restoreReceivers();
    }

    /**
     * This method set screen header title.
     * 
     * */
    @Override
    protected void initScreenHeader() {
        View view = findViewById(R.id.header);
        if (view != null) {
            TextView txtView = (TextView) findViewById(R.id.header_navigation_bar_title);
            if (txtView != null) {
                txtView.setText(getTitleText());
            }
            ImageView actionButton = (ImageView) (view.findViewById(R.id.action_button));
            if (actionButton != null) {
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(this);
            }
        }
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.gov_docdetail_doc_expense);
    }

    @Override
    protected void configureListItems() {
        list = (ListView) findViewById(R.id.gov_drillin_opt_list_view);
        if (list != null) {
            int count = 0;
            List<ExpenseListItem> expList = new ArrayList<ExpenseListItem>();
            List<GovExpense> expenseList = docDetailInfo.expensesList;
            if (expenseList != null) {
                for (GovExpense expenses : expenseList) {
                    ExpenseListItem expInListItem = new ExpenseListItem(expenses);
                    String imageId = expenses.imageid;
                    if (imageId != null && imageId.length() > 0) {
                        count++;
                    }
                    expList.add(expInListItem);
                }
                expenseDrillInOptionAdapter = new ListItemAdapter<ExpenseListItem>(this, expList);
                list.setAdapter(expenseDrillInOptionAdapter);
                list.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedDrillInOption = (ExpenseListItem) list.getItemAtPosition(position);
                        Intent i = new Intent(ExpenseListActivity.this, com.concur.mobile.gov.expense.doc.activity.Expense.class);
                        bundle.putString(DocumentListActivity.EXP_ID, selectedDrillInOption.getDocument().expid);
                        bundle.putBoolean(UnAppliedList.ISFINISH, true);
                        i.putExtra(DocumentDetail.BUNDLE, bundle);
                        startActivityForResult(i, REFRESH_CODE);
                    }
                });

                // Add long-press to delete the selected Expense.
                list.setLongClickable(true);
                list.setOnItemLongClickListener(new OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedDrillInOption = (ExpenseListItem) list.getItemAtPosition(position);
                        registerForContextMenu(parent);
                        openContextMenu(parent);
                        return true;
                    }
                });
                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(GovFlurry.PARAM_NAME_RECEIPT_COUNT, Integer.toString(count));
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_DOCUMENT_DETAIL, GovFlurry.PARAM_VALUE_VIEW_EXPENSES,
                    params);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".configureListItems: docDetailInfo.expenseList is null..finish activity!");
                // TODO no data view
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureListItems: unable to find list view!");
        }
    }

    @Override
    public void onClick(View v) {
        View actionButton = findViewById(R.id.action_button);
        switch (v.getId()) {
        case R.id.action_button:
            registerForContextMenu(actionButton);
            openContextMenu(actionButton);
            break;

        default:
            break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (deleteExpenseReceiver != null) {
                // Clear activity and we will reassign.
                deleteExpenseReceiver.setActivity(null);
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_DELETE_DOC_EXPENSE, deleteExpenseReceiver);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    /**
     * Restore any reciever store for this activity into Retainer.
     */
    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        if (retainer != null) {
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_DELETE_DOC_EXPENSE)) {
                deleteExpenseReceiver = (DeleteExpenseReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_DELETE_DOC_EXPENSE);

                // Reset the activity reference
                deleteExpenseReceiver.setActivity(this);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.action_button) {
            android.view.MenuInflater infl = getMenuInflater();
            menu.setHeaderTitle(R.string.gov_add_to_vch_select_title);
            infl.inflate(R.menu.gov_add_to_vch_context, menu);
            menu.removeItem(R.id.gov_vch_add_to_vch_create);

        } else if (selectedDrillInOption != null) {

            menu.setHeaderTitle(R.string.expense_entry_action);
            android.view.MenuItem menuItem = menu.add(0, 0, 0, R.string.gov_delete_expense);
            menuItem.setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    showDeleteExpenseConfirmation();
                    return true;
                }
            });
        }
    }

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
            pDialog.setMessage(this.getText(R.string.updating_expenses));
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

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean handled = super.onContextItemSelected(item);
        final int itemId = item.getItemId();
        switch (itemId) {
        case R.id.gov_vch_add_to_vch_create:
            Log.d(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG + " do nothing from create");
            break;
        case R.id.gov_vch_add_to_vch_existing:
            goToUnAppliedExpenseListActivity();
            break;
        default:
            break;
        }
        return handled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gov_add_to_vch_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        /*
         * case R.id.gov_vch_add_to_vch_create:
         * Log.d(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG + " do nothing from create");
         * break;
         */
        case R.id.gov_vch_add_to_vch_existing:
            goToUnAppliedExpenseListActivity();
            break;
        default:
            break;
        }
        return true;
    }

    /**
     * Open unapplied expense list and add expense item to this voucher.
     * */
    private void goToUnAppliedExpenseListActivity() {
        Intent it = new Intent(ExpenseListActivity.this, UnAppExpMultiChoiceListActivty.class);
        it.putExtra(BUNDLE, bundle);
        startActivityForResult(it, DocumentDetail.REFRESH_EXPENSE_LIST_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DocumentDetail.REFRESH_EXPENSE_LIST_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                GovAppMobile app = (GovAppMobile) getApplication();
                app.setExpListRefreshReq(true);
                setResult(RESULT_OK);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == REFRESH_CODE) {
            if (resultCode == RESULT_OK) {
                sendDocumentDetailRequest(docname, doctype, travId);
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    /**
     * Displays a confirmation dialog of whether or not to delete the selected Expense Entry.
     */
    private void showDeleteExpenseConfirmation() {

        if (selectedDrillInOption != null) {

            final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dlg_delete_receipt_confirmation_title)
                .setMessage(R.string.gov_dlg_delete_doc_expense_confirmation)
                .setPositiveButton(R.string.general_ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            GovExpense ex = selectedDrillInOption.getDocument();
                            GovService svc = (GovService) getConcurService();

                            registerDeleteExpenseReceiver();
                            deleteTMExpenseRequest = svc.deleteTMExpense(docDetailInfo.documentName, docDetailInfo.docType, ex.expid);
                            if (deleteTMExpenseRequest == null) {
                                unregisterDeleteExpenseReceiver();
                            } else {
                                deleteExpenseReceiver.setServiceRequest(deleteTMExpenseRequest);

                                // Display the "deleting expense" dialog.
                                showDialog(Const.DIALOG_EXPENSE_DELETE);
                            }

                            selectedDrillInOption = null;
                        }
                    }
                )
                .setNegativeButton(R.string.general_cancel, null).create();

            dialog.show();
        }

    }

    protected void registerDeleteExpenseReceiver() {

        if (deleteExpenseReceiver == null) {
            deleteExpenseReceiver = new DeleteExpenseReceiver(this);
            if (deleteExpenseIntentFilter == null) {
                deleteExpenseIntentFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_DOC_EXPENSE_DELETED);
            }
            getApplicationContext().registerReceiver(deleteExpenseReceiver, deleteExpenseIntentFilter);
        }
    }

    protected void unregisterDeleteExpenseReceiver() {
        if (deleteExpenseReceiver != null) {
            getApplicationContext().unregisterReceiver(deleteExpenseReceiver);
            deleteExpenseReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDeleteExpenseReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for responding to the result of
     * deleting a doc expense.
     * 
     * @author Chris N. DIaz
     */
    class DeleteExpenseReceiver extends BaseBroadcastReceiver<ExpenseListActivity, DeleteTMExpenseRequest> {

        final String CLS_TAG = ExpenseListActivity.CLS_TAG + "." + DeleteExpenseReceiver.class.getSimpleName();

        protected DeleteExpenseReceiver(ExpenseListActivity activity) {
            super(activity);
        }

        @Override
        protected void setActivityServiceRequest(DeleteTMExpenseRequest request) {
            activity.deleteTMExpenseRequest = request;
        }

        @Override
        protected void clearActivityServiceRequest(ExpenseListActivity activity) {
            activity.deleteTMExpenseRequest = null;
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
            // Close the Expense List activity and go back to the Document Detail,
            // which will force a refresh of the data and UI.
            activity.setResult(RESULT_OK);
            activity.finish();
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            activity.showDialog(Const.DIALOG_EXPENSE_DELETE_FAILED);
        }
    }

    @Override
    protected void onHandleDocDetailSuccess(DsDocDetailInfo reply) {
        docDetailInfo = reply;
        if (docDetailInfo != null) {
            if (expenseDrillInOptionAdapter != null) {
                list = null;
                expenseDrillInOptionAdapter = null;
                configureListItems();
                // expenseDrillInOptionAdapter.notifyDataSetChanged();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to find document info from cache!");
        }
    }
}
