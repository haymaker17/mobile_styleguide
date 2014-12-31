package com.concur.mobile.core.expense.report.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class DistanceToDateReply extends ServiceReply {

    private static final String CLS_TAG = DistanceToDateReply.class.getSimpleName();

    // Contains the parsed distance.
    public Integer distance;

    public static DistanceToDateReply parseReply(String responseXml) {

        final String responsePattern = "<string.*>(\\d*)</string>";

        DistanceToDateReply reply = new DistanceToDateReply();
        reply.mwsStatus = Const.REPLY_STATUS_FAILURE;

        try {
            // This is a completely trivial response that only includes a single string element
            // Just regex it
            Pattern pat = Pattern.compile(responsePattern);
            Matcher match = pat.matcher(responseXml);
            if (match.lookingAt()) {
                String distanceString = match.group(1);
                if (distanceString != null && distanceString.trim().length() > 0) {
                    reply.distance = Integer.parseInt(distanceString);
                }

                reply.mwsStatus = Const.REPLY_STATUS_SUCCESS;
            }

        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseReply: failure to parse distanceToDate response:\n" + responseXml, e);
            reply.mwsErrorMessage = "Failed to retrieve distance to date.";
        }

        return reply;
    }
}
