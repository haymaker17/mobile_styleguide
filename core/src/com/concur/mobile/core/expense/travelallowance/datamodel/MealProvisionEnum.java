package com.concur.mobile.core.expense.travelallowance.datamodel;

import android.content.Context;

import com.concur.core.R;

/**
 * Created by D028778 on 27-Jun-15.
 */
public enum MealProvisionEnum {

    PROVIDED("PRO", R.string.general_yes),
    NOT_PROVIDED("NPR", R.string.general_no);

    MealProvisionEnum(String code, int resourceId) {
        this.code = code;
        this.resourceId = resourceId;
    }

    public String getCode() {
        return code;
    }

    public int getResourceId() {
        return resourceId;
    }

    public static MealProvisionEnum fromCode(final String code) {
        // Default is provided because due to config changes there could be a different value than PRO or NPR. Here the behavior
        // is the same as in the browser app and iPhone app
        MealProvisionEnum result = PROVIDED;
        for (MealProvisionEnum mealProvisionEnum : values()) {
            if (mealProvisionEnum.code.equals(code)) {
                result = mealProvisionEnum;
                break;
            }
        }
        return result;
    }

    public static MealProvision fromCode(final String code, final Context context) {
        if (context != null) {
            MealProvisionEnum mealProvisionEnum = fromCode(code);
            if (mealProvisionEnum != null) {
                MealProvision mealProvision = new MealProvision(code,
                        context.getString(mealProvisionEnum.getResourceId()));
                return mealProvision;
            }
        }
        return null;
    }

    private String code;
    private int resourceId;
}
