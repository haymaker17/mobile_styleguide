package com.concur.mobile.core.expense.travelallowance.datamodel;

import android.content.Context;

import com.concur.core.R;

/**
 * Created by D028778 on 27-Jun-15.
 */
public enum MealProvisionEnum {

    PROVIDED("PRO", R.string.itin_meal_PRO),
    NOT_PROVIDED("NPR", R.string.itin_meal_not_provided);

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
        for (MealProvisionEnum mealProvisionEnum : values()) {
            if (mealProvisionEnum.code.equals(code)) {
                return mealProvisionEnum;
            }
        }
        return null;
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
