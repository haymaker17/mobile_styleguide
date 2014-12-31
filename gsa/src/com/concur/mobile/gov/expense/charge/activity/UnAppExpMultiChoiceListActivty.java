/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.charge.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.activity.BasicListActivity;
import com.concur.mobile.gov.expense.charge.data.ActionStatus;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.gov.expense.charge.service.AddToVchReply;
import com.concur.mobile.gov.expense.charge.service.AddToVchRequest;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListReply;
import com.concur.mobile.gov.expense.doc.activity.DocumentListActivity;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDateSortComparatorUtil;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.platform.util.Format;

public class UnAppExpMultiChoiceListActivty extends UnAppliedList implements View.OnClickListener {

    protected final static String CLS_TAG = UnAppExpMultiChoiceListActivty.class.getSimpleName();

    private final static String RETAIN_EXP_LIST = "retainer.expenselist.key";
    private final static String SUCCESS = "SUCCESS";
    private static final String SELECTED_EXPENSE_KEY = "selected.mobile.expense.key";
    private final static String SELECT_ALL_VISIBILITY = "SELECT_ALL_VISIBILITY";

    // service helper variables
    private MultiChoiceExpenseAdapter expenseItemAdapter;
    private AddToVchServiceReceiver addVchServiceReceiver;
    private IntentFilter addToVchFilter;
    private AddToVchRequest addToVchRequest;

    private Bundle bundle;
    private String docName, docType;

    private Button selectAll, addToVch, unselectAll;
    private RelativeLayout noDataView;
    private ListView listView;

    private HashSet<MobileExpense> checkedExpItem;
    private ArrayList<Integer> selectedExpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore any receivers.
        restoreReceivers();
        noDataView = (RelativeLayout) findViewById(R.id.expense_no_list_view);
        listView = (ListView) findViewById(R.id.expense_list_view);
        bundle = getIntent().getExtras().getBundle(BasicListActivity.BUNDLE);
        if (bundle != null) {
            // TODO async task stuff
            docName = bundle.getString(DocumentListActivity.DOC_NAME);
            docType = bundle.getString(DocumentListActivity.DOCTYPE);
            // isRefresh = bundle.getBoolean(DocumentDetail.REFRESH);
        }
        initScreenHeader();
        initScreenFooter();
        initValue(savedInstanceState);
    }

    /** set screen footer. For this screen change name too */
    private void initScreenFooter() {
        final View footer = findViewById(R.id.footer);
        if (footer != null) {
            footer.setVisibility(View.VISIBLE);
            selectAll = (Button) (footer.findViewById(R.id.gov_footer_selectall));
            if (selectAll != null) {
                selectAll.setVisibility(View.VISIBLE);
                selectAll.setOnClickListener(this);
            }
            addToVch = (Button) (footer.findViewById(R.id.gov_footer_add_to_voucher));
            if (addToVch != null) {
                addToVch.setEnabled(false);
                addToVch.setOnClickListener(this);
            }
            unselectAll = (Button) (footer.findViewById(R.id.gov_footer_unselectall));
            if (unselectAll != null) {
                unselectAll.setVisibility(View.GONE);
                unselectAll.setOnClickListener(this);
            }
        }
    }

    /**
     * initialize value for the view.
     * */
    @SuppressWarnings({"unchecked"})
    private void initValue(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean visible = savedInstanceState.getBoolean(SELECT_ALL_VISIBILITY);
            if (visible) {
                selectAll.setVisibility(View.VISIBLE);
                unselectAll.setVisibility(View.GONE);
            } else {
                unselectAll.setVisibility(View.VISIBLE);
                selectAll.setVisibility(View.GONE);
            }
            if (retainer != null) {
                expList = (List<MobileExpense>) retainer.get(RETAIN_EXP_LIST);
                if (savedInstanceState.containsKey(SELECTED_EXPENSE_KEY)) {
                    selectedExpList = savedInstanceState.getIntegerArrayList(SELECTED_EXPENSE_KEY);
                    // Clear out the set of checked expenses.
                    if (checkedExpItem != null) {
                        checkedExpItem.clear();
                    } else {
                        checkedExpItem = new HashSet<MobileExpense>();
                    }

                }
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

        if (expList.size() <= 0) {
            showNoDataView();
        } else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (listView != null) {
                if (expList != null) {
                    // TODO it wont work on rotation...unselect any previously selected item.
                    if (checkedExpItem != null) {
                        checkedExpItem.clear();
                    } else {
                        checkedExpItem = new HashSet<MobileExpense>();
                    }
                    expenseItemAdapter = new MultiChoiceExpenseAdapter(this, expList);
                    listView.setAdapter(expenseItemAdapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    if (selectedExpList != null) {
                        final int size = selectedExpList.size();
                        for (int i = 0; i < size; ++i) {
                            int selectedExp = selectedExpList.get(i);
                            MobileExpense exp = ((MobileExpense) expenseItemAdapter.getItem(selectedExp));
                            if (exp != null) {
                                checkedExpItem.add(exp);
                            }
                        }// end of for

                        // update list
                        if (expenseItemAdapter != null) {
                            expenseItemAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    /** set the view where no data is available */
    @Override
    protected void showNoDataView() {
        noDataView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(R.id.expense_no_item_message);
        textView.setText(getString(R.string.gov_home_unapplied_expense_nodata).toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (retainer != null) {
            if (expList != null) {
                retainer.put(RETAIN_EXP_LIST, expList);
            }
        }

        outState.putBoolean(SELECT_ALL_VISIBILITY, selectAll.getVisibility() == View.VISIBLE);

        if (checkedExpItem != null && checkedExpItem.size() > 0) {
            Iterator<MobileExpense> ckExpIter = checkedExpItem.iterator();
            ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
            while (ckExpIter.hasNext()) {
                MobileExpense exp = ckExpIter.next();
                int adapterPos = -1;
                for (int listItemInd = 0; listItemInd < expenseItemAdapter.getCount(); ++listItemInd) {
                    if (expenseItemAdapter.getItem(listItemInd) instanceof MobileExpense) {
                        if (((MobileExpense) expenseItemAdapter.getItem(listItemInd)) == exp) {
                            adapterPos = listItemInd;
                            break;
                        }
                    }
                }
                if (adapterPos != -1) {
                    selectedPositions.add(adapterPos);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onSaveInstanceState: selected mobile expense has -1 position!");
                }
            }
            // Write out the list of selected item positions.
            outState.putIntegerArrayList(SELECTED_EXPENSE_KEY, selectedPositions);
        }
    }

    /**
     * Set list adapter for multi selection of list item.
     * */
    protected class MultiChoiceExpenseAdapter extends BaseAdapter {

        private final List<MobileExpense> list;
        private final Context context;

        public MultiChoiceExpenseAdapter(Context context, List<MobileExpense> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (list != null) {
                count = list.size();
            }
            return count;
        }

        @Override
        public MobileExpense getItem(int position) {
            MobileExpense exp = null;
            if (list != null) {
                exp = list.get(position);
            }
            return exp;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflator = LayoutInflater.from(context);
                view = inflator.inflate(R.layout.mobile_expense_row, null);

                final ViewHolder viewHolder = new ViewHolder();

                viewHolder.expName = (TextView) view.findViewById(R.id.expense_row_expname);
                viewHolder.expDate = (TextView) view.findViewById(R.id.expense_row_date);
                viewHolder.expAmount = (TextView) view.findViewById(R.id.expense_row_amount);
                viewHolder.receiptIcon = (ImageView) view.findViewById(R.id.expense_row_receipt);
                viewHolder.checkbox = (CheckBox) view.findViewById(R.id.expense_row_check);
                viewHolder.checkbox.setVisibility(View.VISIBLE);
                viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton view,
                        boolean isChecked) {
                        MobileExpense expense = (MobileExpense) viewHolder.checkbox.getTag();
                        // expense.setSelected(view.isChecked());
                        if (isChecked) {
                            checkedExpItem.add(expense);
                            changedAddToVchButtonText();
                        } else {
                            checkedExpItem.remove(expense);
                            changedAddToVchButtonText();
                        }
                    }
                });
                view.setTag(viewHolder);
                viewHolder.checkbox.setTag(list.get(position));
            } else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            MobileExpense exp = list.get(position);
            // set name
            holder.expName.setText(exp.tranDescription);
            // set date
            StringBuilder strBuilder = new StringBuilder("");
            strBuilder.append(Format
                .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, exp.tranDate));
            holder.expDate.setText(FormatUtil.nullCheckForString(strBuilder.toString()));
            // set amount
            String reportTotal = FormatUtil
                .formatAmount(exp.postedAmt, com.concur.mobile.gov.util.Const.GOV_LOCALE, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, true, true);
            holder.expAmount.setText(FormatUtil.nullCheckForString(reportTotal));
            // receipt icon
            String imageId = exp.imageid;
            if (imageId != null && imageId.length() > 0) {
                holder.receiptIcon.setVisibility(View.VISIBLE);
            } else {
                holder.receiptIcon.setVisibility(View.INVISIBLE);
            }
            // set checkbox
            holder.checkbox.setChecked(checkedExpItem.contains(exp));
            return view;
        }

        class ViewHolder {

            protected TextView expName, expDate, expAmount;
            protected ImageView receiptIcon;
            protected CheckBox checkbox;
        }

    }

    /***
     * @param setValue
     *            : value needs to be set for each list item
     * @param expList
     *            : Mobile expense list.
     * @return
     */
    private List<MobileExpense> setSelection(boolean setValue, List<MobileExpense> expList) {
        if (expList != null) {
            if (setValue) {
                for (MobileExpense expense : expList) {
                    checkedExpItem.add(expense);
                }
            } else {
                checkedExpItem.clear();
            }
            // change button text
            changedAddToVchButtonText();
        }
        return expList;
    }

    /**
     * change add to vch button text.
     * */
    private void changedAddToVchButtonText() {
        if (addToVch != null && checkedExpItem != null) {
            final int count = checkedExpItem.size();
            if (count == 0) {
                addToVch.setText(getString(R.string.gov_add_to_vch));
                addToVch.setEnabled(false);
            } else {
                addToVch.setEnabled(true);
                StringBuilder builder = new StringBuilder("");
                builder.append(getString(R.string.gov_add_to_vch));
                builder.append(" (");
                builder.append(count);
                builder.append(")");

                addToVch.setText(builder.toString());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.gov_footer_selectall: {
            List<MobileExpense> selectionResultantList = setSelection(true, expList);
            if (selectionResultantList != null) {
                if (listView == null) {
                    listView = (ListView) findViewById(R.id.expense_list_view);
                }
                if (expenseItemAdapter != null) {
                    expenseItemAdapter.notifyDataSetChanged();
                }
            }
            unselectAll.setVisibility(View.VISIBLE);
            selectAll.setVisibility(View.GONE);
            break;
        }
        case R.id.gov_footer_add_to_voucher: {
            if (checkedExpItem != null) {
                int count = checkedExpItem.size();
                if (count > 0) {
                    sendAddExpenseToVchRequest();
                }
            }
            break;
        }
        case R.id.gov_footer_unselectall: {
            List<MobileExpense> selectionResultantList = setSelection(false, expList);
            if (selectionResultantList != null) {
                if (listView == null) {
                    listView = (ListView) findViewById(R.id.expense_list_view);
                }
                if (expenseItemAdapter != null) {
                    expenseItemAdapter.notifyDataSetChanged();
                }
            }
            unselectAll.setVisibility(View.GONE);
            selectAll.setVisibility(View.VISIBLE);
            break;
        }
        default:
            break;
        }
    }

    private void sendAddExpenseToVchRequest() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerAddToVchReceiver();
                addToVchRequest = govService.addListOfExpToVch(docType, docName, checkedExpItem);
                if (addToVchRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendAddExpenseToVchRequest: unable to create request to get upload unapplied expenses to vch!");
                    unregisterAddToVchReceiver();
                } else {
                    // set service request.
                    addVchServiceReceiver.setServiceRequest(addToVchRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_ADD_EXP_TO_VCH);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * Register Add To Voucher Service Receiver
     * */
    private void registerAddToVchReceiver() {
        if (addVchServiceReceiver == null) {
            addVchServiceReceiver = new AddToVchServiceReceiver(this);
            if (addToVchFilter == null) {
                addToVchFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_ADD_TO_VCH_EXP);
            }
            getApplicationContext().registerReceiver(addVchServiceReceiver, addToVchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAddToVchReceiver is not null");
        }
    }

    /**
     * Un-Register Add To Voucher Service Receiver
     * */
    private void unregisterAddToVchReceiver() {
        if (addVchServiceReceiver != null) {
            getApplicationContext().unregisterReceiver(addVchServiceReceiver);
            addVchServiceReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAddToVchReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for mobile expense list(unapplied).
     */
    protected class AddToVchServiceReceiver extends
        BaseBroadcastReceiver<UnAppExpMultiChoiceListActivty, AddToVchRequest>
    {

        private final String CLS_TAG = UnAppExpMultiChoiceListActivty.CLS_TAG + "."
            + AddToVchServiceReceiver.class.getSimpleName();

        protected AddToVchServiceReceiver(UnAppExpMultiChoiceListActivty activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(UnAppExpMultiChoiceListActivty activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_ADD_EXP_TO_VCH);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_ADD_EXP_TO_VCH_FAIL);
            logFlurryEvent(false, 0);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final AddToVchReply reply = app.vchCache.getAddToVchReply();
            if (reply != null) {
                onHandleAddToVchListSuccess(reply);
            } else {
                logFlurryEvent(false, 0);
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(AddToVchRequest request) {
            activity.addToVchRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAddToVchReceiver();
        }
    }

    /**
     * Handle Successful result after successfully upload selected unapplied expenses.
     * 
     * @param reply
     *            : Reference of AddToVchReply.
     */
    private void onHandleAddToVchListSuccess(AddToVchReply reply) {
        String status = reply.status;
        int count = 0;
        if (status.equalsIgnoreCase(SUCCESS)) {
            for (ActionStatus docStatus : reply.documentList) {
                if ((docStatus.status != null) && docStatus.status.equalsIgnoreCase(SUCCESS)) {
                    count++;
                }
            }
            logFlurryEvent(true, count);
            setResult(RESULT_OK);
            finish();
        } else {
            logFlurryEvent(false, 0);
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_ADD_EXP_TO_VCH_FAIL);
        }
    }

    /**
     * Log flurry events that do you have doc info or not
     * 
     * @param count
     * */
    private void logFlurryEvent(boolean isSuccessful, int count) {
        Map<String, String> params = new HashMap<String, String>();
        if (isSuccessful) {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
            if (docType != null) {
                params.put(GovFlurry.PARAM_NAME_DOCUMENT_TYPE, docType);
            }
        } else {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_NO);
        }
        params.put(GovFlurry.PARAM_NAME_UNAPP_EXPENSE_COUNT, Integer.toString(count));
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_UNAPP_EXPLIST,
            GovFlurry.EVENT_ATTACH_EXP_TO_DOCUMENT, params);
    }

}
