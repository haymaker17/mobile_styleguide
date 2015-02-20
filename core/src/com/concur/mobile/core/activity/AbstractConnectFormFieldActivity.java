package com.concur.mobile.core.activity;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.concur.core.R;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.request.dto.FormDTO;
import com.concur.mobile.platform.ui.common.view.MoneyFormField;

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

    private static final String CLS_TAG = "AbstractConnectForm...";

    protected class CustomDatePickerDialog extends DatePickerDialog {

        private View v = null;

        public CustomDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear,
                int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
        }

        public CustomDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear,
                int dayOfMonth) {
            super(context, theme, callBack, year, monthOfYear, dayOfMonth);
        }

        public void setClickedView(View v) {
            this.v = v;
        }

        public View getClickedView() {
            return v;
        }
    }

    protected class CustomTimePickerDialog extends TimePickerDialog {

        private View v = null;

        public CustomTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute,
                boolean is24HourView) {
            super(context, callBack, hourOfDay, minute, is24HourView);
        }

        public CustomTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute,
                boolean is24HourView) {
            super(context, theme, callBack, hourOfDay, minute, is24HourView);
        }

        public void setClickedView(View v) {
            this.v = v;
        }

        public View getClickedView() {
            return v;
        }
    }

    private Map<String, CustomDatePickerDialog> dateFieldMapping = new HashMap<String, CustomDatePickerDialog>();
    private Map<String, CustomTimePickerDialog> timeFieldMapping = new HashMap<String, CustomTimePickerDialog>();

    private Map<String, LinearLayout> titleLayoutFieldsMapping = null;

    /* Arbitrarily set to 100 to avoid any possible conflict.
     * TODO : migrate to View.generateViewId() calls when we'll move to minSdk=17.
     * Cf http://stackoverflow.com/a/15442898
     */
    private int viewID = 100;

    private boolean canSave = true;

    protected static enum DisplayType {
        TEXTFIELD,       //RequestID, Name, Status
        TEXTAREA,        //Purpose, Comment
        DATEFIELD,       //startDate, endDate
        MONEYFIELD,      //totalAmount + currency,
        TIME,            //startTime, endTime
        PICKLIST         //currency (selection)
    }

    /**
     * @param fieldName
     * @return the value contained in the Model object for the given field name as a String
     */
    protected abstract String getModelDisplayedValueByFieldName(FormDTO model, String fieldName);

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
    protected abstract DateUtil.DatePattern getDatePattern();

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
    public abstract void applySaveButtonPolicy(final View saveButtonView);

    /**
     * This has to be overriden with a super call to execute your own remote saving.
     * This will only call the save action on the given layout if it's found.
     *
     * @param form
     * @param model
     */
    protected void save(ConnectForm form, final FormDTO model) {
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
        final TextView tv = getComponent(model, fieldName);
        if (tv != null && tv.getText() != null) {
            return tv.getText().toString();
        }
        return null;
    }

    /**
     * Returned value should only change on multi-tabbed views (ex ViewPager with fragments)
     *
     * @return the layout currently displayed
     */
    protected abstract LinearLayout getCurrentFieldsLayout();

    public void setDisplayFields(FormDTO model, ConnectForm form, final LinearLayout fieldLayout) {
        setDisplayFields(model, form, fieldLayout, null, null);
    }

    /**
     * This one is used by fragments within activities. Must be overriden in the activity  which should then do a call
     * to another initializeFragmentDisplay method with the rights arguments.
     *
     * @param fieldLayout
     * @param fragmentId
     */
    public void initializeFragmentDisplay(final LinearLayout fieldLayout, int fragmentId) {
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
    protected void setDisplayFields(final FormDTO model, final ConnectForm form, final LinearLayout fieldLayout,
            final LinearLayout titleLayout, final String fieldToTitle) {
        if (titleLayout != null && fieldToTitle != null) {
            titleLayoutFieldsMapping = new HashMap<String, LinearLayout>();
            titleLayoutFieldsMapping.put(fieldToTitle, titleLayout);
        }
        // --- creates a mapping of the given layouts to generate a unique id for each component
        if (form != null) {
            final List<ConnectFormField> formfields = form.getFormFields();
            applySpecificSort(formfields);

            LinearLayout.LayoutParams llp = null;

            final LinearLayout segmentLayout = new LinearLayout(this);
            segmentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            segmentLayout.setOrientation(LinearLayout.VERTICAL);
            fieldLayout.addView(segmentLayout);

            for (final ConnectFormField ff : formfields) {

                final boolean isTitleField = fieldToTitle != null && ff.getName().equals(fieldToTitle);
                final ViewGroup layout = isTitleField ? titleLayout : segmentLayout;

                IFormField.DataType dataType = ff.getDataType();
                IFormField.ControlType controlType = ff.getControlType();
                IFormField.AccessType accesType = ff.getAccessType();

                TextView component = null;

                // --- WS OUTPUT FIX (Time fields)
                // note : this is due to the fact that WS can't retrieve the correct value within midtier so it returns null
                if (dataType == IFormField.DataType.CHAR && controlType == IFormField.ControlType.TIME) {
                    // time values
                    accesType = canSave ? IFormField.AccessType.RW : IFormField.AccessType.RO;
                } else if (dataType == IFormField.DataType.MONEY && controlType == IFormField.ControlType.EDIT) {
                    // amount
                    accesType = canSave ? IFormField.AccessType.RW : IFormField.AccessType.RO;
                } else if (dataType == IFormField.DataType.VARCHAR && controlType == IFormField.ControlType.TEXT_AREA) {
                    // comment
                    accesType = canSave ? IFormField.AccessType.RW : IFormField.AccessType.RO;
                } else if (dataType == IFormField.DataType.INTEGER && controlType == IFormField.ControlType.PICK_LIST) {
                    // currency
                    accesType = canSave ? IFormField.AccessType.RW : IFormField.AccessType.RO;
                }

                final DisplayType displayType = getDisplayType(ff);

                if (displayType != null && isFieldVisible(model, ff.getName())) {
                    final boolean isEditable = canSave && accesType != IFormField.AccessType.RO;

                    llp = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT);

                    switch (displayType) {
                    case TEXTFIELD:

                        if (!isEditable) {
                            component = new TextView(this);
                        } else {
                            component = new EditText(this);
                        }

                        component.setText(getModelDisplayedValueByFieldName(model, ff.getName()));
                        component.setMaxLines(1);
                        component.setSingleLine(true);
                        component.setEllipsize(TextUtils.TruncateAt.END); //ellipses
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        break;

                    case TEXTAREA:

                        if (!isEditable) {
                            component = new TextView(this);
                        } else {
                            component = new EditText(this);
                        }

                        component.setText(getModelDisplayedValueByFieldName(model, ff.getName()));
                        component.setLines(5);
                        component.setMaxLines(5);
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        break;

                    case DATEFIELD:

                        component = new TextView(this);

                        component.setText(getModelDisplayedValueByFieldName(model, ff.getName()));
                        component.setInputType(InputType.TYPE_NULL);
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        //ADD LISTENER for setting date
                        if (isEditable) {
                            component.setOnClickListener((View.OnClickListener) this);
                            final Calendar newCalendar = Calendar.getInstance();

                            final CustomDatePickerDialog.OnDateSetListener inDateListener = new CustomDatePickerDialog.OnDateSetListener() {

                                // --- we store the dialog's tag
                                private String fieldName = ff.getName();

                                public void onDateSet(DatePicker datePicker, int year, int monthOfYear,
                                        int dayOfMonth) {
                                    Calendar newDate = Calendar.getInstance();
                                    newDate.set(year, monthOfYear, dayOfMonth);
                                    ((TextView) dateFieldMapping.get(fieldName).getClickedView())
                                            .setText(formatDate(newDate.getTime()));
                                }
                            };

                            final CustomDatePickerDialog fromDatePickerDialog = new CustomDatePickerDialog(this,
                                    inDateListener, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH),
                                    newCalendar.get(Calendar.DAY_OF_MONTH));

                            dateFieldMapping.put(ff.getName(), fromDatePickerDialog);
                        }

                        break;

                    case TIME:
                        component = new TextView(this);

                        component.setText(getModelDisplayedValueByFieldName(model, ff.getName()));
                        component.setInputType(InputType.TYPE_NULL);
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        //ADD LISTENER to set time
                        if (isEditable) {
                            component.setOnClickListener((View.OnClickListener) this);
                            final Calendar newCalendar = Calendar.getInstance();

                            final CustomTimePickerDialog.OnTimeSetListener inTimeListener = new CustomTimePickerDialog.OnTimeSetListener() {

                                // --- we store the dialog's tag
                                private String fieldName = ff.getName();

                                @Override public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                                    final Calendar newDate = Calendar.getInstance();
                                    newDate.set(Calendar.HOUR, hours);
                                    newDate.set(Calendar.MINUTE, minutes);
                                    ((TextView) timeFieldMapping.get(fieldName).getClickedView())
                                            .setText(formatTime(newDate.getTime()));
                                }
                            };

                            final CustomTimePickerDialog fromTimePickerDialog = new CustomTimePickerDialog(this,
                                    inTimeListener, newCalendar.get(Calendar.HOUR), newCalendar.get(Calendar.MINUTE),
                                    android.text.format.DateFormat.is24HourFormat(this));

                            timeFieldMapping.put(ff.getName(), fromTimePickerDialog);
                        }

                        break;

                    case MONEYFIELD:
                        // --- we do not display currency symbol if the field is editable (TODO TBC)
                        if (!isEditable) {
                            component = new TextView(this);
                            final String currencyName = getModelDisplayedValueByFieldName(model, "CurrencyName");
                            component.setText((currencyName != null ? currencyName + " " : "")
                                    + getModelDisplayedValueByFieldName(model, ff.getName()));
                        } else {
                            component = new MoneyFormField(this, getLocale(), null);
                            component.setText(getModelDisplayedValueByFieldName(model, ff.getName()));
                        }

                        component.setMaxLines(1);
                        component.setSingleLine(true);
                        component.setEllipsize(TextUtils.TruncateAt.END); //ellipses
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        break;

                    case PICKLIST:

                        component = new TextView(this);

                        component.setText(getModelDisplayedValueByFieldName(model, ff.getName()));
                        component.setInputType(InputType.TYPE_NULL);
                        component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        //ADD LISTENER for setting date
                        if (isEditable) {
                            component.setOnClickListener((View.OnClickListener) this);
                        }

                        break;

                    default:
                        //displayType = null;
                        llp = null;
                        break;
                    }

                    if (component != null) {
                        if (!isTitleField) {
                            layout.addView(getTextViewFieldName(getLabelFromFieldName(ff.getName())));
                        }

                        component.setTag(ff.getName());

                        if (llp != null) {
                            if (!isEditable) {
                                llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                            }

                            // --- permits to apply specifics within the extending activity
                            applySpecificRender(model, component, llp, ff);

                            component.setLayoutParams(llp);
                            component.setGravity(Gravity.CENTER_VERTICAL);
                        }

                        layout.addView(component);
                        if (isEditable && displayType == DisplayType.PICKLIST) {
                            component.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                    getResources().getDrawable(R.drawable.field_popup), null);
                        }

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
     * @param ff formfield object
     * @return the type of display our component should be of
     */
    protected DisplayType getDisplayType(ConnectFormField ff) {

        DisplayType displayType = null;
        final IFormField.DataType dataType = ff.getDataType();
        final IFormField.ControlType controlType = ff.getControlType();

        switch (dataType) {
        case BOOLEAN:
            break;
        case CHAR:
            if (controlType == IFormField.ControlType.TIME) {
                displayType = DisplayType.TIME;
            }
            break;
        case CONNECTED_LIST:
            break;
        case CURRENCY:
            break;
        case EXPENSE_TYPE:
            break;
        case INTEGER:
            if (controlType == IFormField.ControlType.EDIT) {
                displayType = DisplayType.TEXTFIELD;
            } else if (controlType == IFormField.ControlType.PICK_LIST) {
                displayType = DisplayType.PICKLIST;
            }
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
        return displayType;
    }

    /**
     * Override this to change the type of display whatever access right rules are for a specific fields
     *
     * @param name
     * @return
     */
    protected boolean isFieldReadOnly(FormDTO model, String name) {
        return false;
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
     * Called on each formfield during the initializeFragmentDisplay() processing so that you can customize any component's
     * rendering
     *
     * @param component component
     * @param llp       layout params
     * @param ff        ConnectFormField element
     */
    protected abstract void applySpecificRender(final FormDTO model, final TextView component,
            final LinearLayout.LayoutParams llp, final ConnectFormField ff);

    /**
     * Format a date with the locale & pattern defined with overriden corresponding methods
     *
     * @param date
     * @return the date string
     */
    protected String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtil.getFormattedDateForLocale(getDatePattern(), getLocale(), date);
    }

    /**
     * Parse a date with the locale & pattern defined with overriden corresponding methods
     *
     * @param dateString
     * @return the date object
     */
    protected Date parseDate(String dateString) {
        return DateUtil.parseFormattedDateForLocale(getDatePattern(), getLocale(), dateString);
    }

    protected String convertTimeFormat(String timeValue, boolean isInput24, boolean isOutput24) {
        final Date d = DateUtil.parseFormattedTimeForFormat(timeValue, isInput24);
        if (d != null) {
            return DateUtil.getFormattedTimeForFormat(d, isOutput24);
        }
        return "";
    }

    protected void applyTimeString(Date d, String timeString) {
        if (d != null && timeString != null) {
            final boolean is24 = android.text.format.DateFormat.is24HourFormat(this);
            final DateFormat formatter = new SimpleDateFormat(
                    is24 ? DateUtil.TIME_PATTERN_24H : DateUtil.TIME_PATTERN_12H);
            final Calendar cal = Calendar.getInstance();
            try {
                final Date dt = formatter.parse(timeString);
                cal.setTime(dt);
                final int hour = cal.get(Calendar.HOUR);
                final int minute = cal.get(Calendar.MINUTE);
                final int second = cal.get(Calendar.SECOND);
                final int amPm = !is24 ? cal.get(Calendar.AM_PM) : -1;
                cal.setTime(d);
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, second);
                if (!is24) {
                    cal.set(Calendar.AM_PM, amPm);
                }
            } catch (ParseException e) {
                Log.e(CLS_TAG, "Failed to apply time string : " + timeString);
                // --- unparsable value for timeSring : we set 0 for each time field.
                cal.setTime(d);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                if (!is24) {
                    cal.set(Calendar.AM_PM, 0);
                }
            }
            d.setTime(cal.getTime().getTime());
        }
    }

    protected String formatTime(Date d) {
        if (d == null) {
            return "";
        }
        return formatTime(d, android.text.format.DateFormat.is24HourFormat(this));
    }

    protected String formatTime(Date d, boolean is24) {
        if (d == null) {
            return "";
        }
        final DateFormat formatter = new SimpleDateFormat(is24 ? DateUtil.TIME_PATTERN_24H : DateUtil.TIME_PATTERN_12H);
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
        //llp.setMargins(30, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
        textView.setLayoutParams(llp);

        return textView;
    }

    /**
     * Return the date picker matching the given dateView id
     *
     * @return
     */
    protected CustomDatePickerDialog getDateField(String viewTag) {
        return dateFieldMapping.get(viewTag);
    }

    /**
     * Return the time picker matching the given timeView id
     *
     * @return
     */
    protected CustomTimePickerDialog getTimeField(String viewTag) {
        return timeFieldMapping.get(viewTag);
    }

    protected TextView getComponent(FormDTO model, String fieldName) {
        // --- titleLayout's specific. As current version is, you CAN'T use both titleLayouts & multi-tabbed views
        //     at the same time.
        if (titleLayoutFieldsMapping != null && titleLayoutFieldsMapping.containsKey(fieldName)) {
            return (TextView) titleLayoutFieldsMapping.get(fieldName).findViewWithTag(fieldName);
        }
        final int segmentLayoutIdx = model.getDisplayOrder() != null ? model.getDisplayOrder() : 0;
        // --- get current segment layout
        final LinearLayout segmentLayout = (LinearLayout) getCurrentFieldsLayout().getChildAt(segmentLayoutIdx);
        if (segmentLayout != null) {
            return (TextView) segmentLayout.findViewWithTag(fieldName);
        }
        return null;
    }

    public void setCanSave(boolean canSave) {
        this.canSave = canSave;
    }

    public boolean canSave() {
        return canSave;
    }
}
