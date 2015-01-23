package com.concur.mobile.core.activity;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.IFormField;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OlivierB on 13/01/2015.
 *
 * An abstract class to extend on activities which uses Connect Forms & Fields
 * Extracted from RequestHeaderActivity.
 */
public abstract class AbstractConnectFormFieldActivity extends BaseActivity {

    private static enum DisplayType {
        TEXTFIELD,		//RequestID, Name, Status
        TEXTAREA,		//Purpose, Comment
        DATEFIELD,		//startDate, endate
        MONEYFIELD		//totalAmount&currency
    }

    private Map<Integer, String> views = new HashMap<Integer, String>(); // view ID, filed NAME

    private List<Integer> dateViews = new ArrayList<Integer>();
    private List<DatePickerDialog> datePickerDialogs = new ArrayList<DatePickerDialog>();
    private int viewID = 0;

    protected ConnectForm form;
    protected SimpleDateFormat dateFormatter;

    protected boolean isEditable = false;

    protected abstract String getValueFromFieldName(String fieldName);

    protected abstract void setValueFromFieldName(String fieldName, String value);

    protected void setDisplayFields(final LinearLayout requestHeaderFieldLayout){

        List<ConnectFormField> formfields = form.getFormFields();
        Collections.sort(formfields);

        LinearLayout.LayoutParams llp;

        for(ConnectFormField ff : formfields){

            IFormField.DataType dataType = ff.getDataType();
            IFormField.ControlType controlType = ff.getControlType();
            IFormField.AccessType accesType = ff.getAccessType();


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
                        if(!this.isEditable || accesType == IFormField.AccessType.RO)
                            textField = new TextView(this);
                        else
                            textField = new EditText(this);

                        requestHeaderFieldLayout.addView(getTextView_FieldName(getLabelFromFieldName(ff.getName())));
                        textField.setText(getValueFromFieldName(ff.getName()));
                        textField.setMaxLines(1);
                        textField.setSingleLine(true); textField.setEllipsize(TextUtils.TruncateAt.END); //ellipses
                        textField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        requestHeaderFieldLayout.addView(textField);

                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
                        if(!this.isEditable || accesType == IFormField.AccessType.RO) llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                        textField.setLayoutParams(llp);

                        textField.setId(viewID);
                        views.put(viewID, ff.getName()); viewID++;

                        addWhiteSpace(requestHeaderFieldLayout);
                        if(ff.isLineSeparator()) addSeparator(requestHeaderFieldLayout);

                        break;

                    case TEXTAREA:

                        TextView textArea;
                        if(!this.isEditable || accesType == IFormField.AccessType.RO)
                            textArea = new TextView(this);
                        else
                            textArea = new EditText(this);

                        requestHeaderFieldLayout.addView(getTextView_FieldName(getLabelFromFieldName(ff.getName())));

                        textArea.setText(getValueFromFieldName(ff.getName()));
                        textArea.setLines(5);
                        textArea.setMaxLines(5);
                        textArea.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        requestHeaderFieldLayout.addView(textArea);

                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
                        if(!this.isEditable || accesType == IFormField.AccessType.RO) llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                        textArea.setLayoutParams(llp);

                        textArea.setId(viewID);
                        views.put(viewID, ff.getName()); viewID++;

                        addWhiteSpace(requestHeaderFieldLayout);
                        if(ff.isLineSeparator()) addSeparator(requestHeaderFieldLayout);

                        break;

                    case DATEFIELD:

                        final TextView
                                fromDateEtxt = new TextView(this);

                        requestHeaderFieldLayout.addView(getTextView_FieldName(getLabelFromFieldName(ff.getName())));

                        fromDateEtxt.setText(getValueFromFieldName(ff.getName()));
                        fromDateEtxt.setInputType(InputType.TYPE_NULL);
                        fromDateEtxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        fromDateEtxt.requestFocus();

                        //ADD LISTENER for setting date
                        if(!(!this.isEditable || accesType == IFormField.AccessType.RO)){
                            fromDateEtxt.setOnClickListener((View.OnClickListener) this);
                            Calendar newCalendar = Calendar.getInstance();

                            DatePickerDialog.OnDateSetListener inDateListener = new DatePickerDialog.OnDateSetListener(){

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
                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
                        llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                        fromDateEtxt.setLayoutParams(llp);

                        views.put(viewID, ff.getName()); viewID++;

                        requestHeaderFieldLayout.addView(fromDateEtxt);

                        addWhiteSpace(requestHeaderFieldLayout);
                        if(ff.isLineSeparator()) addSeparator(requestHeaderFieldLayout);

                        break;

                    case MONEYFIELD:

                        requestHeaderFieldLayout.addView(getTextView_FieldName(getLabelFromFieldName(ff.getName())));

                        TextView moneyField = new TextView(this);
                        moneyField.setText(getValueFromFieldName("CurrencyName") + " " + getValueFromFieldName(ff.getName()));
                        moneyField.setMaxLines(1);
                        moneyField.setSingleLine(true); moneyField.setEllipsize(TextUtils.TruncateAt.END); //ellipses
                        moneyField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        requestHeaderFieldLayout.addView(moneyField);

                        moneyField.setId(viewID);
                        views.put(viewID, ff.getName()); viewID++;

                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
                        if(!this.isEditable || accesType == IFormField.AccessType.RO) llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                        moneyField.setLayoutParams(llp);

                        addWhiteSpace(requestHeaderFieldLayout);
                        if(ff.isLineSeparator()) addSeparator(requestHeaderFieldLayout);

                        break;

                    default:
                        //displayType = null;
                        break;
                }
        }
    }

    protected abstract String getLabelFromFieldName(String fieldName);

    private void addWhiteSpace(LinearLayout mainLayout){

        View view = new View(this);
        ColorDrawable backgroundColor = new ColorDrawable(this.getResources().getColor(R.color.White));
        view.setBackground(backgroundColor);

        int viewWidth = ActionBar.LayoutParams.MATCH_PARENT; //FILL_PARENT = MATCH_PARENT
        int viewHeight = 50;
        mainLayout.addView(view, viewWidth, viewHeight);
    }

    private void addSeparator(LinearLayout mainLayout){

        View view = new View(this);
        ColorDrawable backgroundColor = new ColorDrawable(this.getResources().getColor(R.color.ListDivider));
        view.setBackground(backgroundColor);

        int viewWidth = ActionBar.LayoutParams.MATCH_PARENT; //FILL_PARENT = MATCH_PARENT
        int viewHeight = 15;
        mainLayout.addView(view, viewWidth, viewHeight);
    }

    private TextView getTextView_FieldName(String text){

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setTextColor(Color.parseColor("#666666"));

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
        textView.setLayoutParams(llp);

        return textView;
    }

    public List getDateViews() {
        return dateViews;
    }

    public List getDatePickerDialogs() {
        return datePickerDialogs;
    }
}
