package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> to parse frequent traveler program informatino.
 */
public class FrequentTravelerProgram extends BaseParser {

    private static final String CLS_TAG = "FrequentTravelerProgram";

    private static final String TAG_AIRLINE_VENDOR = "AirlineVendor";
    private static final String TAG_PROGRAM_NUMBER = "ProgramNumber";
    private static final String TAG_PROGRAM_VENDOR = "ProgramVendor";
    private static final String TAG_PROGRAM_VENDOR_CODE = "ProgramVendorCode";
    private static final String TAG_STATUS = "Status";

    private static final int CODE_AIRLINE_VENDOR = 0;
    private static final int CODE_PROGRAM_NUMBER = 1;
    private static final int CODE_PROGRAM_VENDOR = 2;
    private static final int CODE_PROGRAM_VENDOR_CODE = 3;
    private static final int CODE_STATUS = 5;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_AIRLINE_VENDOR, CODE_AIRLINE_VENDOR);
        tagMap.put(TAG_PROGRAM_NUMBER, CODE_PROGRAM_NUMBER);
        tagMap.put(TAG_PROGRAM_VENDOR, CODE_PROGRAM_VENDOR);
        tagMap.put(TAG_PROGRAM_VENDOR_CODE, CODE_PROGRAM_VENDOR_CODE);
        tagMap.put(TAG_STATUS, CODE_STATUS);
    }

    public String airlineVendor;
    public String programNumber;
    public String programVendor;
    public String programVendorCode;
    public String status;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (!TextUtils.isEmpty(text)) {
                switch (tagCode) {
                case CODE_AIRLINE_VENDOR: {
                    airlineVendor = text.trim();
                    break;
                }
                case CODE_PROGRAM_NUMBER: {
                    programNumber = text.trim();
                    break;
                }
                case CODE_PROGRAM_VENDOR: {
                    programVendor = text.trim();
                    break;
                }
                case CODE_PROGRAM_VENDOR_CODE: {
                    programVendorCode = text.trim();
                    break;
                }
                case CODE_STATUS: {
                    status = text.trim();
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
