package com.concur.mobile.platform.ui.travel.activity;

import android.app.Dialog;
import android.content.Intent;
import com.concur.mobile.platform.travel.loader.TravelCustomField;
import com.concur.mobile.platform.travel.loader.TravelCustomFieldsConfig;
import com.concur.mobile.platform.ui.common.view.FormFieldView;
import com.concur.mobile.platform.ui.common.view.SearchListFormFieldView;
import com.concur.mobile.platform.ui.travel.fragment.TravelCustomFieldsFragment;

import java.util.List;

/**
 * Created by RatanK on 20/03/2015.
 */
public class TravelBaseActivity extends BaseActivity  {

    protected static final String CLS_TAG = TravelBaseActivity.class.getSimpleName();

    protected static final String TRAVEL_CUSTOM_VIEW_FRAGMENT_TAG = "travel.custom.view";

    // Contains the Cliqbook trip id if a hotel search is being performed in the
    // context of a trip.
    protected String cliqbookTripId;

    //Contains the list of travel custom fields.
    public List<TravelCustomField> formFields;

    // Contains a reference to a fragment used to display travel custom fields.
    protected TravelCustomFieldsFragment travelCustomFieldsFragment;

    protected TravelCustomFieldsConfig travelCustomFieldsConfig;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // View manageViolationsView = null;

        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE:
            // MOB-14331
            // Check whether there a form field view should handle the onActivityResult.
            if (travelCustomFieldsFragment != null && travelCustomFieldsFragment.getFormFieldViewListener() != null
                    && travelCustomFieldsFragment.getFormFieldViewListener().isCurrentFormFieldViewSet()) {
                travelCustomFieldsFragment.getFormFieldViewListener().getCurrentFormFieldView()
                        .onActivityResult(requestCode, resultCode, data);
            }
            break;
        }
    }



    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dlg = null;
        // Check whether there a form field view should handle the dialog creation.
        if (travelCustomFieldsFragment != null && travelCustomFieldsFragment.getFormFieldViewListener() != null
                && travelCustomFieldsFragment.getFormFieldViewListener().isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            dlg = travelCustomFieldsFragment.getFormFieldViewListener().getCurrentFormFieldView().onCreateDialog(id);
        } else {
            super.onCreateDialog(id);
        }
        return dlg;
    }
}
