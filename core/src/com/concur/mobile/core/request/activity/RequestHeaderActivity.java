package com.concur.mobile.core.request.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.ActionBar.LayoutParams;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.request.activity.RequestListActivity.ListAdapterRowClickListener;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.common.formfield.IFormField.AccessType;
import com.concur.mobile.platform.common.formfield.IFormField.ControlType;
import com.concur.mobile.platform.common.formfield.IFormField.DataType;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.util.Parse;

public class RequestHeaderActivity extends BaseActivity implements OnClickListener  {

	private RequestListCache requestListCache = null;
	private ConnectFormFieldsCache formFieldsCache = null;

	private RequestDTO tr;
	private ConnectForm form;

	private static final String CLS_TAG = RequestListActivity.class.getSimpleName();

	private SimpleDateFormat dateFormatter;
	private Locale loc;

	private DatePickerDialog fromDatePickerDialog;

	private Map<Integer, String> views = new HashMap(); // view ID, filed NAME
	private int viewID = 0;

	private List dateViews = new ArrayList<Integer>();
	private List datePickerDialogs = new ArrayList<DatePickerDialog>();

	private static enum DisplayType {
		TEXTFIELD,		//RequestID, Name, Status
		TEXTAREA,		//Purpose, Comment
		DATEFIELD,		//startDate, endate
		MONEYFIELD;		//totalAmount&currency
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.request_header);

		final ConcurCore concurCore = (ConcurCore) getApplication();

		requestListCache = (RequestListCache) concurCore.getRequestListCache();
		formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();

		final Bundle bundle = getIntent().getExtras();
		final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);

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


	private void setDisplayFields(LinearLayout requestHeaderFields){

		LinearLayout ffLayout = requestHeaderFields;

		List<ConnectFormField> formfields = form.getFormFields();    	
		Collections.sort(formfields);
		
		LinearLayout.LayoutParams llp;

		for(ConnectFormField ff : formfields){

			DataType dataType = ff.getDataType();
			ControlType controlType = ff.getControlType();
			AccessType accesType = ff.getAccessType();


			DisplayType displayType = null;

			switch(dataType){
			case BOOLEAN:
				break;
			case CHAR:
				break;
			case CONNECTED_LIST:
				break;
			case CURRENCY:
				break;
			case EXPENSE_TYPE:
				break;
			case INTEGER:
				break;
			case LIST:
				break;
			case LOCATION:
				break;
			case MONEY:
				displayType = DisplayType.MONEYFIELD;
				break;
			case NUMERIC:
				break;
			case TIMESTAMP:
				displayType = DisplayType.DATEFIELD;
				break;
			case UNSPECIFED:
				break;
			case VARCHAR:
				switch(controlType){
				case EDIT:
					displayType = DisplayType.TEXTFIELD;
					break;
				case TEXT_AREA:
					displayType = DisplayType.TEXTAREA;
					break;
				default :
					break;
				}
				break;
			default:
				displayType = null;
				break;
			}


			if(displayType != null)

				switch(displayType){
				case TEXTFIELD:

					TextView textField;
					if(accesType == accesType.RO)
						textField = new TextView(this);
					else
						textField = new EditText(this);

					ffLayout.addView(getTextView_FielddName(getLabelFromFieldName(ff.getName())));
					textField.setText(getValueFromFieldName(ff.getName()));
					textField.setMaxLines(1);
					textField.setSingleLine(true); textField.setEllipsize(TruncateAt.END); //ellipses
					textField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					ffLayout.addView(textField);

					llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					if(accesType == accesType.RO) llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
					textField.setLayoutParams(llp);
					
					textField.setId(viewID);
					views.put(viewID, ff.getName()); viewID++;

					addWhiteSpace(ffLayout);
					if(ff.isLineSeparator()) addSeparator(ffLayout);

					break;

				case TEXTAREA:

					TextView textArea;
					if(accesType == accesType.RO)
						textArea = new TextView(this);
					else
						textArea = new EditText(this);

					ffLayout.addView(getTextView_FielddName(getLabelFromFieldName(ff.getName())));

					textArea.setText(getValueFromFieldName(ff.getName()));
					textArea.setLines(5);
					textArea.setMaxLines(5);
					textArea.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					ffLayout.addView(textArea);

					llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					if(accesType == accesType.RO) llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
					textArea.setLayoutParams(llp);
					
					textArea.setId(viewID);
					views.put(viewID, ff.getName()); viewID++;

					addWhiteSpace(ffLayout);
					if(ff.isLineSeparator()) addSeparator(ffLayout);

					break;

				case DATEFIELD:

					final TextView 
					fromDateEtxt = new TextView(this);

					ffLayout.addView(getTextView_FielddName(getLabelFromFieldName(ff.getName())));

					fromDateEtxt.setText(getValueFromFieldName(ff.getName()));
					fromDateEtxt.setInputType(InputType.TYPE_NULL);
					fromDateEtxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					fromDateEtxt.requestFocus();

					//ADD LISTENER for setting date
					if(!(accesType == accesType.RO)){
						fromDateEtxt.setOnClickListener((OnClickListener) this);
						Calendar newCalendar = Calendar.getInstance();

						OnDateSetListener inDateListener = new OnDateSetListener(){

							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								Calendar newDate = Calendar.getInstance();
								newDate.set(year, monthOfYear, dayOfMonth);
								fromDateEtxt.setText(dateFormatter.format(newDate.getTime()));
							}
						};

						DatePickerDialog fromDatePickerDialog = new DatePickerDialog(this, inDateListener, 
								newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

						dateViews.add(viewID);
						datePickerDialogs.add(fromDatePickerDialog);
					}

					fromDateEtxt.setId(viewID);
					llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
					fromDateEtxt.setLayoutParams(llp);

					views.put(viewID, ff.getName()); viewID++;

					ffLayout.addView(fromDateEtxt);

					addWhiteSpace(ffLayout);
					if(ff.isLineSeparator()) addSeparator(ffLayout);

					break;

				case MONEYFIELD:

					ffLayout.addView(getTextView_FielddName(getLabelFromFieldName(ff.getName())));

					TextView moneyField = new TextView(this);
					moneyField.setText(getValueFromFieldName("CurrencyName") + " " + getValueFromFieldName(ff.getName()));
					moneyField.setMaxLines(1);
					moneyField.setSingleLine(true); moneyField.setEllipsize(TruncateAt.END); //ellipses
					moneyField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					ffLayout.addView(moneyField);

					moneyField.setId(viewID);
					views.put(viewID, ff.getName()); viewID++;
					
					llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					if(accesType == accesType.RO) llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
					moneyField.setLayoutParams(llp);

					addWhiteSpace(ffLayout);
					if(ff.isLineSeparator()) addSeparator(ffLayout);

					break;

				default:
					displayType = null;
					break;
				}
		}
	}


	@Override
	public void onClick(View view) {

		for(int i=0; i<dateViews.size(); i++){
			if((Integer)dateViews.get(i) == view.getId()){
				((DatePickerDialog) datePickerDialogs.get(i)).show();
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

	public String getLabelFromFieldName(String fieldName){

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

	public String getValueFromFieldName(String fieldName){

		if(fieldName.equals("Name")){
			if(tr.getName() == null) return "";
			return tr.getName();
		}
		else if(fieldName.equals("StartDate")){
			if(tr.getStartDate() == null || tr.getStartDate().equals("")) return "";
			return dateFormatter.format(tr.getStartDate());
		}
		else if(fieldName.equals("EndDate")){
			if(tr.getStartDate() == null || tr.getEndDate().equals("")) return "";
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

	public void setValueFromFieldName(String fieldName, String value){

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


	private void addWhiteSpace(LinearLayout mainLayout){

		View view = new View(this);
		ColorDrawable backgroundColor = new ColorDrawable(this.getResources().getColor(R.color.White));
		view.setBackground(backgroundColor);

		int viewWidth = LayoutParams.MATCH_PARENT; //FILL_PARENT = MATCH_PARENT
		int viewHeight = 50;
		mainLayout.addView(view, viewWidth, viewHeight);
	}
	
	private void addSeparator(LinearLayout mainLayout){

		View view = new View(this);
		ColorDrawable backgroundColor = new ColorDrawable(this.getResources().getColor(R.color.ListDivider));
		view.setBackground(backgroundColor);

		int viewWidth = LayoutParams.MATCH_PARENT; //FILL_PARENT = MATCH_PARENT
		int viewHeight = 15;
		mainLayout.addView(view, viewWidth, viewHeight);
	}

	private TextView getTextView_FielddName(String text){

		TextView textView = new TextView(this);
		textView.setText(text);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
		textView.setTextColor(Color.parseColor("#666666"));

		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
		textView.setLayoutParams(llp);

		return textView;
	}



}
