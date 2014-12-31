package com.concur.mobile.core.util;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.view.SpinnerItem;

/**
 * A class with a set of static methods used to construct layouts common to various related activites.
 * 
 * @author andy
 */
public class LayoutUtil {

    private static final String CLS_TAG = "LayoutUtil";

    /**
     * Will perform a layout of a violation section within an activities view. The activities layout file should include the
     * layout file 'violations'. Activities passed into this method should be able to handle the dialog creation/preparation for
     * the following dialog ID's: <code>Const.DIALOG_VIOLATION_REASON, Const.DIALOG_VIOLATION_NO_REASONS,
     * Const.DIALOG_VIOLATION_JUSTIFICATION and Const.DIALOG_VIEW_VIOLATION_MESSAGE</code>. Each violation will also have
     * constructed a clickable view with passed in click listener <code>violationClickListener</code> where the constructed view
     * has the violation instance set on it as a <code>tag</code> object. Passed in instances of <code>OnClickListener</code>
     * should call <code>View.getTag</code> to retrieve the object of type <code>Violation</code> from which a custom dialog may
     * be displayed.
     * 
     * @param activity
     *            the activity containing the UI elements.
     * @param violations
     *            the list of violations.
     * @param reasonCodeChoices
     *            the list of <code>SpinnerItem</code> objects reflecting the violation reason code choices.
     * @param violationClickListener
     *            a <code>OnClickListener</code> to handle clicking on a specific violation message.
     */
    public static void layoutViolations(final Activity activity, List<Violation> violations,
            final SpinnerItem[] reasonCodeChoices, SpinnerItem curReasonCode, OnClickListener violationClickListener,
            String curJustificationText) {

        if (violations != null && violations.size() > 0) {
            // First, set up the violation reason selection UI.
            boolean helpfulMessageEnforcementLevel = ViewUtil.isEnforcementLevelForHelpfulMessages(violations);
            boolean showButNoBooking = ViewUtil.showButNoBooking(violations);
            if (helpfulMessageEnforcementLevel || showButNoBooking) {
                // Violations are for informative messages only, so hide the reason/justification UX elements.
                View view = activity.findViewById(R.id.reason_justification);
                if (view != null) {
                    view.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'reason_justification' view!");
                }
            } else {
                // Set the icon on the violation reason popup.
                int fieldIconResId = -1;
                int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
                RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(enforcementLevel);
                switch (ruleEnfLevel) {
                case NONE: {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: rule enforcement level is 'NONE'!");
                    break;
                }
                case ERROR: {
                    fieldIconResId = R.drawable.icon_redex;
                    break;
                }
                case WARNING: {
                    fieldIconResId = R.drawable.icon_yellowex;
                    break;
                }
                case INACTIVE: {
                    // No-op.
                    break;
                }
                case HIDE: {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: rule enforcement level is 'HIDE'!");
                    break;
                }
                }
                View reasonView = activity.findViewById(R.id.violation_reason);
                if (reasonView != null) {
                    // Set the field icon.
                    ImageView imgView = (ImageView) reasonView.findViewById(R.id.field_icon);
                    if (imgView != null) {
                        if (fieldIconResId != -1) {
                            imgView.setImageResource(fieldIconResId);
                        } else {
                            imgView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'field_icon' view!");
                    }
                    // Set the field name.
                    TextView txtView = (TextView) reasonView.findViewById(R.id.field_name);
                    if (txtView != null) {
                        txtView.setText(R.string.general_violation_reason);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'field_name' view!");
                    }

                    // Initialize the view.
                    updateViolationReasonChoiceView(activity, curReasonCode);

                    // Set the click handler.
                    reasonView.setOnClickListener(new OnClickListener() {

                        public void onClick(View v) {
                            if (reasonCodeChoices != null) {
                                activity.showDialog(Const.DIALOG_TRAVEL_VIOLATION_REASON);
                            } else {
                                activity.showDialog(Const.DIALOG_TRAVEL_VIOLATION_NO_REASONS);
                            }
                        }
                    });

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'violation_reason' view!");
                }

                // Set up the violation justification.
                View justificationView = activity.findViewById(R.id.violation_justification);
                if (justificationView != null) {
                    // Set the field name.
                    TextView txtView = (TextView) justificationView.findViewById(R.id.field_name);
                    if (txtView != null) {
                        txtView.setText(R.string.general_violation_justification);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'field_name' view!");
                    }

                    // Set the field icon.
                    ImageView imgView = (ImageView) justificationView.findViewById(R.id.field_icon);
                    if (imgView != null) {
                        if (fieldIconResId != -1) {
                            imgView.setImageResource(fieldIconResId);
                        } else {
                            imgView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".layoutViolations: unable to locate 'field_icon' justificationView!");
                    }

                    // Initialize the view.
                    updateViolationJustificationView(activity, curJustificationText);
                    // Set the click handler.
                    justificationView.setOnClickListener(new OnClickListener() {

                        public void onClick(View v) {
                            activity.showDialog(Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION);
                        }
                    });
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".layoutViolations: unable to locate 'violation_justification' view!");
                }
            }

            addViolationMessages(activity, violations, violationClickListener);

        } else {
            // Hide the entire violations section of the layout.
            View view = activity.findViewById(R.id.violation_reason_justification);
            if (view != null) {
                view.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".layoutViolations: unable to locate 'violation_reason_justification' view!");
            }
        }

    }

    public static void layoutViolationsForTravelPoints(final Activity activity, List<Violation> violations,
            final SpinnerItem[] reasonCodeChoices, SpinnerItem curReasonCode, String curJustificationText) {

        if (violations != null && violations.size() > 0) {
            // First, set up the violation reason selection UI.
            boolean helpfulMessageEnforcementLevel = ViewUtil.isEnforcementLevelForHelpfulMessages(violations);
            boolean showButNoBooking = ViewUtil.showButNoBooking(violations);
            if (helpfulMessageEnforcementLevel || showButNoBooking) {
                // Violations are for informative messages only, so hide the reason/justification UX elements.
                View view = activity.findViewById(R.id.reason_justification);
                if (view != null) {
                    view.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'reason_justification' view!");
                }
            } else {
                // Set the icon on the violation reason popup.
                int fieldIconResId = -1;
                int enforcementLevel = ViewUtil.getMaxRuleEnforcementLevel(violations);
                RuleEnforcementLevel ruleEnfLevel = ViewUtil.getRuleEnforcementLevel(enforcementLevel);
                switch (ruleEnfLevel) {
                case NONE: {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: rule enforcement level is 'NONE'!");
                    break;
                }
                case ERROR: {
                    fieldIconResId = R.drawable.icon_redex;
                    break;
                }
                case WARNING: {
                    fieldIconResId = R.drawable.icon_yellowex;
                    break;
                }
                case INACTIVE: {
                    // No-op.
                    break;
                }
                case HIDE: {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: rule enforcement level is 'HIDE'!");
                    break;
                }
                }
                View reasonView = activity.findViewById(R.id.violation_reason);
                if (reasonView != null) {
                    // Set the field icon.
                    ImageView imgView = (ImageView) reasonView.findViewById(R.id.field_icon);
                    if (imgView != null) {
                        if (fieldIconResId != -1) {
                            imgView.setImageResource(fieldIconResId);
                        } else {
                            imgView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'field_icon' view!");
                    }
                    // Set the field name.
                    TextView txtView = (TextView) reasonView.findViewById(R.id.field_name);
                    if (txtView != null) {
                        txtView.setText(R.string.general_violation_reason);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'field_name' view!");
                    }

                    // Initialize the view.
                    updateViolationReasonChoiceView(activity, curReasonCode);

                    // Set the click handler.
                    reasonView.setOnClickListener(new OnClickListener() {

                        public void onClick(View v) {
                            if (reasonCodeChoices != null) {
                                activity.showDialog(Const.DIALOG_TRAVEL_VIOLATION_REASON);
                            } else {
                                activity.showDialog(Const.DIALOG_TRAVEL_VIOLATION_NO_REASONS);
                            }
                        }
                    });

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'violation_reason' view!");
                }

                // Set up the violation justification.
                View justificationView = activity.findViewById(R.id.violation_justification);
                if (justificationView != null) {
                    // Set the field name.
                    TextView txtView = (TextView) justificationView.findViewById(R.id.field_name);
                    if (txtView != null) {
                        txtView.setText(R.string.general_violation_justification);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'field_name' view!");
                    }

                    // Set the field icon.
                    ImageView imgView = (ImageView) justificationView.findViewById(R.id.field_icon);
                    if (imgView != null) {
                        if (fieldIconResId != -1) {
                            imgView.setImageResource(fieldIconResId);
                        } else {
                            imgView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".layoutViolations: unable to locate 'field_icon' justificationView!");
                    }

                    // Initialize the view.
                    updateViolationJustificationView(activity, curJustificationText);
                    // Set the click handler.
                    justificationView.setOnClickListener(new OnClickListener() {

                        public void onClick(View v) {
                            activity.showDialog(Const.DIALOG_TRAVEL_VIOLATION_JUSTIFICATION);
                        }
                    });
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".layoutViolations: unable to locate 'violation_justification' view!");
                }
            }
        } else {
            // Hide the entire violations section of the layout.
            View view = activity.findViewById(R.id.violation_reason_justification);
            if (view != null) {
                view.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".layoutViolations: unable to locate 'violation_reason_justification' view!");
            }
        }

    }

    public static void addViolationMessages(final Activity activity, List<Violation> violations,
            OnClickListener violationClickListener) {
        // Add the violation messages.
        ViewGroup violationsGroup = (ViewGroup) activity.findViewById(R.id.violation);
        if (violationsGroup != null) {

            // added while addressing Travel Points
            if (violationsGroup.getChildCount() > 0) {
                // violation messages already added
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(activity);
            for (Violation violation : violations) {
                if (violations.indexOf(violation) > 0) {
                    // Add a separator.
                    ViewUtil.addSeparatorView(activity, violationsGroup);
                }
                View ruleRow = inflater.inflate(R.layout.violation_row, null);

                // Set the rule icon.
                ImageView imgView = (ImageView) ruleRow.findViewById(R.id.exception_icon);
                if (imgView != null) {
                    switch (ViewUtil.getRuleEnforcementLevel(violation.enforcementLevel)) {
                    case NONE: {
                        imgView.setImageResource(R.drawable.icon_informational);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case WARNING: {
                        imgView.setImageResource(R.drawable.icon_yellowex);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ERROR: {
                        imgView.setImageResource(R.drawable.icon_redex);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case INACTIVE: {
                        imgView.setImageResource(R.drawable.icon_redex);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case HIDE: {
                        imgView.setVisibility(View.GONE);
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".layoutViolations: airchoice has rule enforcement level of 'hidden'!");
                        break;
                    }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'exception_icon' image view!");
                }
                // Set the rule text.
                TextView txtView = (TextView) ruleRow.findViewById(R.id.exception_text);
                if (txtView != null) {
                    txtView.setText(violation.message);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'exception_text' text view!");
                }
                ruleRow.setFocusable(true);
                ruleRow.setClickable(true);
                ruleRow.setTag(violation);
                ruleRow.setOnClickListener(violationClickListener);
                violationsGroup.addView(ruleRow);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'violation' view group!");
        }
    }

    /**
     * Will update the violation reason view based on the value of <code>curReasonCode</code>.
     */
    public static void updateViolationReasonChoiceView(Activity activity, SpinnerItem reasonCode) {
        View reasonSelectionView = activity.findViewById(R.id.violation_reason);
        if (reasonSelectionView != null) {
            TextView txtView = (TextView) reasonSelectionView.findViewById(R.id.field_value);
            if (reasonCode != null) {
                txtView.setText(reasonCode.name);
            } else {
                txtView.setText(R.string.general_specify_reason);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".updateViolationReasonChoiceView: unable to locate 'violation_reason' view!");
        }
    }

    /**
     * Will update the violation justification view based on the value of <code>justificationText</code>.
     */
    public static void updateViolationJustificationView(Activity activity, String justificationText) {
        View justificationView = activity.findViewById(R.id.violation_justification);
        if (justificationView != null) {
            TextView txtView = (TextView) justificationView.findViewById(R.id.field_value);
            if (justificationText != null) {
                txtView.setText(justificationText);
            } else {
                txtView.setText(R.string.general_specify_justification);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".updateViolationJustificationView: unable to locate 'violation_justification' view!");
        }
    }

    /**
     * Gives a Travel Points header TextView
     * 
     * @param activity
     *            - activity that needs the header text
     * @param priceToBeat
     * @param priceToBeatResId
     *            -
     * @param travelPointsInBank
     * @param travelPointsInBankResId
     * @return - TextView - if header text available or returns null
     */
    public static TextView getTravelPointsHeader(Activity activity, String formattedMinPriceToBeat,
            String formattedMaxPriceToBeat, String travelPointsInBank) {

        TextView travelPointsHdrView = null;
        StringBuilder travelPointsHdrBldr = new StringBuilder();

        boolean hasMinPriceToBeat = false;
        boolean hasMaxPriceToBeat = false;
        // show the price to beat header text only in case we have a value for price to beat
        if (!TextUtils.isEmpty(formattedMinPriceToBeat)) {
            hasMinPriceToBeat = true;
        }

        if (!TextUtils.isEmpty(formattedMaxPriceToBeat)) {
            hasMaxPriceToBeat = true;
        }

        String priceToBeatHdrText = null;

        StringBuilder formattedPriceToBeatColored = new StringBuilder("<b><font color=#1f4272>");
        if (hasMinPriceToBeat && hasMaxPriceToBeat) {
            // show the price to beat range values
            formattedPriceToBeatColored.append(formattedMinPriceToBeat);
            formattedPriceToBeatColored.append(" - ");
            formattedPriceToBeatColored.append(formattedMaxPriceToBeat);
            formattedPriceToBeatColored.append("</font></b>");
        } else {
            // show the price to beat value
            String formattedPriceToBeat = formattedMinPriceToBeat == null ? formattedMaxPriceToBeat
                    : formattedMinPriceToBeat;
            if (formattedPriceToBeat != null) {
                formattedPriceToBeatColored.append(formattedPriceToBeat);
            }
        }
        formattedPriceToBeatColored.append("</font></b>");

        priceToBeatHdrText = Format.localizeText(ConcurCore.getContext(),
                R.string.travel_points_air_booking_workflow_p2b_header, new Object[] { formattedPriceToBeatColored });
        travelPointsHdrBldr.append(priceToBeatHdrText);

        // show the points in bank header text only in case we have a value for points in bank
        if (!(TextUtils.isEmpty(travelPointsInBank) || travelPointsInBank.equals("0"))) {
            String pointsInBankHdrText = Format.localizeText(ConcurCore.getContext(),
                    R.string.travel_points_air_booking_workflow_points_header, new Object[] { travelPointsInBank });

            if (travelPointsHdrBldr.length() > 0) {
                travelPointsHdrBldr.append("<br/>");
            }
            travelPointsHdrBldr.append(pointsInBankHdrText);
        }

        if (travelPointsHdrBldr.length() > 0) {
            View view = activity.findViewById(R.id.travel_points_header);
            view.setVisibility(View.VISIBLE);
            travelPointsHdrView = (TextView) view.findViewById(R.id.desc_field);
            travelPointsHdrView.setText(Html.fromHtml(travelPointsHdrBldr.toString()));
        }

        return travelPointsHdrView;
    }

    public static TextView getTravelPointsHeader(Activity activity, String formattedPriceToBeat, int priceToBeatResId,
            String travelPointsInBank, int travelPointsInBankResId) {

        TextView travelPointsHdrView = null;

        StringBuilder travelPointsHdrBldr = new StringBuilder();

        // show the price to beat header text only in case we have a value for price to beat
        if (!TextUtils.isEmpty(formattedPriceToBeat)) {

            StringBuilder formattedPriceToBeatColored = new StringBuilder("<b><font color=#1f4272>");
            formattedPriceToBeatColored.append(formattedPriceToBeat).append("</font></b>");

            String priceToBeatHdrText = Format.localizeText(ConcurCore.getContext(), priceToBeatResId,
                    new Object[] { formattedPriceToBeatColored });
            travelPointsHdrBldr.append(priceToBeatHdrText);
        }

        // show the points in bank header text only in case we have a value for points in bank
        if (!(TextUtils.isEmpty(travelPointsInBank) || travelPointsInBank.equals("0"))) {
            String pointsInBankHdrText = Format.localizeText(ConcurCore.getContext(), travelPointsInBankResId,
                    new Object[] { travelPointsInBank });

            if (travelPointsHdrBldr.length() > 0) {
                travelPointsHdrBldr.append("<br/>");
            }
            travelPointsHdrBldr.append(pointsInBankHdrText);
        }

        if (travelPointsHdrBldr.length() > 0) {
            View view = activity.findViewById(R.id.travel_points_header);
            view.setVisibility(View.VISIBLE);
            travelPointsHdrView = (TextView) view.findViewById(R.id.desc_field);
            travelPointsHdrView.setText(Html.fromHtml(travelPointsHdrBldr.toString()));
        }

        return travelPointsHdrView;
    }

    public static void initTravelPointsAtItemLevel(View view, int viewId, Integer travelPointsInt) {
        // Null check required for GSA
        if (view != null) {
            TextView txtView = (TextView) view.findViewById(viewId);
            int travelPoints = (travelPointsInt == null ? 0 : travelPointsInt);
            if (travelPoints != 0 && txtView != null) {

                Context context = ConcurCore.getContext();

                txtView.setVisibility(View.VISIBLE);

                String tpts = Integer.toString(travelPoints);
                int resId = R.string.travel_points_use_points;
                if (tpts.contains("-")) {
                    tpts = tpts.replace("-", "");
                    // negative number means Use travel Points
                    txtView.setTextColor(context.getResources().getColor(R.color.FareRed));
                } else {
                    tpts = tpts.replace("+", "");
                    // positive number means Earn travel Points
                    txtView.setTextColor(context.getResources().getColor(R.color.FareGreen));
                    resId = R.string.travel_points_earn_points;
                }

                txtView.setText(com.concur.mobile.base.util.Format.localizeText(context, resId, new Object[] { tpts }));
            } else {
                if (txtView != null) {
                    // hide the view
                    txtView.setVisibility(View.GONE);
                }
            }

        }
    }

    public static void addViolationMessages(final Activity activity, List<Violation> violations) {
        // Add the violation messages.
        ViewGroup violationsGroup = (ViewGroup) activity.findViewById(R.id.violation_messages);
        if (violationsGroup != null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            for (Violation violation : violations) {
                View ruleRow = inflater.inflate(R.layout.violation_message_in_use_travel_points, null);

                // Set the rule icon.
                ImageView imgView = (ImageView) ruleRow.findViewById(R.id.exception_icon);
                if (imgView != null) {
                    switch (ViewUtil.getRuleEnforcementLevel(violation.enforcementLevel)) {
                    case NONE: {
                        imgView.setImageResource(R.drawable.icon_informational);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case WARNING: {
                        imgView.setImageResource(R.drawable.icon_yellowex);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ERROR: {
                        imgView.setImageResource(R.drawable.icon_redex);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case INACTIVE: {
                        imgView.setImageResource(R.drawable.icon_redex);
                        imgView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case HIDE: {
                        imgView.setVisibility(View.GONE);
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".layoutViolations: airchoice has rule enforcement level of 'hidden'!");
                        break;
                    }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'exception_icon' image view!");
                }
                // Set the rule text.
                TextView txtView = (TextView) ruleRow.findViewById(R.id.exception_text);
                if (txtView != null) {
                    txtView.setText(violation.message);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'exception_text' text view!");
                }
                violationsGroup.addView(ruleRow);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".layoutViolations: unable to locate 'violation' view group!");
        }
    }

    /**
     * Will update the violation reason view based on the value of user Travel Points.
     */
    public static void updateViolationReasonChoiceView(Activity activity, int travelPointsBeingUsed) {
        View reasonSelectionView = activity.findViewById(R.id.violation_reason);
        if (reasonSelectionView != null) {
            TextView txtView = (TextView) reasonSelectionView.findViewById(R.id.field_value);
            if (travelPointsBeingUsed == 0) {
                txtView.setText(R.string.general_specify_reason);
            } else {
                String violationReasonText = Format.localizeText(ConcurCore.getContext(),
                        R.string.travel_points_being_used_for_booking, new Object[] { travelPointsBeingUsed });
                txtView.setText(violationReasonText);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".updateViolationReasonChoiceView: unable to locate 'violation_reason' view!");
        }
    }

    // Used by Travel Points functionality
    public static View showManageViolationsView(Activity activity) {
        View manageViolationsView = activity.findViewById(R.id.manage_violations);

        if (manageViolationsView != null) {
            manageViolationsView.setVisibility(View.VISIBLE);
            ((TextView) manageViolationsView.findViewById(R.id.field_name))
                    .setText(R.string.travel_points_manage_violations_label);
        }
        return manageViolationsView;
    }
}
