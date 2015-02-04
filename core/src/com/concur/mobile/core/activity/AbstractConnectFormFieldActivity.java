package com.concur.mobile.core.activity;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.concur.core.R;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.request.dto.FormDTO;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by OlivierB on 13/01/2015.
 * <p/>
 * An abstract class to extend on activities which uses Connect Forms & Fields
 * Extracted from RequestHeaderActivity.
 */
public abstract class AbstractConnectFormFieldActivity extends BaseActivity {

    private static final String CLS_TAG = "AbstractConnectFormFieldActivity";

    private static enum DisplayType {
        TEXTFIELD,        //RequestID, Name, Status
        TEXTAREA,        //Purpose, Comment
        DATEFIELD,        //startDate, endate
        MONEYFIELD        //totalAmount&currency
    }

    private Map<String, Integer> views = new HashMap<String, Integer>(); // view ID, filed NAME

    private List<Integer> dateViews = new ArrayList<Integer>();
    private List<DatePickerDialog> datePickerDialogs = new ArrayList<DatePickerDialog>();
    /* Arbitrarily set to 100 to avoid any possible conflict.
     * TODO : migrate to View.generateViewId() calls when we'll move to minSdk=17.
     * Cf http://stackoverflow.com/a/15442898
     */
    private int viewID = 100;

    protected boolean isEditable = false;

    /**
     * @param fieldName
     * @return the value contained in the Model object for the given field name as a String
     */
    protected abstract String getModelValueByFieldName(FormDTO model, String fieldName);

    /**
     * Set the value into the Model object for the given field name
     *
     * @param fieldName
     * @param value
     */
    protected abstract void setModelValueByFieldName(FormDTO model, String fieldName, String value);

    /**
     * @return the locale in use for values processing such as dates
     */
    protected abstract Locale getLocale();

    /**
     * @return the pattern in use for date values processing
     */
    protected abstract DateUtil.DatePattern getPattern();

    /**
     * @param fieldName
     * @return the label to display for the corresponding ConnectFormField's name
     */
    protected abstract String getLabelFromFieldName(String fieldName);

    /**
     * Apply a policy to apply the save button display & handling if there is any
     * This is made to be accessible from a fragment in case you'd have some and your button would be at
     * the end of a scrollable view making it dependent of each view
     * You may not use this at all if you have no use of it.
     *
     * @param saveButtonView
     */
    public abstract void applySaveButtonPolicy(View saveButtonView);

    /**
     * This has to be overriden with a super call to execute your own remote saving.
     */
    protected void save(ConnectForm form, FormDTO model) {
        if (form != null) {
            final List<ConnectFormField> formFields = form.getFormFields();
            Collections.sort(formFields);

            for (ConnectFormField ff : formFields) {
                setModelValueByFieldName(model, ff.getName(), getValueByFieldName(model, ff.getName()));
            }
        }
    }

    /**
     * @param fieldName
     * @return the value currently displayed & contained in the View object
     */
    private String getValueByFieldName(FormDTO model, String fieldName) {
        if (views.containsKey(fieldName)) {
            final TextView tv = (TextView) findViewById(views.get(getVIewId(model, fieldName)));
            if (tv != null && tv.getText() != null) {
                return tv.getText().toString();
            }
        }
        return null;
    }

    private String getVIewId(FormDTO model, String fieldName) {
        return model.getId() + "_" + fieldName;
    }

    public void setDisplayFields(FormDTO model, ConnectForm form, final LinearLayout fieldLayout) {
        setDisplayFields(model, form, fieldLayout, null, null);
    }

    /**
     * This one is used by fragments within activities. Must be overriden in the activity  which should then do a call
     * to another setDisplayFields method with the rights arguments.
     *
     * @param fieldLayout
     * @param fragmentId
     */
    public void setDisplayFields(final LinearLayout fieldLayout, int fragmentId) {
        // --- Override on needs
    }

    /**
     * Handle fields generation within the given layouts. fieldToTitle define witch field will be displayed
     * in the titleLayout
     *
     * @param titleLayout  layout of the header
     * @param fieldLayout  layout of the content
     * @param fieldToTitle the ConnectFormField name to display within the titleLayout
     */
    protected void setDisplayFields(FormDTO model, ConnectForm form, final LinearLayout fieldLayout,
            final LinearLayout titleLayout, final String fieldToTitle) {
        if (form != null) {
            final List<ConnectFormField> formfields = form.getFormFields();
            applySpecificSort(formfields);

            LinearLayout.LayoutParams llp = null;

            for (ConnectFormField ff : formfields) {

                final boolean isTitleField = fieldToTitle != null && ff.getName().equals(fieldToTitle);
                final ViewGroup layout = isTitleField ? titleLayout : fieldLayout;

                final IFormField.DataType dataType = ff.getDataType();
                final IFormField.ControlType controlType = ff.getControlType();
                final IFormField.AccessType accesType = ff.getAccessType();

                DisplayType displayType = null;
                TextView component = null;

                switch (dataType) {
                case BOOLEAN:
                    break;
                case CHAR: // Time
                    if (controlType == IFormField.ControlType.TIME) {
                        displayType = DisplayType.TEXTFIELD;
                    }
                    break;
                case CONNECTED_LIST:
                    break;
                case CURRENCY:
                    break;
                case EXPENSE_TYPE:
                    break;
                case INTEGER: // List IDs
                    displayType = DisplayType.TEXTFIELD;
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
                    switch (controlType) {
                    case EDIT:
                        displayType = DisplayType.TEXTFIELD;
                        break;
                    case TEXT_AREA:
                        displayType = DisplayType.TEXTAREA;
                        break;
                    default:
                        break;
                    }
                    break;
                default:
                    displayType = null;
                    break;
                }

                if (displayType != null && isFieldVisible(model, ff.getName())) {
                    switch (displayType) {
                    case TEXTFIELD:

                        if (!this.isEditable || accesType == IFormField.AccessType.RO) {
                            component = new TextView(this);
                        } else {
                            component = new EditText(this);
                        }

                        component.setText(getModelValueByFieldName(model, ff.getName()));
                        component.setMaxLines(1);
                        component.setSingleLine(true);
                        component.setEllipsize(TextUtils.TruncateAt.END); //ellipses
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                                ActionBar.LayoutParams.MATCH_PARENT);
                        break;

                    case TEXTAREA:

                        if (!this.isEditable || accesType == IFormField.AccessType.RO) {
                            component = new TextView(this);
                        } else {
                            component = new EditText(this);
                        }

                        component.setText(getModelValueByFieldName(model, ff.getName()));
                        component.setLines(5);
                        component.setMaxLines(5);
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                                ActionBar.LayoutParams.MATCH_PARENT);
                        break;

                    case DATEFIELD:

                        component = new TextView(this);

                        component.setText(getModelValueByFieldName(model, ff.getName()));
                        component.setInputType(InputType.TYPE_NULL);
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        component.requestFocus();

                        //ADD LISTENER for setting date
                        if (!(!this.isEditable || accesType == IFormField.AccessType.RO)) {
                            component.setOnClickListener((View.OnClickListener) this);
                            Calendar newCalendar = Calendar.getInstance();
                            final TextView finalComp = component;

                            final DatePickerDialog.OnDateSetListener inDateListener = new DatePickerDialog.OnDateSetListener() {

                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    Calendar newDate = Calendar.getInstance();
                                    newDate.set(year, monthOfYear, dayOfMonth);
                                    finalComp.setText(formatDate(newDate.getTime()));
                                }
                            };

                            final DatePickerDialog fromDatePickerDialog = new DatePickerDialog(this, inDateListener,
                                    newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH),
                                    newCalendar.get(Calendar.DAY_OF_MONTH));

                            dateViews.add(viewID);
                            datePickerDialogs.add(fromDatePickerDialog);
                        }

                        llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                                ActionBar.LayoutParams.MATCH_PARENT);
                        break;

                    case MONEYFIELD:
                        component = new TextView(this);
                        // TODO : rework this
                        final String currencyName = getModelValueByFieldName(model, "CurrencyName");
                        component.setText(
                                (currencyName != null ? currencyName + " " : "") + getModelValueByFieldName(model,
                                        ff.getName()));
                        component.setMaxLines(1);
                        component.setSingleLine(true);
                        component.setEllipsize(TextUtils.TruncateAt.END); //ellipses
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        break;

                    default:
                        //displayType = null;
                        break;
                    }

                    if (component != null) {
                        if (!isTitleField) {
                            layout.addView(getTextViewFieldName(getLabelFromFieldName(ff.getName())));
                        }

                        component.setId(viewID);
                        views.put(ff.getName(), viewID);
                        viewID++;

                        // --- permits to apply specifics within the extending activity
                        applySpecificRender(component, llp, ff);

                        if (llp != null) {
                            if (!this.isEditable || accesType == IFormField.AccessType.RO) {
                                llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                            }
                            component.setLayoutParams(llp);
                        }

                        layout.addView(component);

                        addWhiteSpace(layout);
                        if (ff.isLineSeparator()) {
                            addSeparator(layout);
                        }
                    }
                }
            }
        }
    }

    /**
     * Override this to apply custom visibility on specific fields
     *
     * @param name
     * @return
     */
    protected boolean isFieldVisible(FormDTO model, String name) {
        return true;
    }

    /**
     * Override this if you need a specific display order. You have to update the sequence number of each field for it.
     *
     * @param formfields
     */
    private void applySpecificSort(List<ConnectFormField> formfields) {
        Collections.sort(formfields);
    }

    /**
     * Called on each formfield during the setDisplayFields() processing so that you can customize any component's
     * rendering
     *
     * @param component component
     * @param llp       layout params
     * @param ff        ConnectFormField element
     */
    protected abstract void applySpecificRender(final TextView component, final LinearLayout.LayoutParams llp,
            final ConnectFormField ff);

    /**
     * Format a date with the locale & pattern defined with overriden corresponding methods
     *
     * @param date
     * @return the date string
     */
    protected String formatDate(Date date) {
        return DateUtil.getFormattedDateForLocale(getPattern(), getLocale(), date);
    }

    /**
     * Parse a date with the locale & pattern defined with overriden corresponding methods
     *
     * @param dateString
     * @return the date object
     */
    protected Date parseDate(String dateString) {
        return DateUtil.parseFormattedDateForLocale(getPattern(), getLocale(), dateString);
    }

    protected void applyTimeString(Date d, String timeString) {
        final DateFormat formatter = new SimpleDateFormat(DateUtil.TIME_PATTERN);
        try {
            final Date dt = formatter.parse(timeString);
            final Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            final int hour = cal.get(Calendar.HOUR);
            final int minute = cal.get(Calendar.MINUTE);
            final int second = cal.get(Calendar.SECOND);
            cal.setTime(d);
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            d.setTime(cal.getTime().getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(CLS_TAG, "Failed to apply time string : " + timeString);
        }
    }

    protected String formatTime(Date d) {
        final DateFormat formatter = new SimpleDateFormat(DateUtil.TIME_PATTERN);
        return formatter.format(d);
    }

    private void addWhiteSpace(ViewGroup mainLayout) {

        View view = new View(this);
        ColorDrawable backgroundColor = new ColorDrawable(this.getResources().getColor(R.color.White));
        view.setBackground(backgroundColor);

        int viewWidth = ActionBar.LayoutParams.MATCH_PARENT; //FILL_PARENT = MATCH_PARENT
        int viewHeight = 50;
        mainLayout.addView(view, viewWidth, viewHeight);
    }

    private void addSeparator(ViewGroup mainLayout) {

        View view = new View(this);
        ColorDrawable backgroundColor = new ColorDrawable(this.getResources().getColor(R.color.ListDivider));
        view.setBackground(backgroundColor);

        int viewWidth = ActionBar.LayoutParams.MATCH_PARENT; //FILL_PARENT = MATCH_PARENT
        int viewHeight = 15;
        mainLayout.addView(view, viewWidth, viewHeight);
    }

    private TextView getTextViewFieldName(String text) {

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setTextColor(Color.parseColor("#666666"));

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
        textView.setLayoutParams(llp);

        return textView;
    }

    /**
     * TODO by ECO
     *
     * @return
     */
    public List getDateViews() {
        return dateViews;
    }

    /**
     * TODO by ECO
     *
     * @return
     */
    public List getDatePickerDialogs() {
        return datePickerDialogs;
    }
}
