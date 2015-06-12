package com.concur.mobile.core.expense.travelallowance.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;


/**
 * This is the default date formatter used to format the date and time visible
 * on the itinerary {@code Stop}. This formatter can handle the different
 * localized representations. Time zone information is not considered.
 *
 * @author Patricius Komarnicki, Michael Becherer
 *
 */
public class DefaultDateFormat implements IDateFormat {

    private Context ctx;

    /**
     *
     * @param ctx
     *            the application context needed to get the default date and
     *            time format of the device.
     */
    public DefaultDateFormat(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(final Date date, final boolean includeTime) {
        if (date == null) {
            return "";
        }

        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(ctx);

        SimpleDateFormat dateFormat = new SimpleDateFormat(createDatePattern(), Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        if (includeTime) {
            String formattedTime = timeFormat.format(date);
            return formattedDate + ", " + formattedTime;
        } else {
            return formattedDate;
        }
    }

    /**
     * This is needed to get the localized order of the day and month. API
     * level 18 offers a method which returns the correct order related to a
     * pattern. As soon as the app min sdk level reaches 18 this part can be
     * refactored.
     *
     * @see android.text.format.DateFormat#getBestDateTimePattern(Locale,
     *      String).
     *
     * @return the default date pattern for a {@code SimpleDateFormat}
     */
    private String createDatePattern() {
        char[] order = android.text.format.DateFormat.getDateFormatOrder(ctx);
        StringBuffer pattern = new StringBuffer();
        int i = 0;
        for (char c : order) {
            switch (c) {
                case 'd':
                    pattern.append("d");
                    break;
                case 'M':
                    pattern.append("MMMM");
                    break;
                default:
                    break;
            }

            if (i == 0) {
                pattern.append(" ");
            }

            i++;
        }

        return pattern.toString();
    }
}
