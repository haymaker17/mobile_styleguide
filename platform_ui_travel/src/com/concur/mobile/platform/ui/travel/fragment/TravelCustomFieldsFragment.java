package com.concur.mobile.platform.ui.travel.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.common.formfield.IFormField.AccessType;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.ui.common.view.FormFieldView;
import com.concur.mobile.platform.ui.common.view.InlineTextFormFieldView;
import com.concur.mobile.platform.ui.common.view.MultiLineTextFormFieldView;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.activity.BaseActivity;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomField;
import com.concur.mobile.platform.ui.travel.util.FormUtil;
import com.concur.mobile.platform.ui.travel.view.FormFieldViewEditHandler;
import com.concur.mobile.platform.ui.travel.view.FormFieldViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to show the travel custom fields
 *
 * @author RatanK
 */
public class TravelCustomFieldsFragment extends PlatformFragmentV1 {

    public static final String READ_ONLY = "read.only";
    public static final String DISPLAY_AT_START = "display.at.start";
    private static final String CLS_TAG = TravelCustomFieldsFragment.class.getSimpleName();
    /**
     * Contains whether the view should be a read-only view.
     */
    public boolean readOnly;
    /**
     * Contains whether the view should only display travel custom fields that match on <code>displayAtStart</code>.
     */
    public Boolean displayAtStart;
    /**
     * contains the travel custom fields
     */
    public List<TravelCustomField> customFields;
    /**
     * Contains a reference to the containing <code>BaseActivity</code>.
     */
    protected BaseActivity baseActivity;
    /**
     * Contains a reference to a form field view listener.
     */
    protected TravelCustomFieldsViewListener frmFldViewListener;
    private TravelCustomFieldsFragmentCallBackListener callBackListener;

    /**
     * Constructs an instance of <code>TravelCustomFieldsFragment</code> with editable fields.
     */
    public TravelCustomFieldsFragment() {
    }

    // TODO - copied from FormUtil...need to move there
    public static List<TravelCustomField> cloneFields(List<TravelCustomField> flds) {
        List<TravelCustomField> retVal = null;
        if (flds != null) {
            retVal = new ArrayList<TravelCustomField>(flds.size());
            for (IFormField tcf : flds) {
                try {
                    retVal.add((TravelCustomField) tcf.clone());
                } catch (CloneNotSupportedException cnsExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".cloneFields: ", cnsExc);
                }
            }
        }
        return retVal;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(DISPLAY_AT_START)) {
                displayAtStart = savedInstanceState.getBoolean(DISPLAY_AT_START);
            }
        }

        // TODO - does this need to be set from the activity that is invoking this fragment?
        frmFldViewListener = new TravelCustomFieldsViewListener((BaseActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
        super.onCreateView(inflater, container, inState);

        View mainView = inflater.inflate(R.layout.travel_custom_fields_view, container, false);
        ViewGroup viewGroup = (ViewGroup) mainView.findViewById(R.id.custom_fields);
        populateTravelCustomFields(viewGroup, inState);
        // If there are no custom fields to be displayed at this time, just
        // set the visibility to 'GONE'.
        if (!hasDisplayCustomFields()) {
            mainView.setVisibility(View.GONE);
        }
        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BaseActivity) {
            baseActivity = (BaseActivity) activity;
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".onAttach: activity is not instance of BaseActivity!");
        }

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (TravelCustomFieldsFragmentCallBackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TravelCustomFieldsFragmentCallBackListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseActivity = null;
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

    /**
     * Gets any current form field view listener object associated with this travel custom fields viewer.
     *
     * @return the travel custom fields viewer.
     */
    public FormFieldViewListener getFormFieldViewListener() {
        return frmFldViewListener;
    }

    protected void populateTravelCustomFields(ViewGroup viewGroup, Bundle inState) {

        // First, clear out any children of 'viewGroup'
        viewGroup.removeAllViews();
        if (customFields != null) {
            List<TravelCustomField> tcfs = customFields;
            if (readOnly) {
                tcfs = cloneFieldsReadOnly(tcfs);
            }
            if (displayAtStart != null) {
                for (TravelCustomField tcf : customFields) {
                    // Mark the appropriate fields as hidden based on 'displayAtStart'.
                    if (displayAtStart) {
                        // Only show fields displayed at start.
                        if (tcf.displayAtStart()) {
                            // Show the field.
                            tcf.setAccessType(IFormField.AccessType.RW);
                        } else {
                            // Hide the field.
                            tcf.setAccessType(IFormField.AccessType.HD);
                        }
                    } else {
                        // Only show fields not displayed at start.
                        if (tcf.displayAtStart()) {
                            // Hide the field.
                            tcf.setAccessType(IFormField.AccessType.HD);
                        } else {
                            // Show the field.
                            tcf.setAccessType(IFormField.AccessType.RW);
                        }
                    }
                }
            }
            List<FormFieldView> frmFldViews = FormUtil
                    .populateViewWithFormFields(baseActivity, viewGroup, tcfs, null, frmFldViewListener,
                            new FormFieldViewEditHandler());
            frmFldViewListener.setFormFieldViews(frmFldViews);
            if (inState != null) {
                FormUtil.restoreFormFieldState(frmFldViewListener, inState);
            }
            // Perform any field initialization.
            frmFldViewListener.initFields();
        }

    }

    /**
     * Will clone a list of passed in travel custom field objects and set their access types to read-only.
     *
     * @param flds the list of fields to clone.
     * @return returns the list of cloned fields.
     */
    protected List<TravelCustomField> cloneFieldsReadOnly(List<TravelCustomField> flds) {
        List<TravelCustomField> retVal = cloneFields(flds);
        if (retVal != null) {
            for (IFormField frmFld : retVal) {
                frmFld.setAccessType(AccessType.RO);
            }
        }
        return retVal;
    }

    /**
     * Will return whether based on the value of <code>displayAtStart</code> whether there are any travel custom fields that will
     * be displayed.
     *
     * @return whether there are any fields that should be displayed at based on the value of <code>displayAtStart</code>.
     */
    protected boolean hasDisplayCustomFields() {
        boolean retVal = false;
        if (customFields != null) {
            for (TravelCustomField tcf : customFields) {
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
        return retVal;
    }

    /**
     * Will store the current field values on the application object.
     */
    public void saveFieldValues() {
        if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null) {
            // Iterate over each form field view and commit the values to the underlying object model.
            for (FormFieldView ffv : frmFldViewListener.getFormFieldViews()) {
                ffv.commit();
            }
        }
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
     * Gets the hint text for a form field.
     *
     * @param frmFld an instance of <code>IFormField</code>.
     * @return the form field hint text.
     */
    protected String getHintText(IFormField frmFld) {
        return FormUtil.getHintText(frmFld, getActivity());
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
                                if (tcf.getControlType() == IFormField.ControlType.EDIT
                                        || tcf.getControlType() == IFormField.ControlType.TEXT_AREA) {
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

    public interface TravelCustomFieldsFragmentCallBackListener {

        public void sendTravelCustomFieldsUpdateRequest(List<TravelCustomField> fields);
    }

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
     * An extension of <code>FormFieldViewListener</code>
     */
    protected class TravelCustomFieldsViewListener extends FormFieldViewListener {

        public TravelCustomFieldsViewListener(BaseActivity activity) {
            super(activity);
        }

        private final String CLS_TAG =
                TravelCustomFieldsFragment.CLS_TAG + "." + TravelCustomFieldsViewListener.class.getSimpleName();

        @Override
        public void initFields() {
            super.initFields();

            // Set the appropriate hint text on edit fields.
            List<FormFieldView> ffvs = getFormFieldViews();
            if (ffvs != null) {
                for (FormFieldView ffv : ffvs) {
                    IFormField frmFld = ffv.getFormField();
                    String hintText = getHintText(frmFld);
                    if (hintText != null) {
                        if (ffv instanceof InlineTextFormFieldView) {
                            ((InlineTextFormFieldView) ffv).setHintText(hintText);
                        } else if (ffv instanceof MultiLineTextFormFieldView) {
                            ((MultiLineTextFormFieldView) ffv).setHintText(hintText);
                        } else {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".initFields: unexpected FormFieldView type -- '" + ffv.getClass()
                                            .getSimpleName() + "'");
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
                        callBackListener.sendTravelCustomFieldsUpdateRequest(getTravelCustomFields());
                    }
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".valueChanged: unexpected form field type -- '" + frmFldView.getFormField()
                                    .getClass().getSimpleName() + "'");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".valueChanged: form field is null!");
            }
        }

    }

}
