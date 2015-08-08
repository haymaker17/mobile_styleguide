package com.concur.mobile.platform.ui.travel.util;

import android.content.Context;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.concur.mobile.platform.travel.search.hotel.HotelPreference;
import com.concur.mobile.platform.travel.search.hotel.HotelRecommended;
import com.concur.mobile.platform.travel.search.hotel.HotelViolation;
import com.concur.mobile.platform.travel.search.hotel.RuleEnforcementLevel;
import com.concur.mobile.platform.ui.travel.R;

import java.util.List;

/**
 * Utility class for the travel related views
 *
 * @author RatanK
 */
public class ViewUtil {

    public static final String CLS_TAG = ViewUtil.class.getSimpleName();
    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    /**
     * Get the hotel company preferred localized string id
     *
     * @param hotelPreference
     * @return
     */
    public static int getHotelCompanyPreferredTextId(HotelPreference hotelPreference) {
        if (hotelPreference != null && hotelPreference.companyPreference != null) {
            if (hotelPreference.companyPreference.equalsIgnoreCase("ChainLessPreferred")) {
                return R.string.hotel_chain_less_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("ChainPreferred")) {
                return R.string.hotel_chain_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("ChainMostPreferred")) {
                return R.string.hotel_chain_most_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("PropertyLessPreferred")) {
                return R.string.hotel_property_less_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("PropertyPreferred")) {
                return R.string.hotel_property_preferred;
            } else if (hotelPreference.companyPreference.equalsIgnoreCase("PropertyMostPreferred")) {
                return R.string.hotel_property_most_preferred;
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
     * @param level the enforcement level from a violation.
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
     * Will determine the mapping from an integer-based enforcement level to a String
     *
     * @param level the enforcement level from a violation.
     * @return
     */
    public static String getRuleEnforcementLevelAsString(Integer level) {
        String ruleLevel = "NONE";
        if (level != null) {
            if (level < 10 || level == 100) {
                ruleLevel = "NONE";
            } else if (level == 10 || level == 20) {
                ruleLevel = "WARNING";
            } else if (level == 25 || level == 30) {
                ruleLevel = "ERROR";
            } else if (level == 40) {
                ruleLevel = "INACTIVE";
            } else if (level == 50) {
                ruleLevel = "HIDE";
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
    public static void showGDSName(Context context, TextView textView, String gdsName) {
        if (gdsName != null) {
            StringBuilder sb = new StringBuilder();
            String name = sb.append("(").append(gdsName).append(")").toString();
            textView.setText(name);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }

    }

    /**
     * method to strip the underline from Linkify text
     */
    public static void stripUnderlines(TextView tv) {

        CharSequence text = tv.getText();
        if (text != null && text.length() > 0) {
            try {
                Spannable s = (Spannable) text;
                URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);

                for (URLSpan span : spans) {
                    int start = s.getSpanStart(span);
                    int end = s.getSpanEnd(span);
                    s.removeSpan(span);
                    span = new URLSpanNoUnderline(span.getURL());
                    s.setSpan(span, start, end, 0);

                }

                tv.setText(s);

            } catch (java.lang.ClassCastException e) {
                e.printStackTrace();
                Linkify.addLinks(tv, Linkify.PHONE_NUMBERS);

            }
        }
    }

    /**
     * Gets the 'show but no booking' maxenforcement violation from the passed in maxenforcementlevel
     */

    public static HotelViolation getShowButNoBookingViolation(List<HotelViolation> violations,
            String maxEnforcementLevel, int maxEnforcelimit) {

        HotelViolation maxEnforcementViolation = getMaxRuleEnforcementViolation(violations, maxEnforcementLevel);
        if ((maxEnforcementViolation != null
                && getRuleEnforcementLevel(maxEnforcelimit) == RuleEnforcementLevel.INACTIVE)) {
            return maxEnforcementViolation;
        }
        return null;
    }

    /**
     * Gets the Violation with the enforcement level matched to the passed in MaxEnforcementLevel
     */
    public static HotelViolation getMaxRuleEnforcementViolation(List<HotelViolation> violations,
            String maxEnforcementLevel) {
        HotelViolation maxEnforcementViolation = null;
        if (violations != null && maxEnforcementLevel != null) {
            for (HotelViolation violation : violations) {
                if (maxEnforcementLevel.equals(violation.enforcementLevel)) {
                    maxEnforcementViolation = violation;
                    break;
                }
            }
        }
        return maxEnforcementViolation;
    }

    /**
     * Will determine the mapping from an integer-based enforcement level to an instance of <code>RuleEnforcementLevel</code>.
     *
     * @param level the enforcement level from a violation.
     * @return an instance of <code>RuleEnforcementLevel</code>.
     */
    public static RuleEnforcementLevel getRuleEnforcementLevel(int level) {
        RuleEnforcementLevel ruleLevel = RuleEnforcementLevel.NONE;
        if (level != 0) {
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
     * Will add a separator view to a view group.
     *
     * @param context the context used to inflate the separator view.
     * @param root    the parent of the inflated view.
     */
    public static void addSeparatorView(Context context, ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.group_separator_v1, root);
    }

    /**
     * Will convert dp to pixels for view positions via code instead of xml
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * @param myListView calculate the listView size
     */
    public static void getListViewSize(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }

        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(UNBOUNDED, UNBOUNDED);

            totalHeight += listItem.getMeasuredHeight();
        }
        //setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + 50 + (myListView.getDividerHeight() * (myListAdapter.getCount()));
        myListView.setLayoutParams(params);
        // print height of adapter on log
        Log.i("height of listItem:", String.valueOf(totalHeight));
    }

    /**
     * Verify the scroll option for gridView
     *
     * @param v
     * @param checkV
     * @param dy
     * @param x
     * @param y
     * @return
     */
    public static boolean canScroll(View v, boolean checkV, int dy, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop()
                        && y + scrollY < child.getBottom() && canScroll(child, true, dy, x + scrollX - child.getLeft(),
                        y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }
        return checkV;
    }

}
