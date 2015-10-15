package com.concur.mobile.core.expense.travelallowance.util;

import android.content.Context;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a date formatter for SHORT dates. It is implemented similar to DefaultDateFormat.
 * This formatter can handle the different localized representations.
 * Time zone information is not considered.
 *
 * Created by D023077 on 14.10.2015
 *
 */
public class ShortDateFormat implements IDateFormat {
    private Context ctx;

    /**
     * @param ctx the application context needed to get the default date and
     *            time format of the device.
     */
    public ShortDateFormat(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(final Date date, final boolean includeTime, final boolean includeDayOfWeek,
                         final boolean includeYear) {
        if (date == null) {
            return "";
        }

        String formattedDate;
        DateFormat dateFormat;
        if (includeYear){
            dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            formattedDate = dateFormat.format(date);
        }else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(createDatePattern(), Locale.getDefault());
            formattedDate = simpleDateFormat.format(date);
        }

        String dayOfWeek = null;
        if (includeDayOfWeek) {
            dateFormat =  new SimpleDateFormat("E", Locale.getDefault());
            dayOfWeek = dateFormat.format(date);
        }

        String formattedTime = null;
        if (includeTime) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(ctx);
            formattedTime = timeFormat.format(date);
        }

        String result = formattedDate;
        if (formattedTime != null) {
            result = result + ", " + formattedTime;
        }

        if (dayOfWeek != null) {
            result = dayOfWeek + ", " + result;
        }

        return result;

    }

    private String createDatePattern( ) {
        String separator = getDateSeparator();

        char[] order = android.text.format.DateFormat.getDateFormatOrder(ctx);
        StringBuffer pattern = new StringBuffer();
        int i = 0;
        for (char c : order) {
            i++;
            switch (c) {
                case 'd':
                    if (i == 2) {
                        pattern.append(separator);
                    }
                    pattern.append("dd");
                    break;
                case 'M':
                    if (i == 2) {
                        pattern.append(separator);
                    }
                    pattern.append("MM");
                    break;
                default:
                    break;
            }
        }

        return pattern.toString();
    }

    private static String getDateSeparator(){
        String dateString = DateFormat.getInstance().format(new java.util.Date());

        Matcher matcher = Pattern.compile("[^\\w]").matcher(dateString);

        if (!matcher.find())  {
            return " "; //Default separator
        }else{
            return matcher.group(0);
        }
    }

}