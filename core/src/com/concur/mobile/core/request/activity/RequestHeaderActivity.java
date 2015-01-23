package com.concur.mobile.core.request.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.util.Parse;

public class RequestHeaderActivity extends AbstractConnectFormFieldActivity implements OnClickListener  {

    private static final String CLS_TAG = RequestListActivity.class.getSimpleName();

	private RequestListCache requestListCache = null;
	private ConnectFormFieldsCache formFieldsCache = null;
	private RequestDTO tr;
	private Locale loc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.request_header);

		final ConcurCore concurCore = (ConcurCore) getApplication();

		requestListCache = (RequestListCache) concurCore.getRequestListCache();
		formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();

		final Bundle bundle = getIntent().getExtras();
		final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);

        this.isEditable = bundle.getString(RequestDigestActivity.REQUEST_IS_EDITABLE).equals("true")? true : false;

		if (requestId != null){
			tr = requestListCache.getValue(requestId);
			form = formFieldsCache.getFormFields(tr.getHeaderFormId());
		}
		else{
			Log.e(Const.LOG_TAG, CLS_TAG + " onCreate() : problem on tr retrieved, going back to list activity.");
			// TODO : throw exception & display toast message ? @See with PM
			finish();
		}

		loc = this.getResources().getConfiguration().locale;
		dateFormatter = new SimpleDateFormat("yyyy-mm-dd", loc);

		this.setUI();

	}

	private void setUI(){
		final TextView name = (TextView) findViewById(R.id.requestHeaderName);
		name.setText(tr.getName());
		name.setTypeface(Typeface.DEFAULT_BOLD);

		final LinearLayout requestHeaderFields = (LinearLayout) findViewById(R.id.requestHeaderFields);
		this.setDisplayFields(requestHeaderFields);
	}

	@Override
	public void onClick(View view) {
		for(int i = 0; i < getDateViews().size(); i++){
			if((Integer)getDateViews().get(i) == view.getId()){
				((DatePickerDialog) getDatePickerDialogs().get(i)).show();
			}
		}
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
	public void onBackPressed() {
		super.onBackPressed();
	}

    @Override
	protected String getLabelFromFieldName(String fieldName){

		if(fieldName.equals("Name")){
			return this.getResources().getString(R.string.tr_name);
		}
		else if(fieldName.equals("StartDate")){
			return this.getResources().getString(R.string.tr_start_date);
		}
		else if(fieldName.equals("EndDate")){
			return this.getResources().getString(R.string.tr_end_date);
		}
		else if(fieldName.equals("Purpose")){
			return this.getResources().getString(R.string.business_purpose);
		}
		else if(fieldName.equals("Comment")){
			return this.getResources().getString(R.string.comment);
		}
		else if(fieldName.equals("EmpName")){
			return this.getResources().getString(R.string.tr_employee_name);
		}
		else if(fieldName.equals("CurrencyName")){
			return this.getResources().getString(R.string.currency);
		}
		else if(fieldName.equals("TotalPostedAmount")){
			return this.getResources().getString(R.string.amount);
		}
		else return "";
	}

    @Override
	public String getValueFromFieldName(String fieldName){

		if(fieldName.equals("Name")){
			if(tr.getName() == null) return "";
			return tr.getName();
		}
		else if(fieldName.equals("StartDate")){
			if(tr.getStartDate() == null) return "";
			return dateFormatter.format(tr.getStartDate());
		}
		else if(fieldName.equals("EndDate")){
			if(tr.getStartDate() == null) return "";
			return dateFormatter.format(tr.getEndDate());
		}
		else if(fieldName.equals("Purpose")){
			if(tr.getPurpose() == null) return "";
			return tr.getPurpose();
		}
		else if(fieldName.equals("Comment")){
			if(tr.getLastComment() == null) return "";
			return tr.getLastComment();
		}
		else if(fieldName.equals("EmpName")){
			if(tr.getEmployeeName() == null) return "";
			return tr.getEmployeeName();
		}
		else if(fieldName.equals("CurrencyName")){
			if(tr.getCurrency() == null) return "";
			return tr.getCurrency();
		}
		else if(fieldName.equals("TotalPostedAmount"))
			return String.valueOf(tr.getTotal());
		else return "";
	}

    @Override
    protected void setValueFromFieldName(String fieldName, String value){
		if(fieldName.equals("Name"))
			tr.setName(value);
		else if(fieldName.equals("StartDate"))
			tr.setStartDate(parseDate(value, dateFormatter));
		else if(fieldName.equals("EndDate"))
			tr.setEndDate(parseDate(value, dateFormatter));
		else if(fieldName.equals("Purpose"))
			tr.setPurpose(value);
		else if(fieldName.equals("Comment"))
			tr.setLastComment(value);
		else if(fieldName.equals("EmpName"))
			tr.setEmployeeName(value);
		else if(fieldName.equals("CurrencyName"))
			tr.setCurrency(value);
		else if(fieldName.equals("TotalPostedAmount"))
			tr.setTotal(Parse.safeParseDouble(value));
	}

	private Date parseDate(String baseStr, SimpleDateFormat sdf) {
		if (baseStr != null) {
			try {
				return sdf.parse(baseStr);
			} catch (ParseException e) {
				// do nothing - either data is corrupted or format isn't
				// recognized, which is quite the same.
			}
		}
		return null;
	}
}
