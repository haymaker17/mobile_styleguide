package com.concur.mobile.platform.ui.travel.util.test;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.HotelPreference;
import com.concur.mobile.platform.travel.search.hotel.HotelRecommended;
import com.concur.mobile.platform.travel.search.hotel.HotelViolation;
import com.concur.mobile.platform.travel.search.hotel.RuleEnforcementLevel;
import com.concur.mobile.platform.ui.travel.BuildConfig;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ratank
 */
@Config(constants = BuildConfig.class, manifest = "src/main/AndroidManifest.xml")
@RunWith(RobolectricGradleTestRunner.class)
public class ViewUtilTest {

    @Test
    public void testGetHotelCompanyPreferredTextId_hotelPreferenceNull() {
        Assert.assertEquals(-1, ViewUtil.getHotelCompanyPreferredTextId(null));
    }

    @Test
    public void testGetHotelCompanyPreferredTextId_propertyMostPreferred() {
        HotelPreference hotelPreference = new HotelPreference();
        hotelPreference.companyPreference = "PropertyMostPreferred";
        Assert.assertEquals(R.string.hotel_property_most_preferred,
                ViewUtil.getHotelCompanyPreferredTextId(hotelPreference));
    }

    @Test
    public void testGetHotelSuggestionTextId_hotelRecommendedNull() {
        Assert.assertEquals(-1, ViewUtil.getHotelSuggestionTextId(null));
    }

    @Test
    public void testGetHotelSuggestionTextId_companyFavorite() {
        HotelRecommended hotelRecommended = new HotelRecommended();
        hotelRecommended.category = "CompanyFavorite";
        hotelRecommended.totalScore = 2.0;// some value
        Assert.assertEquals(R.string.hotel_suggestion_company_favorite,
                ViewUtil.getHotelSuggestionTextId(hotelRecommended));
    }

    @Test
    public void testGetRuleEnforcementLevelAsString() {
        // test null - default returns NONE
        Assert.assertEquals("NONE", ViewUtil.getRuleEnforcementLevelAsString(null));

        //test NONE - less than 0 or 100
        Assert.assertEquals("NONE", ViewUtil.getRuleEnforcementLevelAsString(9));
        Assert.assertEquals("NONE", ViewUtil.getRuleEnforcementLevelAsString(100));

        // test WARNING - 10 or 20
        Assert.assertEquals("WARNING", ViewUtil.getRuleEnforcementLevelAsString(10));
        Assert.assertEquals("WARNING", ViewUtil.getRuleEnforcementLevelAsString(20));

        // test ERROR - 25 or 30
        Assert.assertEquals("ERROR", ViewUtil.getRuleEnforcementLevelAsString(25));
        Assert.assertEquals("ERROR", ViewUtil.getRuleEnforcementLevelAsString(30));

        // test INACTIVE - 40
        Assert.assertEquals("INACTIVE", ViewUtil.getRuleEnforcementLevelAsString(40));

        // test HIDE - 50
        Assert.assertEquals("HIDE", ViewUtil.getRuleEnforcementLevelAsString(50));
    }

    @Test
    public void testGetRuleEnforcementLevel() {
        // test NONE - less than 0 or 100
        RuleEnforcementLevel ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(9);
        Assert.assertEquals(RuleEnforcementLevel.NONE, ruleEnforcementLevel);
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(100);
        Assert.assertEquals(RuleEnforcementLevel.NONE, ruleEnforcementLevel);

        // test WARNING - 10 or 20
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(10);
        Assert.assertEquals(RuleEnforcementLevel.WARNING, ruleEnforcementLevel);
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(20);
        Assert.assertEquals(RuleEnforcementLevel.WARNING, ruleEnforcementLevel);

        // test ERROR - 25 or 30
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(25);
        Assert.assertEquals(RuleEnforcementLevel.ERROR, ruleEnforcementLevel);
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(30);
        Assert.assertEquals(RuleEnforcementLevel.ERROR, ruleEnforcementLevel);

        // test INACTIVE - 40
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(40);
        Assert.assertEquals(RuleEnforcementLevel.INACTIVE, ruleEnforcementLevel);

        // test HIDE - 50
        ruleEnforcementLevel = ViewUtil.getRuleEnforcementLevel(50);
        Assert.assertEquals(RuleEnforcementLevel.HIDE, ruleEnforcementLevel);

    }

    @Test
    public void testShowGDSName_nameMismatch() {
        // for some reasons this is giving null hence, for time being, will use RuntimeEnvironment.application
        //Context context = PlatformUITravelTestApplication.getApplication();
        Context context = RuntimeEnvironment.application;
        TextView txtView = new TextView(context);
        ViewUtil.showGDSName(context, txtView, "Sabre");
        Assert.assertEquals("GDS Name set in TextView is in-correct", "(Sabre)", txtView.getText());
    }

    @Test
    public void testStripUnderlines() {
        Context context = RuntimeEnvironment.application;
        TextView txtView = new TextView(context);
        txtView.setText(PhoneNumberUtils.formatNumber("040 12345 6789"));
        Linkify.addLinks(txtView, Linkify.PHONE_NUMBERS);
        // ViewUtil.stripUnderlines(txtView);
        // TODO - check that the underline is removed !
    }

    @Test
    public void testGetShowButNoBookingViolation_null() {
        Assert.assertNull(ViewUtil.getShowButNoBookingViolation(null, null, -1));
    }

    @Test
    public void testGetShowButNoBookingViolation() {
        List<HotelViolation> violations = new ArrayList<HotelViolation>();
        HotelViolation violation = new HotelViolation();
        violation.enforcementLevel = "40";
        violations.add(violation);
        Assert.assertEquals("40", ViewUtil.getShowButNoBookingViolation(violations, "40", 40).enforcementLevel);
    }

    @Test
    public void testGetMaxRuleEnforcementViolation() {
        List<HotelViolation> violations = new ArrayList<HotelViolation>();
        HotelViolation violation = new HotelViolation();
        violation.enforcementLevel = "40";
        violations.add(violation);
        Assert.assertEquals("40", ViewUtil.getMaxRuleEnforcementViolation(violations, "40").enforcementLevel);
    }

}
