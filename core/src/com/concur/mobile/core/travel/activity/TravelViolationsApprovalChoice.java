package com.concur.mobile.core.travel.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.LayoutUtil;

/**
 * Activity displaying the type of approval user can make for the policy violations.
 * 
 * @author RatanK
 * 
 */
public class TravelViolationsApprovalChoice extends TravelBaseActivity {

    private String violationType;
    private int travelPointsToUse;
    private String travelPointsInBank;
    private boolean useTravelPoints;
    private boolean continueBtnClicked;
    private boolean updatedUseTravelPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_violations_approval_choice);

        // The title
        getSupportActionBar().setTitle(R.string.travel_points_violations_desc_header);

        final Intent thisIntent = getIntent();
        // get the violation type to determine the localization data to be shown
        violationType = thisIntent.getStringExtra(EXTRA_VIOLATION_TYPE_KEY);
        travelPointsToUse = thisIntent.getIntExtra(EXTRA_TRAVEL_POINTS_TO_USE_KEY, 0);
        travelPointsInBank = thisIntent.getStringExtra(EXTRA_TRAVEL_POINTS_IN_BANK_KEY);

        useTravelPoints = thisIntent.getBooleanExtra(EXTRA_USE_TRAVEL_POINTS_SELECTED_KEY, false);

        TextView contentView = (TextView) findViewById(R.id.travel_points_bank_field);
        if (contentView != null) {

            contentView.setText(Format.localizeText(ConcurCore.getContext(),
                    R.string.travel_points_hotel_booking_workflow_points_header, new Object[] { travelPointsInBank }));
            contentView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Intent travelPointsExpActivity = new Intent(TravelViolationsApprovalChoice.this,
                            TravelPointsExplanation.class);
                    travelPointsExpActivity.putExtra(EXTRA_TITLE_RESOURCE_ID_KEY,
                            R.string.travel_points_using_points_explanation_title);

                    if (violationType.equals("A")) {
                        travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                R.string.travel_points_using_points_explanation_for_air);
                    } else if (violationType.equals("H")) {
                        travelPointsExpActivity.putExtra(EXTRA_CONTENT_RESOURCE_ID_KEY,
                                R.string.travel_points_using_points_explanation_for_hotel);
                    }

                    startActivity(travelPointsExpActivity);
                }
            });
        }

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.violation_fix_group);

        RadioButton useTPBtn = (RadioButton) radioGroup.findViewById(R.id.use_travel_points_select);
        String useTravelPointsText = Format.localizeText(ConcurCore.getContext(),
                R.string.travel_points_use_for_booking, new Object[] { travelPointsToUse });
        useTPBtn.setText(useTravelPointsText);

        // check the specific radio button
        if (useTravelPoints) {
            useTPBtn.setChecked(true);
        } else {
            RadioButton mgrApprovalButton = (RadioButton) radioGroup.findViewById(R.id.use_manager_approval_select);
            mgrApprovalButton.setChecked(true);
        }

        // show the list of violations
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        List<Violation> violations = core.getTravelPolicyViolations();
        LayoutUtil.addViolationMessages(this, violations);

        // Set the title on the footer button.
        Button button = (Button) findViewById(R.id.footer_button_one);
        if (button != null) {
            button.setText(R.string.travel_points_continue);
            button.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    RadioButton rb = (RadioButton) radioGroup.findViewById(R.id.use_manager_approval_select);
                    if (rb.isChecked()) {
                        Intent violationsForManagerApprovalActivity = new Intent(TravelViolationsApprovalChoice.this,
                                TravelViolationsForManagerApproval.class);

                        violationsForManagerApprovalActivity.putExtra(EXTRA_VIOLATION_TYPE_KEY, violationType);

                        // pass on the reason code and justification text if available to the activity
                        if (thisIntent.hasExtra(EXTRA_REASON_CODE_SELECTED_KEY)) {
                            violationsForManagerApprovalActivity.putExtra(EXTRA_REASON_CODE_SELECTED_KEY,
                                    thisIntent.getSerializableExtra(EXTRA_REASON_CODE_SELECTED_KEY));
                        }
                        if (thisIntent.hasExtra(EXTRA_JUSTIFICATION_TEXT_KEY)) {
                            violationsForManagerApprovalActivity.putExtra(EXTRA_JUSTIFICATION_TEXT_KEY,
                                    thisIntent.getStringExtra(EXTRA_JUSTIFICATION_TEXT_KEY));
                        }

                        startActivityForResult(violationsForManagerApprovalActivity,
                                Const.REQUEST_CODE_USE_MANAGER_APPROVAL);
                    } else {
                        continueBtnClicked = true;
                        Intent data = new Intent();
                        data.putExtra(EXTRA_USE_TRAVEL_POINTS_SELECTED_KEY, true);
                        setResult(RESULT_OK, data);
                        updatedUseTravelPoints = true;
                        finish();
                    }
                    // GA & Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    if (violationType == "H") {
                        params.put(Flurry.PARAM_NAME_TYPE, Flurry.EVENT_NAME_HOTEL);
                    } else {
                        params.put(Flurry.PARAM_NAME_TYPE, Flurry.EVENT_NAME_AIR);
                    }
                    params.put(Flurry.PARAM_NAME_TRAVEL_POINTS_TO_USE, String.valueOf(travelPointsToUse));
                    params.put(Flurry.PARAM_NAME_TRAVEL_POINTS_IN_BANK, String.valueOf(travelPointsInBank));
                    if (updatedUseTravelPoints) {
                        params.put(Flurry.PARAM_NAME_USE_TRAVEL_POINTS, Flurry.PARAM_VALUE_YES);
                    } else {
                        params.put(Flurry.PARAM_NAME_USE_TRAVEL_POINTS, Flurry.PARAM_VALUE_NO);
                    }

                    if (updatedUseTravelPoints != useTravelPoints) {
                        if (updatedUseTravelPoints) {
                            params.put(Flurry.PARAM_NAME_CHANGED_OPTIONS, Flurry.PARAM_VALUE_USE);
                        } else {
                            params.put(Flurry.PARAM_NAME_CHANGED_OPTIONS, Flurry.PARAM_VALUE_MANAGER_APPROVAL);
                        }
                    }

                    EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                            Flurry.EVENT_NAME_VIEWED_MANAGE_VIOLATIONS, params);

                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'footer_button_one' button!");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // if not from 'Continue' then let the started activity know
        if (!continueBtnClicked) {
            setResult(RESULT_CANCELED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode) {
        case RESULT_CANCELED:
            // the started activity was cancelled
            break;
        case RESULT_OK:
            data.putExtra(EXTRA_USE_TRAVEL_POINTS_SELECTED_KEY, false);
            setResult(RESULT_OK, data);
            finish();
            break;
        }
    }
}
