package com.concur.mobile.core.expense.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.AsyncReplyAdapter;
import com.concur.mobile.core.expense.travelallowance.DeleteItineraryRowRequest;
import com.concur.mobile.core.expense.travelallowance.GetItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.GetTAConfigRequest;
import com.concur.mobile.core.expense.travelallowance.Itinerary;
import com.concur.mobile.core.expense.travelallowance.ItineraryRow;
import com.concur.mobile.core.expense.travelallowance.TaConfig;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;

public class TAItineraryActivity extends BaseActivity {

    String LOGTAG = TAItineraryActivity.class.getSimpleName();

    private String rptKey;
    private String rptName;
    private GetItinerariesRequest getTaItineraries;
    private BaseAsyncResultReceiver asyncReceiver = new BaseAsyncResultReceiver(new Handler());

    private Itinerary itinerary;

    private static final int DIALOG_FETCHING_ITINERARY = 0;
    ProgressDialog dialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ta_itin_activity);

        getSupportActionBar().setTitle(R.string.itinerary);
 
        ListView lv = (ListView) findViewById(R.id.itinStops);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItineraryRow row = itinerary.getItineraryRows().get(position);
                ConcurCore core = (ConcurCore) getApplication();
                core.setSelectedTAItineraryRow(row);

                Intent i = new Intent(TAItineraryActivity.this, TAStopActivity.class);
                startActivity(i);
            }
        });

        if (savedInstanceState != null) {
            Log.i(LOGTAG, "getting rptKey from savedInstanceState...");
            rptKey = savedInstanceState.getString(Const.EXTRA_EXPENSE_REPORT_KEY);
            rptName = savedInstanceState.getString(Const.EXTRA_EXPENSE_REPORT_NAME);
        } else if (getIntent().getExtras() != null) {
            Log.i(LOGTAG, "getting rptKey from extras...");
            rptKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_KEY);
            rptName = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_REPORT_NAME);
        } else {
            Log.i(LOGTAG, "no savedInstanceState or extras...");
            return;
        }

        fetchItinerary();
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
        inflater.inflate(R.menu.itinerary, menu);
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
        if (itemId == R.id.menuAdd) {
            addStop();
            return true;
        }
        if (itemId == R.id.menuProceed) {
            proceedToExpenses();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		setResult(resultCode, data);
    		finish();
    	}
    }
    
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_FETCHING_ITINERARY:
	        dialog = new ProgressDialog(this);
	        dialog.setMessage(getText(R.string.itin_fetching));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog;
		}
		return super.onCreateDialog(id, args);
	}

	private void fetchItinerary() {
        Log.i(LOGTAG, "rptKey = " + rptKey + "; fetching itineraries...");
        asyncReceiver.setListener(new GetTAItinerariesListener(false));
        getTaItineraries = new GetItinerariesRequest(getApplicationContext(), 1, asyncReceiver, rptKey);
        showDialog(DIALOG_FETCHING_ITINERARY);
        getTaItineraries.execute();
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
        fetchItinerary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, final View view, final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderTitle(R.string.itin_stop_actions);
        MenuItem menuItem = menu.add(0, 0, 0, R.string.itin_edit_stop);
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                ItineraryRow row = itinerary.getItineraryRows().get(info.position);
                ConcurCore core = (ConcurCore) getApplication();
                core.setSelectedTAItineraryRow(row);

                Intent i = new Intent(TAItineraryActivity.this, TAStopActivity.class);
                startActivity(i);

                return true;
            }
        });

        menuItem = menu.add(0, 1, 1, R.string.itin_delete_stop);
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                ItineraryRow row = itinerary.getItineraryRows().get(info.position);

                asyncReceiver.setListener(new DeleteItineraryRowListener());

                new DeleteItineraryRowRequest(getApplicationContext(), 1, asyncReceiver, itinerary.getItinKey(), row
                        .getIrKey()).execute();
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Const.EXTRA_EXPENSE_REPORT_KEY, rptKey);
        outState.putString(Const.EXTRA_EXPENSE_REPORT_NAME, rptName);
        super.onSaveInstanceState(outState);
    }

    public void addStop() {
        ItineraryRow row = new ItineraryRow();
        ConcurCore core = (ConcurCore) getApplication();
        core.setSelectedTAItineraryRow(row);

        Intent i = new Intent(TAItineraryActivity.this, TAStopActivity.class);
        startActivity(i);
    }

    public void proceedToExpenses() {
        Intent i = new Intent(TAItineraryActivity.this, TAExpensesActivity.class);
        i.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, rptKey);
        startActivityForResult(i, 0);
    }
    
    private void populateList() {
        List<ListItem> listItems = new ArrayList<ListItem>();
        int idx=1;
        for (ItineraryRow row : itinerary.getItineraryRows()) {
            listItems.add(new ItineraryStopListItem(row, idx++));
        }
        ListItemAdapter<ListItem> listItemAdapter = new ListItemAdapter<ListItem>(this, listItems, 1);
        ListView listView = (ListView)findViewById(R.id.itinStops);
        if (listView != null) {
            listView.setAdapter(listItemAdapter);
        } else {
            Log.e(Const.LOG_TAG, "no list view found!");
        }
        TextView v = (TextView)findViewById(R.id.itin_infotext);
        if (itinerary.getItineraryRows().size() > 0) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    protected class ItineraryStopListItem extends ListItem {
        ItineraryRow itineraryRow;
        int index;
        
        public ItineraryStopListItem(ItineraryRow itineraryRow, int idx) {
            this.itineraryRow = itineraryRow;
            this.index = idx;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public View buildView(final Context context, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.ta_stop_row, null);
            } else {
                view = convertView;
            }
            
            TextView v = (TextView)view.findViewById(R.id.stopNo);
            v.setText(Format.localizeText(context, R.string.itin_stop_n_of, new Object[] { index, itinerary.getItineraryRows().size() }));
            
            v = (TextView)view.findViewById(R.id.departLocation);
            v.setText(itineraryRow.getDepartLocation());       
            
            v = (TextView)view.findViewById(R.id.departDate);
            v.setText(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY.format(itineraryRow.getDepartDateTime()));

            v = (TextView)view.findViewById(R.id.arriveLocation);
            v.setText(itineraryRow.getArrivalLocation());

            v = (TextView)view.findViewById(R.id.arriveDate);
            v.setText(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY.format(itineraryRow.getArrivalDateTime()));

            v = (TextView)view.findViewById(R.id.arrivalRateLocation);
            v.setText(itineraryRow.getArrivalRateLocation());
            
            return view;
        }
    }
    

    protected class GetTAItinerariesListener extends AsyncReplyAdapter {
        boolean preserveRows = false;
        Itinerary prevItinerary;

        public GetTAItinerariesListener(boolean preserveRows) {
    		super(TAItineraryActivity.this);
            this.preserveRows = preserveRows;
            if (preserveRows) {
                prevItinerary = itinerary;
            }
        }

        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                ConcurCore core = (ConcurCore) getApplication();
                itinerary = core.getTAItinerary();
                itinerary.setRptKey(rptKey);
                if (itinerary.getItinKey() == null) {
                	// New Itinerary, let's set the name
                	itinerary.setName(Format.localizeText(TAItineraryActivity.this, R.string.itin_name_template, new Object[] { rptName }));
                	// and kick off a request for TAConfig, so we can save.
                    asyncReceiver.setListener(new GetTAConfigListener());
                    new GetTAConfigRequest(getApplicationContext(), 1, asyncReceiver).execute();
                    return;
                } else {
	                if (preserveRows && prevItinerary != null) {
	                    itinerary.setItineraryRows(prevItinerary.getItineraryRows());
	                }
	                Log.e(LOGTAG, "Got Itin response: " + itinerary);
	                populateList();
                }
            } 
        }
        
    	@Override
    	public void cleanup() {
        	dismissDialog(DIALOG_FETCHING_ITINERARY);
    	}

        
		@Override
		public void onRequestFail(Bundle resultData) {
			super.onRequestFail(resultData);
			finish();
		}
    }

    protected class GetTAConfigListener extends AsyncReplyAdapter {
    	public GetTAConfigListener() {
    		super(TAItineraryActivity.this);
    	}
    	
        @Override
        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                ConcurCore core = (ConcurCore) getApplication();
                TaConfig taConfig = core.getTAConfig();
                itinerary.setTacKey(taConfig.getTacKey());
            } 
        }

		@Override
		public void onRequestFail(Bundle resultData) {
			super.onRequestFail(resultData);
			finish();
		}
    }

    protected class DeleteItineraryRowListener extends AsyncReplyAdapter  {
    	public DeleteItineraryRowListener() {
    		super(TAItineraryActivity.this);
    	}

        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                String status = resultData.getString(DeleteItineraryRowRequest.STATUS);
                if (DeleteItineraryRowRequest.SUCCESS.equals(status)) {
                    // reload the itin
                    getTaItineraries = new GetItinerariesRequest(getApplicationContext(), 1, asyncReceiver, rptKey);
                    getTaItineraries.execute();
                } else {
                    Toast t = Toast.makeText(TAItineraryActivity.this, "Delete failed", Toast.LENGTH_SHORT);
                    t.show();
                }
            } 
        }
    }
}
