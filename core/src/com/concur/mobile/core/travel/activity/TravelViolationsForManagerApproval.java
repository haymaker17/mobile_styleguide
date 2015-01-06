package com.concur.mobile.core.travel.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.LayoutUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.SpinnerItem;

public class TravelViolationsForManagerApproval extends TravelBaseActivity {

    private List<Violation> violations;
    private boolean continueBtnClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConcurCore core = (ConcurCore) ConcurCore.getContext();

        // set the violations as needed in the super class on recreation of the activity, need to be before the call to initValues
        violations = core.getTravelPolicyViolations();

        super.initValues(savedInstanceState);

        setContentView(R.layout.travel_violations_for_manager_approval);

        // The title
        getSupportActionBar().setTitle(R.string.travel_points_violations_header);

        // The header text
        int contentResId = R.string.travel_points_p2b_explanation_desc;// default
        Intent activityStarted = getIntent();
        String violationType = activityStarted.getStringExtra(EXTRA_VIOLATION_TYPE_KEY);
        if (violationType.equals("A")) {
            contentResId = R.string.travel_violations_air_for_manager_approval_header;
        } else if (violationType.equals("H")) {
            contentResId = R.string.travel_violations_hotel_for_manager_approval_header;
        }
        TextView headerView = (TextView) findViewById(R.id.travel_violations_for_manager_approval_header);
        if (headerView != null) {
            headerView.setText(contentResId);
        }

        // if not orientation change then get it from the intent that started this activity
        if (savedInstanceState == null) {
            Intent thisIntent = getIntent();
            if (thisIntent.hasExtra(EXTRA_REASON_CODE_SELECTED_KEY)) {
                reasonCode = (SpinnerItem) thisIntent.getSerializableExtra(EXTRA_REASON_CODE_SELECTED_KEY);
            }
            if (thisIntent.hasExtra(EXTRA_JUSTIFICATION_TEXT_KEY)) {
                justificationText = thisIntent.getStringExtra(EXTRA_JUSTIFICATION_TEXT_KEY);
            }
        }

        // The violations
        initViolations();

        // Set the title on the footer button.
        Button button = (Button) findViewById(R.id.footer_button_one);
        if (button != null) {
            button.setText(R.string.travel_points_continue);
            button.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    onContinue(v);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'footer_button_one' button!");
        }
    }

    private void initViolations() {
        View violationsView = findViewById(R.id.violation_reason_justification);
        if (!violationsView.isShown()) {
            violationsView.setVisibility(View.VISIBLE);
        }

        // Construct the violation message click listener.
        OnClickListener violationClickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof Violation) {
                    Violation violation = (Violation) v.getTag();
                    selectedViolationEnforcementLevel = violation.enforcementLevel;
                    selectedViolationEnforcementText = violation.message;
                    showDialog(Const.DIALOG_TRAVEL_VIOLATION_VIEW_MESSAGE);
                }
            }
        };

        LayoutUtil.layoutViolations(TravelViolationsForManagerApproval.this, violations, reasonCodeChoices, reasonCode,
                violationClickListener, justificationText, getTravelViolationJustificationCustomText());
    }

    @Override
    protected List<Violation> getViolations() {
        return violations;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // if not from 'Continue' then let the started activity know
        if (!continueBtnClicked) {
            setResult(RESULT_CANCELED);
        }
    }

    public void onContinue(View view) {
        // check the violation reason is selected
        int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(getViolations());
        RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(enforcementLevel);
        if ((ruleEnfLevel == RuleEnforcementLevel.ERROR || ruleEnfLevel == RuleEnforcementLevel.WARNING)
                && (reasonCodeChoices == null || reasonCodeChoices.length == 0)) {
            // Reason selection required but no reasons!
            showDialog(Const.DIALOG_TRAVEL_VIOLATION_NO_REASONS);
        } else if (isViolationRequiredFieldsNeeded()) {
            showDialog(REQUIRED_FIELDS_DIALOG);
        } else {

            continueBtnClicked = true;

            Intent data = new Intent();
            data.putExtra(EXTRA_REASON_CODE_SELECTED_KEY, reasonCode);
            data.putExtra(EXTRA_JUSTIFICATION_TEXT_KEY, justificationText);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
