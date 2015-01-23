package com.concur.mobile.core.request.activity;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.platform.common.formfield.ConnectFormField;
import com.concur.mobile.platform.common.formfield.ConnectFormFieldsCache;
import com.concur.mobile.platform.request.RequestGroupConfigurationCache;
import com.concur.mobile.platform.request.RequestListCache;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.util.Parse;

import java.util.Locale;

public class RequestHeaderActivity extends AbstractConnectFormFieldActivity implements OnClickListener {

    private static final String CLS_TAG = RequestListActivity.class.getSimpleName();
    private static final String FIELD_NAME = "Name";
    private static final String FIELD_START_DATE = "StartDate";
    private static final String FIELD_END_DATE = "EndDate";
    private static final String FIELD_PURPOSE = "Purpose";
    private static final String FIELD_COMMENT = "Comment";
    private static final String FIELD_EMP_NAME = "EmpName";
    private static final String FIELD_CURRENCY_NAME = "CurrencyName";
    private static final String FIELD_TOTAL_POSTED_AMOUNT = "TotalPostedAmount";

    private RequestListCache requestListCache = null;
    private ConnectFormFieldsCache formFieldsCache = null;
    private RequestGroupConfigurationCache groupConfigurationCache = null;
    private RequestDTO tr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_header);

        final ConcurCore concurCore = (ConcurCore) getApplication();

        requestListCache = (RequestListCache) concurCore.getRequestListCache();
        formFieldsCache = (ConnectFormFieldsCache) concurCore.getRequestFormFieldsCache();
        groupConfigurationCache = (RequestGroupConfigurationCache) concurCore.getRequestGroupConfigurationCache();

        final Bundle bundle = getIntent().getExtras();
        final String requestId = bundle.getString(RequestListActivity.REQUEST_ID);
        
        this.isEditable = bundle.getString(RequestDigestActivity.REQUEST_IS_EDITABLE).equals("true")? true : false;

        // --- update mode
        if (requestId != null) {
            tr = requestListCache.getValue(requestId);
            form = formFieldsCache.getFormFields(tr.getHeaderFormId());
        }
        // --- create mode
        else {
            tr = new RequestDTO();
            final String formId = groupConfigurationCache.getValue(getUserId()).getFormId();
            form = formFieldsCache.getValue(formId);
        }

        locale = this.getResources().getConfiguration().locale != null ?
                this.getResources().getConfiguration().locale :
                Locale.US;
        inputDatePattern = DateUtil.DatePattern.DB_INPUT;

        configureUI();
    }

    private void configureUI() {
        /*final TextView name = (TextView) findViewById(R.id.Name);
        if (tr != null) {
            name.setText(tr.getName());
        }
        name.setTypeface(Typeface.DEFAULT_BOLD);*/

        final LinearLayout requestTitleLayout = (LinearLayout) findViewById(R.id.requestTitleLayout);
        final LinearLayout requestHeaderFields = (LinearLayout) findViewById(R.id.requestHeaderFields);
        final String fieldToTitle = FIELD_NAME;
        this.setDisplayFields(requestTitleLayout, requestHeaderFields, fieldToTitle);
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < getDateViews().size(); i++) {
            if ((Integer) getDateViews().get(i) == view.getId()) {
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
    protected String getLabelFromFieldName(String fieldName) {
        // TODO : extract those strings in strings xml file
        if (fieldName.equals(FIELD_NAME)) {
            return this.getResources().getString(R.string.tr_name);
        } else if (fieldName.equals(FIELD_START_DATE)) {
            return this.getResources().getString(R.string.tr_start_date);
        } else if (fieldName.equals(FIELD_END_DATE)) {
            return this.getResources().getString(R.string.tr_end_date);
        } else if (fieldName.equals(FIELD_PURPOSE)) {
            return this.getResources().getString(R.string.business_purpose);
        } else if (fieldName.equals(FIELD_COMMENT)) {
            return this.getResources().getString(R.string.comment);
        } else if (fieldName.equals(FIELD_EMP_NAME)) {
            return this.getResources().getString(R.string.tr_employee_name);
        } else if (fieldName.equals(FIELD_CURRENCY_NAME)) {
            return this.getResources().getString(R.string.currency);
        } else if (fieldName.equals(FIELD_TOTAL_POSTED_AMOUNT)) {
            return this.getResources().getString(R.string.amount);
        } else {
            return "";
        }
    }

    @Override
    public String getValueFromFieldName(String fieldName) {

        if (fieldName.equals(FIELD_NAME)) {
            if (tr.getName() == null) {
                return "";
            }
            return tr.getName();
        } else if (fieldName.equals(FIELD_START_DATE)) {
            if (tr.getStartDate() == null) {
                return "";
            }
            return formatDate(tr.getStartDate());
        } else if (fieldName.equals(FIELD_END_DATE)) {
            if (tr.getStartDate() == null) {
                return "";
            }
            return formatDate(tr.getEndDate());
        } else if (fieldName.equals(FIELD_PURPOSE)) {
            if (tr.getPurpose() == null) {
                return "";
            }
            return tr.getPurpose();
        } else if (fieldName.equals(FIELD_COMMENT)) {
            if (tr.getLastComment() == null) {
                return "";
            }
            return tr.getLastComment();
        } else if (fieldName.equals(FIELD_EMP_NAME)) {
            if (tr.getEmployeeName() == null) {
                return "";
            }
            return tr.getEmployeeName();
        } else if (fieldName.equals(FIELD_CURRENCY_NAME)) {
            if (tr.getCurrency() == null) {
                return "";
            }
            return tr.getCurrency();
        } else if (fieldName.equals(FIELD_TOTAL_POSTED_AMOUNT)) {
            return String.valueOf(tr.getTotal());
        } else {
            return "";
        }
    }

    @Override
    protected void setValueFromFieldName(String fieldName, String value) {
        if (fieldName.equals(FIELD_NAME)) {
            tr.setName(value);
        } else if (fieldName.equals(FIELD_START_DATE)) {
            tr.setStartDate(parseDate(value));
        } else if (fieldName.equals(FIELD_END_DATE)) {
            tr.setEndDate(parseDate(value));
        } else if (fieldName.equals(FIELD_PURPOSE)) {
            tr.setPurpose(value);
        } else if (fieldName.equals(FIELD_COMMENT)) {
            tr.setLastComment(value);
        } else if (fieldName.equals(FIELD_EMP_NAME)) {
            tr.setEmployeeName(value);
        } else if (fieldName.equals(FIELD_CURRENCY_NAME)) {
            tr.setCurrency(value);
        } else if (fieldName.equals(FIELD_TOTAL_POSTED_AMOUNT)) {
            tr.setTotal(Parse.safeParseDouble(value));
        }
    }

    @Override
    protected void applySpecifics(final TextView component, final LinearLayout.LayoutParams llp,
            final ConnectFormField ff) {
        if (ff.getName().equals(FIELD_NAME)) {
            component.setTextAppearance(this, R.style.ListCellHeaderText);
            component.setTextColor(getResources().getColor(R.color.White));
            component.setTypeface(Typeface.DEFAULT_BOLD);
            component.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }
}
