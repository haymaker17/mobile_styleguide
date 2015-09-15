package com.concur.mobile.platform.ui.travel.util.test;

import android.content.Context;
import android.view.ViewGroup;

import com.concur.mobile.base.util.Format;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.ui.travel.BuildConfig;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomField;
import com.concur.mobile.platform.ui.travel.util.FormUtil;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by RatanK on 25/07/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, manifest = "src/test/AndroidManifest.xml", sdk = 21)
public class FormUtilTest {

    @Test
    public void testGetHintTextNullCheck() {
        // for some reasons this is giving null hence, for time being, will use RuntimeEnvironment.application
        //Context context = PlatformUITravelTestApplication.getApplication();

        Context context = RuntimeEnvironment.application;
        Assert.assertNull(FormUtil.getHintText(null, context));
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetHintText_MinLengthForInteger() {
        // for some reasons this is giving null hence, for time being, will use RuntimeEnvironment.application

        //Context context = PlatformUITravelTestApplication.getApplication();

        Context context = RuntimeEnvironment.application;
        TravelCustomField tcf = new TravelCustomField();
        tcf.setControlType(IFormField.ControlType.EDIT);
        tcf.setDataType(IFormField.DataType.INTEGER);
        tcf.setMinLength(5);
        String expectedStr = Format
                .localizeText(context, R.string.general_enter_at_least_n_digits_hint, tcf.getMinLength());
        Assert.assertEquals(expectedStr, FormUtil.getHintText(tcf, context));
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetHintText_MinAndMaxLengthForChar() {
        // for some reasons this is giving null hence, for time being, will use RuntimeEnvironment.application
        //Context context = PlatformUITravelTestApplication.getApplication();

        Context context = RuntimeEnvironment.application;
        TravelCustomField tcf = new TravelCustomField();
        tcf.setControlType(IFormField.ControlType.EDIT);
        tcf.setDataType(IFormField.DataType.VARCHAR);
        tcf.setMinLength(5);
        tcf.setMaxLength(25);
        String expectedStr = Format
                .localizeText(context, R.string.general_enter_n_to_m_characters_hint, tcf.getMinLength(),
                        tcf.getMaxLength());
        Assert.assertEquals(expectedStr, FormUtil.getHintText(tcf, context));
    }

    @Test
    @Config(qualifiers = "en")
    public void testGetHintText_MaxLengthForChar() {
        Context context = RuntimeEnvironment.application;
        TravelCustomField tcf = new TravelCustomField();
        tcf.setControlType(IFormField.ControlType.EDIT);
        tcf.setDataType(IFormField.DataType.VARCHAR);
        tcf.setMaxLength(5);
        String expectedStr = Format
                .localizeText(context, R.string.general_enter_up_to_n_characters_hint, tcf.getMaxLength());
        Assert.assertEquals(expectedStr, FormUtil.getHintText(tcf, context));
    }

    @Test
    public void testPopulateViewWithFormFields_nullViewGroup() {
        Context context = RuntimeEnvironment.application;
        ViewGroup viewGroup = null;
        Assert.assertEquals(0, FormUtil.populateViewWithFormFields(context, viewGroup, null, null, null, null).size());
    }

//    @Test
//    public void testBuildFormFieldView_staticPickList_layout() {
//        TravelCustomField tcf = new TravelCustomField();
//        tcf.setControlType(IFormField.ControlType.PICK_LIST);
//        SpinnerItem[] staticList = new SpinnerItem[2];
//        staticList[0] = new SpinnerItem("1", "option 1");
//        staticList[1] = new SpinnerItem("2", "option 2");
//        tcf.setStaticList(staticList);
//        tcf.setAccessType(IFormField.AccessType.RO);
//        tcf.setInputType(IFormField.InputType.USER);
//        // FormFieldView staticListView = FormUtil.buildFormFieldView(tcf, null, null);
//        Assert.assertEquals(R.layout.travel_custom_form_field, FormUtil.buildFormFieldView(tcf, null, null).layoutResourceId);
//    }

//    @Test
//    public void testBuildFormFieldView_staticPickList_currentValue() {
//        TravelCustomField tcf = new TravelCustomField();
//        tcf.setControlType(IFormField.ControlType.PICK_LIST);
//        SpinnerItem[] staticList = new SpinnerItem[2];
//        staticList[0] = new SpinnerItem("1", "option 1");
//        staticList[1] = new SpinnerItem("2", "option 2");
//        tcf.setStaticList(staticList);
//        tcf.setLiKey("2");
//        tcf.setAccessType(IFormField.AccessType.RO);
//        tcf.setInputType(IFormField.InputType.USER);
//        StaticPickListFormFieldView staticView = (StaticPickListFormFieldView) FormUtil
//                .buildFormFieldView(tcf, null, null);
//        Assert.assertEquals("2", staticView.getCurrentValue());
//    }

}
