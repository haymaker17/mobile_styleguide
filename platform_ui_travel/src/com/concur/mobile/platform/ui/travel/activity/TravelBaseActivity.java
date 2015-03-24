package com.concur.mobile.platform.ui.travel.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.concur.mobile.platform.ui.common.view.FormFieldView;
import com.concur.mobile.platform.ui.common.view.SearchListFormFieldView;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.fragment.TravelCustomFieldsFragment;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomField;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsConfig;

import java.util.List;

/**
 * Created by RatanK on 20/03/2015.
 */
public class TravelBaseActivity extends BaseActivity {

    protected static final String CLS_TAG = TravelBaseActivity.class.getSimpleName();

    protected static final String TRAVEL_CUSTOM_VIEW_FRAGMENT_TAG = "travel.custom.view";
    //Contains the list of travel custom fields.
    public List<TravelCustomField> formFields;
    // Contains the Cliqbook trip id if a hotel search is being performed in the
    // context of a trip.
    protected String cliqbookTripId;
    // Contains a reference to a fragment used to display travel custom fields.
    protected TravelCustomFieldsFragment travelCustomFieldsFragment;

    protected TravelCustomFieldsConfig travelCustomFieldsConfig;

    protected boolean update = false;
    // Contains the last list of invalid fields.
    protected List<TravelCustomFieldsFragment.TravelCustomFieldHint> invalidFields;

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
        }

        return dlg;
    }

    /**
     * Will validate the set of travel custom fields. If the fields are found to be invalid, then this method will display a
     * dialog.
     *
     * @return returns whether the custom fields had invalid values.
     */
    protected boolean validateTravelCustomFields() {
        boolean retVal = true;
        if (travelCustomFieldsFragment != null) {
            List<TravelCustomFieldsFragment.TravelCustomFieldHint> invalidFields = travelCustomFieldsFragment
                    .findInvalidFieldValues();
            if (invalidFields != null && invalidFields.size() > 0) {
                // Set the field reference.
                this.invalidFields = invalidFields;
                showInvalidCustomFieldsDialog();
                retVal = false;
            }
        }
        return retVal;
    }

    protected void showInvalidCustomFieldsDialog() {
        Dialog dlg;
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setTitle(getText(R.string.dlg_travel_booking_info_title).toString());
        dlgBldr.setCancelable(true);
        dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                invalidFields = null;
            }
        });
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.travel_custom_field_invalid, null);

        TableLayout tblLayout = (TableLayout) view.findViewById(R.id.field_list_table);
        if (tblLayout != null) {
            // First, clear out all rows in 'tblLayout' except for the header row.
            for (int rowInd = tblLayout.getChildCount() - 1; rowInd >= 0; --rowInd) {
                View tblRowView = tblLayout.getChildAt(rowInd);
                if (tblRowView.getId() != R.id.field_list_table_header) {
                    tblLayout.removeViewAt(rowInd);
                }
            }
            // Iterate over the list of invalid fields and inflate one instance of
            // 'travel_invalid_custom_field_row' view per field.
            if (invalidFields != null && invalidFields.size() > 0) {
                //LayoutInflater inflater1 = LayoutInflater.from(this);
                for (TravelCustomFieldsFragment.TravelCustomFieldHint tcfh : invalidFields) {

                    TableRow tblRow = (TableRow) inflater.inflate(R.layout.travel_custom_field_invalid_row, null);
                    // Set the field name.
                    TextView txtView = (TextView) tblRow.findViewById(R.id.field_row_name);
                    if (txtView != null) {
                        txtView.setText(tcfh.fieldName);
                    } else {
                        //Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing 'field_row_name' text view!");
                    }
                    // Set the field hint.
                    txtView = (TextView) tblRow.findViewById(R.id.field_row_hint);
                    if (txtView != null) {
                        txtView.setText(tcfh.hintText);
                    } else {
                        //Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing 'field_row_hint' text view!");
                    }
                    // Add it to the table layout.
                    tblLayout.addView(tblRow);
                }
            }
        }

        dlgBldr.setView(view);
        dlg = dlgBldr.create();
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                invalidFields = null;
            }
        });
        dlg.show();
    }

}
