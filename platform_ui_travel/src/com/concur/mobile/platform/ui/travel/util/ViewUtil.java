package com.concur.mobile.platform.ui.travel.util;

import com.concur.mobile.platform.travel.search.hotel.HotelPreference;
import com.concur.mobile.platform.travel.search.hotel.HotelRecommended;
import com.concur.mobile.platform.travel.search.hotel.RuleEnforcementLevel;
import com.concur.mobile.platform.ui.travel.R;

/**
 * Utility class for the travel related views
 * 
 * @author RatanK
 * 
 */
public class ViewUtil {

    public static final String CLS_TAG = ViewUtil.class.getSimpleName();

    /**
     * Get the hotel company preferred localized string id
     * 
     * @param hotelPreference
     * @return
     */
    public static int getHotelCompanyPreferredTextId(HotelPreference hotelPreference) {
        if (hotelPreference != null && hotelPreference.companyPreference != null) {
            if (hotelPreference.companyPreference.equalsIgnoreCase("LessPreferred")) {
                return R.string.hotel_less_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("Preferred")) {
                return R.string.hotel_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("MostPreferred")) {
                return R.string.hotel_most_preferred;
            }
        }
        return -1;
    }

    /**
     * Get the hotel suggestion localized string id
     * 
     * @param hotelRecommended
     * @return
     */
    public static int getHotelSuggestionTextId(HotelRecommended hotelRecommended) {
        if (hotelRecommended != null && hotelRecommended.getSuggestedCategory() != null) {
            if (hotelRecommended.category.equalsIgnoreCase("PersonalHistory")) {
                return R.string.hotel_suggestion_personal_history;
            } else if (hotelRecommended.category.equalsIgnoreCase("CompanyFavorite")) {
                return R.string.hotel_suggestion_company_favorite;
            } else if (hotelRecommended.category.equalsIgnoreCase("Algorithm")) {
                return R.string.hotel_suggestion_algorithm;
            }
        }
        return -1;

    }

    /**
     * Will determine the mapping from an integer-based enforcement level to an instance of <code>RuleEnforcementLevel</code>.
     * 
     * @param level
     *            the enforcement level from a violation.
     * @return an instance of <code>RuleEnforcementLevel</code>.
     */
    public static RuleEnforcementLevel getRuleEnforcementLevel(Integer level) {
        RuleEnforcementLevel ruleLevel = RuleEnforcementLevel.NONE;
        if (level != null) {
            if (level < 10 || level == 100) {
                ruleLevel = RuleEnforcementLevel.NONE;
            } else if (level == 10 || level == 20) {
                ruleLevel = RuleEnforcementLevel.WARNING;
            } else if (level == 25 || level == 30) {
                ruleLevel = RuleEnforcementLevel.ERROR;
            } else if (level == 40) {
                ruleLevel = RuleEnforcementLevel.INACTIVE;
            } else if (level == 50) {
                ruleLevel = RuleEnforcementLevel.HIDE;
            }
        }
        return ruleLevel;
    }

    /**
     * MOB-15911 - Show GDSName in travel search results - only for DEV & QA
     * 
     * @param context
     * @param textView
     * @param gdsName
     */
    // public static void showGDSName(Context context, TextView textView, String gdsName) {
    // Activity activity = (Activity) context;
    // // UserConfig uc = (activity.getApplication()).getUserConfig();
    // if (uc.showGDSNameInSearchResults && gdsName != null) {
    // StringBuilder sb = new StringBuilder();
    // String name = sb.append("(").append(gdsName).append(")").toString();
    // textView.setText(name);
    // textView.setVisibility(View.VISIBLE);
    // } else {
    // textView.setVisibility(View.GONE);
    // }

    // }

}
