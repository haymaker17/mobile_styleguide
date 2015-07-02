package com.concur.mobile.platform.ui.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.concur.mobile.platform.ui.common.R;

/**
 * Partial port of ViewUtil in core. Only APIs relevant to platform.ui.common is included here.
 * 
 */
public class ViewUtil {

    /**
     * Models a location selection value.
     */
    public static class LocationSelection {

        public String liKey;

        public String liCode;

        public String value;

    }

    public static final String CLS_TAG = ViewUtil.class.getSimpleName();
    public static final String MAP_URI = "http://maps.google.com/maps/api/staticmap?center=";
    public static final String MAP_PREFERRENCES = "&zoom=15&size=360x168&sensor=false";

    /**
     * Will add a separator view to a view group.
     * 
     * @param context
     *            the context used to inflate the separator view.
     * @param root
     *            the parent of the inflated view.
     */
    public static void addSeparatorView(Context context, ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.group_separator, root);
    }

    /**
     * Will add a separator view to a view group.
     * 
     * @param context
     *            the context used to inflate the separator view.
     * @param root
     *            the parent of the inflated view.
     * @param index
     *            the index at which to add the separator.
     */
    public static void addSeparatorView(Context context, ViewGroup root, int index) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.group_separator, null);
        root.addView(view, index);
    }

    /**
     * Determines whether an activity is available that can handle a mapping intent, i.e., one with a 'geo:' scheme.
     * 
     * @return whether an activity is available that can handle a mapping intent, i.e., one with a 'geo:' scheme.
     */
    public static boolean isMappingAvailable(Context context) {
        boolean mappingAvailable = false;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Seattle%2CWA+98188"));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        mappingAvailable = isActivityAvailable(context, intent);
        return mappingAvailable;
    }

    /**
     * Will determine whether or not there are any activities available that can handle the given intent.
     * 
     * @param context
     *            the application context.
     * @param intent
     *            the intent.
     * @return returns <code>true</code> if there's an activity available; <code>false</code> otherwise.
     */
    public static boolean isActivityAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (list != null && list.size() > 0);
    }

    /**
     * Will write all bytes from <code>inStream</code> to <code>outStream</code> transferring up to <code>bufSize</code> bytes at
     * a time.
     * 
     * @param inStream
     *            contains the input stream to read from.
     * @param outStream
     *            contains the output stream to write to.
     * @param bufSize
     *            contains the number of bytes read/written at one time.
     * @throws throws an IOException if an exception occurs while reading/writing.
     */
    public static void writeAllBytes(InputStream inStream, OutputStream outStream, int bufSize) throws IOException {
        if (inStream != null && outStream != null) {
            byte[] buffer = new byte[bufSize];
            int numBytesRead = 0;
            while ((numBytesRead = inStream.read(buffer, 0, bufSize)) != -1) {
                outStream.write(buffer, 0, numBytesRead);
            }
        }
    }

    /**
     * Will set the text to be displayed in a <code>TextView</code> component within a <code>View</code>.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param text
     *            the text view string value.
     * @param singleLine
     *            whether the text displayed should be constrained to a single line.
     */
    public static void setTextViewText(Activity activity, int field_view_res_id, int field_name_res_id, String text,
            boolean singleLine) {

        setTextViewText(activity.getWindow().getDecorView(), field_view_res_id, field_name_res_id, text, singleLine);
    }

    /**
     * Will set the text to be displayed in a <code>TextView</code> component within a <code>View</code>.
     * 
     * @param view
     *            the parent view containing the TextView
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param text
     *            the text view string value.
     * @param singleLine
     *            whether the text displayed should be constrained to a single line.
     */
    public static void setTextViewText(View parent, int field_view_res_id, int field_name_res_id, String text,
            boolean singleLine) {

        // Grab a handle to the containing view.
        View view = parent.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            TextView txtView = (TextView) view.findViewById(field_name_res_id);
            if (txtView != null) {
                if (text != null) {
                    txtView.setText(text);
                    txtView.setSingleLine(singleLine);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: null text!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find view by resource id.");
        }
    }

    /**
     * Gets the last saved location selection information if the selection isn't older than
     * <code>Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS</code> old.
     * 
     * @param ctx
     *            contains a context.
     * @return an instance of <code>LocationSelection</code> if the data has been stored and not expired.
     */
    public static LocationSelection getLocationSelection(Context ctx) {
        LocationSelection retVal = null;
        if (!ViewUtil.isLastLocationSelectionExpired(ctx, Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
            String lastLocSel = ViewUtil.getLastLocationSelection(ctx);
            if (lastLocSel != null && lastLocSel.length() > 0) {
                retVal = new LocationSelection();
                String[] lastSelLocVals = lastLocSel.split(":");
                if (lastSelLocVals[0] != null && !lastSelLocVals[0].equalsIgnoreCase("null")) {
                    retVal.liKey = lastSelLocVals[0];
                }
                if (lastSelLocVals[1] != null && !lastSelLocVals[1].equalsIgnoreCase("null")) {
                    retVal.liCode = lastSelLocVals[1];
                }
                if (lastSelLocVals[2] != null && !lastSelLocVals[2].equalsIgnoreCase("null")) {
                    retVal.value = lastSelLocVals[2];
                }
            }
        }
        return retVal;
    }

    /**
     * Will determine whether the last saved location selection is expired.
     * 
     * @param ctx
     *            the context object to obtain a shared preferences object.
     * @param expiration
     *            the expiration in milliseconds.
     * @return returns <code>false</code> if the last location selection is not expired; otherwise returns <code>true</code> if it
     *         has or if no last location selection exists.
     */
    public static boolean isLastLocationSelectionExpired(Context ctx, long expiration) {
        boolean retVal = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME)) {
            long lastSavedDateMillis = prefs.getLong(Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME, 0L);
            if (lastSavedDateMillis != 0L) {
                long currentTimeMillis = System.currentTimeMillis();
                retVal = ((currentTimeMillis - lastSavedDateMillis) >= expiration);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".isLastLocationSelectionExpired: last saved date time is 0!");
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Will get the last stored location selection if it exists and is not expired.
     * 
     * @param ctx
     *            an application context.
     * @return returns the last location selection if it exists and is non-expired; otherwise, returns <code>null</code>.
     */
    public static String getLastLocationSelection(Context ctx) {
        String locationSelection = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_SELECTION)) {
            if (prefs.contains(Const.PREF_LAST_SAVED_LOCATION_SELECTION_TIME)) {
                if (!ViewUtil.isLastLocationSelectionExpired(ctx,
                        Const.LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS)) {
                    locationSelection = prefs.getString(Const.PREF_LAST_SAVED_LOCATION_SELECTION, "");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getLastLocationSelection: missing saved location selection time!");
            }
        }
        return locationSelection;
    }

    /**
     * Gets the last used currency code that was saved into Preferences.
     * 
     * @param ctx
     *            the context.
     * @return the String of the 4217 currency code abbreviation stored in preferences
     */
    public static String getLastUsedCrnCode(Context ctx) {
        String userCrnCode = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(Const.PREF_LAST_USED_CRN_CODE)) {
            userCrnCode = prefs.getString(Const.PREF_LAST_USED_CRN_CODE, "");
        }
        return userCrnCode;
    }

    /**
     * Sets the visibility for a child view by resource id.
     * 
     * @param parent
     *            the parent containing the child view.
     * @param childId
     *            the resource id of the child view.
     * @param visibility
     *            the visiblity.
     */
    public static void setVisibility(View parent, int childId, int visibility) {
        if (parent != null) {
            View v = parent.findViewById(childId);
            if (v != null) {
                v.setVisibility(visibility);
            }
        }
    }

    /**
     * Will set view to be enabled.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param enabled
     *            whether the field is enabled.
     */
    public static void setViewEnabled(Activity activity, int field_view_res_id, int field_name_res_id, boolean enabled) {
        // Grab a handle to the containing view.
        View view = activity.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            View subView = view.findViewById(field_name_res_id);
            if (subView != null) {
                subView.setEnabled(enabled);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find view by resource id.");
        }
    }

    /**
     * Will set view to be enabled.
     * 
     * @param fragmentView
     *            the main view, likely for a fragment.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     * @param enabled
     *            whether the field is enabled.
     */
    public static void setViewEnabled(View fragmentView, int field_view_res_id, int field_name_res_id, boolean enabled) {
        // Grab a handle to the containing view.
        View view = fragmentView.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            View subView = view.findViewById(field_name_res_id);
            if (subView != null) {
                subView.setEnabled(enabled);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: can't find view by resource id.");
        }
    }

    /**
     * Will get the text displayed in <code>TextView</code> component within a <code>View</code>.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     */
    public static String getTextViewText(Activity activity, int field_view_res_id, int field_name_res_id) {
        String txt = null;
        // Grab a handle to the containing view.
        View view = activity.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            TextView txtView = (TextView) view.findViewById(field_name_res_id);
            if (txtView != null) {
                txt = txtView.getText().toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: can't find view by resource id.");
        }
        return txt;
    }

    /**
     * Will get the text displayed in <code>TextView</code> component within a <code>View</code>.
     * 
     * @param activity
     *            the activity containing the view.
     * @param field_view_res_id
     *            the resource id of the view containing text view.
     * @param field_name_res_id
     *            the resource id of the text view.
     */
    public static String getTextViewText(View rootView, int field_view_res_id, int field_name_res_id) {
        String txt = null;
        // Grab a handle to the containing view.
        View view = rootView.findViewById(field_view_res_id);
        if (view != null) {
            // Grab a handle to the text view.
            TextView txtView = (TextView) view.findViewById(field_name_res_id);
            if (txtView != null) {
                txt = txtView.getText().toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: can't find text view by resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: can't find view by resource id.");
        }
        return txt;
    }

    /**
     * Will locate a sub-view contained within another view.
     * 
     * @param activity
     *            the activity containing the view.
     * @param view_res_id
     *            the parent view.
     * @param subview_res_id
     *            the sub-view or child view.
     * @return the sub-view upon success; otherwise <code>null</code> is returned.
     */
    public static View findSubView(Activity activity, int view_res_id, int subview_res_id) {
        View subView = null;
        View view = activity.findViewById(view_res_id);
        if (view != null) {
            subView = view.findViewById(subview_res_id);
            if (subView == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".findView: can't find sub-view resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".findView: can't find view by resource id.");
        }
        return subView;
    }

    /**
     * Will locate a sub-view contained within another view.
     * 
     * @param rootView
     *            the rootView containing the view.
     * @param view_res_id
     *            the parent view.
     * @param subview_res_id
     *            the sub-view or child view.
     * @return the sub-view upon success; otherwise <code>null</code> is returned.
     */
    public static View findSubView(View rootView, int view_res_id, int subview_res_id) {
        View subView = null;
        View view = rootView.findViewById(view_res_id);
        if (view != null) {
            subView = view.findViewById(subview_res_id);
            if (subView == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".findView: can't find sub-view resource id.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".findView: can't find view by resource id.");
        }
        return subView;
    }

    /**
     * Helper to quickly set the text on a text view and turn it into a clickable link
     * 
     * @param viewId
     *            The ID of a TextView
     * @param text
     *            The text to set into the view
     * @param linkifyMask
     *            An integer mask indicating the type of text to link. See {@link Linkify}.
     */
    public static TextView setText(View view, int viewId, String text, int linkifyMask) {

        TextView tv = (TextView) view.findViewById(viewId);

        if (tv != null && text != null) {
            Spannable linkText = new SpannableString(text);
            boolean linksFound = Linkify.addLinks(linkText, linkifyMask);
            tv.setText(linkText);
            if (linksFound) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setFocusable(false);
                tv.setFocusableInTouchMode(false);
            }
        }
        return tv;
    }

    public static Locale getResourcesConfigurationLocale(Context context) {

        Locale locale = context.getResources().getConfiguration().locale;
        String cntryCode = locale.getCountry();

        // MOB-17048 Rare case when the device doesn't properly set the country code.
        if (cntryCode == null || cntryCode.length() == 0) {
            // It seems some Samsung GS2 in Poland aren't setting the Locale
            // correctly. That is, only the language is set as both language_countrycode,
            // which is in correct ISO 3166 format. So try to parse the language
            // to get the country code.
            String lang = locale.getLanguage();
            if (lang != null && lang.contains("_")) {
                String[] split = lang.split("_"); // e.g. en_us
                if (split.length > 1) {
                    locale = new Locale(split[0], split[1]);
                }
            }
        }

        return locale;
    }

    /**
     * Shows the soft keyboard if either the user has no hardware, or the hardware is hidden.
     * 
     * @param context
     *            application context
     * @param config
     *            current phone configuration
     */
    public static void showSoftKeyboard(Context context, Configuration config) {
        // Make sure user doesn't have a hard keyboard available.
        if (config.keyboard == Configuration.KEYBOARD_NOKEYS
                || config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {

            InputMethodManager immMthdMngr = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            immMthdMngr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    /**
     * Will hide the soft keyboard.
     * 
     * @param context
     *            an application context.
     * @param windowToken
     *            an <code>IBinder</code> instance resulting from a call to <code>View.getWindowToken</code>.
     */
    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        InputMethodManager immMthdMngr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (immMthdMngr != null) {
            immMthdMngr.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public static void setClearIconToEditText(final EditText editText){
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float rawX = event.getRawX();
                    int right = editText.getRight();
                    Drawable array = editText.getCompoundDrawables()[DRAWABLE_RIGHT];
                    if (array != null) {
                        Rect bound = array.getBounds();
                        if (bound != null) {
                            if (rawX >= (right - bound.width())) {
                                editText.setText("");
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.sign_in_clear_icon, 0);
                } else {
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        });
    }
}
