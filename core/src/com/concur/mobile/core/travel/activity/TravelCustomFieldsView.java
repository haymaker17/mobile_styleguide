/**
 * 
 */
package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.TravelCustomField.SavedFieldInfo;
import com.concur.mobile.core.travel.data.TravelCustomFieldValueSpinnerItem;
import com.concur.mobile.core.travel.data.TravelCustomFieldsConfig;
import com.concur.mobile.core.travel.service.TravelCustomFieldsUpdateRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.InlineTextFormFieldView;
import com.concur.mobile.core.view.MultiLineTextFormFieldView;

/**
 * An extension of <code>Fragment</code> for displaying a set of travel custom fields.
 */
public class TravelCustomFieldsView extends BaseFragment {

    private static final String CLS_TAG = TravelCustomFieldsView.class.getSimpleName();

    protected static final String TRAVEL_CUSTOM_FIELDS_UPDATE_RECEIVER_KEY = "travel.custom.fields.update.receiver";

    public static final String READ_ONLY = "read.only";
    public static final String DISPLAY_AT_START = "display.at.start";

    /**
     * Contains a reference to the containing <code>BaseActivity</code>.
     */
    protected BaseActivity baseActivity;
    /**
     * Contains a reference to a form field view listener.
     */
    protected TravelCustomFieldsViewListener frmFldViewListener;
    /**
     * Contains the travel custom fields receiver.
     */
    protected TravelCustomFieldsReceiver travelCustomFieldsReceiver;
    /**
     * Contains the filter used to register the travel custom fields receiver.
     */
    protected IntentFilter travelCustomFieldsFilter;
    /**
     * Contains a reference to an outstanding request to retrieve travel custom fields information based on a current set of
     * values.
     */
    protected TravelCustomFieldsUpdateRequest travelCustomFieldsRequest;
    /**
     * Contains whether the view should be a read-only view.
     */
    protected boolean readOnly;

    /**
     * Contains whether the view should only display travel custom fields that match on <code>displayAtStart</code>.
     */
    protected Boolean displayAtStart;

    /**
     * Models travel custom field hint information.
     */
    public static class TravelCustomFieldHint {

        /**
         * Contains the field name.
         */
        public String fieldName;

        /**
         * Contains the hint text.
         */
        public String hintText;

    }

    /**
     * Constructs an instance of <code>TravelCustomFieldsView</code> with editable fields.
     */
    public TravelCustomFieldsView() {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        // * @param readOnly
        // * contains whether the fields should be read-only.
        // * @param displayAtStart
        // * if <code>true</code> then only those fields with a value of <code>true</code> for the
        // * <code>TravelCustomField.displayAtStart</code> will be displayed. if <code>false</code>, then only those fields
        // * with a value of <code>false</code> will be displayed.
        this.readOnly = args.getBoolean(READ_ONLY, false);
        this.displayAtStart = args.getBoolean(DISPLAY_AT_START, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(DISPLAY_AT_START)) {
                displayAtStart = savedInstanceState.getBoolean(DISPLAY_AT_START);
            }
        }
        frmFldViewListener = new TravelCustomFieldsViewListener(getBaseActivity());
        restoreReceivers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
        View view = null;
        view = inflater.inflate(R.layout.travel_custom_fields, container, false);
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.custom_fields);
        populateTravelCustomFields(viewGroup, inState);
        // If there are no custom fields to be displayed at this time, just
        // set the visibility to 'GONE'.
        if (!hasDisplayCustomFields()) {
            view.setVisibility(View.GONE);
        }
        return view;
    }

    protected void populateTravelCustomFields(ViewGroup viewGroup, Bundle inState) {
        if (baseActivity != null) {
            ConcurCore concurCore = baseActivity.getConcurCore();
            TravelCustomFieldsConfig config = concurCore.getTravelCustomFieldsConfig();
            // First, clear out any children of 'viewGroup'
            viewGroup.removeAllViews();
            if (config != null && config.formFields != null) {
                List<? extends ExpenseReportFormField> tcfs = config.formFields;
                if (readOnly) {
                    tcfs = cloneFieldsReadOnly(tcfs);
                }
                // else {
                // tcfs = FormUtil.cloneFields(tcfs);
                // }
                // restoreSavedValues(tcfs);
                if (displayAtStart != null) {
                    for (TravelCustomField tcf : config.formFields) {
                        // Mark the appropriate fields as hidden based on 'displayAtStart'.
                        if (displayAtStart) {
                            // Only show fields displayed at start.
                            if (!tcf.displayAtStart()) {
                                // Hide the field.
                                tcf.setAccessType(ExpenseReportFormField.AccessType.HD);
                            } else {
                                // Show the field.
                                tcf.setAccessType(ExpenseReportFormField.AccessType.RW);
                            }
                        } else {
                            // Only show fields not displayed at start.
                            if (tcf.displayAtStart()) {
                                // Hide the field.
                                tcf.setAccessType(ExpenseReportFormField.AccessType.HD);
                            } else {
                                // Show the field.
                                tcf.setAccessType(ExpenseReportFormField.AccessType.RW);
                            }
                        }
                    }
                }
                List<FormFieldView> frmFldViews = FormUtil.populateViewWithFormFields(baseActivity, viewGroup, tcfs,
                        null, frmFldViewListener);
                frmFldViewListener.setFormFieldViews(frmFldViews);
                if (inState != null) {
                    FormUtil.restoreFormFieldState(frmFldViewListener, inState, baseActivity.getRetainer());
                }
                // Perform any field initialization.
                frmFldViewListener.initFields();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateTravelCustomFields: baseActivity is null!");
        }
    }

    /**
     * Will clone a list of passed in travel custom field objects and set their access types to read-only.
     * 
     * @param flds
     *            the list of fields to clone.
     * 
     * @return returns the list of cloned fields.
     */
    protected List<? extends ExpenseReportFormField> cloneFieldsReadOnly(List<? extends ExpenseReportFormField> flds) {
        List<ExpenseReportFormField> retVal = FormUtil.cloneFields(flds);
        if (retVal != null) {
            for (ExpenseReportFormField frmFld : retVal) {
                frmFld.setAccessType(AccessType.RO);
            }
        }
        return retVal;
    }

    /**
     * Gets the list of currently edited <code>TravelCustomField</code> objects.
     * 
     * @return returns the list of currently edited <code>TravelCustomField</code> objects.
     */
    protected List<TravelCustomField> getTravelCustomFields() {
        List<TravelCustomField> retVal = null;
        if (frmFldViewListener != null) {
            List<FormFieldView> frmFldViews = frmFldViewListener.getFormFieldViews();
            if (frmFldViews != null) {
                retVal = new ArrayList<TravelCustomField>(frmFldViews.size());
                for (FormFieldView ffv : frmFldViews) {
                    if (ffv.getFormField() != null && ffv.getFormField() instanceof TravelCustomField) {
                        retVal.add((TravelCustomField) ffv.getFormField());
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will restore any receivers.
     */
    protected void restoreReceivers() {
        if (baseActivity != null) {
            RetainerFragment retainer = baseActivity.getRetainer();
            if (retainer != null) {
                // Store travel custom fields update receiver.
                if (retainer.contains(TRAVEL_CUSTOM_FIELDS_UPDATE_RECEIVER_KEY)) {
                    travelCustomFieldsReceiver = (TravelCustomFieldsReceiver) retainer
                            .get(TRAVEL_CUSTOM_FIELDS_UPDATE_RECEIVER_KEY);
                    if (travelCustomFieldsReceiver != null) {
                        travelCustomFieldsReceiver.setFragment(this);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".restoreReceivers: retainer contains a null travel custom fields receiver!");
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: retainer is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".restoreReceivers: baseActivity is null!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (baseActivity != null) {
            RetainerFragment retainer = baseActivity.getRetainer();
            if (retainer != null) {
                // Store travel custom fields update receiver.
                if (travelCustomFieldsReceiver != null) {
                    retainer.put(TRAVEL_CUSTOM_FIELDS_UPDATE_RECEIVER_KEY, travelCustomFieldsReceiver);
                    travelCustomFieldsReceiver.setFragment(null);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: retainer is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPause: baseActivity is null!");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Display the 'displayAtStart' value.
        if (displayAtStart != null) {
            outState.putBoolean(DISPLAY_AT_START, displayAtStart);
        }

        // Store any form field information.
        FormUtil.storeFormFieldState(frmFldViewListener, outState, true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActivity) {
            baseActivity = (BaseActivity) activity;
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".onAttach: activity is not instance of BaseActivity!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseActivity = null;
    }

    /**
     * Restore any saved values into a set of form fields.
     * 
     * @param frmFlds
     *            the list of form fields to write persisted saved values.
     */
    protected void restoreSavedValues(List<? extends ExpenseReportFormField> frmFlds) {
        if (frmFlds != null) {
            // Retrieve a set of saved values.
            if (baseActivity != null) {
                ConcurCore concurCore = baseActivity.getConcurCore();
                ConcurService concurService = concurCore.getService();
                if (concurService != null) {
                    String savedFieldInfoStr = concurService.getTravelCustomFieldInfo(baseActivity.getUserId());
                    if (savedFieldInfoStr != null) {
                        List<SavedFieldInfo> savedFieldInfos = TravelCustomField
                                .deserializeSavedFieldInfo(savedFieldInfoStr);
                        if (savedFieldInfos != null) {
                            for (ExpenseReportFormField frmFld : frmFlds) {
                                try {
                                    TravelCustomField tcf = (TravelCustomField) frmFld;
                                    for (SavedFieldInfo savedFldInfo : savedFieldInfos) {
                                        // Match on field id?
                                        if (savedFldInfo.fieldId != null && tcf.getId() != null
                                                && savedFldInfo.fieldId.equalsIgnoreCase(tcf.getId())) {
                                            switch (tcf.getControlType()) {
                                            case PICK_LIST: {
                                                if (tcf.getFieldValues() != null) {
                                                    // Locate the saved field "value id" in the new list of possible values, and
                                                    // if found, set the current value and liKey value.
                                                    List<TravelCustomFieldValueSpinnerItem> fldVals = tcf
                                                            .getFieldValues();
                                                    for (TravelCustomFieldValueSpinnerItem fldVal : fldVals) {
                                                        if (fldVal.valueId != null
                                                                && fldVal.valueId
                                                                        .equalsIgnoreCase(savedFldInfo.valueId)) {
                                                            if (fldVal.value != null) {
                                                                tcf.setValue(fldVal.name);
                                                            }
                                                            if (fldVal.valueId != null) {
                                                                tcf.setLiKey(fldVal.valueId);
                                                            }
                                                            // Break out of the loop for current possible field values.
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    // No field values, so perhaps the field has changed.
                                                }
                                                break;
                                            }
                                            case EDIT:
                                            case TEXT_AREA: {
                                                if (savedFldInfo.value != null) {
                                                    tcf.setValue(savedFldInfo.value);
                                                }
                                                break;
                                            }
                                            case CHECKBOX: {
                                                if (savedFldInfo.value != null) {
                                                    tcf.setValue(savedFldInfo.value);
                                                }
                                                break;
                                            }
                                            }
                                            // Break out of the above loop over SavedFieldInfo objects.
                                            break;
                                        }
                                    }
                                } catch (ClassCastException ccExc) {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".restoreSavedValues: expected TravelCustomField type!", ccExc);
                                }
                            }
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".restoreSavedValues: concur service not available!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".restoreSavedValues: 'baseActivity' is null!");
            }
        }
    }

    /**
     * Will store the current field values on the application object.
     */
    public void saveFieldValues() {
        if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null) {
            // List<TravelCustomField> tcfs = new ArrayList<TravelCustomField>(frmFldViewListener.getFormFieldViews().size());
            // Iterate over each form field view and commit the values to the underlying object model.
            for (FormFieldView ffv : frmFldViewListener.getFormFieldViews()) {
                ffv.commit();
                // tcfs.add((TravelCustomField) ffv.getFormField());
            }
            // Persist the set of field values.
            // if( baseActivity != null ) {
            // ConcurCore concurCore = baseActivity.getConcurCore();
            // ConcurService concurService = concurCore.getService();
            // if( concurService != null ) {
            // StringBuilder strBldr = new StringBuilder();
            // TravelCustomField.serializeToXMLForWire(strBldr, tcfs, true);
            // concurService.saveTravelCustomFieldInfo(baseActivity.getUserId(), strBldr.toString());
            // } else {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveFieldValues: concur service is not available!");
            // }
            // } else {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".saveFieldValues: baseActivity is null!");
            // }

        }
    }

    /**
     * Gets any current form field view listener object associated with this travel custom fields viewer.
     * 
     * @return the travel custom fields viewer.
     */
    public FormFieldViewListener getFormFieldViewListener() {
        return frmFldViewListener;
    }

    /**
     * Will return whether based on the value of <code>displayAtStart</code> whether there are any travel custom fields that will
     * be displayed.
     * 
     * @return whether there are any fields that should be displayed at based on the value of <code>displayAtStart</code>.
     */
    protected boolean hasDisplayCustomFields() {
        boolean retVal = false;
        ConcurCore concurCore = baseActivity.getConcurCore();
        TravelCustomFieldsConfig config = concurCore.getTravelCustomFieldsConfig();
        if (config != null) {
            List<TravelCustomField> tcfs = config.formFields;
            if (tcfs != null) {
                for (TravelCustomField tcf : tcfs) {
                    if (displayAtStart != null) {
                        if (displayAtStart) {
                            // Displaying fields at beginning of booking process.
                            if (tcf.displayAtStart()) {
                                // Found a field.
                                retVal = true;
                                break;
                            }
                        } else {
                            // Displaying fields at end of booking process.
                            if (!tcf.displayAtStart()) {
                                // Found a field.
                                retVal = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Determines whether there are any required fields missing values.
     * 
     * @return returns whether there are any required fields missing values.
     */
    public boolean isMissingRequiredFields() {
        boolean retVal = false;
        if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null) {
            for (FormFieldView ffv : frmFldViewListener.getFormFieldViews()) {
                if (ffv.getFormField().getAccessType() != ExpenseReportFormField.AccessType.HD
                        && ffv.getFormField().isRequired()) {
                    if (!ffv.hasValue()) {
                        retVal = true;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will cancel any outstanding request to update the set of travel custom fields.
     */
    public void cancelCustomFieldsUpdate() {
        if (travelCustomFieldsRequest != null) {
            travelCustomFieldsRequest.cancel();
        }
    }

    /**
     * Will find all field values that are: <li>Required, but missing a field value</li> <li>Having a value with an invalid
     * length.
     * 
     * @return returns a list of <code>TravelCustomFieldHint</code> objects describing the invalid fields.
     */
    public List<TravelCustomFieldHint> findInvalidFieldValues() {
        List<TravelCustomFieldHint> retVal = new ArrayList<TravelCustomFieldHint>();

        if (frmFldViewListener != null) {
            List<FormFieldView> ffvs = frmFldViewListener.getFormFieldViews();
            if (ffvs != null) {
                for (FormFieldView ffv : ffvs) {
                    try {
                        TravelCustomField tcf = (TravelCustomField) ffv.getFormField();
                        // Only perform validity checks on fields that have a matching value on 'TravelCustomField.displayAtStart'
                        // with 'this.displayAtStart'.
                        // NOTE: The 'AccessType' attribute on the TravelCustomField object is manipulated in order
                        // hide/show fields based on their 'displayAtStart' values. So, fields with an access type
                        // value of 'AccessType.RW' for 'displayAtStart' values of 'true' will get set to 'AccessType.HD'
                        // on the booking screens.
                        if (Boolean.valueOf(tcf.displayAtStart()).equals(displayAtStart)) {
                            if (ffv.hasValue()) {
                                // Only check edit or text areas.
                                if (tcf.getControlType() == ControlType.EDIT
                                        || tcf.getControlType() == ControlType.TEXT_AREA) {
                                    String value = ffv.getCurrentValue();
                                    if (tcf.getMaxLength() != -1 && tcf.getMinLength() != -1) {
                                        // Check range.
                                        if (!(value.length() >= tcf.getMinLength() && value.length() <= tcf
                                                .getMaxLength())) {
                                            TravelCustomFieldHint tcfh = new TravelCustomFieldHint();
                                            tcfh.fieldName = tcf.getLabel();
                                            tcfh.hintText = getHintText(tcf);
                                            retVal.add(tcfh);
                                        }
                                    } else if (tcf.getMaxLength() != -1) {
                                        // Check <= max.
                                        if (!(value.length() <= tcf.getMaxLength())) {
                                            TravelCustomFieldHint tcfh = new TravelCustomFieldHint();
                                            tcfh.fieldName = tcf.getLabel();
                                            tcfh.hintText = getHintText(tcf);
                                            retVal.add(tcfh);
                                        }
                                    } else {
                                        // Check >= min.
                                        if (!(value.length() >= tcf.getMinLength())) {
                                            TravelCustomFieldHint tcfh = new TravelCustomFieldHint();
                                            tcfh.fieldName = tcf.getLabel();
                                            tcfh.hintText = getHintText(tcf);
                                            retVal.add(tcfh);
                                        }
                                    }
                                }
                            } else if (tcf.isRequired()) {
                                switch (tcf.getControlType()) {
                                case TEXT_AREA:
                                case EDIT: {
                                    TravelCustomFieldHint tcfh = new TravelCustomFieldHint();
                                    tcfh.fieldName = tcf.getLabel();
                                    tcfh.hintText = getHintText(tcf);
                                    retVal.add(tcfh);
                                    break;
                                }
                                case PICK_LIST: {
                                    TravelCustomFieldHint tcfh = new TravelCustomFieldHint();
                                    tcfh.fieldName = tcf.getLabel();
                                    tcfh.hintText = getActivity().getText(R.string.general_requires_selection)
                                            .toString();
                                    retVal.add(tcfh);
                                    break;
                                }
                                }
                            }
                        }
                    } catch (ClassCastException ccExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".findInvalidFieldValues: expected type TravelCustomField.",
                                ccExc);
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will send a request to retrieve travel custom booking information.
     */
    protected void sendTravelCustomFieldsRequest() {
        ConcurService concurService = baseActivity.getConcurService();
        registerTravelCustomFieldsReceiver();
        travelCustomFieldsRequest = concurService.sendTravelCustomFieldsUpdateRequest(getTravelCustomFields());
        if (travelCustomFieldsRequest == null) {
            Log.e(Const.LOG_TAG,
                    CLS_TAG
                            + ".sendTravelCustomFieldsRequest: unable to create request to update travel custom field information!");
            unregisterTravelCustomFieldsUpdateReceiver();
        } else {
            // Set the request object on the receiver.
            travelCustomFieldsReceiver.setServiceRequest(travelCustomFieldsRequest);
            // Show the travel custom fields progress dialog.
            baseActivity.showDialog(TravelBaseActivity.CUSTOM_FIELDS_UPDATE_PROGRESS_DIALOG);
        }
    }

    /**
     * Will register an instance of <code>TravelCustomFieldsReceiver</code> with the application context and set the
     * <code>travelCustomFieldsReceiver</code> attribute.
     */
    protected void registerTravelCustomFieldsReceiver() {
        if (travelCustomFieldsReceiver == null) {
            travelCustomFieldsReceiver = new TravelCustomFieldsReceiver(this);
            if (travelCustomFieldsFilter == null) {
                travelCustomFieldsFilter = new IntentFilter(Const.ACTION_TRAVEL_CUSTOM_FIELDS_UPDATED);
            }
            baseActivity.getApplicationContext().registerReceiver(travelCustomFieldsReceiver, travelCustomFieldsFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerTravelCustomFieldsReceiver: travelCustomFieldsReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>TravelCustomFieldsReceiver</code> with the application context and set the
     * <code>travelCustomFieldsReceiver</code> to <code>null</code>.
     */
    protected void unregisterTravelCustomFieldsUpdateReceiver() {
        if (travelCustomFieldsReceiver != null) {
            baseActivity.getApplicationContext().unregisterReceiver(travelCustomFieldsReceiver);
            travelCustomFieldsReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterTravelCustomFieldsReceiver: travelCustomFieldsReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the result of retrieving an updated list of
     * travel custom fields.
     */
    static class TravelCustomFieldsReceiver extends
            BaseBroadcastReceiver<TravelCustomFieldsView, TravelCustomFieldsUpdateRequest> {

        private static String CLS_TAG = TravelCustomFieldsView.CLS_TAG + "."
                + TravelCustomFieldsReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>TravelCustomFieldsReceiver</code>.
         * 
         * @param fragment
         *            the fragment.
         */
        TravelCustomFieldsReceiver(TravelCustomFieldsView fragment) {
            super(fragment);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearFragmentServiceRequest(TravelCustomFieldsView fragment) {
            fragment.travelCustomFieldsRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            if (fragment.baseActivity != null) {
                fragment.baseActivity.dismissDialog(TravelBaseActivity.CUSTOM_FIELDS_UPDATE_PROGRESS_DIALOG);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".dismissRequestDialog: baseActivity is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            if (fragment.baseActivity != null) {
                fragment.baseActivity.actionStatusErrorMessage = fragment.actionStatusErrorMessage;
                fragment.baseActivity.lastHttpErrorMessage = fragment.lastHttpErrorMessage;
                fragment.baseActivity.showDialog(TravelBaseActivity.CUSTOM_FIELDS_UPDATE_FAILURE_DIALOG);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure: baseActivity is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ViewGroup viewGroup = (ViewGroup) fragment.getActivity().findViewById(R.id.custom_fields);
            if (viewGroup != null) {
                fragment.populateTravelCustomFields(viewGroup, null);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: unable to locate 'custom_fields' view group!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            fragment.unregisterTravelCustomFieldsUpdateReceiver();
        }

        @Override
        protected void setFragmentServiceRequest(TravelCustomFieldsUpdateRequest request) {
            this.serviceRequest = request;
        }

    }

    /**
     * Gets the hint text for a form field.
     * 
     * @param frmFld
     *            an instance of <code>ExpenseReportFormField</code>.
     * @return the form field hint text.
     */
    protected String getHintText(ExpenseReportFormField frmFld) {
        return FormUtil.getHintText(frmFld, getActivity());
    }

    /**
     * An extension of <code>FormFieldViewListener</code>
     */
    protected class TravelCustomFieldsViewListener extends FormFieldViewListener {

        private final String CLS_TAG = TravelCustomFieldsView.CLS_TAG + "."
                + TravelCustomFieldsViewListener.class.getSimpleName();

        public TravelCustomFieldsViewListener(BaseActivity activity) {
            super(activity);
        }

        @Override
        public void initFields() {
            super.initFields();

            // Set the appropriate hint text on edit fields.
            List<FormFieldView> ffvs = getFormFieldViews();
            if (ffvs != null) {
                for (FormFieldView ffv : ffvs) {
                    ExpenseReportFormField frmFld = ffv.getFormField();
                    String hintText = getHintText(frmFld);
                    if (hintText != null) {
                        if (ffv instanceof InlineTextFormFieldView) {
                            ((InlineTextFormFieldView) ffv).setHintText(hintText);
                        } else if (ffv instanceof MultiLineTextFormFieldView) {
                            ((MultiLineTextFormFieldView) ffv).setHintText(hintText);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".initFields: unexpected FormFieldView type -- '"
                                    + ffv.getClass().getSimpleName() + "'");
                        }
                    }
                }
            }
        }

        @Override
        public void valueChanged(FormFieldView frmFldView) {
            super.valueChanged(frmFldView);

            if (frmFldView.getFormField() != null) {
                if (frmFldView.getFormField() instanceof TravelCustomField) {
                    TravelCustomField tcf = (TravelCustomField) frmFldView.getFormField();
                    if (tcf.hasDependency()) {
                        // Commit the current field form view values to the underlying form fields.
                        saveFieldValues();
                        // Fire off a request to fetch an updated form.
                        sendTravelCustomFieldsRequest();
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: unexpected form field type -- '"
                            + frmFldView.getFormField().getClass().getSimpleName() + "'");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: form field is null!");
            }
        }

    }

}
