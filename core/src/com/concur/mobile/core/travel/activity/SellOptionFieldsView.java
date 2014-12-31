package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.travel.activity.TravelCustomFieldsView.TravelCustomFieldHint;
import com.concur.mobile.core.travel.data.SellOptionField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldViewListener;

/**
 * An extension of <code>TravelCustomFieldsView</code> for displaying a set of travel segment sell options i.e flight options
 */
public class SellOptionFieldsView extends BaseFragment {

    private static final String CLS_TAG = SellOptionFieldsView.class.getSimpleName();

    private List<SellOptionField> sellOptionFields;

    private SellOptionFieldsViewListener sellOptionFldViewListener;

    /**
     * Contains a reference to the containing <code>BaseActivity</code>.
     */
    protected BaseActivity baseActivity;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        sellOptionFields = (List<SellOptionField>) args.getSerializable("sellOptionFields");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sellOptionFldViewListener = new SellOptionFieldsViewListener(getBaseActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
        View view = null;
        if (sellOptionFields != null && sellOptionFields.size() > 0) {
            view = inflater.inflate(R.layout.travel_custom_fields, container, false);
            // view.setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.custom_fields_header)).setText(R.string.travel_flight_options);
            ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.custom_fields);
            populateSellOptionFields(viewGroup, inState);
        }
        return view;
    }

    private void populateSellOptionFields(ViewGroup viewGroup, Bundle inState) {
        // First, clear out any children of 'viewGroup'
        viewGroup.removeAllViews();

        List<FormFieldView> frmFldViews = FormUtil.populateViewWithFormFields(baseActivity, viewGroup,
                sellOptionFields, null, sellOptionFldViewListener);
        sellOptionFldViewListener.setFormFieldViews(frmFldViews);
        if (inState != null) {
            FormUtil.restoreFormFieldState(sellOptionFldViewListener, inState, baseActivity.getRetainer());
        }
        // Perform any field initialization.
        sellOptionFldViewListener.initFields();
    }

    /**
     * Gets the list of currently edited <code>SellOptionField</code> objects.
     * 
     * @return returns the list of currently edited <code>SellOptionField</code> objects.
     */
    public List<SellOptionField> getSellOptionFields() {
        List<SellOptionField> retVal = null;
        if (sellOptionFldViewListener != null) {
            List<FormFieldView> frmFldViews = sellOptionFldViewListener.getFormFieldViews();
            if (frmFldViews != null) {
                retVal = new ArrayList<SellOptionField>(frmFldViews.size());
                for (FormFieldView ffv : frmFldViews) {
                    if (ffv.getFormField() != null && ffv.getFormField() instanceof SellOptionField) {
                        retVal.add((SellOptionField) ffv.getFormField());
                    }
                }
            }
        }
        return retVal;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store any form field information.
        FormUtil.storeFormFieldState(sellOptionFldViewListener, outState, true);
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
     * Will store the current field values on the application object.
     */
    public void saveFieldValues() {
        if (sellOptionFldViewListener != null && sellOptionFldViewListener.getFormFieldViews() != null) {
            // Iterate over each form field view and commit the values to the underlying object model.
            for (FormFieldView ffv : sellOptionFldViewListener.getFormFieldViews()) {
                ffv.commit();
            }
        }
    }

    /**
     * Gets any current form field view listener object associated with this travel custom fields viewer.
     * 
     * @return the travel custom fields viewer.
     */
    public FormFieldViewListener getFormFieldViewListener() {
        return sellOptionFldViewListener;
    }

    /**
     * Will find all field values that are: <li>Required, but missing a field value</li> <li>Having a value with an invalid
     * length.
     * 
     * @return returns a list of <code>TravelCustomFieldHint</code> objects describing the invalid fields.
     */
    public List<TravelCustomFieldHint> findInvalidFieldValues() {
        List<TravelCustomFieldHint> retVal = new ArrayList<TravelCustomFieldHint>();

        if (sellOptionFldViewListener != null) {
            List<FormFieldView> ffvs = sellOptionFldViewListener.getFormFieldViews();
            if (ffvs != null) {
                for (FormFieldView ffv : ffvs) {
                    try {
                        if (ffv.getFormField() instanceof SellOptionField) {
                            SellOptionField tcf = (SellOptionField) ffv.getFormField();
                            if (tcf.isRequired() && !ffv.hasValue()) {
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
                        Log.e(Const.LOG_TAG, CLS_TAG + ".findInvalidFieldValues: expected type SellOptionField.", ccExc);
                    }
                }
            }
        }
        return retVal;
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
    protected class SellOptionFieldsViewListener extends FormFieldViewListener {

        private final String CLS_TAG = SellOptionFieldsView.CLS_TAG + "."
                + SellOptionFieldsViewListener.class.getSimpleName();

        public SellOptionFieldsViewListener(BaseActivity activity) {
            super(activity);
        }

        @Override
        public void valueChanged(FormFieldView frmFldView) {
            super.valueChanged(frmFldView);

            if (frmFldView.getFormField() != null) {
                if (frmFldView.getFormField() instanceof SellOptionField) {
                    saveFieldValues();
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
