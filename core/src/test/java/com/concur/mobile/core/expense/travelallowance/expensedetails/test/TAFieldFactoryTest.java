package com.concur.mobile.core.expense.travelallowance.expensedetails.test;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import com.concur.core.BuildConfig;
import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.activity.ExpenseEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.travelallowance.activity.RoboDummyActivity;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.expensedetails.TAFieldFactory;


import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowContext;
import org.robolectric.shadows.ShadowContextWrapper;

import java.util.List;

import testconfig.ActivityDouble;
import testconfig.RoboTestRunner;
import testconfig.StringConstants;

/**
 * Created by D049515 on 16.07.2015.
 */
@Config(constants = BuildConfig.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(RoboTestRunner.class)
public class TAFieldFactoryTest extends TestCase {





    private TAFieldFactory factory;

    private ExpenseReportEntryDetail expRepEntryDetail;


    @Test
    @Config(shadows = {ActivityDouble.class})
    public void testNoDailyAllowance() {

        expRepEntryDetail = new ExpenseReportEntryDetail();
        expRepEntryDetail.taDayKey = "1";
        expRepEntryDetail.expKey = "FXMLS";

        factory = new TAFieldFactory(new Activity(), expRepEntryDetail, new FixedTravelAllowanceController(new Activity()));

        List<ExpenseReportFormField> fieldList = factory.getFormFields();
        assertEquals(1, fieldList.size());

        ExpenseReportFormField field = fieldList.get(0);
        assertNull(field.getLabel());
        assertEquals(StringConstants.NO_ADJUSTMEMTS, field.getValue());
    }

}
