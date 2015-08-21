/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.corp.test;

import android.app.Activity;
import android.util.Log;

import com.concur.mobile.base.util.Const;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.charge.activity.ExpenseItDetailActivity;
import com.concur.mobile.core.expense.charge.activity.ExpenseItReceiptView;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpenseItUITestSuite extends CorporateTestSuite {

    private static String CLS_TAG = ExpenseItUITestSuite.class.getSimpleName();

    @Test
    public void testExpenseItActivitiesLifecycle() {
        if (!testActivities) {
            return;
        }
        List<Class<? extends Activity>> failedActivities = new ArrayList<>();
        List<Class<? extends Activity>> successfulLaunchActivities = new ArrayList<>();
        List<Class<? extends BaseActivity>> activities = Arrays.asList(
            ExpenseItDetailActivity.class
            , ExpenseItReceiptView.class
            , ExpenseItDetailActivity.class
            , ExpenseItReceiptView.class
        );


        for (Class<? extends Activity> item : activities) {
            try {
                testActivity(item);
                successfulLaunchActivities.add(item);
            } catch (Exception e) {
                failedActivities.add(item);
            }
        }
        Log.v(Const.LOG_TAG, CLS_TAG + "================================================================");
        Log.v(Const.LOG_TAG, CLS_TAG + " Successful count: [" + successfulLaunchActivities.size() + "]");

        for (Class<? extends Activity> item : successfulLaunchActivities) {
            Log.v(Const.LOG_TAG, CLS_TAG + " Successful Activity: ===" + item.getSimpleName() + "===");
        }

        Log.v(Const.LOG_TAG, CLS_TAG + " Failed count: [" + failedActivities.size() + "]");

        for (Class<? extends Activity> item : failedActivities) {
            Log.v(Const.LOG_TAG, CLS_TAG + " Failed Activity: ###" + item.getSimpleName() + "###");
        }
    }
}
