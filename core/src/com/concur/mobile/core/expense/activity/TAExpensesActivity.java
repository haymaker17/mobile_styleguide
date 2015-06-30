package com.concur.mobile.core.expense.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.AsyncReplyAdapter;
import com.concur.mobile.core.expense.travelallowance.FixedAllowanceAmounts;
import com.concur.mobile.core.expense.travelallowance.FixedAllowanceRow;
import com.concur.mobile.core.expense.travelallowance.FixedAllowances;
import com.concur.mobile.core.expense.travelallowance.GetTAFixedAllowancesRequest;
import com.concur.mobile.core.expense.travelallowance.GetUpdatedFixedAllowanceAmounts;
import com.concur.mobile.core.expense.travelallowance.UpdateFixedAllowances;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;

public class TAExpensesActivity extends BaseActivity {

    String CLS_TAG = TAExpensesActivity.class.getSimpleName();

    private String rptKey;
    private FixedAllowances fixedAllowances;

    private BaseAsyncResultReceiver asyncReceiver = new BaseAsyncResultReceiver(new Handler());
    private GetTAFixedAllowancesRequest getFixedAllowancesReq;
    private GetUpdatedFixedAllowanceAmounts getUpdatedAmountsReq;
    private UpdateFixedAllowances updateFixedAllowancesReq;
    
    private FixedAllowanceRowListItem rowBeingUpdated = null;
    
    private static final int DIALOG_UPDATING_EXPENSES = 0;
    private static final int DIALOG_FETCHING_ALLOWANCES = 1;
    private static final int DIALOG_UPDATING_AMOUNTS = 2;
    private ProgressDialog dialog;
    
    private static final String[] MEAL_PROVIDED_STR = { "NPR", "PRO", "TAX" };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ta_expenses_activity);

        getSupportActionBar().setTitle(R.string.itin_expenses_and_adjustments);

        if (savedInstanceState != null) {
            Log.i(CLS_TAG, "getting rptKey from savedInstanceState...");
            rptKey = savedInstanceState.getString(Const.EXTRA_EXPENSE_REPORT_KEY);
        } else if (getIntent().getExtras() != null) {
            Log.i(CLS_TAG, "getting rptKey from extras...");
            rptKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_KEY);
        } else {
            Log.i(CLS_TAG, "no savedInstanceState or extras...");
            return;
        }
        
        fetchFixedAllowances();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Blow up the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itinerary_save_menu, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.menuSave) {
        	updateFixedAllowances();
            return true;
        }
        return false;
    }

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_UPDATING_EXPENSES:
	        dialog = new ProgressDialog(this);
	        dialog.setMessage(getText(R.string.itin_updating_fixed));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog;
		case DIALOG_FETCHING_ALLOWANCES:
	        dialog = new ProgressDialog(this);
	        dialog.setMessage(getText(R.string.itin_fetching_allowances));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog;
		case DIALOG_UPDATING_AMOUNTS:
	        dialog = new ProgressDialog(this);
	        dialog.setMessage(getText(R.string.itin_updating_amounts));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog;
		}
		return super.onCreateDialog(id, args);
	}
    
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(Const.LOG_TAG, "onResart...");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Const.EXTRA_EXPENSE_REPORT_KEY, rptKey);
        super.onSaveInstanceState(outState);
    }

    private void populateList() {
        List<ListItem> listItems = new ArrayList<ListItem>();
        for (FixedAllowanceRow row : fixedAllowances.getRows()) {
            listItems.add(new FixedAllowanceRowListItem(row));
        }
        ListItemAdapter<ListItem> listItemAdapter = new ListItemAdapter<ListItem>(this, listItems, 1);
        ListView listView = (ListView)findViewById(R.id.listView1);
        if (listView != null) {
            listView.setAdapter(listItemAdapter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureReportEntries: no list view found!");
        }
        
    }
    
    private void fetchFixedAllowances() {
        asyncReceiver.setListener(new GetFixedAllowancesListener());
        getFixedAllowancesReq = new GetTAFixedAllowancesRequest(getApplicationContext(), 1, asyncReceiver, rptKey);
        showDialog(DIALOG_FETCHING_ALLOWANCES);
        getFixedAllowancesReq.execute();
    }

    private void updateFixedAllowances() {
        asyncReceiver.setListener(new UpdateFixedAllowancesListener());
        updateFixedAllowancesReq = new UpdateFixedAllowances(getApplicationContext(), 1, asyncReceiver, rptKey, fixedAllowances);
        showDialog(DIALOG_UPDATING_EXPENSES);
        updateFixedAllowancesReq.execute();
    }

    private void fetchUpdatedFixedAllowanceAmounts(FixedAllowanceRowListItem item) {
        this.rowBeingUpdated = item;
        
        rowBeingUpdated.setUpdating(true);
        repainList();

        asyncReceiver.setListener(new GetUpdatedFixedAllowanceAmountsListener());
        getUpdatedAmountsReq = new GetUpdatedFixedAllowanceAmounts(getApplicationContext(), 1, asyncReceiver, rptKey, fixedAllowances, item.row);
        showDialog(DIALOG_UPDATING_AMOUNTS);
        getUpdatedAmountsReq.execute();
    }

    private void repainList() {
        ListView list = (ListView)findViewById(R.id.listView1);
        ((ListItemAdapter<?>)list.getAdapter()).notifyDataSetChanged();
    }
    
    private void updateFixedAllowanceAmounts(FixedAllowanceAmounts amounts) {
        if (rowBeingUpdated == null) {
            return;
        }
        rowBeingUpdated.row.setAllowanceAmount(amounts.getAllowanceAmount());
        rowBeingUpdated.setUpdating(false);
        repainList();
    }
    
    protected class FixedAllowanceRowListItem extends ListItem {
        private boolean updating = false;
        private FixedAllowanceRow row;
        
        String[] choicesCheckbox = new String[] { getString(R.string.itin_meal_NPR), getString(R.string.itin_meal_PRO) }; 
        String[] choicesPicklist = new String[] { getString(R.string.itin_meal_NPR), getString(R.string.itin_meal_PRO), getString(R.string.itin_meal_TAX) };
        
        public FixedAllowanceRowListItem(FixedAllowanceRow row) {
            this.row = row;
        }
        
        public void setUpdating(boolean b) {
            this.updating = b;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }

        private Spinner configureSpinnerForRow(Context context, View view, boolean isCheckbox, boolean isPicklist, int spinnerId, String providedStr, int containerId) {
            ArrayAdapter<String> checkboxAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, choicesCheckbox);
            checkboxAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

            ArrayAdapter<String> picklistAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, choicesPicklist);
            picklistAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            
            Spinner spinner = (Spinner)view.findViewById(spinnerId);
            if (isCheckbox) {
                spinner.setAdapter(checkboxAdapter);
                if ("PRO".equals(providedStr)) {
                    spinner.setSelection(1, false);
                } 
            } else if (isPicklist) {
                spinner.setAdapter(picklistAdapter);
                if ("PRO".equals(providedStr)) {
                    spinner.setSelection(1, false);
                } else if ("TAX".equals(providedStr)) {
                    spinner.setSelection(2, false);
                }
            } else {
                View b = view.findViewById(containerId);
                b.setVisibility(View.GONE);
            }
            return spinner;
        }
        
        @Override
        public View buildView(final Context context, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.ta_expense_row, null);
            } else {
                view = convertView;
            }
            
            TextView dateView = (TextView)view.findViewById(R.id.taStopDate);
            dateView.setText(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(row.getAllowanceDate()));

            TextView cityView = (TextView)view.findViewById(R.id.taStopCity);
            cityView.setText(row.getLocation());
            
            TextView amountView = (TextView)view.findViewById(R.id.taStopAmount);
            if (updating) {
                amountView.setText(getString(R.string.title_header_progress_text)); 
            } else {
                amountView.setText("$" + String.valueOf(row.getAllowanceAmount())); // todo: find proper way to format money
            }

            Spinner spinner = configureSpinnerForRow(context, view, fixedAllowances.getShowBreakfastProvidedCheckBox(), fixedAllowances.getShowBreakfastProvidedPickList(), 
                    R.id.taBreakfastSpinner, row.getBreakfastProvided(), R.id.breakfast);
            spinner.setOnItemSelectedListener(new MyItemSelectedListener(row) {;
                @Override
                public void myOnItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    row.setBreakfastProvided(MEAL_PROVIDED_STR[pos]);
                    fetchUpdatedFixedAllowanceAmounts(FixedAllowanceRowListItem.this);
                }
            });
            
            spinner = configureSpinnerForRow(context, view, fixedAllowances.getShowLunchProvidedCheckBox(), fixedAllowances.getShowLunchProvidedPickList(), 
                    R.id.taLunchSpinner, row.getLunchProvided(), R.id.lunch);
            spinner.setOnItemSelectedListener(new MyItemSelectedListener(row) {;
                @Override
                public void myOnItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        row.setLunchProvided(MEAL_PROVIDED_STR[pos]);
                        fetchUpdatedFixedAllowanceAmounts(FixedAllowanceRowListItem.this);
                    }
                });

            spinner = configureSpinnerForRow(context, view, fixedAllowances.getShowDinnerProvidedCheckBox(), fixedAllowances.getShowDinnerProvidedPickList(), 
                    R.id.taDinnerSpinner, row.getDinnerProvided(), R.id.dinner);
            spinner.setOnItemSelectedListener(new MyItemSelectedListener(row) {;
                @Override
                public void myOnItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        row.setDinnerProvided(MEAL_PROVIDED_STR[pos]);
                        fetchUpdatedFixedAllowanceAmounts(FixedAllowanceRowListItem.this);
                    }
                });
            return view;
        }
    }

    // OnItemSelectedListener that only fires if the selection has changed.
    // Also ignores the first selection, which is set programatically.
    protected static abstract class MyItemSelectedListener implements AdapterView.OnItemSelectedListener {
        FixedAllowanceRow row;
        int lastPos = -1;
        
        public MyItemSelectedListener(FixedAllowanceRow row) {
            this.row = row;
        }
        
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (lastPos != -1 && lastPos != pos) {
                myOnItemSelected(parent, view, pos, id);
            }
            lastPos = pos;
        }

        public abstract void myOnItemSelected(AdapterView<?> parent, View view, int pos, long id);

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
    
    protected class GetFixedAllowancesListener extends AsyncReplyAdapter {
    	public GetFixedAllowancesListener() {
    		super(TAExpensesActivity.this);
    	}
    	
        @Override
        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                ConcurCore core = (ConcurCore) getApplication();
                fixedAllowances = core.getFixedAllowances();
                Log.i(Const.LOG_TAG, "fetched FixedAllowances: " + fixedAllowances);
                populateList();
            }
        }
        
		@Override
		public void onRequestFail(Bundle resultData) {
			super.onRequestFail(resultData);
			finish();
		}
        
    	@Override
        public void cleanup() {
    		dismissDialog(DIALOG_FETCHING_ALLOWANCES);
        }
    }
    
    protected class GetUpdatedFixedAllowanceAmountsListener extends AsyncReplyAdapter {
    	public GetUpdatedFixedAllowanceAmountsListener() {
    		super(TAExpensesActivity.this);
    	}
    	
        @Override
        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                FixedAllowanceAmounts amounts = (FixedAllowanceAmounts)resultData.getSerializable(GetUpdatedFixedAllowanceAmounts.KEY);
                updateFixedAllowanceAmounts(amounts);
            }
        }

        @Override
        public void cleanup() {
    		dismissDialog(DIALOG_UPDATING_AMOUNTS);
        }
    }
    
    protected class UpdateFixedAllowancesListener extends AsyncReplyAdapter {
    	public UpdateFixedAllowancesListener() {
    		super(TAExpensesActivity.this);
    	}
    	
        @Override
        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                setResult(RESULT_OK);
                finish();
            }
        }
        
    	@Override
        public void cleanup() {
    		dismissDialog(DIALOG_UPDATING_EXPENSES);
        }
    }
}
