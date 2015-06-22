package com.concur.mobile.core.expense.travelallowance.activity;

import android.os.Bundle;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;

public class FixedTravelAllowanceDetailsActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_NAME_TAG = FixedTravelAllowanceDetailsActivity.class
            .getSimpleName();

    public static final String INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE =
            FixedTravelAllowanceDetailsActivity.class.getName() + "FixedTravelAllowance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fixed_travel_allowance_details_activity);
    }

}
